package com.nupco.tests.InboundFlowsTests;

import com.nupco.pages.LaunchPadPage;
import com.nupco.pages.LoginPage;
import com.nupco.pages.POsListPage;
import com.nupco.pages.PoDetailsPage;
import com.nupco.sqlqueries.PurchaseOrderManagerSqlQueries;
import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PurchasingTests {
    private SHAFT.GUI.WebDriver driver;
    private SHAFT.TestData.JSON usersDetailsTestData, filterOptionsTestData, launchpadIcons;
    String connectionString, PONumber, asnPassword, asnEmail, nupcoPOs_launchpadIcon, myRequests_launchpadIcon, purchaseSloc,  purchaseCustomerName,  purchaseRegion;
    PurchaseOrderManagerSqlQueries purchaseOrderManagerSqlQueries;

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/117299")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/117297")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/117296")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/117292")
    @Test(groups = {"Regression", "Purchasing"}, description = "Asn Supplier Can Check The POs Details")
    @Description("Validate that Asn Supplier Can Check The POs Details")
    public void asnSupplierCanCheckPosDetails() {
        ReportManager.log(" Starting flow: ASN Supplier Can Check PO Details");

        try {
            ReportManager.log("Step 1: Retrieve active RHD PO with remaining quantities from database");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            PONumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(connectionString, purchaseOrderManagerSqlQueries.getPoNumber())
                    .getFirst();
            ReportManager.logDiscrete("Fetched PO Number: " + PONumber);

            ReportManager.log(String.format("Step 2: Login as ASN Supplier (%s)", asnEmail));
            new LoginPage(driver)
                    .userLogin(asnEmail, asnPassword)
                    .navigateToStagEnvDashboard()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Navigate to NUPCO POs from Launchpad");
            new LaunchPadPage(driver)
                    .openLaunchpadMenu()
                    .openPageFromLaunchpad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter NUPCO POs using RHD customer details and open PO details");
            new POsListPage(driver)
                    .escapeAnnouncementAndSelectRegion(purchaseRegion)
                    .selectCustomerName(purchaseCustomerName)
                    .selectStorageLocationName(purchaseSloc)
                    .clickToRetrievePOs()
                    .validatePoListIsNotEmpty()
                    .searchWithPONumber(PONumber)
                    .validatePoListIsNotEmpty()
                    .openPoDetailsAfterSearch();

            ReportManager.log("Step 5: Select PO line items and initiate ASN request");
            new PoDetailsPage(driver)
                    .selectFirstPoLineItems()
                    .clickOnRequestAsnButton();

            ReportManager.log(" ASN Supplier successfully viewed PO details and initiated ASN request.");
        } catch (Exception e) {
            ReportManager.log(" Test failed due to: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log("Step 6: Logging out from the system...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
            ReportManager.log(" Logout successful. Test completed.");
        }
    }



    @BeforeClass(groups = {"Regression","Purchasing"})
    public void beforeClass() {
        usersDetailsTestData = new SHAFT.TestData.JSON("UserCredentials.Json");
        filterOptionsTestData = new SHAFT.TestData.JSON("StorageLocationsFilters.json");
        launchpadIcons = new SHAFT.TestData.JSON("LaunchpadIconsName.json");
        launchpadIcons = new SHAFT.TestData.JSON("LaunchpadIconsName.json");
        connectionString = usersDetailsTestData.getTestData("asnSupplier_PurchasingDBConnectionString");
        nupcoPOs_launchpadIcon = launchpadIcons.getTestData("NUPCO_POs");
        myRequests_launchpadIcon = launchpadIcons.getTestData("My_Requests");
        asnEmail = usersDetailsTestData.getTestData("asnSupplier_email");
        asnPassword = usersDetailsTestData.getTestData("asnSupplier_password");
        purchaseRegion = filterOptionsTestData.getTestData("RHD_Region");
        purchaseCustomerName = filterOptionsTestData.getTestData("RHD_CustomerName");
        purchaseSloc = filterOptionsTestData.getTestData("RHD_SLOC");

    }

    @BeforeMethod(groups = {"Regression","Purchasing"})
    public void beforeMethod() {
        driver = new SHAFT.GUI.WebDriver();
        // Step1 --> Login with ASN supplier
        new LoginPage(driver)
                .navigateToLoginPage();

    }

    @AfterMethod(groups = {"Regression", "Purchasing"})
    public void afterMethod() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

}
