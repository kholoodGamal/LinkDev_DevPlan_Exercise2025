package com.nupco.utils;

import com.nupco.pages.*;
import com.nupco.sqlqueries.PurchaseOrderManagerSqlQueries;
import com.shaft.driver.SHAFT;

import java.text.ParseException;
import java.time.Duration;

public class BaseFunctions {
    private SHAFT.GUI.WebDriver driver;
    private PurchaseOrderManagerSqlQueries purchaseOrderManagerSqlQueries;
    private AsnNewRequestPage asnNewRequestPage;
    private BlanketAsnRequestPage blanketAsnNewRequestPage;
    private SQLConnectionManager connectionManager;
    private DatabaseRepository databaseRepository;
    private SuccessScreenPage successScreenPage;
    private String PONumber, businessKey, palletsNo;
    PoDetailsPage poDetailsPage;
    int initialBookingQty, initialRemainingQty;

    public BaseFunctions(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    public void createOutboundRequest(OutboundRequestParams outBoundRequestParameter) throws InterruptedException, ParseException {
        new LoginPage(driver)
                .userLoggedIntoSiteSuccessfully(outBoundRequestParameter.getUserName(), outBoundRequestParameter.getPassword());
        new LaunchPadPage(driver)
                .openTheSpecificPageFromLaunchPad(outBoundRequestParameter.getIconTitle());

        new InventoryListingPage(driver)
                .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                .selectInventoryItemUnderClassification();

        new OutboundNewRequestPage(driver)
                .fillAndSubmitOutboundRequest(outBoundRequestParameter.getRequestedQty() ,outBoundRequestParameter.getFillOptionalFields() ,outBoundRequestParameter.getNoteText() , outBoundRequestParameter.getValue());
        successScreenPage = new SuccessScreenPage(driver);
        successScreenPage.copyRequestBusinessKey();
        businessKey = successScreenPage.getBusinessKey();
        successScreenPage.BackToMyRequests();
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        databaseRepository.waitForStatusChange(outBoundRequestParameter.getConnectionString(), purchaseOrderManagerSqlQueries.getOutBoundStatusQuery(businessKey), outBoundRequestParameter.getDesiredStatus());

    }

    public void createReturnOutboundRequest(ReturnOutboundRequestParams returnOutBoundRequestParameter, String reason) throws InterruptedException {
        new LoginPage(driver)
                .userLoggedIntoSiteSuccessfully(returnOutBoundRequestParameter.getUserName(), returnOutBoundRequestParameter.getPassword());
        new LaunchPadPage(driver)
                .openTheSpecificPageFromLaunchPad(returnOutBoundRequestParameter.getIconTitle());
        new GRListingPage(driver)
                .initiateReturnOutboundRequest(returnOutBoundRequestParameter.getOrderId());

        new  ReturnOutboundNewRequestPage(driver)
                .fillAndSubmitReturnOutboundRequest(returnOutBoundRequestParameter.getRequestedQty() ,reason ,returnOutBoundRequestParameter.getFillOptionalFields() , returnOutBoundRequestParameter.getNoteText(),returnOutBoundRequestParameter.getFilePath());
        successScreenPage = new SuccessScreenPage(driver);
        successScreenPage.copyRequestBusinessKey();
        businessKey = successScreenPage.getBusinessKey();
        successScreenPage.BackToMyReturnRequests();
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        databaseRepository.waitForStatusChange(returnOutBoundRequestParameter.getConnectionString(), purchaseOrderManagerSqlQueries.getReturnOutBoundStatusQuery(businessKey), returnOutBoundRequestParameter.getDesiredStatus());

    }

    public void createRhdRequest(InboundRequestsParams rhdRequestParameter) throws InterruptedException, ParseException {
        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        PONumber = purchaseOrderManagerSqlQueries.getPurchaseOrderList(rhdRequestParameter.getConnectionString(), purchaseOrderManagerSqlQueries.getPoNumber()).get(2);
        new LoginPage(driver)
                .userLoggedIntoSiteSuccessfully(rhdRequestParameter.getUserName(), rhdRequestParameter.getPassword());
        new LaunchPadPage(driver)
                .openTheSpecificPageFromLaunchPad(rhdRequestParameter.getIconTitle());
        new POsListPage(driver)
                .openPoDetailsPageWhetherFilterExist(rhdRequestParameter.getRegion(), rhdRequestParameter.getCustomerName(), rhdRequestParameter.getSloc(), PONumber);

        // Step5 --> Read the initial Booking and remaining quantities
        poDetailsPage = new PoDetailsPage(driver);
        initialBookingQty = poDetailsPage.readBookedQtyValue();
        initialRemainingQty = poDetailsPage.readRemainingQtyValue();

        new PoDetailsPage(driver)
                .startAsnRequestCreation();
        asnNewRequestPage = new AsnNewRequestPage(driver);
        asnNewRequestPage.fillAndSubmitASNRequest(
                rhdRequestParameter.getInvoiceNum(), rhdRequestParameter.getDeliveryTime(), rhdRequestParameter.getPalletsNum(),
                rhdRequestParameter.getBatchNum(), rhdRequestParameter.getQty(), rhdRequestParameter.getFillOptionalFields(), rhdRequestParameter.getNoteText(), rhdRequestParameter.getvalue(),rhdRequestParameter.getManufacturingDate(),rhdRequestParameter.getExpiryDate(),rhdRequestParameter.getDeliveryDate());
        businessKey = asnNewRequestPage.getBusinessKey();
        palletsNo = asnNewRequestPage.getNumOfPallets();
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        databaseRepository.waitForStatusChange(rhdRequestParameter.getConnectionString(), purchaseOrderManagerSqlQueries.getStatusQuery(businessKey), rhdRequestParameter.getDesiredStatus());

    }

    public void createAsnRequest(InboundRequestsParams normalAsnRequestParameter) throws InterruptedException, ParseException {
        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        PONumber = purchaseOrderManagerSqlQueries.getPurchaseOrderList(normalAsnRequestParameter.getConnectionString(), purchaseOrderManagerSqlQueries.getPoNumberOfAsnRequest()).get(0);
        new LoginPage(driver)
                .userLoggedIntoSiteSuccessfully(normalAsnRequestParameter.getUserName(), normalAsnRequestParameter.getPassword());
        new LaunchPadPage(driver)
                .openTheSpecificPageFromLaunchPad(normalAsnRequestParameter.getIconTitle());
        new POsListPage(driver)
                .openPoDetailsPageWhetherFilterExist(normalAsnRequestParameter.getRegion(), normalAsnRequestParameter.getCustomerName(), normalAsnRequestParameter.getSloc(), PONumber);

        // Step5 --> Read the initial Booking and remaining quantities
        poDetailsPage = new PoDetailsPage(driver);
        initialBookingQty = poDetailsPage.readBookedQtyValue();
        initialRemainingQty = poDetailsPage.readRemainingQtyValue();


        new PoDetailsPage(driver)
                .startAsnRequestCreation();
        asnNewRequestPage = new AsnNewRequestPage(driver);
        asnNewRequestPage.fillAndSubmitASNRequest(
                normalAsnRequestParameter.getInvoiceNum(), normalAsnRequestParameter.getDeliveryTime(), normalAsnRequestParameter.getPalletsNum(),
                normalAsnRequestParameter.getBatchNum(), normalAsnRequestParameter.getQty(), normalAsnRequestParameter.getFillOptionalFields(), normalAsnRequestParameter.getNoteText(), normalAsnRequestParameter.getvalue(),normalAsnRequestParameter.getManufacturingDate(),normalAsnRequestParameter.getExpiryDate(),normalAsnRequestParameter.getDeliveryDate());
        palletsNo = asnNewRequestPage.getNumOfPallets();
        businessKey = asnNewRequestPage.getBusinessKey();
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        databaseRepository.waitForStatusChange(normalAsnRequestParameter.getConnectionString(), purchaseOrderManagerSqlQueries.getStatusQuery(businessKey), normalAsnRequestParameter.getDesiredStatus());

    }

    public void createBlanketAsnRequest(blanketAsnRequestsParams normalBlanketAsnRequestParameter) throws InterruptedException {
        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        PONumber = purchaseOrderManagerSqlQueries.getPurchaseOrderList(normalBlanketAsnRequestParameter.getConnectionString(), purchaseOrderManagerSqlQueries.getBlanketPONumber()).get(2);

        new LoginPage(driver)
                .userLoggedIntoSiteSuccessfully(normalBlanketAsnRequestParameter.getUserName(), normalBlanketAsnRequestParameter.getPassword());
        new LaunchPadPage(driver)
                .openTheE_ServicesTap()
                .validateThatUserNavigatedToE_ServicesPage();
        new E_ServicesPage(driver)
                .searchAndOpenTargetedService(normalBlanketAsnRequestParameter.getIconTitle())
                .startBlanketAsnCreationPage(PONumber);

        new PoDetailsPage(driver)
                .startAsnRequestCreation();
        //Step7 --> Fill & submit ASN request then return the business key
        blanketAsnNewRequestPage = new BlanketAsnRequestPage(driver);
        blanketAsnNewRequestPage.fillAndSubmitBlanketASNRequest(
                normalBlanketAsnRequestParameter.getInvoiceNum(),
                normalBlanketAsnRequestParameter.getLocation(),
                normalBlanketAsnRequestParameter.getDeliveryTime(),
                normalBlanketAsnRequestParameter.getBatchNum(),
                normalBlanketAsnRequestParameter.getQty(),
                normalBlanketAsnRequestParameter.getFillOptionalFields(),
                normalBlanketAsnRequestParameter.getNoteText(),
                normalBlanketAsnRequestParameter.getvalue(),
                normalBlanketAsnRequestParameter.getManufacturingDateValue(),
                normalBlanketAsnRequestParameter.getExpiryDateValue(),
                normalBlanketAsnRequestParameter.getDeliveryDateValue());
        businessKey = blanketAsnNewRequestPage.getBusinessKey();

        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        databaseRepository.waitForStatusChange(normalBlanketAsnRequestParameter.getConnectionString(), purchaseOrderManagerSqlQueries.getStatusQuery(businessKey), normalBlanketAsnRequestParameter.getDesiredStatus());

    }

    public void createDDAsnRequest(InboundRequestsParams DDAsnRequestParameter) throws InterruptedException, ParseException {

        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        PONumber = purchaseOrderManagerSqlQueries.getPurchaseOrderList(DDAsnRequestParameter.getConnectionString(), purchaseOrderManagerSqlQueries.getPoNumber()).getFirst();

        new LoginPage(driver)
                .userLoggedIntoSiteSuccessfully(DDAsnRequestParameter.getUserName(), DDAsnRequestParameter.getPassword());

        new LaunchPadPage(driver)
                .openTheSpecificPageFromLaunchPad(DDAsnRequestParameter.getIconTitle());

        new POsListPage(driver)
                .openPoDetailsPageWhetherFilterExist(DDAsnRequestParameter.getRegion(), DDAsnRequestParameter.getCustomerName(), DDAsnRequestParameter.getSloc(), PONumber);

        // Read the initial Booking and remaining quantities
        poDetailsPage = new PoDetailsPage(driver);
        initialBookingQty = poDetailsPage.readBookedQtyValue();
        initialRemainingQty = poDetailsPage.readRemainingQtyValue();

        new PoDetailsPage(driver)
                .selectFirstPoLineItems()
                .clickOnRequestDDAsnButton()
                .clickEscapeAnnouncementButton();

        asnNewRequestPage = new AsnNewRequestPage(driver);
        asnNewRequestPage
                .fillAndSubmitDd_ASNRequest(
                        DDAsnRequestParameter.getInvoiceNum(),
                        DDAsnRequestParameter.getDeliveryTime(),
                        DDAsnRequestParameter.getBatchNum(),
                        DDAsnRequestParameter.getQty(),
                        DDAsnRequestParameter.getFillOptionalFields(),
                        DDAsnRequestParameter.getNoteText(),
                        DDAsnRequestParameter.getvalue(),
                        DDAsnRequestParameter.getManufacturingDate(),
                        DDAsnRequestParameter.getExpiryDate(),
                        DDAsnRequestParameter.getDeliveryDate());

        businessKey = asnNewRequestPage.getBusinessKey();
        palletsNo = asnNewRequestPage.getNumOfPallets();

        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        databaseRepository.waitForStatusChange(DDAsnRequestParameter.getConnectionString(), purchaseOrderManagerSqlQueries.getStatusQuery(businessKey), DDAsnRequestParameter.getDesiredStatus());

        new MyRequestListPage(driver).openTheRequestFromMyRequests(businessKey);
    }

    public void approveRhdRequestByInboundUser(String email, String password, String businessKey) throws InterruptedException {
        new LoginPage(driver)
                .userLogin(email, password)
                .navigateToStagEnvDashboardDirectly()
                .validateLoginSuccessfully();

        new MyTasksPage(driver)
                .openMyTasksPage()
                .openSpacePage()
                .searchInSearchBar(businessKey)
                .userAssignTaskToHim(businessKey)
                .searchInSearchBar(businessKey)
                .taskDetailsPageForInboundUser(businessKey)
                .userApproveAssignedRequest()
                .validateThatRequestStatusIsApproved();

        new LoginPage(driver)
                .userCanLogoutFromSiteSuccessfully();
    }

    public void validateQuantitiesAreBookedAfterSubmission(InboundRequestsParams rhdRequestParameter) throws InterruptedException, ParseException {

        new LaunchPadPage(driver)
                .openTheSpecificPageFromLaunchPad(rhdRequestParameter.getIconTitle());

        //Should not exceed 3 min to reflect the new booking quantity
        helper.WaitForIntegrationReflection(Duration.ofMinutes(3));


        new POsListPage(driver)
                .openPoDetailsPageWhetherFilterExist(rhdRequestParameter.getRegion(), rhdRequestParameter.getCustomerName(), rhdRequestParameter.getSloc(), PONumber);


        // Step11 --> Re-Read the Booking and remaining quantities
        int updatedBookingQty = new PoDetailsPage(driver).readBookedQtyValue();
        int updatedRemainingQty = new PoDetailsPage(driver).readRemainingQtyValue();
        int requestQty = Integer.parseInt(rhdRequestParameter.getQty());

        poDetailsPage.ValidateNewBookingQtyValue(initialBookingQty, updatedBookingQty, requestQty)
                .ValidateNewRemainingQtyValue(initialRemainingQty, updatedRemainingQty, requestQty);
    }

    public int getPoInitialBookedQuantity() {
        return initialBookingQty;
    }

    public int getPoInitialRemainingQuantity() {
        return initialRemainingQty;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getPONumber() {
        return PONumber;
    }

    public String getPalletsNo() {
        return palletsNo;
    }

}


