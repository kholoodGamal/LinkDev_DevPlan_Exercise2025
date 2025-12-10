package com.nupco.tests.InboundFlowsTests;

import com.nupco.pages.*;
import com.nupco.sqlqueries.PurchaseOrderManagerSqlQueries;
import com.nupco.utils.*;
import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.Duration;

import static com.nupco.utils.helper.exportPDFAndValidateFileDownloaded;


public class AsnFlowTests {

    // region Variables
    private SHAFT.GUI.WebDriver driver;
    private SHAFT.TestData.JSON usersDetailsTestData, filterOptionsTestData, AsnRequestDetailsTestData, launchpadIcons;
    InboundRequestsParams ASNRequestDetailsParams;
    String poNumber, businessKey, SubmittedDraftedBusinessKey, updatedInvoiceNumber, exportedPdfName, asnTruckValue, asnFeedbackData, asnQuantity, asnBatchNumber, asnPalletsNumber, asnDeliveryTime, asnInvoiceNumber, asnSloc, asnCustomerName, asnRegion, asnPassword, asnEmail, inboundEmail, inboundPassword, connectionString, draftedBusinessKey, myRequests_launchpadIcon, nupcoPOs_launchpadIcon;
    AsnNewRequestPage asnNewRequestPage;
    DatabaseRepository databaseRepository;
    PurchaseOrderManagerSqlQueries purchaseOrderManagerSqlQueries;
    SQLConnectionManager connectionManager;
    BaseFunctions baseFunctions;

    //endregion

    //region Test Cases

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118140")
    @Test(groups = {"Regression", "Shipping_Asn", "Normal_Sanity"}, description = "ASN supplier can Create ASN request")
    @Description("Validate that the ASN supplier can Create ASN request")
    public void createAsnRequest() throws InterruptedException {

        try {
            ReportManager.log("Step 1: Retrieve active PO number with remaining quantities from DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            poNumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(
                            connectionString,
                            purchaseOrderManagerSqlQueries.getPoNumberOfAsnRequest()
                    ).get(0);
            Assert.assertNotNull(poNumber, "PO Number should not be null before creating ASN request");

            ReportManager.log("Step 2: Login with ASN supplier credentials");
            new LoginPage(driver)
                    .userLoggedIntoSiteSuccessfully(asnEmail, asnPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Open NUPCO POs page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter and open PO details using supplier filters");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(
                            asnRegion,
                            asnCustomerName,
                            asnSloc,
                            poNumber
                    );

            ReportManager.log("Step 5: Start ASN request creation from PO details page");
            new PoDetailsPage(driver).startAsnRequestCreation();

            ReportManager.log("Step 6: Fill and submit ASN request form");
            AsnNewRequestPage asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage.fillAndSubmitASNRequest(
                    asnInvoiceNumber,
                    asnDeliveryTime,
                    asnPalletsNumber,
                    asnBatchNumber,
                    asnQuantity,
                    false,
                    asnFeedbackData,
                    asnTruckValue,
                    DateValues.manufacturingDateMedium,
                    DateValues.expiryDateMedium,
                    DateValues.deliveryDateMedium
            );

            businessKey = asnNewRequestPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after ASN submission");

            ReportManager.log("Step 7: Wait until ASN request status becomes NEW in the database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 8: Search and open the submitted ASN request in My Requests");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 9: Validate ASN request status is NEW");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.NEW);

        } finally {
            ReportManager.log("Final Step: Logout ASN supplier user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118145")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118161")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118156")
    @Test(groups = {"Regression", "Shipping_Asn", "Normal_Sanity"}, description = "In Bound User Can Approve The Request Which Created By Asn User", dependsOnMethods = "createAsnRequest")
    @Description("Validate that InBound User Can Accept The Request Which Added By Asn User")
    public void inBoundUserCanAcceptRequestAddedByAsnUser() throws InterruptedException {
        try {
            ReportManager.log("Step 1: Login with Inbound user credentials");
            new LoginPage(driver)
                    .userLogin(inboundEmail, inboundPassword)
                    .navigateToStagEnvDashboard()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open My Tasks page and locate the ASN request by business key");
            MyTasksPage myTasksPage = new MyTasksPage(driver);
            myTasksPage
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey);

            ReportManager.log("Step 3: Assign the ASN request task to the Inbound user");
            myTasksPage.userAssignTaskToHim(businessKey);

            ReportManager.log("Step 4: Search again for the same request and open its details page");
            myTasksPage
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey);

            ReportManager.log("Step 5: Approve the assigned ASN request as the Inbound user");
            myTasksPage.userApproveAssignedRequest();

            ReportManager.log("Step 6: Wait until ASN request status becomes APPROVED in the database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(businessKey),
                    RequestStatus.APPROVED
            );

            ReportManager.log("Step 7: Validate request status is APPROVED in UI");
            myTasksPage.validateThatRequestStatusIsApproved();

        } finally {
            ReportManager.log("Final Step: Logout Inbound user from the application");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118152")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118162")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118160")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118167")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118165")
    @Test(groups = {"Regression", "Shipping_Asn", "Normal_Sanity"}, description = "In Bound User Can Reject The Request Which Created By Asn User And Booked Quantity Is Deducted")
    @Description("Validate that InBound User Can Reject The Request Which Added By Asn User And Booked Quantity Is Deducted")
    public void inBoundUserCanRejectRequestAddedByAsnUserAndBookedQuantityIsReleased() throws InterruptedException, ParseException {

        try {
            ReportManager.log("Step 1: Create ASN request using BaseFunctions");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createAsnRequest(ASNRequestDetailsParams);

            ReportManager.log("Step 2: Validate quantities are booked after submission");
            baseFunctions.validateQuantitiesAreBookedAfterSubmission(ASNRequestDetailsParams);

            businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after ASN creation");

            ReportManager.log("Step 3: Logout ASN supplier user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 4: Login with Inbound user credentials");
            new LoginPage(driver)
                    .userLogin(inboundEmail, inboundPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 5: Locate and reject ASN request from My Tasks");
            MyTasksPage myTasksPage = new MyTasksPage(driver);
            myTasksPage
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .rejectRequestAssign()
                    .validateThatRequestStatusIsRejected();

            ReportManager.log("Step 6: Logout Inbound user after rejection");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 7: Login again with ASN supplier credentials");
            new LoginPage(driver)
                    .userLogin(asnEmail, asnPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 8: Open NUPCO POs page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 9: Wait up to 3 minutes for integration reflection before verifying booking quantity");
            helper.WaitForIntegrationReflection(Duration.ofMinutes(3));

            ReportManager.log("Step 10: Open PO details page using ASN customer filters");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(
                            asnRegion,
                            asnCustomerName,
                            asnSloc,
                            baseFunctions.getPONumber()
                    );

            ReportManager.log("Step 11: Validate booking quantity is released after rejection");
            PoDetailsPage poDetailsPage = new PoDetailsPage(driver);
            int updatedBookedQty = poDetailsPage.readBookedQtyValue();

            poDetailsPage.ValidateNewBookingQtyValueAfterRequestRejection(
                    baseFunctions.getPoInitialBookedQuantity(),
                    updatedBookedQty
            );

            ReportManager.log("Step 12: Assertion — Booked quantity should be released successfully");
            Assert.assertTrue(
                    updatedBookedQty < baseFunctions.getPoInitialBookedQuantity(),
                    "Booked quantity should decrease after ASN request rejection"
            );

        } finally {
            ReportManager.log("Final Step: Ensure user is logged out from the application");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118130")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118150")
    @Test(groups = {"Regression", "Shipping_Asn"}, description = "ASN supplier can Draft empty ASN request then delete it")
    @Description("Validate that the ASN supplier can Draft empty ASN request then delete it")
    public void draftEmptyAsnRequest() {

        try {
            ReportManager.log("Step 1: Retrieve active PO number with remaining quantities from the database");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            poNumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(
                            connectionString,
                            purchaseOrderManagerSqlQueries.getPoNumberOfAsnRequest()
                    ).getFirst();
            Assert.assertNotNull(poNumber, "PO Number should not be null before drafting ASN request");

            ReportManager.log("Step 2: Login with ASN Supplier credentials");
            new LoginPage(driver)
                    .userLoggedIntoSiteSuccessfully(asnEmail, asnPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Open NUPCO POs page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter and open PO details using supplier filters");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(
                            asnRegion,
                            asnCustomerName,
                            asnSloc,
                            poNumber
                    );

            ReportManager.log("Step 5: Start ASN request creation from PO details page");
            new PoDetailsPage(driver).startAsnRequestCreation();

            ReportManager.log("Step 6: Draft ASN request without filling mandatory fields");
            AsnNewRequestPage asnNewRequestPage = new AsnNewRequestPage(driver);
            draftedBusinessKey = asnNewRequestPage
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();
            Assert.assertNotNull(draftedBusinessKey, "Drafted Business Key should not be null after drafting");

            ReportManager.log("Step 7: Open My Requests page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequests_launchpadIcon);

            ReportManager.log("Step 8: Search and open drafted ASN request from My Requests list");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 9: Validate that the request status is DRAFTED and delete the draft");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED)
                    .ClickOnDeleteDraftButton();

            ReportManager.log("Step 10: Validate the deleted draft no longer appears in My Requests list");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .ValidateDeletedDraftNotInRequests();

        } finally {
            ReportManager.log("Final Step: Logout ASN Supplier user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118144")
    @Test(groups = {"Regression", "Shipping_Asn"}, description = "ASN supplier can Draft Drafted ASN request")
    @Description("Validate that the ASN supplier can Draft Drafted ASN request")
    public void validateAsnCanDraftDraftedRequest() {

        try {
            ReportManager.log("Step 1: Retrieve active PO number with remaining quantities from the database");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            poNumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(
                            connectionString,
                            purchaseOrderManagerSqlQueries.getPoNumberOfAsnRequest()
                    ).get(2);
            Assert.assertNotNull(poNumber, "PO Number should not be null before drafting ASN request");

            ReportManager.log("Step 2: Login with ASN Supplier credentials");
            new LoginPage(driver)
                    .userLoggedIntoSiteSuccessfully(asnEmail, asnPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Open NUPCO POs page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter and open PO details using supplier filters");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(
                            asnRegion,
                            asnCustomerName,
                            asnSloc,
                            poNumber
                    );

            ReportManager.log("Step 5: Start ASN request creation from PO details page");
            new PoDetailsPage(driver).startAsnRequestCreation();

            ReportManager.log("Step 6: Draft ASN request and capture drafted business key");
            AsnNewRequestPage asnNewRequestPage = new AsnNewRequestPage(driver);
            draftedBusinessKey = asnNewRequestPage
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();
            Assert.assertNotNull(draftedBusinessKey, "Drafted Business Key should not be null after drafting request");

            ReportManager.log("Step 7: Open My Requests page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequests_launchpadIcon);

            ReportManager.log("Step 8: Search and open the drafted ASN request in My Requests");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 9: Validate that the request status is DRAFTED");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED);

            ReportManager.log("Step 10: Re-draft the same ASN request again");
            asnNewRequestPage
                    .clickDraftButton();

            ReportManager.log("Step 11: Validate the request status remains DRAFTED after re-drafting");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED);

        } finally {
            ReportManager.log("Final Step: Logout ASN Supplier user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/180076")
    @Test(groups = {"Regression", "Shipping_Asn"}, description = "Check that Number of Pallets are reduced after requests creation")
    @Description("Check that Pallets are booked successfully")
    public void validatePalletsAreBookedSuccessfully() throws InterruptedException {
        try {
            ReportManager.log("Step 1: Retrieve active PO number with remaining quantities from the database");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            poNumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(
                            connectionString,
                            purchaseOrderManagerSqlQueries.getPoNumberOfAsnRequest()
                    ).getFirst();
            Assert.assertNotNull(poNumber, "PO Number should not be null before creating ASN request");

            ReportManager.log("Step 2: Login with ASN Supplier credentials");
            new LoginPage(driver)
                    .userLoggedIntoSiteSuccessfully(asnEmail, asnPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Open NUPCO POs page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter and open PO details using ASN customer details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(
                            asnRegion,
                            asnCustomerName,
                            asnSloc,
                            poNumber
                    );

            ReportManager.log("Step 5: Start ASN request creation from PO details page");
            new PoDetailsPage(driver).startAsnRequestCreation();

            ReportManager.log("Step 6: Fill and submit ASN request form");
            AsnNewRequestPage asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage.fillAndSubmitASNRequest(
                    asnInvoiceNumber,
                    asnDeliveryTime,
                    asnPalletsNumber,
                    asnBatchNumber,
                    asnQuantity,
                    false,
                    asnFeedbackData,
                    asnTruckValue,
                    DateValues.manufacturingDateMedium,
                    DateValues.expiryDateMedium,
                    DateValues.deliveryDateMedium
            );

            businessKey = asnNewRequestPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after ASN submission");

            ReportManager.log("Step 7: Wait until ASN request status becomes NEW in the database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 8: Reopen NUPCO POs page from Launchpad to validate pallet updates");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 9: Open the same PO details again for verification");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(
                            asnRegion,
                            asnCustomerName,
                            asnSloc,
                            poNumber
                    );

            ReportManager.log("Step 10: Start new ASN request creation again for pallet validation");
            new PoDetailsPage(driver).startAsnRequestCreation();

            ReportManager.log("Step 11: Open ASN request page and fill delivery date and time");
            asnNewRequestPage.openAsnRequestPageAndFillDeliveryDateAndTime(
                    asnInvoiceNumber,
                    asnDeliveryTime,
                    DateValues.normalDeliveryDate

            );

            ReportManager.log("Step 12: Validate that pallet quantity is booked correctly after previous submission");
            int numOfPallets = Integer.parseInt(asnNewRequestPage.getNumOfPallets());
            int availablePallets = Integer.parseInt(asnNewRequestPage.readAvailableNumOfPallets());
            int orderedQty = Integer.parseInt(asnQuantity);

            asnNewRequestPage.ValidateNewNumOfPalletsValue(numOfPallets, availablePallets, orderedQty);

        } finally {
            ReportManager.log("Final Step: Logout ASN Supplier user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/180077")
    @Test(groups = {"Regression", "Shipping_Asn"}, description = "Validate that Number of Pallets are increased after requests is rejected by inbound")
    @Description("Validate that Number of Pallets are increased after requests is rejected by inbound")
    public void validateThatNoOfPalletsAreIncreasedAfterRequestRejectedByInbound() throws InterruptedException, ParseException {

        try {
            ReportManager.log("Step 1 → Creating ASN request as Supplier...");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createAsnRequest(ASNRequestDetailsParams);
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            businessKey = baseFunctions.getBusinessKey();
            String initialPalletsNo = baseFunctions.getPalletsNo();

            ReportManager.log("Step 2 → Logging in as Inbound user...");
            new LoginPage(driver)
                    .userLogin(usersDetailsTestData.getTestData("Inbound_email"), usersDetailsTestData.getTestData("Inbound_password"))
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3 → Rejecting ASN request from Inbound queue...");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .rejectRequestAssign()
                    .validateThatRequestStatusIsRejected();

            ReportManager.log("Step 4 → Logging out from Inbound user and logging in as Supplier...");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(asnEmail, asnPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 5 → Opening NUPCO POs page...");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 6 → Filtering and opening the same PO details...");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(
                            asnRegion,
                            asnCustomerName,
                            asnSloc,
                            baseFunctions.getPONumber()
                    );

            ReportManager.log("Step 7 → Initiating ASN request creation...");
            new PoDetailsPage(driver)
                    .startAsnRequestCreation();

            // Step 8 → Fill ASN delivery date and time
            ReportManager.log("Step 8 → Filling ASN delivery date and time...");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage.openAsnRequestPageAndFillDeliveryDateAndTime(
                    asnInvoiceNumber,
                    asnDeliveryTime,
                    DateValues.normalDeliveryDate
                    );

            ReportManager.log("Step 9 → Validating pallet count after rejection...");
            asnNewRequestPage.ValidateNumOfPalletsAreUpdatedAfterInboundRejection(
                    Integer.parseInt(initialPalletsNo),
                    Integer.parseInt(asnNewRequestPage.readAvailableNumOfPallets())
            );

            ReportManager.logDiscrete(" Test Passed: Pallet count was correctly updated after Inbound rejection.");

        } catch (Exception e) {
            ReportManager.logDiscrete(" Test Failed: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log(" Cleanup → Logging out user (if still logged in)...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/180079")
    @Test(groups = {"Regression", "Shipping_Asn"}, description = "Verify that Quantity still booked after approval by inbound user")
    @Description("Verify that Quantity still booked after approval by inbound user")
    public void verifyThatQuantityStillBookedAfterApprovalByInboundUser() throws InterruptedException, ParseException {

        try {

            ReportManager.log("Step 1 → Creating ASN request as Supplier...");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createAsnRequest(ASNRequestDetailsParams);

            String businessKey = baseFunctions.getBusinessKey();
            ReportManager.log("Business Key captured: " + businessKey);


            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 2 → Logging in as Inbound user...");
            new LoginPage(driver)
                    .userLogin(inboundEmail, inboundPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();


            ReportManager.log("Step 3 → Approving ASN request as Inbound user...");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .userApproveAssignedRequest()
                    .validateThatRequestStatusIsApproved();

            ReportManager.log(" Request approved successfully in Inbound workflow.");


            ReportManager.log("Step 4 → Logging out from Inbound user and logging in as Supplier...");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(asnEmail, asnPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 5 → Validating booked quantities after Inbound approval...");
            baseFunctions.validateQuantitiesAreBookedAfterSubmission(ASNRequestDetailsParams);

            ReportManager.logDiscrete(" Test Passed: Quantities remain correctly booked after Inbound approval.");

        } catch (Exception e) {
            ReportManager.logDiscrete(" Test Failed: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log(" Cleanup → Logging out user (if still logged in)...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118157")
    @Test(groups = {"Regression", "Shipping_Asn"}, description = "In Bound User Can Change Delivery date and time")
    @Description("Validate that InBound User Can Change Delivery date and time")
    public void inBoundUserCanChangeDeliveryDateAndTimeAddedByAsnUser() throws InterruptedException, ParseException {

        try {

            ReportManager.log("Step 1 → Creating ASN request as Supplier...");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createAsnRequest(ASNRequestDetailsParams);

            String businessKey = baseFunctions.getBusinessKey();
            ReportManager.log("Business Key captured: " + businessKey);


            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();


            ReportManager.log("Step 2 → Logging in as Inbound user...");
            new LoginPage(driver)
                    .userLogin(inboundEmail, inboundPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();


            ReportManager.log("Step 3 → Opening request and updating delivery date/time...");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .changeDeliveryDateAndTime(DateValues.normalDeliveryDate)
                    .validateThatDeliveryTimeIsChangedSuccessfully();

            ReportManager.logDiscrete(" Test Passed: Inbound user successfully changed delivery date and time of the ASN request.");

        } catch (Exception e) {
            ReportManager.logDiscrete(" Test Failed: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log(" Cleanup → Logging out user (if still logged in)...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118139")
    @Test(groups = {"Regression", "Shipping_Asn"}, description = "ASN User Can export request as PDF After Inbound Confirmation")
    @Description("Asn User Can Export PDF After Inbound Confirm")
    public void AsnUserCanExportPDFAfterInboundConfirm() throws InterruptedException, ParseException {

        try {

            ReportManager.log("Step 1 → Creating ASN request as ASN Supplier...");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createAsnRequest(ASNRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();
            ReportManager.log("Captured Business Key: " + businessKey);
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 2 → Logging in as Inbound user...");
            new LoginPage(driver)
                    .userLogin(inboundEmail, inboundPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3 → Approving ASN request by Inbound user...");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .userApproveAssignedRequest();

            ReportManager.log("Step 4 → Logging out Inbound user and re-login as ASN Supplier...");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(asnEmail, asnPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 5 → Navigating to My Requests page...");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequests_launchpadIcon);

            ReportManager.log("Step 6 → Opening the submitted ASN request...");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 7 → Exporting the ASN request as PDF and validating file download...");
            exportPDFAndValidateFileDownloaded(driver,businessKey,exportedPdfName);


            ReportManager.logDiscrete(" Test Passed: ASN Supplier successfully exported the ASN request as PDF after Inbound approval.");

        } catch (Exception e) {
            ReportManager.logDiscrete(" Test Failed: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log(" Cleanup → Logging out any logged-in user...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118148")
    @Test(groups = {"Regression", "Shipping_Asn", "Normal_Sanity"}, description = "Asn User Can Export PDF After Request Submission")
    @Description("Asn User Can Export PDF After Request Submission")
    public void AsnUserCanExportPDFAfterRequestSubmission() throws InterruptedException, ParseException {
        try {

            ReportManager.log("Step 1 → Creating ASN request as ASN Supplier...");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createAsnRequest(ASNRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();
            ReportManager.log("Captured Business Key: " + businessKey);


            ReportManager.log("Step 2 → Opening the newly submitted ASN request from My Requests...");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);


            ReportManager.log("Step 3 → Exporting the ASN request as PDF and validating download...");
            exportPDFAndValidateFileDownloaded(driver,businessKey,exportedPdfName);

            ReportManager.logDiscrete(" Test Passed: ASN Supplier successfully exported the ASN request as PDF after submission.");

        } catch (Exception e) {
            ReportManager.logDiscrete(" Test Failed: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log(" Cleanup → Logging out any logged-in user...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118147")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118149")
    @Test(groups = {"Regression", "Shipping_Asn"}, description = "Verify that Note And Truck Number are displayed in review By Inbound and request Details By Asn")
    @Description("Verify that Note And Truck Number are displayed in review By Inbound and request Details By Asn")
    public void verifyThatNoteAndTruckNumberAreDisplayedForAsnAndInbound() throws InterruptedException, ParseException {

        try {
            ReportManager.log("Step 1 → Creating ASN request as ASN Supplier...");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createAsnRequest(ASNRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();
            ReportManager.log("Captured Business Key: " + businessKey);

            ReportManager.log("Step 2 → Opening ASN request from My Requests to verify details...");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 3 → Validating Truck Number and Notes visibility for ASN user...");
            new AsnNewRequestPage(driver)
                    .ValidateThatTheValueOfTrucksIsDisplayedForAsn(asnTruckValue)
                    .ValidateThatTheDataOfNoteIsDisplayed(asnFeedbackData);

            ReportManager.log("Step 4 → Logging out ASN Supplier user...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 5 → Logging in as Inbound user to verify same data...");
            new LoginPage(driver)
                    .userLogin(inboundEmail, inboundPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 6 → Navigating to My Tasks and opening the ASN request...");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey);


            ReportManager.log("Step 7 → Validating Truck Number and Notes visibility for Inbound user...");
            new AsnNewRequestPage(driver)
                    .ValidateThatTheValueOfTrucksIsDisplayedForInbound(asnTruckValue)
                    .ValidateThatTheDataOfNoteIsDisplayed(asnFeedbackData);

            ReportManager.logDiscrete(" Test Passed: Truck Number and Note fields are displayed correctly for both ASN and Inbound users.");

        } catch (Exception e) {
            ReportManager.logDiscrete(" Test Failed: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log(" Cleanup → Logging out any logged-in user...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118169")
    @Test(groups = {"Regression", "Shipping_Asn", "Normal_Sanity"}, description = "Check that ASN supplier can change Invoive Number after request is approved")
    @Description("Check that ASN Supplier can change Invoice Number")
    public void validateSupplierCanChangeInvoiceNumber() throws InterruptedException, ParseException {

        try {
            ReportManager.log("Step 1 → Creating ASN request as ASN Supplier...");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createAsnRequest(ASNRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();
            ReportManager.log("Captured Business Key: " + businessKey);

            ReportManager.log("Step 2 → Logging out ASN Supplier user...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 3 → Logging in as Inbound user to approve the ASN request...");
            new LoginPage(driver)
                    .userLogin(inboundEmail, inboundPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 4 → Assigning and approving the ASN request by Inbound user...");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .userApproveAssignedRequest()
                    .validateThatRequestStatusIsApproved();

            ReportManager.log("Step 5 → Logging out Inbound user...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 6 → Logging back in as ASN Supplier to edit Invoice Number...");
            new LoginPage(driver)
                    .userLogin(asnEmail, asnPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 7 → Navigating to My Requests and opening the ASN request...");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequests_launchpadIcon);
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 8 → Updating Invoice Number and validating success message...");
            new RequestDetailsPage(driver)
                    .inputInvNumber(updatedInvoiceNumber)
                    .updateIRNumber()
                    .validateInvNumberMessageDisplayed()
                    .validateInvNumberisUpdated(updatedInvoiceNumber);

            ReportManager.logDiscrete(" Test Passed: ASN Supplier successfully updated Invoice Number after Inbound approval.");

        } catch (Exception e) {
            ReportManager.logDiscrete(" Test Failed: " + e.getMessage());
            throw e;
        } finally {
            ReportManager.log(" Cleanup → Logging out any logged-in user...");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118142")
    @Test(groups = {"Regression", "Shipping_Asn"}, description = "Validate Supplier's Ability to Submit a Drafted Request")
    @Description("Validate Supplier's Ability to Submit a Drafted Request")
    public void submitDraftedAsnRequest() throws InterruptedException {

        try {
            ReportManager.log("Step 1: Retrieve active PO number with remaining quantities from the database");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            poNumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(
                            connectionString,
                            purchaseOrderManagerSqlQueries.getPoNumberOfAsnRequest()
                    ).getFirst();
            Assert.assertNotNull(poNumber, "PO Number should not be null before drafting ASN request");

            ReportManager.log("Step 2: Login with ASN Supplier credentials");
            new LoginPage(driver)
                    .userLoggedIntoSiteSuccessfully(asnEmail, asnPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Open NUPCO POs page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter and open PO details using ASN customer details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(
                            asnRegion,
                            asnCustomerName,
                            asnSloc,
                            poNumber
                    );

            ReportManager.log("Step 5: Select PO line items and initiate ASN request");
            new PoDetailsPage(driver)
                    .startAsnRequestCreation();

            ReportManager.log("Step 6: Draft ASN request filling filling mandatory fields");
            AsnNewRequestPage asnNewRequestPage = new AsnNewRequestPage(driver);
            draftedBusinessKey = asnNewRequestPage
                    .createNewAsnRequest(
                            asnInvoiceNumber,
                            asnDeliveryTime,
                            asnPalletsNumber,
                            asnBatchNumber,
                            asnQuantity,
                            DateValues.manufacturingDateMedium,
                            DateValues.expiryDateMedium,
                            DateValues.deliveryDateMedium)
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();
            Assert.assertNotNull(draftedBusinessKey, "Drafted Business Key should not be null after drafting");

            ReportManager.log("Step 7: Open My Requests page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequests_launchpadIcon);

            ReportManager.log("Step 8: Search and open drafted ASN request from My Requests list");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 9: Validate that the request status is DRAFTED and delete the draft");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED);

            ReportManager.log("Step 10: Submit the drafted ASN request");
            SubmittedDraftedBusinessKey = new AsnNewRequestPage(driver)
                    .clickSubmitButton()
                    .copyRequestBusinessKey()
                    .BackToMyRequests()
                    .getBusinessKey();

            ReportManager.log("Step 11: Validate submitted BusinessKey equals the Drafted BusinessKey");
            new RequestDetailsPage(driver)
                    .validateSubmittedBusinessKeySameDrafted(SubmittedDraftedBusinessKey, draftedBusinessKey);

            ReportManager.log("Step 12: Wait until status updates to 'NEW' in the Database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(SubmittedDraftedBusinessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 13: Search again in My Requests for the same BusinessKey");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 14: Validate request status is now 'NEW'");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.NEW);

        } finally {
            ReportManager.log("Final Step: Logout from the site to close the session cleanly");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }



    @BeforeClass(groups = {"Regression", "Shipping_Asn", "Normal_Sanity"})
    public void beforeClass() {
        usersDetailsTestData = new SHAFT.TestData.JSON("UserCredentials.Json");
        filterOptionsTestData = new SHAFT.TestData.JSON("StorageLocationsFilters.json");
        AsnRequestDetailsTestData = new SHAFT.TestData.JSON("AsnRequestDetails.json");
        launchpadIcons = new SHAFT.TestData.JSON("LaunchpadIconsName.json");
        connectionString = usersDetailsTestData.getTestData("asnSupplier_PurchasingDBConnectionString");
        asnEmail = usersDetailsTestData.getTestData("asnSupplier_email");
        asnPassword = usersDetailsTestData.getTestData("asnSupplier_password");
        inboundEmail = usersDetailsTestData.getTestData("Inbound_email");
        inboundPassword = usersDetailsTestData.getTestData("Inbound_password");
        nupcoPOs_launchpadIcon = launchpadIcons.getTestData("NUPCO_POs");
        myRequests_launchpadIcon = launchpadIcons.getTestData("My_Requests");
        asnRegion = filterOptionsTestData.getTestData("ASN_Region");
        asnCustomerName = filterOptionsTestData.getTestData("ASN_CustomerName");
        asnSloc = filterOptionsTestData.getTestData("ASN_SLOC");
        asnInvoiceNumber = AsnRequestDetailsTestData.getTestData("invoiceNum");
        updatedInvoiceNumber = AsnRequestDetailsTestData.getTestData("updateInvoiceNum");
        asnDeliveryTime = AsnRequestDetailsTestData.getTestData("deliveryTime");
        asnPalletsNumber = AsnRequestDetailsTestData.getTestData("palletsNum");
        asnBatchNumber = AsnRequestDetailsTestData.getTestData("batchNum");
        asnQuantity = AsnRequestDetailsTestData.getTestData("qty");
        asnFeedbackData = AsnRequestDetailsTestData.getTestData("feedbackData");
        asnTruckValue = AsnRequestDetailsTestData.getTestData("truckValue");
        exportedPdfName = AsnRequestDetailsTestData.getTestData("exportPdfFileName");
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);


        ASNRequestDetailsParams = new InboundRequestsParams
                (
                        connectionString,
                        asnEmail,
                        asnPassword,
                        nupcoPOs_launchpadIcon,
                        asnRegion,
                        asnCustomerName,
                        asnSloc,
                        asnInvoiceNumber,
                        asnDeliveryTime,
                        asnPalletsNumber,
                        asnBatchNumber,
                        asnQuantity,
                        RequestStatus.NEW, true, asnFeedbackData,
                        asnTruckValue,
                        DateValues.manufacturingDateMedium,
                        DateValues.expiryDateMedium,
                        DateValues.deliveryDateMedium
                );

    }

    @BeforeMethod(groups = {"Regression", "Shipping_Asn", "Normal_Sanity"})
    public void beforeMethod() {
        driver = new SHAFT.GUI.WebDriver();
        new LoginPage(driver)
                .navigateToLoginPage();
    }

    @AfterMethod(groups = {"Regression", "Shipping_Asn", "Normal_Sanity"})
    public void afterMethod() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
