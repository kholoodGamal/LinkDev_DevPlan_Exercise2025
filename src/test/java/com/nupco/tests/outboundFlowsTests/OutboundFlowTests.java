package com.nupco.tests.outboundFlowsTests;

import com.nupco.pages.*;
import com.nupco.sqlqueries.PurchaseOrderManagerSqlQueries;
import com.nupco.utils.*;
import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import com.shaft.validation.Validations;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.LocalDate;

import static com.nupco.utils.helper.*;

public class OutboundFlowTests {

    // region Variables
    private SHAFT.GUI.WebDriver driver;
    private SHAFT.TestData.JSON usersDetailsTestData, launchpadIcons, outboundRequestDetails;
    String businessKey, SubmittedDraftedBusinessKey, draftedBusinessKey, soEmail, soPassword, requestedQty, editableRequestedQty, orderNote,
            itemNote, updatedItemNote,connectionString, haEmail, haPassword, inventoryIcon, myRequestsPage, fileName, emergencyRequestType, itemGenericCode;
    DatabaseRepository databaseRepository;
    SQLConnectionManager connectionManager;
    PurchaseOrderManagerSqlQueries purchaseOrderManagerSqlQueries;
    OutboundRequestParams outBoundRequestParameter;
    BaseFunctions baseFunctions;
    InventoryListingPage inventoryListingPage;
    int noOfItemsToAdd = 2;
    double itemOriginalBookedValue, itemOriginalAvailableValue, itemOriginalStockValue;
    SuccessScreenPage successPage;
    MyTasksPage myTasks;


    // endregion

    // region Test Cases

    @Test(groups = {"Regression", "Outbound"}, description = "SO user submits the Outbound request without notes")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165531")
    @Description("Validate that the SO user can submit an Outbound request without adding notes")
    public void submitOutboundRequest_WithoutNotes() throws InterruptedException {

        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);


            ReportManager.log("Step 3: Select 'Medical Supplies' classification and choose an item");
            new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: Fill and submit outbound request without notes");
            new OutboundNewRequestPage(driver)
                    .fillAndSubmitOutboundRequest(requestedQty, false, orderNote, itemNote);

            ReportManager.log("Step 5: Capture submitted request business key");
            SuccessScreenPage successPage = new SuccessScreenPage(driver);
            successPage.copyRequestBusinessKey();
            businessKey = successPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request submission");
            successPage.BackToMyRequests();

            ReportManager.log("Step 6: Wait until request status becomes NEW in DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getOutBoundStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 7: Search and open request from My Requests");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 8: Validate request status is NEW");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.NEW);

        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165584")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165587")
    @Test(groups = {"Regression", "Outbound"}, description = "SO user adds a note while creating an Outbound request")
    @Description("Validate that the SO user adds a note while creating an Outbound request")
    public void createOutboundRequestHavingNote() {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Navigate to Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Select 'Medical Supplies' classification and choose an item");
            new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: Fill and submit outbound request with notes");
            new OutboundNewRequestPage(driver)
                    .fillAndSubmitOutboundRequest(requestedQty, true, orderNote, itemNote);

            ReportManager.log("Step 5: Capture submitted request business key");
            SuccessScreenPage successPage = new SuccessScreenPage(driver);
            successPage.copyRequestBusinessKey();
            businessKey = successPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request submission");

            ReportManager.log("Step 6: Navigates To My Requests Page");
            successPage.BackToMyRequests();

            ReportManager.log("Step 7: Search for the Outbound request and open details page");
            new MyRequestListPage(driver).openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 8: Validate That Note And Comment Are Existed");
            new RequestDetailsPage(driver)
                    .validateItemCommentHaveDataInsertedBefore(itemNote)
                    .validateThatNoteSectionHaveDataInsertedBefore(orderNote);

            ReportManager.log("Step 9: Wait until request status becomes NEW in DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getOutBoundStatusQuery(businessKey),
                    RequestStatus.NEW
            );

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @Test(groups = {"Regression", "Outbound"}, description = "SO user submits the Outbound request and cancels it")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165549")
    @Description("Validate that SO user can cancel an outbound request after creation")
    public void cancelOutboundRequest() {

        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver).userLogin(soEmail, soPassword).validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Inventory from Launchpad");
            new LaunchPadPage(driver).openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Select classification and item");
            inventoryListingPage = new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: Fill and submit outbound request");
            new OutboundNewRequestPage(driver).fillAndSubmitOutboundRequest(requestedQty, false, orderNote, itemNote);

            SuccessScreenPage successPage = new SuccessScreenPage(driver);
            successPage.copyRequestBusinessKey();
            businessKey = successPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request submission");
            successPage.BackToMyRequests();

            ReportManager.log("Step 5: Wait until status becomes NEW in DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getOutBoundStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 6: Open request from My Requests");
            new MyRequestListPage(driver).openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 7: Validate and cancel request");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.NEW)
                    .cancelOutboundAndReturnOutboundRequest()
                    .validateRequestStatus(RequestStatus.CANCELLED);

        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for DB status change", e);
        } finally {
            ReportManager.log("Step 8: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165739")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165764")
    @Test(groups = {"Regression", "Outbound"}, description = "SO user submits the Outbound request and HA accepts it", dependsOnMethods = "submitOutboundRequest_WithoutNotes")
    @Description("Validate that the HA user can approve the Outbound request")
    public void hospitalApprovalUserCanApproveTheOutboundRequest() {

        try {
            ReportManager.log("Step 1: Login with HA user");
            new LoginPage(driver)
                    .userLogin(haEmail, haPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: HA user approves the request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForHA(businessKey)
                    .userApproveAssignedRequest();

            ReportManager.log("Step 3: Wait until status becomes APPROVED in DB");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getOutBoundStatusQuery(businessKey),
                    RequestStatus.APPROVED
            );

            ReportManager.log("Step 4: Validate request status in UI");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.APPROVED);

        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted while waiting for status update", e);
        } finally {
            ReportManager.log("Step 5: Logout HA user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/169921")
    @Test(groups = {"Regression", "Outbound"}, description = "SO user submits the Outbound request and HA rejects it")
    @Description("Validate that the HA user can reject the Outbound request")
    public void hospitalApprovalUserCanRejectsTheOutboundRequest() {
        try {
            ReportManager.log("Step 1: Create Outbound Request with SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createOutboundRequest(outBoundRequestParameter);
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
            businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request creation");

            ReportManager.log("Step 2: Login with HA user");
            new LoginPage(driver)
                    .userLogin(haEmail, haPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: HA user rejects the request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForHA(businessKey)
                    .HospitalApproverRejectRequest();

            ReportManager.log("Step 4: Wait until status becomes REJECTED in DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getOutBoundStatusQuery(businessKey),
                    RequestStatus.REJECTED
            );

            ReportManager.log("Step 5: Validate request status in UI");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.REJECTED);

        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted while waiting for status update", e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } finally {
            // Step 6: Ensure logout
            ReportManager.log("Step 6: Logout HA user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165520")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165468")
    @Test(groups = {"Regression", "Outbound"}, description = "SO User can Draft OutBound request then submit it")
    @Description("Validate that the SO User can Draft OutBound request then submit it")
    public void draftOutBoundRequestThenSubmitIt() {

        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Select classification and item");
            new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: Fill and draft Outbound request");
            draftedBusinessKey = new OutboundNewRequestPage(driver)
                    .fillOutboundRequestData(requestedQty, false, orderNote, itemNote)
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();
            Assert.assertNotNull(draftedBusinessKey, "Drafted business key should not be null");

            ReportManager.log("Step 5: Open My Requests and validate draft");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequestsPage);
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED);

            ReportManager.log("Step 6: Submit the drafted request");
            new OutboundNewRequestPage(driver).clickOnSubmitButtonInCaseSubmittedDraftedRequest();
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
                    purchaseOrderManagerSqlQueries.getOutBoundStatusQuery(draftedBusinessKey),
                    RequestStatus.NEW
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
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165469")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165471")
    @Test(groups = {"Regression", "Outbound"}, description = "SO User can Draft empty Outbound request then delete it")
    @Description("Validate that the SO can Draft empty Outbound request then delete it")
    public void draftEmptyOutboundRequestThenDeleteIt() {

        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Select classification and item");
            new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: Draft an empty Outbound request");
            draftedBusinessKey = new OutboundNewRequestPage(driver)
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();
            Assert.assertNotNull(draftedBusinessKey, "Drafted business key should not be null");

            ReportManager.log("Step 5: Open My Requests from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequestsPage);

            ReportManager.log("Step 6: Search for drafted request in My Requests");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 7: Validate status is DRAFTED and delete the request");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED)
                    .ClickOnDeleteDraftButton();

            ReportManager.log("Step 8: Validate the draft is no longer available in My Requests");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .ValidateDeletedDraftNotInRequests();

        } finally {
            ReportManager.log("Step 9: Logout SO user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165507")
    @Test(groups = {"Regression", "Outbound"}, description = "SO user drafts the drafted Outbound request")
    @Description("Validate that the SO user can draft an already drafted Outbound request")
    public void draftTheDraftedOutboundRequest() {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Navigate to Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Select 'Medical Supplies' classification and choose an item");
            new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: Draft Outbound request and capture business key");
            draftedBusinessKey = new OutboundNewRequestPage(driver)
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();
            Assert.assertNotNull(draftedBusinessKey, "Drafted Business Key should not be null");

            ReportManager.log("Step 5: Open My Requests from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequestsPage);

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

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165740")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165744")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165746")
    @Test(groups = {"Regression", "Outbound"}, description = "Check that the HA user requests edits to the Outbound request")
    @Description("Validate that the HA user can request edits to an Outbound request, SO user updates it, and HA finally approves")
    public void hospitalApprovalUserCanRequestToEditTheOutboundRequest() throws ParseException, InterruptedException {
        PurchaseOrderManagerSqlQueries poManager = new PurchaseOrderManagerSqlQueries();

        try {
            ReportManager.log("Step 1: Create Outbound Request as SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createOutboundRequest(outBoundRequestParameter);
            businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after creating Outbound request");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 2: Login with HA user");
            new LoginPage(driver)
                    .userLogin(haEmail, haPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: HA user requests to edit the Outbound request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForHA(businessKey)
                    .userRequestToEditOutbound();

            ReportManager.log("Step 4: Validate status becomes EDIT_REQUESTED in DB");
            databaseRepository.waitForStatusChange(
                    connectionString,
                    poManager.getOutBoundStatusQuery(businessKey),
                    RequestStatus.EDIT_REQUESTED
            );
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.EDIT_REQUESTED);

            ReportManager.log("Step 5: Switch to SO user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(soEmail, soPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 6: SO user edits and resubmits the request with updated Qty");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForSO(businessKey);
            new OutboundNewRequestPage(driver)
                    .editAndReSubmitOutboundRequest(editableRequestedQty);

            ReportManager.log("Step 7: Switch back to HA user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(haEmail, haPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 8: HA validates updated Qty and approves the request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForHA(businessKey)
                    .validateTheUpdatedRequestedQtyValue(editableRequestedQty)
                    .userApproveAssignedRequest();

            ReportManager.log("Step 9: Validate status becomes APPROVED in DB");
            databaseRepository.waitForStatusChange(
                    connectionString,
                    poManager.getOutBoundStatusQuery(businessKey),
                    RequestStatus.APPROVED
            );
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.APPROVED);

        } finally {
            ReportManager.log("Final Step: Ensure logout");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165578")
    @Test(groups = {"Regression", "Outbound"}, description = "Check that the SO user adds a new item while creating an Outbound request")
    @Description("Check that the SO user adds a new item while creating an Outbound request")
    public void validateThatSoCanAddItemsWhileCreatingOutboundRequest() throws InterruptedException {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Select 'Medical Supplies' classification and choose an item");
            new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: SO Add Items to the Outbound request");
            userAddItemsToAssignedRequest(driver, noOfItemsToAdd);
            fillRequestedQuantities(driver, noOfItemsToAdd, requestedQty);

            ReportManager.log("Step 5: SO Fill first Added Item and submit the request");
            new OutboundNewRequestPage(driver)
                    .fillAndSubmitOutboundRequest(requestedQty, false, orderNote, itemNote);

            ReportManager.log("Step 6: Capture submitted request business key");
            SuccessScreenPage successPage = new SuccessScreenPage(driver);
            successPage.copyRequestBusinessKey();
            businessKey = successPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request submission");
            successPage.BackToMyRequests();

            ReportManager.log("Step 7: Wait until request status becomes NEW in DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getOutBoundStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 8: Search and open request from My Requests");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 9: Validate request status is NEW");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.NEW);

        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165580")
    @Test(groups = {"Regression", "Outbound"}, description = "Check that the SO user deletes items while creating an Outbound request")
    @Description("Check that the SO user deletes items while creating an Outbound request")
    public void validateThatSoCanDeleteLastItemAfterAddingSomeItemsWhileCreatingOutboundRequest() {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Select 'Medical Supplies' classification and choose an item");
            new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: SO Add Items to the Outbound request Then Delete Las Item is Added");
            userAddItemsToAssignedRequest(driver, noOfItemsToAdd);
            fillRequestedQuantities(driver, noOfItemsToAdd, requestedQty);
            deleteLastItemFromTable(driver);

            ReportManager.log("Step 5: SO Fill first Added Item and submit the request");
            new OutboundNewRequestPage(driver)
                    .fillAndSubmitOutboundRequest(requestedQty, false, orderNote, itemNote);

            ReportManager.log("Step 6: Capture submitted request business key");
            SuccessScreenPage successPage = new SuccessScreenPage(driver);
            successPage.copyRequestBusinessKey();
            businessKey = successPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request submission");
            successPage.BackToMyRequests();

            ReportManager.log("Step 7: Search and open request from My Requests");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 8: Validate That Item Is Deleted Successfully");
            validateLastItemDeleted(driver, noOfItemsToAdd);

        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165747")
    @Test(groups = {"Regression", "Outbound"}, description = "Validate that the HA adds a new item while approving the Outbound request")
    @Description("Validate that the HA adds a new item while approving the Outbound request")
    public void hospitalApprovalUserCanAddItemsWhileApprovingTheRequest() throws ParseException, InterruptedException {
        PurchaseOrderManagerSqlQueries poManager = new PurchaseOrderManagerSqlQueries();

        try {

            ReportManager.log("Step 1: Create Outbound Request as SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createOutboundRequest(outBoundRequestParameter);
            businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after creating Outbound request");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 2: Login with HA user");
            new LoginPage(driver)
                    .userLogin(haEmail, haPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: HA user requests to edit the Outbound request");
            myTasks = new MyTasksPage(driver);
            myTasks.openMyTasksPage();
            myTasks.openSpacePage();
            myTasks.searchInSearchBar(businessKey);
            myTasks.userAssignTaskToHim(businessKey);
            myTasks.searchInSearchBar(businessKey);
            myTasks.openTaskDetailsPageForHA(businessKey);
            userAddItemsToAssignedRequest(driver, noOfItemsToAdd);
            fillRequestedQuantities(driver, noOfItemsToAdd, requestedQty);
            myTasks.haUserApproveAssignedRequest();

            ReportManager.log("Step 4: Validate status becomes APPROVED in DB");
            databaseRepository.waitForStatusChange(
                    connectionString,
                    poManager.getOutBoundStatusQuery(businessKey),
                    RequestStatus.APPROVED
            );
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.APPROVED);

            ReportManager.log("Step 5: Hospital Approval User Log out from System");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        } finally {
            ReportManager.log("Final Step: Ensure logout");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165472")
    @Test(groups = {"Regression", "Outbound"}, description = "Check that the HA can edits items while approving the Outbound request and it's reflected in the next task")
    @Description("Check that the HA can edits items while approving the Outbound request and it's reflected in the next task")
    public void HACanEditsItemsWhileApprovingTheOutboundRequestAndReflectedInTheNextTask() throws ParseException, InterruptedException {
        PurchaseOrderManagerSqlQueries poManager = new PurchaseOrderManagerSqlQueries();

        try {

            ReportManager.log("Step 1: Create Outbound Request as SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createOutboundRequest(outBoundRequestParameter);
            businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after creating Outbound request");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 2: Login with HA user");
            new LoginPage(driver)
                    .userLogin(haEmail, haPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: HA user requests to edit the Outbound request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForHA(businessKey)
                    .haUserEditQtyOfOutboundRequest(editableRequestedQty)
                    .userRequestToEditOutbound();

            ReportManager.log("Step 4: Validate status becomes EDIT_REQUESTED in DB");
            databaseRepository.waitForStatusChange(
                    connectionString,
                    poManager.getOutBoundStatusQuery(businessKey),
                    RequestStatus.EDIT_REQUESTED
            );
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.EDIT_REQUESTED);


            ReportManager.log("Step 6: Switch to SO user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 7: SO Validates That updated Qty By HA Is Reflected Successfully");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForSO(businessKey)
                    .validateTheUpdatedRequestedQtyValue(editableRequestedQty);
        } finally {
            ReportManager.log("Final Step: Ensure logout");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165473")
    @Test(groups = {"Regression", "Outbound"}, description = "Check that the HA changes request urgency while approving the Outbound request and it's reflected in the next task")
    @Description("Check that the HA changes request urgency while approving the Outbound request and it's reflected in the next task")
    public void validateThatHaCanChangeRequestTypeAndReflectedInNextTask() throws ParseException, InterruptedException {
        PurchaseOrderManagerSqlQueries poManager = new PurchaseOrderManagerSqlQueries();
        try {
            ReportManager.log("Step 1: Create Outbound Request as SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createOutboundRequest(outBoundRequestParameter);
            businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after creating Outbound request");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 2: Login with HA user");
            new LoginPage(driver)
                    .userLogin(haEmail, haPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: HA user change request Emergency to be Emergency And make edit request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForHA(businessKey)
                    .changeRequestUrgency(emergencyRequestType)
                    .userRequestToEditOutbound();


            ReportManager.log("Step 4: Validate status becomes EDIT_REQUESTED in DB");
            databaseRepository.waitForStatusChange(
                    connectionString,
                    poManager.getOutBoundStatusQuery(businessKey),
                    RequestStatus.EDIT_REQUESTED
            );
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.EDIT_REQUESTED);

            ReportManager.log("Step 5: Switch to SO user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 6: SO Validates That updated emergency Request Type By HA Is Reflected Successfully");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForSO(businessKey)
                    .validateTheRequestEmergencyValue(emergencyRequestType);

        } finally {
            ReportManager.log("Final Step: Ensure logout");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165474")
    @Test(groups = {"Regression", "Outbound"}, description = "Check that the HA edits the delivery date while approving the Outbound request and it's reflected in the next task")
    @Description("Check that the HA edits the delivery date while approving the Outbound request and it's reflected in the next task")
    public void validateThatHaCanChangeDeliveryDateAndReflectedInNextTask() throws ParseException, InterruptedException {
        PurchaseOrderManagerSqlQueries poManager = new PurchaseOrderManagerSqlQueries();
        try {
            ReportManager.log("Step 1: Create Outbound Request as SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createOutboundRequest(outBoundRequestParameter);
            businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after creating Outbound request");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 2: Login with HA user");
            new LoginPage(driver)
                    .userLogin(haEmail, haPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: HA user edits the delivery date And make edit request");
            myTasks = new MyTasksPage(driver);
            myTasks.openMyTasksPage();
            myTasks.openSpacePage();
            myTasks.searchInSearchBar(businessKey);
            myTasks.userAssignTaskToHim(businessKey);
            myTasks.searchInSearchBar(businessKey);
            myTasks.openTaskDetailsPageForHA(businessKey);
            LocalDate actualSelectedDate = myTasks.changeDeliveryDate(DateValues.updatedDeliveryDateForOutbound);
            myTasks.userRequestToEditOutbound();


            ReportManager.log("Step 4: Validate status becomes EDIT_REQUESTED in DB");
            databaseRepository.waitForStatusChange(
                    connectionString,
                    poManager.getOutBoundStatusQuery(businessKey),
                    RequestStatus.EDIT_REQUESTED
            );
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.EDIT_REQUESTED);

            ReportManager.log("Step 5: Switch to SO user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 6: SO Validates That updated emergency Request Type By HA Is Reflected Successfully");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForSO(businessKey)
                    .validateTheUpdatedDeliveryDateSetByHA(actualSelectedDate);

        } finally {
            ReportManager.log("Final Step: Ensure logout");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165476")
    @Test(groups = {"Regression", "Outbound"}, description = "Check that the HA adds item level note while approving the Outbound request and it's reflected in the next task")
    @Description("Check that the HA adds item level note while approving the Outbound request and it's reflected in the next task")
    public void haUserAddItemLevelNoteWhileApprovingTheRequest() throws ParseException, InterruptedException {
        PurchaseOrderManagerSqlQueries poManager = new PurchaseOrderManagerSqlQueries();

        try {

            ReportManager.log("Step 1: Create Outbound Request as SO user");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createOutboundRequest(outBoundRequestParameter);
            businessKey = baseFunctions.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after creating Outbound request");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 2: Login with HA user");
            new LoginPage(driver)
                    .userLogin(haEmail, haPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: HA user add item level note to the Outbound request And Approve It");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForHA(businessKey)
                    .haUserAddItemLevelNote(itemNote)
                    .haUserApproveAssignedRequest();

            ReportManager.log("Step 4: Validate status becomes Approved in DB");
            databaseRepository.waitForStatusChange(
                    connectionString,
                    poManager.getOutBoundStatusQuery(businessKey),
                    RequestStatus.APPROVED
            );
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.APPROVED);


            ReportManager.log("Step 5: Switch to SO user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 6: Open My Requests from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequestsPage);

            ReportManager.log("Step 7: Search and open request from My Requests");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 8: Validate That Note And Comment Are Existed");
            new RequestDetailsPage(driver)
                    .validateItemCommentHaveDataInsertedBefore(itemNote);

        } finally {
            ReportManager.log("Final Step: Ensure logout");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165478")
    @Test(groups = {"Regression", "Outbound"}, description = "Check that the HA edits item-level notes while approving the Outbound request and it's reflected in the next task", dependsOnMethods = "createOutboundRequestHavingNote")
    @Description("Check that the HA edits item-level notes while approving the Outbound request and it's reflected in the next task")
    public void haUserEditItemLevelNoteAndReflectedInTheNextTask() throws InterruptedException {
        PurchaseOrderManagerSqlQueries poManager = new PurchaseOrderManagerSqlQueries();

        try {
            ReportManager.log("Step 1: Login with HA user");
            new LoginPage(driver)
                    .userLogin(haEmail, haPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: HA user Edit item level note to the Outbound request And Press On Edit Request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForHA(businessKey)
                    .haUserEditItemLevelNote(updatedItemNote)
                    .userRequestToEditOutbound();

            ReportManager.log("Step 3: Validate status becomes EDIT_REQUESTED in DB");
            databaseRepository.waitForStatusChange(
                    connectionString,
                    poManager.getOutBoundStatusQuery(businessKey),
                    RequestStatus.EDIT_REQUESTED
            );
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.EDIT_REQUESTED);

            ReportManager.log("Step 4: Switch to SO user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 5: Open My Requests from Launchpad");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForSO(businessKey);

            ReportManager.log("Step 6: Validate That Note And Comment Are Existed");
            new RequestDetailsPage(driver)
                    .validateItemCommentHaveDataInsertedBefore(updatedItemNote);

        } finally {
            ReportManager.log("Final Step: Ensure logout");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165553")
    @Test(groups = {"Regression", "Outbound"}, description = "SO user exports PDF from request creation success popup")
    @Description("Validate that the SO user exports PDF from request creation success popup")
    public void exportOutboundRequestPDFFromPopup() {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Select 'Medical Supplies' classification and choose an item");
            new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: Fill and submit outbound request without notes");
            new OutboundNewRequestPage(driver)
                    .fillAndSubmitOutboundRequest(requestedQty, false, orderNote, itemNote);

            ReportManager.log("Step 5: Capture submitted request business key");
            SuccessScreenPage successPage = new SuccessScreenPage(driver);
            successPage.copyRequestBusinessKey();
            businessKey = successPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request submission");

            ReportManager.log("Step 6: Export PDF from success popup");
            exportPDFAndValidateFileDownloaded(driver, businessKey, fileName);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165555")
    @Test(groups = {"Regression", "Outbound"}, description = "SO user exports PDF from request request details page")
    @Description("Validate that the SO user exports PDF from request details page")
    public void exportPDFRequestDetails() {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Select 'Medical Supplies' classification and choose an item");
            new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: Fill and submit outbound request without notes");
            new OutboundNewRequestPage(driver)
                    .fillAndSubmitOutboundRequest(requestedQty, false, orderNote, itemNote);

            ReportManager.log("Step 5: Capture submitted request business key");
            successPage = new SuccessScreenPage(driver);
            successPage.copyRequestBusinessKey();
            businessKey = successPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request submission");
            successPage.BackToMyRequests();

            ReportManager.log("Step 6: Wait until request status becomes NEW in DB");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    new PurchaseOrderManagerSqlQueries().getOutBoundStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 7: Search and open request from My Requests");

            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 8: Validate request status is NEW");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.NEW);

            ReportManager.log("Step 9: Press export PDF from details screen");
            exportPDFAndValidateFileDownloaded(driver, businessKey, fileName);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165556")
    @Test(groups = {"Regression", "Outbound"}, description = "SO user exports PDF for Cancelled request from details page", dependsOnMethods = "cancelOutboundRequest")
    @Description("Validate that the SO user exports PDF for Cancelled request from details page")
    public void exportCancelledPDFRequestDetails() {
        try {
            ReportManager.log("Step 1: Login with SO user then navigate to My Requests");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully()
                    .navigateToRequestsPage();


            ReportManager.log("Step 2: Search and open request from My Requests");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 3: Validate request status is Cancelled");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.CANCELLED);

            ReportManager.log("Step 4: Press export PDF from details screen");
            exportPDFAndValidateFileDownloaded(driver, businessKey, fileName);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165568")
    @Test(groups = {"Regression", "Outbound"}, description = "SO user exports PDF for Rejected request from details page", dependsOnMethods = "hospitalApprovalUserCanRejectsTheOutboundRequest")
    @Description("Validate that the SO user exports PDF for Rejected request from details page")
    public void exportRejectedPDFRequestDetails() {
        try {
            ReportManager.log("Step 1: Login with SO user then navigate to My Requests");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Navigate to My Requests");
            new LoginPage(driver).navigateToRequestsPage();


            ReportManager.log("Step 3: Search and open request from My Requests");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 4: Validate request status is Rejected");
            new RequestDetailsPage(driver).validateRequestStatus(RequestStatus.REJECTED);

            ReportManager.log("Step 5: Press export PDF from details screen");
            exportPDFAndValidateFileDownloaded(driver, businessKey, fileName);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165552")
    @Test(groups = {"Regression", "Outbound"}, description = "SO user checks quantities are updated after cancelling outbound request ", dependsOnMethods = "cancelOutboundRequest")
    @Description("Validate that item quantities are updated in inventory when request gets cancelled")
    public void ValidateItemQuantitiesAreUpdatedAfterRequestCancellation() {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver).userLogin(soEmail, soPassword).validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Inventory from Launchpad");
            //openTheSpecificPageFromLaunchPad
            new LaunchPadPage(driver).openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Verify item quantities are reset to their initial values before request creation ");
            ReportManager.log("Step 3.1: Item quantities before request creation ");
             itemOriginalBookedValue = inventoryListingPage.getBookedQuantity();
             itemOriginalAvailableValue = inventoryListingPage.getAvailableQuantity();
             itemGenericCode = inventoryListingPage.getGenericCode();
            ReportManager.log("available = "+itemOriginalAvailableValue+" , Booked = "+itemOriginalBookedValue);
            ReportManager.log("Step 3.2: Current item quantities ");
            inventoryListingPage = new InventoryListingPage(driver)
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .SearchItemAndGetDetails(itemGenericCode);
            ReportManager.log("Step 3.3: Assert Item quantities match ");
            SHAFT.Validations.assertThat().number(itemOriginalBookedValue).isEqualTo(inventoryListingPage.getBookedQuantity())
                    .withCustomReportMessage("Comparing booked quantity before and after outbound request creation and cancellation");
            SHAFT.Validations.assertThat().number(itemOriginalAvailableValue).isEqualTo(inventoryListingPage.getAvailableQuantity())
                    .withCustomReportMessage("Comparing available quantity before and after outbound request creation and cancellation");



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/165481")
    @Test(groups = {"Regression", "Outbound"}, description = "Check that Qty is booked and available Qty is updated with booked Qty & stock Qty is not updated in the warehouse after creating Outbound")
    @Description("Check that Qty is booked and available Qty is updated with booked Qty & stock Qty is not updated in the warehouse after creating Outbound")
    public void validateInventoryQuantitiesAfterOutboundCreation() {
        try {
            ReportManager.log("Step 1: Login with SO user");
            new LoginPage(driver)
                    .userLogin(soEmail, soPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Open Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 3: Select 'Medical Supplies' classification and choose an item");
            inventoryListingPage = new InventoryListingPage(driver);
            inventoryListingPage
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .selectInventoryItemUnderClassification();

            ReportManager.log("Step 4: Read the inventory Quantities");
            itemOriginalAvailableValue = inventoryListingPage.getAvailableQuantity();
            itemOriginalStockValue = inventoryListingPage.getStockQuantity();
            itemOriginalBookedValue = inventoryListingPage.getBookedQuantity();

            ReportManager.log("Step 5: Fill and submit outbound request without notes");
            new OutboundNewRequestPage(driver)
                    .fillAndSubmitOutboundRequest(requestedQty, false, orderNote, itemNote);

            ReportManager.log("Step 6: Capture submitted request business key");
            SuccessScreenPage successPage = new SuccessScreenPage(driver);
            successPage.copyRequestBusinessKey();
            String businessKey = successPage.getBusinessKey();
            Assert.assertNotNull(businessKey, "Business key should not be null after request submission");

            ReportManager.log("Step 7: Wait until request status becomes NEW in DB");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    connectionString,
                    purchaseOrderManagerSqlQueries.getOutBoundStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 8: Open Inventory from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(inventoryIcon);

            ReportManager.log("Step 9: Open the inventory again and validate the Qtys");
            inventoryListingPage
                    .selectInventoryItemClassification(helper.Classification.MEDICAL_SUPPLIES.getLabel())
                    .SearchItemAndGetDetails(itemGenericCode);

            ReportManager.log("Step 10: Validate Qtys after submission");
            Validations.assertThat()
                    .number(inventoryListingPage.getBookedQuantity())
                    .isEqualTo(itemOriginalBookedValue + Integer.parseInt(requestedQty))
                    .withCustomReportMessage("The Booked Qty doesn't change after creating Outbound Request");

            Validations.assertThat()
                    .number(inventoryListingPage.getAvailableQuantity())
                    .isEqualTo(itemOriginalAvailableValue - Integer.parseInt(requestedQty))
                    .withCustomReportMessage("The Available Qty doesn't change after creating Outbound Request");

            Validations.assertThat()
                    .number(inventoryListingPage.getStockQuantity())
                    .isEqualTo(itemOriginalStockValue)
                    .withCustomReportMessage("The Stock Qty changed after creating Outbound Request");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReportManager.log("Final Step: Logout user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
        }


    }

    // endregion

    // region Configurations

    @BeforeClass(groups = {"Regression", "Outbound"})
    public void beforeClass() {
        usersDetailsTestData = new SHAFT.TestData.JSON("UserCredentials.Json");
        launchpadIcons = new SHAFT.TestData.JSON("LaunchpadIconsName.json");
        outboundRequestDetails = new SHAFT.TestData.JSON("OutboundRequestDetails.json");
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        soEmail = usersDetailsTestData.getTestData("StoreOrder_email");
        soPassword = usersDetailsTestData.getTestData("StoreOrder_password");
        requestedQty = outboundRequestDetails.getTestData("requestedQty");
        editableRequestedQty = outboundRequestDetails.getTestData("requestedQty_update");
        orderNote = outboundRequestDetails.getTestData("orderNote");
        itemNote = outboundRequestDetails.getTestData("itemNote");
        updatedItemNote = outboundRequestDetails.getTestData("updatedItemNote");
        connectionString = usersDetailsTestData.getTestData("OutBound_OutBoundDBConnectionString");
        haEmail = usersDetailsTestData.getTestData("HospitalApproval_email");
        haPassword = usersDetailsTestData.getTestData("HospitalApproval_password");
        myRequestsPage = launchpadIcons.getTestData("My_Requests");
        inventoryIcon = launchpadIcons.getTestData("Inventory");
        fileName = outboundRequestDetails.getTestData("fileName");
        emergencyRequestType = outboundRequestDetails.getTestData("emergencyRequestType");

        outBoundRequestParameter = new OutboundRequestParams
                (
                        connectionString,
                        soEmail,
                        soPassword,
                        launchpadIcons.getTestData("Inventory"),
                        requestedQty,
                        RequestStatus.NEW, false,
                        orderNote,
                        itemNote
                );


    }

    @BeforeMethod(groups = {"Regression", "Outbound"})
    public void beforeMethod() {
        driver = new SHAFT.GUI.WebDriver();
        new LoginPage(driver)
                .navigateToLoginPage();
    }

    @AfterMethod(groups = {"Regression", "Outbound"})
    public void afterMethod() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    //endregion
}
