package com.nupco.tests.InboundFlowsTests;

import com.nupco.pages.*;
import com.nupco.sqlqueries.PurchaseOrderManagerSqlQueries;
import com.nupco.utils.*;
import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.ParseException;

public class DdAsnFlowTests {
    private SHAFT.GUI.WebDriver driver;
    private SHAFT.TestData.JSON usersDetailsTestData, filterOptionsTestData, ddAsnRequestDetailsTestData, launchpadIcons;

    String connectionString, businessKey,submittedBusinessKey, draftedBusinessKey, PONumber, asnPassword, asnEmail, nupcoPOs_launchpadIcon, myRequests_launchpadIcon, ddAsnQuantity, ddAsnBatchNumber, ddAsnPalletsNumber, ddAsnDeliveryTime, ddAsnInvoiceNumber, ddAsnSloc, ddAsnCustomerName, ddAsnRegion,ddAsnFeedbackData,ddAsnTruckValue;
    AsnNewRequestPage asnNewRequestPage;
    RequestDetailsPage requestDetailsPage;
    InboundRequestsParams ddASNRequestDetailsParams;
    LaunchPadPage launchPadPage;
    MyRequestListPage myRequestListPage;
    DatabaseRepository databaseRepository;
    PurchaseOrderManagerSqlQueries purchaseOrderManagerSqlQueries;
    SQLConnectionManager connectionManager;
    BaseFunctions baseFunctions;

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130457")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130460")
    @Test(groups = {"Regression", "Shipping_DDASN"}, description = "Validate that the ASN supplier can Create DD ASN request Then Cancel it")
    @Description("Validate that the ASN supplier can Create DD ASN request Then Cancel it")
    public void createDDASNRequestThenCancel() throws InterruptedException {

        ReportManager.log(" Starting DD ASN creation and cancellation process...");

        try {

            ReportManager.log("Step 1: Connect to DB and get active PO with remaining quantities");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            PONumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(connectionString, purchaseOrderManagerSqlQueries.getPoNumber())
                    .getFirst();

            ReportManager.log(String.format("Step 2: Login as ASN Supplier (%s)", asnEmail));
            new LoginPage(driver).userLoggedIntoSiteSuccessfully(asnEmail, asnPassword);

            ReportManager.log("Step 3: Open NUPCO POs page from Launchpad");
            new LaunchPadPage(driver).openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log(String.format("Step 4: Filter POs by Region [%s], Customer [%s], SLOC [%s]",
                    ddAsnRegion, ddAsnCustomerName, ddAsnSloc));
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(ddAsnRegion, ddAsnCustomerName, ddAsnSloc, PONumber);

            ReportManager.log("Step 5: Select PO line items and initiate DD ASN request");
            new PoDetailsPage(driver)
                    .selectFirstPoLineItems()
                    .clickOnRequestDDAsnButton()
                    .clickEscapeAnnouncementButton();

            ReportManager.log("Step 6: Fill and submit DD ASN request form");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage.fillAndSubmitDd_ASNRequest(
                            ddAsnInvoiceNumber,
                            ddAsnDeliveryTime,
                            ddAsnBatchNumber,
                            ddAsnQuantity,
                            false,
                            ddAsnFeedbackData,
                            ddAsnTruckValue,
                            DateValues.manufacturingDateMedium,
                            DateValues.expiryDateMedium,
                            DateValues.deliveryDateMedium)
                    .clickSubmitButton()
                    .copyRequestBusinessKey()
                    .BackToMyRequests();

            businessKey = asnNewRequestPage.getBusinessKey();
            ReportManager.logDiscrete("Business Key created: " + businessKey);

            ReportManager.log("Step 7: Wait for status to become 'APPROVED' in DB");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(businessKey),
                    RequestStatus.APPROVED
            );

            ReportManager.log("Step 8: Open the request from My Requests list");
            myRequestListPage = new MyRequestListPage(driver);
            myRequestListPage.openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 9: Validate request is APPROVED, then cancel it");
            requestDetailsPage = new RequestDetailsPage(driver);
            requestDetailsPage.validateRequestStatus(RequestStatus.APPROVED)
                    .ClickOnCancelButton()
                    .ClickOnCancellationConfirmButton();

            ReportManager.log("Step 10: Back to My Requests list");
            asnNewRequestPage.BackToMyRequests();

            ReportManager.log("Step 11: Validate request is CANCELLED");
            myRequestListPage.openTheRequestFromMyRequests(businessKey);
            requestDetailsPage.validateRequestStatus(RequestStatus.CANCELLED);

            ReportManager.log(" DD ASN request successfully created and cancelled.");
        } catch (Exception e) {
            ReportManager.log(" Test failed: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log("Step 12: Logging out from the site");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
            ReportManager.log(" Logout successful. Test flow completed.");
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/146091")
    @Test(groups = {"Regression", "Shipping_DDASN"}, description = "Validate that the ASN supplier can Create DD ASN request Having Notes")
    @Description("Validate that the ASN supplier can Create DD ASN request Having Additional Fields Not Required As Notes")
    public void createDDASNRequestHavingNotes() throws InterruptedException, ParseException {

        ReportManager.log(" Starting DD ASN creation ...");

        try {
            ReportManager.log("Step 1: Create DD_ASN request using BaseFunctions");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createDDAsnRequest(ddASNRequestDetailsParams);
            businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after DD_ASN creation");

            ReportManager.log("Step 2: Validate request is APPROVED On GUI");
            requestDetailsPage = new RequestDetailsPage(driver);
            requestDetailsPage.validateRequestStatus(RequestStatus.APPROVED);

        } catch (Exception e) {
            ReportManager.log(" Test failed: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log("Step 3: Logging out from the site");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
            ReportManager.log(" Logout successful. Test flow completed.");
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130452")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130456")
    @Test(groups = {"Regression", "Shipping_DDASN"}, description = "Check that the RGR supplier can draft and then submit the auto approved DD ASN request")
    @Description("Check that the RGR supplier can draft and then submit the auto approved DD ASN request")
    public void draftThenSubmitDDASNRequest() throws InterruptedException {

        ReportManager.log(" Starting flow: Draft then Submit DD ASN Request");

        try {
            ReportManager.log("Step 1: Connect to DB and fetch active PO with remaining quantities");
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
            launchPadPage = new LaunchPadPage(driver);
            launchPadPage.openLaunchpadMenu()
                    .openPageFromLaunchpad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter POs by DD ASN details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(ddAsnRegion, ddAsnCustomerName, ddAsnSloc, PONumber);

            ReportManager.log("Step 5: Select PO line items and initiate DD ASN request");
            new PoDetailsPage(driver)
                    .selectFirstPoLineItems()
                    .clickOnRequestDDAsnButton()
                    .clickEscapeAnnouncementButton();

            ReportManager.log("Step 6: Fill and draft DD ASN request");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage.createNewDDAsnRequest(
                            ddAsnInvoiceNumber,
                            ddAsnDeliveryTime,
                            ddAsnBatchNumber,
                            ddAsnQuantity,
                            DateValues.manufacturingDateMedium,
                            DateValues.expiryDateMedium,
                            DateValues.deliveryDateMedium)
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster();
            draftedBusinessKey = asnNewRequestPage.getDraftedBusinessKey();
            ReportManager.logDiscrete("Drafted Business Key: " + draftedBusinessKey);

            ReportManager.log("Step 7: Open My Requests from Launchpad");
            launchPadPage.openLaunchpadMenu()
                    .openPageFromLaunchpad(myRequests_launchpadIcon);

            ReportManager.log("Step 8: Search and open drafted request");
            myRequestListPage = new MyRequestListPage(driver);
            myRequestListPage.SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 9: Validate request status is DRAFTED");
            requestDetailsPage = new RequestDetailsPage(driver);
            requestDetailsPage.validateRequestStatus(RequestStatus.DRAFTED);

            ReportManager.log("Step 10: Submit the drafted DD ASN request");
            asnNewRequestPage.clickSubmitButton()
                    .copyRequestBusinessKey()
                    .BackToMyRequests();
            submittedBusinessKey = asnNewRequestPage.getBusinessKey();
            ReportManager.logDiscrete("Submitted Business Key: " + submittedBusinessKey);

            ReportManager.log("Step 11: Validate submitted business key matches drafted one");
            requestDetailsPage.validateSubmittedBusinessKeySameDrafted(submittedBusinessKey, draftedBusinessKey);

            ReportManager.log("Step 12: Wait for status to change to APPROVED in DB");
            databaseRepository.waitForStatusChange(
                    connectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(submittedBusinessKey),
                    RequestStatus.APPROVED
            );

            ReportManager.log("Step 13: Open submitted request and validate APPROVED status");
            myRequestListPage.SearchForBusinessKeyOnMyRequests(submittedBusinessKey)
                    .openRequestDetails(submittedBusinessKey);
            requestDetailsPage.validateRequestStatus(RequestStatus.APPROVED);

            ReportManager.log(" DD ASN draft request successfully submitted and approved.");
        } catch (Exception e) {
            ReportManager.log(" Test failed due to: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log("Step 14: Logging out from the system...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
            ReportManager.log(" Logout successful. Test completed.");
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130453")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130454")
    @Test(groups = {"Regression", "Shipping_DDASN"}, description = "Check that the RGR supplier can draft the empty auto approved DD ASN request then Delete it")
    @Description("Check that the RGR supplier can draft the empty auto approved DD ASN request then Delete it")
    public void draftEmptyDDASNRequestThenDelete() {

        ReportManager.log(" Starting flow: Draft Empty DD ASN Request Then Delete");

        try {
            ReportManager.log("Step 1: Fetch active PO with remaining quantities from database");
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
            launchPadPage = new LaunchPadPage(driver);
            launchPadPage.openLaunchpadMenu()
                    .openPageFromLaunchpad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter POs by DD ASN details and open PO details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(ddAsnRegion, ddAsnCustomerName, ddAsnSloc, PONumber);

            ReportManager.log("Step 5: Select PO line items and initiate DD ASN request");
            new PoDetailsPage(driver)
                    .selectFirstPoLineItems()
                    .clickOnRequestDDAsnButton()
                    .clickEscapeAnnouncementButton();

            ReportManager.log("Step 6: Draft empty DD ASN request");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage.clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster();
            draftedBusinessKey = asnNewRequestPage.getDraftedBusinessKey();
            ReportManager.logDiscrete("Drafted Business Key: " + draftedBusinessKey);

            ReportManager.log("Step 7: Open My Requests page from Launchpad");
            launchPadPage.openLaunchpadMenu()
                    .openPageFromLaunchpad(myRequests_launchpadIcon);

            ReportManager.log("Step 8: Search for drafted request in My Requests");
            myRequestListPage = new MyRequestListPage(driver);
            myRequestListPage.SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 9: Validate drafted status and delete the draft");
            requestDetailsPage = new RequestDetailsPage(driver);
            requestDetailsPage.validateRequestStatus(RequestStatus.DRAFTED)
                    .ClickOnDeleteDraftButton();

            ReportManager.log("Step 10: Verify the draft request no longer exists in My Requests");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .ValidateDeletedDraftNotInRequests();

            ReportManager.log(" Empty DD ASN draft successfully created and deleted.");
        } catch (Exception e) {
            ReportManager.log(" Test failed due to: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log("Step 11: Logging out from the system...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
            ReportManager.log(" Logout successful. Test completed.");
        }
    }
    @BeforeClass(groups = {"Regression", "Shipping_DDASN",})
    public void beforeClass() {
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        usersDetailsTestData = new SHAFT.TestData.JSON("UserCredentials.Json");
        filterOptionsTestData = new SHAFT.TestData.JSON("StorageLocationsFilters.json");
        ddAsnRequestDetailsTestData = new SHAFT.TestData.JSON("AsnRequestDetails.json");
        launchpadIcons = new SHAFT.TestData.JSON("LaunchpadIconsName.json");
        connectionString = usersDetailsTestData.getTestData("asnSupplier_PurchasingDBConnectionString");
        nupcoPOs_launchpadIcon = launchpadIcons.getTestData("NUPCO_POs");
        myRequests_launchpadIcon = launchpadIcons.getTestData("My_Requests");
        asnEmail = usersDetailsTestData.getTestData("asnSupplier_email");
        asnPassword = usersDetailsTestData.getTestData("asnSupplier_password");
        ddAsnRegion = filterOptionsTestData.getTestData("RHD_Region");
        ddAsnCustomerName = filterOptionsTestData.getTestData("RHD_CustomerName");
        ddAsnSloc = filterOptionsTestData.getTestData("RHD_SLOC");
        ddAsnInvoiceNumber = ddAsnRequestDetailsTestData.getTestData("invoiceNum");
        ddAsnDeliveryTime = ddAsnRequestDetailsTestData.getTestData("deliveryTime");
        ddAsnPalletsNumber = ddAsnRequestDetailsTestData.getTestData("palletsNum");
        ddAsnBatchNumber = ddAsnRequestDetailsTestData.getTestData("batchNum");
        ddAsnQuantity = ddAsnRequestDetailsTestData.getTestData("qty");
        ddAsnFeedbackData = ddAsnRequestDetailsTestData.getTestData("feedbackData");
        ddAsnTruckValue = ddAsnRequestDetailsTestData.getTestData("truckValue");

        ddASNRequestDetailsParams = new InboundRequestsParams
                (
                        connectionString,
                        asnEmail,
                        asnPassword,
                        nupcoPOs_launchpadIcon,
                        ddAsnRegion,
                        ddAsnCustomerName,
                        ddAsnSloc,
                        ddAsnInvoiceNumber,
                        ddAsnDeliveryTime,
                        ddAsnPalletsNumber,
                        ddAsnBatchNumber,
                        ddAsnQuantity,
                        RequestStatus.APPROVED, true, ddAsnFeedbackData,
                        ddAsnTruckValue,
                        DateValues.manufacturingDateMedium,
                        DateValues.expiryDateMedium,
                        DateValues.deliveryDateMedium
                );

    }

    @BeforeMethod(groups = {"Regression", "Shipping_DDASN"})
    public void beforeMethod() {
        driver = new SHAFT.GUI.WebDriver();
        new LoginPage(driver)
                .navigateToLoginPage();
    }

    @AfterMethod(groups = {"Regression", "Shipping_DDASN"})
    public void afterMethod() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
