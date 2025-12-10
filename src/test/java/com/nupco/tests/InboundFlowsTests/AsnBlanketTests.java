package com.nupco.tests.InboundFlowsTests;

import com.nupco.pages.*;
import com.nupco.sqlqueries.PurchaseOrderManagerSqlQueries;
import com.nupco.utils.*;
import com.shaft.driver.SHAFT;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AsnBlanketTests {

    // region Variables
    private SHAFT.GUI.WebDriver driver;
    private SHAFT.TestData.JSON usersDetailsTestData, e_ServicesOptions, AsnRequestDetailsTestData;
    BlanketAsnRequestPage blanketAsnNewRequestPage;
    PurchaseOrderManagerSqlQueries purchaseOrderManagerSqlQueries;
    String PONumber, businessKey;

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/142847")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/84666")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/84670")
    @Test(groups = {"Regression", "Shipping_BlanketAsn"}, description = "User Can Create A Blanket Asn Request")
    @Description("Validate that the User can Create Blanket ASN request")
    public void createBlanketAsnRequest() {

        // Step1 --> Connect to the DB to get active Blanket ASN PO having remaining quantities
        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        PONumber = purchaseOrderManagerSqlQueries.getPurchaseOrderList(usersDetailsTestData.getTestData("asnSupplier_PurchasingDBConnectionString"), purchaseOrderManagerSqlQueries.getBlanketPONumber()).get(1);

        // Step2 --> Login with ASN supplier
        new LoginPage(driver)
                .userLoggedIntoSiteSuccessfully(usersDetailsTestData.getTestData("asnSupplier_email"), usersDetailsTestData.getTestData("asnSupplier_password"));

        // Step3 --> Open E-Services Page
        new LaunchPadPage(driver)
                .openTheE_ServicesTap()
                .validateThatUserNavigatedToE_ServicesPage();

        //Step4 --> Search and open targeted service
        new E_ServicesPage(driver)
                .searchAndOpenTargetedService(e_ServicesOptions.getTestData("Blanket_POs"))
                .startBlanketAsnCreationPage(PONumber);

        //Step6 --> select line items and initiate ASN request
        new PoDetailsPage(driver)
                .startAsnRequestCreation();
        //Step7 --> Fill & submit ASN request then return the business key
        blanketAsnNewRequestPage = new BlanketAsnRequestPage(driver);
        blanketAsnNewRequestPage.fillAndSubmitBlanketASNRequest(
                AsnRequestDetailsTestData.getTestData("invoiceNum"),
                AsnRequestDetailsTestData.getTestData("location"),
                AsnRequestDetailsTestData.getTestData("deliveryTime"),
                AsnRequestDetailsTestData.getTestData("batchNum"),
                AsnRequestDetailsTestData.getTestData("qty")
                , false, AsnRequestDetailsTestData.getTestData("feedbackData"),
                AsnRequestDetailsTestData.getTestData("truckValue"),
                DateValues.manufacturingDateMedium,
                DateValues.expiryDateMedium,
                DateValues.deliveryDateMedium);
        businessKey = blanketAsnNewRequestPage.getBusinessKey();
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/84683")
    @Test(groups = {"Regression", "Shipping_BlanketAsn"}, description = "Validate that BlanketPO details page is loaded")
    @Description("Validate that Blanket POdetails page is loaded after selecting BlanketPO")
    public void validateBlanketPODetailsPageIsLoaded()  {

        //Step1-->Connect to the DB to get active Blanket ASN PO having remaining quantities
        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        PONumber = purchaseOrderManagerSqlQueries.getPurchaseOrderList(usersDetailsTestData.getTestData("asnSupplier_PurchasingDBConnectionString"), purchaseOrderManagerSqlQueries.getBlanketPONumber()).get(1);


        new LoginPage(driver)
                .userLoggedIntoSiteSuccessfully(usersDetailsTestData.getTestData("asnSupplier_email"), usersDetailsTestData.getTestData("asnSupplier_password"));

        //Step3-->OpenE-ServicesPage
        new LaunchPadPage(driver)
                .openTheE_ServicesTap()
                .validateThatUserNavigatedToE_ServicesPage();

        //Step4-->Search and open targeted service
        new E_ServicesPage(driver)
                .searchAndOpenTargetedService(e_ServicesOptions.getTestData("Blanket_POs"));
        new BlanketPOListPage(driver).validateIsPageLoaded()
                .validateBlanketPoListIsNotEmpty()
                .searchWithPONumber(PONumber)
                .validateBlanketPoListIsNotEmpty()
                .openPoDetailsAfterSearch();
    }

    @BeforeClass(groups = {"Regression", "Shipping_BlanketAsn"})
    public void beforeClass() {
        usersDetailsTestData = new SHAFT.TestData.JSON("UserCredentials.Json");
        e_ServicesOptions = new SHAFT.TestData.JSON("E_ServicesOptions.json");
        AsnRequestDetailsTestData = new SHAFT.TestData.JSON("AsnRequestDetails.json");

    }

    @BeforeMethod(groups = {"Regression", "Shipping_BlanketAsn"})
    public void beforeMethod() {
        driver = new SHAFT.GUI.WebDriver();
        new LoginPage(driver)
                .navigateToLoginPage();
    }

    @AfterMethod(groups = {"Regression", "Shipping_BlanketAsn"})
    public void afterMethod() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

}
