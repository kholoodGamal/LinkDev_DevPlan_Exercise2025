package com.nupco.sqlqueries;

import com.nupco.utils.DatabaseRepository;
import com.nupco.utils.SQLConnectionManager;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderManagerSqlQueries {

    private SQLConnectionManager connectionManager;
    private DatabaseRepository databaseRepository;

    //region SQL Queries

    private static final String BASE_QUERY = "SELECT tr.GenericCode, tr.TradeCode, tr.TradeDescription, li.OrderQuantity, tr.UnitOfMeasure "
            + "FROM [dbo].[PurchaseOrder] po " + "INNER JOIN [dbo].[TradeCode] tr ON tr.PurchaseOrderId = po.Id "
            + "INNER JOIN [dbo].[LineItem] li ON li.TradeCodeId = tr.Id "
            + "LEFT JOIN [dbo].[Inventory] i ON i.Id = li.InventoryId "
            + "LEFT JOIN [dbo].[Customer] c ON c.Id = li.CustomerId "
            + "INNER JOIN [dbo].[Region] r ON r.Id = i.RegionId ";

    private static final String COMMON_CONDITIONS = "WHERE OrderQuantity >= 0 AND PoType = 'Z104' AND PlantCode = '1100' AND IsRHD = 1 AND IsActive = 1 AND CustomerNumber = '120215' ";
    private static final String COMMON_CONDITIONS_OF_ASN_REQUEST = "WHERE OrderQuantity >= 0 AND PoType = 'Z113' AND PlantCode = 'C2C1' And StorageLocationCode = '1000' AND IsRHD = 0 AND IsActive = 1 AND CustomerNumber = '120000' AND li.IsDeleted = 0 AND li.DeliveryCompleted = 0 And OpenQuantity != 0 AND ReleaseIndicatorId = '05' AND li.IsInbound = 1 AND VendorNumber = '400034' ";
    private static final String COMMON_CONDITIONS_OF_BLANKET_REQUEST = "WHERE OrderQuantity >= 0 AND PoType = 'ZV01'  AND IsActive = 1 ORDER BY PoNumber DESC";

    /**
     * Get the latest Purchase Order number.
     *
     * @return the SQL statement to retrieve the latest Purchase Order number.
     */
    public String getPoNumber() {
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT DISTINCT TOP 10 PoNumber FROM [dbo].[PurchaseOrder] po ")
                .append("INNER JOIN [dbo].[TradeCode] tr ON tr.PurchaseOrderId = po.Id ")
                .append("INNER JOIN [dbo].[LineItem] li ON li.TradeCodeId = tr.Id ")
                .append("LEFT JOIN [dbo].[Inventory] i ON i.Id = li.InventoryId ")
                .append("LEFT JOIN [dbo].[Customer] c ON c.Id = li.CustomerId ")
                .append("INNER JOIN [dbo].[Region] r ON r.Id = i.RegionId ").append(COMMON_CONDITIONS);
        return sqlStatement.toString();
    }


    /**
     * Get the latest Purchase Order number.
     *
     * @return the SQL statement to retrieve the latest Purchase Order number.
     */
    public String getPoNumberOfAsnRequest() {
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT DISTINCT TOP 10 PoNumber FROM [dbo].[PurchaseOrder] po ")
                .append("INNER JOIN [dbo].[TradeCode] tr ON tr.PurchaseOrderId = po.Id ")
                .append("INNER JOIN [dbo].[LineItem] li ON li.TradeCodeId = tr.Id ")
                .append("LEFT JOIN [dbo].[Inventory] i ON i.Id = li.InventoryId ")
                .append("LEFT JOIN [dbo].[Customer] c ON c.Id = li.CustomerId ")
                .append("INNER JOIN [dbo].[Region] r ON r.Id = i.RegionId ")
                .append("INNER JOIN [dbo].[Organization] org ON org.Id = po.OrganizationId ")
                .append(COMMON_CONDITIONS_OF_ASN_REQUEST);
        return sqlStatement.toString();
    }

    /**
     * Get the trade code details for a given Purchase Order number.
     *
     * @param poNumber the Purchase Order number.
     * @return the SQL statement to retrieve trade code details.
     */
    public String getTradeCode(String poNumber) {
        StringBuilder sqlStatement = new StringBuilder(BASE_QUERY);
        sqlStatement.append("WHERE PoNumber = ? AND ").append(COMMON_CONDITIONS)
                .append("AND li.LineItemNumber LIKE '000%' ");
        return sqlStatement.toString();
    }

    /**
     * Get detailed information for a given Purchase Order number.
     *
     * @param poNumber the Purchase Order number.
     * @return the SQL statement to retrieve Purchase Order information.
     */
    public String getPOInformation(String poNumber) {

        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement
                .append("SELECT DISTINCT tr.TradeCode, NetOrderValue, po.PoDate, i.PlantCode, po.Currency, po.PoType ")
                .append("FROM [dbo].[PurchaseOrder] po ")
                .append("INNER JOIN [dbo].[TradeCode] tr ON tr.PurchaseOrderId = po.Id ")
                .append("INNER JOIN [dbo].[LineItem] li ON li.TradeCodeId = tr.Id ")
                .append("LEFT JOIN [dbo].[Inventory] i ON i.Id = li.InventoryId ")
                .append("LEFT JOIN [dbo].[Customer] c ON c.Id = li.CustomerId ")
                .append("INNER JOIN [dbo].[Region] r ON r.Id = i.RegionId ").append("WHERE po.PoNumber = '")
                .append(poNumber).append("' ");
        return sqlStatement.toString();
    }

    // Here is the Enhancement of Connect To DB Function and the origin function is commented below
    public List<String> getPurchaseOrderList(String connectionString , String sqlQuery) {
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);

        int maxRetries = 10; // Maximum number of retry attempts
        int retryDelayMillis = 2000; // Delay between retries in milliseconds
        List<String> queryResult;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            queryResult = databaseRepository.executeQuery(connectionString, sqlQuery);

            if (!queryResult.isEmpty()) {
                // Valid result found, return the query result
                return queryResult;
            }

            System.out.println("Attempt " + attempt + ": No valid POs found. Retrying...");

            try {
                // Wait before retrying
                Thread.sleep(retryDelayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted state
                throw new IllegalStateException("Retry process was interrupted.", e);
            }
        }

        // After all attempts, throw an exception if no valid result is found
        throw new IllegalStateException("Unable to retrieve valid POs to create RHD request after " + maxRetries + " attempts.");
    }

    public String getStatusQuery(String businessKey) {
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement
                .append("SELECT StatusActions.StatusName FROM InupcoShipping.dbo.asn")
                .append(" INNER JOIN InupcoShipping.dbo.StatusActions")
                .append(" ON StatusActions.StatusId = asn.Status")
                .append(" WHERE RequestBusinessKey='")
                .append(businessKey)
                .append("'");
        return sqlStatement.toString();
    }

    public String getOutBoundStatusQuery(String businessKey) {
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement
                .append("SELECT OrderStatus.Name from OrderRequest o")
                .append(" JOIN OrderStatus  ")
                .append(" ON o.OrderStatusId = OrderStatus.Id ")
                .append(" WHERE o.RequestBusinessKey='")
                .append(businessKey)
                .append("'");
        return sqlStatement.toString();
    }

    public String getRGRStatusQuery(String businessKey) {
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement
                .append("SELECT StatusActions.StatusName FROM InupcoRGR.dbo.Rgr")
                .append(" INNER JOIN InupcoShipping.dbo.StatusActions")
                .append(" ON StatusActions.StatusId = Rgr.Status")
                .append(" WHERE RequestBusinessKey='")
                .append(businessKey)
                .append("'");
        return sqlStatement.toString();
    }

    public String getBlanketPONumber(){
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT DISTINCT TOP 10 PoNumber FROM [dbo].[PurchaseOrder] po ")
                .append("INNER JOIN [dbo].[TradeCode] tr ON tr.PurchaseOrderId = po.Id ")
                .append("INNER JOIN [dbo].[LineItem] li ON li.TradeCodeId = tr.Id ")
                .append("LEFT JOIN [dbo].[Inventory] i ON i.Id = li.InventoryId ")
                .append("LEFT JOIN [dbo].[Customer] c ON c.Id = li.CustomerId ")
                .append("LEFT JOIN [dbo].[Region] r ON r.Id = i.RegionId ")
                .append(COMMON_CONDITIONS_OF_BLANKET_REQUEST);
        return sqlStatement.toString();
    }

    public String getReturnOutBoundStatusQuery(String businessKey) {
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement
                .append("SELECT o.StepName from ReturnOrder o")
                .append(" WHERE o.RequestBusinessKey='")
                .append(businessKey)
                .append("'");
        return sqlStatement.toString();
    }
    //endregion

    //region Actions (Methods)

    /*
    public List<String> getPurchaseOrderList(String connectionString) {
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);

        List<String> queryResult = databaseRepository.executeQuery(connectionString, getPoNumber());

        if(queryResult.isEmpty())
            throw new IllegalStateException("There is no Valid POs to create RHD request");

        return queryResult;
    }

     */


    //endregion
}
