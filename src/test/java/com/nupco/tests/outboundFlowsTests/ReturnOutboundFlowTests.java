package com.nupco.tests.outboundFlowsTests;

import com.nupco.pages.*;
import com.nupco.sqlqueries.PurchaseOrderManagerSqlQueries;
import com.nupco.utils.*;
import com.shaft.cli.FileActions;
import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.nupco.utils.helper.exportPDFAndValidateFileDownloaded;

public class ReturnOutboundFlowTests {

    // region Variables
    private SHAFT.GUI.WebDriver driver;
    private SHAFT.TestData.JSON usersDetailsTestData, launchpadIcons, returnOutboundRequestDetails;
    String businessKey, soEmail, soPassword, requestedQty, quantityReturnReason, qualityReturnReason, orderNoteTxt, connectionString, haEmail, draftedBusinessKey, haPassword, inboundEmail, inboundPwd, launchPad_ReturnOutbound, launchPad_MyRequests, filePath,SubmittedDraftedBusinessKey, fileName;
    DatabaseRepository databaseRepository;
    SQLConnectionManager connectionManager;
    PurchaseOrderManagerSqlQueries purchaseOrderManagerSqlQueries;
    ReturnOutboundRequestParams returnOutboundRequestParams;
    String orderId = "5040-250925-5026";
    String pdfName = "dummy.pdf";
    BaseFunctions baseFunctions;

    // endregion

    // region Test Cases

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165489")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "SO user submits the Return Outbound request")
    @Description("Check that the SO user submits the Return Outbound request without notes")
    public void createReturnOutboundRequest() throws InterruptedException {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver).userLogin(soEmail, soPassword).validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Return Outbound from Launchpad");
            new LaunchPadPage(driver).openLaunchpadMenu().openPageFromLaunchpad(launchPad_ReturnOutbound);

            ReportManager.log("Step 3: Choose GR, create and submit return outbound request");

            new GRListingPage(driver)
                    .initiateReturnOutboundRequest(orderId);
            new ReturnOutboundNewRequestPage(driver)
                    .fillAndSubmitReturnOutboundRequest(requestedQty, quantityReturnReason, false, orderNoteTxt, filePath);

            ReportManager.log("Step 4: Capture submitted request business key");
            SuccessScreenPage successPage = new SuccessScreenPage(driver);
            successPage.copyRequestBusinessKey();
            businessKey = successPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after Return Outbound request submission");
            successPage.BackToMyReturnRequests();

            ReportManager.log("Step 5: Wait until Return Outbound request status becomes 'Created' in DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            DBRequestStatus.waitForRequestStatus(databaseRepository, connectionString, purchaseOrderManagerSqlQueries.getReturnOutBoundStatusQuery(businessKey), RequestStatus.Created);

            ReportManager.log("Step 6: Search and open request from My Requests");
            new MyRequestListPage(driver).openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 7: Validate request status is NEW");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.NEW);

        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165523")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "Hospital Approver user approves return request with quantity issue", dependsOnMethods = "createReturnOutboundRequest")
    @Description("Validate that the HA user can approve the Return Outbound request with qty issue")
    public void validateHAUserApprovesQtyReturnRequest() {
        try {
            ReportManager.log("Step 1: Login with HA user");
            new LoginPage(driver).userLogin(haEmail, haPassword).validateLoginSuccessfully();

            ReportManager.log("Step 2: HA user approves the request");
            new MyTasksPage(driver).openMyTasksPage().openSpacePage().searchInSearchBar(businessKey).userAssignTaskToHim(businessKey).searchInSearchBar(businessKey).openTaskDetailsPageForHA(businessKey).userApproveAssignedRequest();

            ReportManager.log("Step 3: Wait until status becomes APPROVED in DB");
            DBRequestStatus.waitForRequestStatus(databaseRepository, connectionString, purchaseOrderManagerSqlQueries.getReturnOutBoundStatusQuery(businessKey), //CHANGE THIS
                    RequestStatus.ApprovedByApprovalUser);

            ReportManager.log("Step 4: Validate request status in UI");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.L1_APPROVED);

        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted while waiting for status update", e);
        } finally {
            ReportManager.log("Step 5: Logout HA user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }

    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165543")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "Inbound user approves return request with quantity issue", dependsOnMethods = {"createReturnOutboundRequest", "validateHAUserApprovesQtyReturnRequest"})
    @Description("Validate that the inbound user can approve the Return Outbound request with qty issue")
    public void validateInboundUserApprovesQtyReturnRequest() {
        try {
            ReportManager.log("Step 1: Login with inbound user");
            new LoginPage(driver).userLogin(inboundEmail, inboundPwd).validateLoginSuccessfully();

            ReportManager.log("Step 2: Inbound user approves the request");
            new MyTasksPage(driver).openMyTasksPage().openSpacePage().searchInSearchBar(businessKey).userAssignTaskToHim(businessKey).searchInSearchBar(businessKey).taskDetailsPageForInboundUserSecondReview(businessKey).userApproveAssignedRequest();

            ReportManager.log("Step 3: Wait until status becomes APPROVED in DB");
            DBRequestStatus.waitForRequestStatus(databaseRepository, connectionString, purchaseOrderManagerSqlQueries.getReturnOutBoundStatusQuery(businessKey), //CHANGE THIS
                    RequestStatus.INBOUND_APPROVED);

            ReportManager.log("Step 4: Validate request status in UI");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.APPROVED);

        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted while waiting for status update", e);
        } finally {
            ReportManager.log("Step 5: Logout Inbound user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }

    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165495")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "SO user cancels the created return Outbound request")
    @Description("SO user cancels the created return Outbound request")
    public void validateSoUserCanCancelReturnOutboundRequest() {
        try {

            ReportManager.log("Step 1: Create Outbound Request with SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createReturnOutboundRequest(returnOutboundRequestParams, quantityReturnReason);
            String businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request creation");

            ReportManager.log("Step 2: Search and open request from My Requests");
            new MyRequestListPage(driver).openTheRequestFromMyRequests(businessKey);


            ReportManager.log("Step 3: Cancel the return outbound request from request details page");
            new RequestDetailsPage(driver).cancelOutboundAndReturnOutboundRequest().validateRequestStatus(RequestStatus.CANCELLED);


        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted", e);
        } finally {

            ReportManager.log("Step 6: Logout SO user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165524")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "Inbound user approves outbound return request with Quality issue")
    @Description("Inbound user approves outbound return request with Quality issue")
    public void validateInboundUserApprovesQualityReturnOutboundRequest() {
        try {

            ReportManager.log("Step 1: Create Return Outbound Request with Quality issue using SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createReturnOutboundRequest(returnOutboundRequestParams, qualityReturnReason);
            String businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request creation");

            ReportManager.log("Step 2: Search and open request from My Requests");
            new MyRequestListPage(driver).openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 3: Validate request status is NEW");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.NEW);

            ReportManager.log("Step 4: Logout SO user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 5: Login with inbound user");
            new LoginPage(driver).userLogin(inboundEmail, inboundPwd).navigateToStagEnvDashboardDirectly().validateLoginSuccessfully();

            ReportManager.log("Step 6: Inbound user approves the request");
            new MyTasksPage(driver).openMyTasksPage().openSpacePage().searchInSearchBar(businessKey).userAssignTaskToHim(businessKey).searchInSearchBar(businessKey).taskDetailsPageForInboundUserSecondReview(businessKey).userApproveAssignedRequest();

            ReportManager.log("Step 7: Wait until status becomes ApprovedByInboundUser in DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            DBRequestStatus.waitForRequestStatus(databaseRepository, connectionString, purchaseOrderManagerSqlQueries.getReturnOutBoundStatusQuery(businessKey), RequestStatus.INBOUND_APPROVED);

            ReportManager.log("Step 8: Validate request status in UI");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.APPROVED);

        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted", e);
        } finally {

            ReportManager.log("Step 9: Logout SO user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165512")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165513")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "Check that the SO user uploads the document / Add Note while creating the return Outbound request")
    @Description("Check that the SO user uploads the document / Add Note while creating the return Outbound request")
    public void validateThatSoCreatedReturnOutboundRequestWithNotesAndDocAttached() {
        try {

            ReportManager.log("Step 1: Create Return Outbound Request with Quality issue using SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createReturnOutboundRequest(returnOutboundRequestParams, qualityReturnReason);
            String businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request creation");

            ReportManager.log("Step 2: Search and open request from My Requests");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 3: Validate request status is NEW And The Note / File Are displayed");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.NEW)
                    .ValidateThatTheFileIsUploadedAndDisplayedSuccessfullyInFilesSection(pdfName)
                    .validateThatNoteSectionHaveDataInsertedBefore(orderNoteTxt);

        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted", e);
        } finally {

            ReportManager.log("Step 4: Logout SO user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165486")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165485")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "Check that the SO user deletes the drafted return Outbound request")
    @Description("Check that the SO user deletes the drafted return Outbound request")
    public void draftEmptyReturnRequestThenDeleteIt() {

        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver).userLogin(soEmail, soPassword).validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Return Outbound from Launchpad");
            new LaunchPadPage(driver).openTheSpecificPageFromLaunchPad(launchPad_ReturnOutbound);

            ReportManager.log("Step 3: Choose GR, create and submit return outbound request");
            new GRListingPage(driver).initiateReturnOutboundRequest(orderId);

            ReportManager.log("Step 4: Draft an empty Outbound request");
            draftedBusinessKey = new ReturnOutboundNewRequestPage(driver).clickDraftButton().catchDraftedRequestBusinessKeyFromToaster().getDraftedBusinessKey();
            Assert.assertNotNull(draftedBusinessKey, "Drafted business key should not be null");

            ReportManager.log("Step 5: Open My Requests from Launchpad");
            new LaunchPadPage(driver).openTheSpecificPageFromLaunchPad(launchPad_MyRequests);

            ReportManager.log("Step 6: Search for drafted request in My Requests");
            new MyRequestListPage(driver).SearchForBusinessKeyOnMyRequests(draftedBusinessKey).openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 7: Validate status is DRAFTED and delete the request");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.DRAFTED).ClickOnDeleteDraftButton();

            ReportManager.log("Step 8: Validate the draft is no longer available in My Requests");
            new MyRequestListPage(driver).SearchForBusinessKeyOnMyRequests(draftedBusinessKey).ValidateDeletedDraftNotInRequests();

        } finally {
            ReportManager.log("Step 9: Logout SO user");
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165484")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "Check that the SO user draft return Outbound request then submit it as a new request")
    @Description("Check that the SO user draft return Outbound request then submit it as a new request")
    public void DraftEmptyReturnOutboundRequestThenSubmitIt() throws InterruptedException {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver).userLogin(soEmail, soPassword).validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Return Outbound from Launchpad");
            new LaunchPadPage(driver).openLaunchpadMenu().openPageFromLaunchpad(launchPad_ReturnOutbound);

            ReportManager.log("Step 3: Draft Return outbound request then return the business key");
            new GRListingPage(driver).initiateReturnOutboundRequest(orderId);
            draftedBusinessKey = new ReturnOutboundNewRequestPage(driver).clickDraftButton().catchDraftedRequestBusinessKeyFromToaster().getDraftedBusinessKey();

            Assert.assertNotNull(draftedBusinessKey, "Drafted business key should not be null");

            new LaunchPadPage(driver).openLaunchpadMenu().openPageFromLaunchpad(launchPad_MyRequests);

            ReportManager.log("Step 4: Search and open request from My Requests");
            new MyRequestListPage(driver).SearchForBusinessKeyOnMyRequests(draftedBusinessKey).openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 5: Validate request status is DRAFT");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.DRAFTED);

            ReportManager.log("Step 6: submit return outbound request from drafted");
            new ReturnOutboundNewRequestPage(driver).fillAndSubmitReturnOutboundRequest(requestedQty, quantityReturnReason, false, orderNoteTxt, filePath);

            ReportManager.log("Step 7 Capture submitted request business key");
            SuccessScreenPage successPage = new SuccessScreenPage(driver);
            successPage.copyRequestBusinessKey();
            businessKey = successPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after Return Outbound request submission");
            successPage.BackToMyReturnRequests();

            ReportManager.log("Step 8: Wait until Return Outbound request status becomes 'Created' in DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            DBRequestStatus.waitForRequestStatus(databaseRepository, connectionString, purchaseOrderManagerSqlQueries.getReturnOutBoundStatusQuery(businessKey), RequestStatus.Created);

            ReportManager.log("Step 9: Search and open request from My Requests");
            new MyRequestListPage(driver).openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 10: Validate request status is NEW");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.NEW);


        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165488")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "Check that the SO user submits the drafted return Outbound request")
    @Description("Check that the SO user submits the drafted return Outbound request")
    public void draftReturnOutBoundRequestWithDataThenSubmitIt() {

        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Return Outbound from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(launchPad_ReturnOutbound);

            ReportManager.log("Step 3: Choose GR, create and submit return outbound request");
            new GRListingPage(driver)
                    .initiateReturnOutboundRequest(orderId);

            ReportManager.log("Step 4: Fill and draft Return Outbound request");
            draftedBusinessKey = new ReturnOutboundNewRequestPage(driver)
                    .fillReturnOutboundRequestData(requestedQty, quantityReturnReason, false, orderNoteTxt, filePath)
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();
            Assert.assertNotNull(draftedBusinessKey, "Drafted business key should not be null");

            ReportManager.log("Step 5: Open My Requests and validate draft");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(launchPad_MyRequests);
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED);

            ReportManager.log("Step 6: Submit the drafted request");
            new OutboundNewRequestPage(driver)
                    .clickOnSubmitButtonInCaseSubmittedDraftedRequest();

            SubmittedDraftedBusinessKey = new SuccessScreenPage(driver)
                    .copyRequestBusinessKey()
                    .BackToMyRequests()
                    .getBusinessKey();
            Assert.assertEquals(SubmittedDraftedBusinessKey, draftedBusinessKey,
                    "Submitted business key should match drafted business key");

            ReportManager.log("Step 7: Wait until status becomes NEW in DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getReturnOutBoundStatusQuery(draftedBusinessKey),
                    RequestStatus.Created
            );

            ReportManager.log("Step 8: Validate request status in UI");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.NEW);

        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted while waiting for status update", e);
        } finally {
            ReportManager.log("Step 9: Logout SO user");
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165487")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "Check that the SO user drafts the drafted return Outbound request")
    @Description("Check that the SO user drafts the drafted return Outbound request")
    public void draftTheDraftedReturnOutboundRequest() {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Return Outbound from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(launchPad_ReturnOutbound);

            ReportManager.log("Step 3: Choose GR, create and submit return outbound request");
            new GRListingPage(driver)
                    .initiateReturnOutboundRequest(orderId);

            ReportManager.log("Step 4: Draft Return Outbound request and capture business key");
            draftedBusinessKey = new ReturnOutboundNewRequestPage(driver)
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();
            Assert.assertNotNull(draftedBusinessKey, "Drafted Business Key should not be null");

            ReportManager.log("Step 5: Open My Requests from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(launchPad_MyRequests);

            ReportManager.log("Step 6: Search for the drafted request and open details page");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 7: Validate request status is DRAFTED and draft again");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED)
                    .clickDraftButton();

            ReportManager.log("Step 8: Validate status remains DRAFTED after drafting again");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED);

        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165514")
    @Test(groups = {"Regression", "ReturnOutbound"}, description = "Check that the SO user exports pdf for any created return Outbound request")
    @Description("Check that the SO user exports pdf for any created return Outbound request")
    public void exportPDFOfReturnRequestFromRequestDetailsPage() {
        try {
            ReportManager.log("Step 1: Create Return Outbound Request with Quality issue using SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createReturnOutboundRequest(returnOutboundRequestParams, qualityReturnReason);
            String businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request creation");

            ReportManager.log("Step 2: Search and open request from My Requests");
            new MyRequestListPage(driver).openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 3: Press export PDF from details screen");
            exportPDFAndValidateFileDownloaded(driver, businessKey, fileName);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }


// endregion

// region Configurations

    @BeforeClass(groups = {"Regression", "ReturnOutbound"})
    public void beforeClass() {
        usersDetailsTestData = new SHAFT.TestData.JSON("UserCredentials.Json");
        launchpadIcons = new SHAFT.TestData.JSON("LaunchpadIconsName.json");
        returnOutboundRequestDetails = new SHAFT.TestData.JSON("ReturnOutboundRequestDetails.json");
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        soEmail = usersDetailsTestData.getTestData("StoreOrder_email");
        soPassword = usersDetailsTestData.getTestData("StoreOrder_password");
        requestedQty = returnOutboundRequestDetails.getTestData("requestedQty");
        quantityReturnReason = returnOutboundRequestDetails.getTestData("qtyIssueLabel");
        qualityReturnReason = returnOutboundRequestDetails.getTestData("qualityIssueLabel");
        orderNoteTxt = returnOutboundRequestDetails.getTestData("orderNote");
        connectionString = usersDetailsTestData.getTestData("OutBound_ReturnOutBoundDBConnectionString");
        haEmail = usersDetailsTestData.getTestData("HospitalApproval_email");
        haPassword = usersDetailsTestData.getTestData("HospitalApproval_password");
        inboundEmail = usersDetailsTestData.getTestData("Inbound_email");
        inboundPwd = usersDetailsTestData.getTestData("Inbound_password");
        launchPad_ReturnOutbound = launchpadIcons.getTestData("Return_Outbound");
        launchPad_MyRequests = launchpadIcons.getTestData("My_Requests");
        fileName = returnOutboundRequestDetails.getTestData("fileName");
        filePath = FileActions.getInstance().getAbsolutePath(SHAFT.Properties.paths.testData(),pdfName );
        returnOutboundRequestParams = new ReturnOutboundRequestParams
                (
                        connectionString,
                        soEmail,
                        soPassword,
                        launchPad_ReturnOutbound,
                        orderId,
                        requestedQty,
                        RequestStatus.Created, true,
                        orderNoteTxt,
                        filePath
                );

    }

    @BeforeMethod(groups = {"Regression", "ReturnOutbound"})
    public void beforeMethod() {
        driver = new SHAFT.GUI.WebDriver();
        new LoginPage(driver).navigateToLoginPage();
    }

    @AfterMethod(groups = {"Regression", "ReturnOutbound"})
    public void afterMethod() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

//endregion
}
