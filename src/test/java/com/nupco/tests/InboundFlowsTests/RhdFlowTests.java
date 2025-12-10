package com.nupco.tests.InboundFlowsTests;

import com.nupco.pages.*;
import com.nupco.sqlqueries.PurchaseOrderManagerSqlQueries;
import com.nupco.utils.*;
import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Description;
import io.qameta.allure.TmsLink;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.Duration;

import static com.nupco.utils.helper.exportProformaAndValidateFileDownloaded;


public class RhdFlowTests {

    // region Variables
    private SHAFT.GUI.WebDriver driver;
    private SHAFT.TestData.JSON usersDetailsTestData, filterOptionsTestData, rhdRequestDetailsTestData, launchpadIcons;
    InboundRequestsParams RhdRequestDetailsParams;
    String PONumber, businessKey, draftedBusinessKey, SubmittedDraftedBusinessKey, asnConnectionString, asnEmail, asnPassword, inboundEmail, inboundPassword, rhdEmail, rhdPassword, nupcoPOs_launchpadIcon, myRequests_launchpadIcon, rhdRegion, rhdCustomerName, rhdSloc, rhdInvoiceNumber, rhdDeliveryTime, rhdPalletsNumber, rhdBatchNumber, rhdQuantity, rhdOrderNote, rhdTruckValue, printProformaFileName, pdfFilePath, formNumberValue, rhdUpdatedInvoiceNumber;
    AsnNewRequestPage asnNewRequestPage;
    DatabaseRepository databaseRepository;
    PurchaseOrderManagerSqlQueries purchaseOrderManagerSqlQueries;
    SQLConnectionManager connectionManager;
    BaseFunctions baseFunctions;


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118136")
    @Test(groups = {"Regression", "Shipping_RHD", "RhdRequestCreation"}, description = "ASN supplier can Create RHD request")
    @Description("Validate that the ASN supplier can Create RHD request")
    public void createRhdRequest() throws InterruptedException {

        try {
            ReportManager.log("Step 1: Connect to the database to get an active RHD PO with remaining quantities");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            PONumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(asnConnectionString, purchaseOrderManagerSqlQueries.getPoNumber())
                    .get(2);

            ReportManager.log("Step 2: Login with ASN Supplier credentials");
            new LoginPage(driver)
                    .userLoggedIntoSiteSuccessfully(asnEmail, asnPassword);

            ReportManager.log("Step 3: Open NUPCO POs icon from the Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Open PO details filtered by RHD customer details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(rhdRegion, rhdCustomerName, rhdSloc, PONumber);

            ReportManager.log("Step 5: Select line items and initiate ASN request creation");
            new PoDetailsPage(driver)
                    .startAsnRequestCreation();

            ReportManager.log("Step 6: Fill and submit the ASN request, then capture the business key");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage.fillAndSubmitASNRequest(
                    rhdInvoiceNumber,
                    rhdDeliveryTime,
                    rhdPalletsNumber,
                    rhdBatchNumber,
                    rhdQuantity,
                    false,
                    rhdOrderNote,
                    rhdTruckValue,
                    DateValues.manufacturingDateMedium,
                    DateValues.expiryDateMedium,
                    DateValues.deliveryDateMedium
            );
            businessKey = asnNewRequestPage.getBusinessKey();

            ReportManager.log("Step 7: Wait until the request status becomes NEW in the database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    asnConnectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 8: Open My Requests and search for the created business key");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 9: Validate that the RHD request status is NEW");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.NEW);

        } finally {
            ReportManager.log("Final Step: Logout user from the system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118153")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118154")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "In Bound User Can Approve The Request Which Created By Asn User", dependsOnMethods = "createRhdRequest")
    @Description("Validate that InBound User Can Accept The Request Which Added By Asn User")
    public void inBoundUserCanAcceptRequestAddedByAsnUser() throws InterruptedException {

        try {
            ReportManager.log("Step 1: Login with Inbound user credentials");
            new LoginPage(driver)
                    .userLogin(inboundEmail, inboundPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Inbound user opens My Tasks and approves the ASN request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .userApproveAssignedRequest();

            ReportManager.log("Step 3: Wait until the request status becomes APPROVED in the database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    asnConnectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(businessKey),
                    RequestStatus.APPROVED
            );

            ReportManager.log("Step 4: Validate that the request status on UI is 'Approved'");
            new MyTasksPage(driver)
                    .validateThatRequestStatusIsApproved();

        } finally {
            ReportManager.log("Final Step: Logout Inbound user from the system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/125248")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/171692")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/125278")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/125397")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "In Bound User Can Reject The Request Which Created By Asn User And Booked Quantity Is Deducted")
    @Description("Validate that InBound User Can Reject The Request Which Added By Asn User And Booked Quantity Is Deducted")
    public void inBoundUserCanRejectRequestAddedByAsnUserAndBookedQuantityIsReleased() throws InterruptedException, ParseException {

        try {
            ReportManager.log("Step 1: Create a new ASN request as a prerequisite");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createRhdRequest(RhdRequestDetailsParams);
            baseFunctions.validateQuantitiesAreBookedAfterSubmission(RhdRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();

            ReportManager.log("Step 2: Logout ASN supplier and login with Inbound user credentials");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(inboundEmail, inboundPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Inbound user rejects the ASN request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .rejectRequestAssign()
                    .validateThatRequestStatusIsRejected();

            ReportManager.log("Step 4: Logout Inbound user and re-login as ASN supplier");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(asnEmail, asnPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 5: Open NUPCO POs page from the Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 6: Wait for integration reflection (up to 3 minutes)");
            helper.WaitForIntegrationReflection(Duration.ofMinutes(3));

            ReportManager.log("Step 7: Filter POs with RHD customer details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(rhdRegion, rhdCustomerName, rhdSloc, baseFunctions.getPONumber());

            ReportManager.log("Step 8: Validate that booked quantity is released after rejection");
            int updatedBookingQty = new PoDetailsPage(driver).readBookedQtyValue();
            new PoDetailsPage(driver)
                    .ValidateNewBookingQtyValueAfterRequestRejection(baseFunctions.getPoInitialBookedQuantity(), updatedBookingQty);

        } finally {
            ReportManager.log("Final Step: Ensure user logs out from the system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118143")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "In Bound User Can Change Delivery date and time")
    @Description("Validate that InBound User Can Change Delivery date and time")
    public void inBoundUserCanChangeDeliveryDateAndTimeAddedByAsnUser() throws InterruptedException, ParseException {

        try {
            ReportManager.log("Step 1: Create a new ASN request as a prerequisite");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createRhdRequest(RhdRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();

            ReportManager.log("Step 2: Logout ASN supplier and login with Inbound user credentials");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(inboundEmail, inboundPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Navigate to My Tasks, open the request, and change delivery date/time");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .changeDeliveryDateAndTime(DateValues.normalDeliveryDate)
                    .validateThatDeliveryTimeIsChangedSuccessfully();

        } finally {
            ReportManager.log("Final Step: Ensure user logs out from the system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118138")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "ASN User Can print proforma After Inbound Confirmation")
    @Description("Validate that InBound User Can print proforma for Approved ASN")
    public void AsnUserCanPrintProformaAfterInboundConfirm() throws InterruptedException, ParseException {

        try {
            ReportManager.log("Step 1: Create a new ASN request as a prerequisite");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createRhdRequest(RhdRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();

            ReportManager.log("Step 2: Logout ASN supplier and login as Inbound user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(inboundEmail, inboundPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Inbound user approves the assigned request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .userApproveAssignedRequest();

            ReportManager.log("Step 4: Logout Inbound user and login as ASN supplier to print Proforma");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(asnEmail, asnPassword)
                    .navigateToStagEnvDashboardDirectly();

            ReportManager.log("Step 5: Open My Requests page and access the ASN request");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequests_launchpadIcon);

            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 6: Print the Proforma and validate that the file is downloaded successfully");
            exportProformaAndValidateFileDownloaded(driver, businessKey, printProformaFileName);

        } finally {
            ReportManager.log("Final Step: Ensure user logs out from the system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/125251")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "RHD User Can Post The Request Which Created By InBound User", dependsOnMethods = {"createRhdRequest", "inBoundUserCanAcceptRequestAddedByAsnUser"})
    @Description("Validate that RHD User Can Post The Request Which Added By InBound User")
    public void rhdUserCanPostRequestAddedByInboundUser() throws InterruptedException {
        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        try {
            ReportManager.log("Step 1: Login with RHD user credentials");
            new LoginPage(driver)
                    .userLogin(rhdEmail, rhdPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: Post the request by RHD user");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForRHDUser(businessKey)
                    .userCanPostGr(formNumberValue, pdfFilePath)
                    .rhdUserSubmitRequest();

            ReportManager.log("Step 3: Wait for request status to change to 'Delivered' in the database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    asnConnectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(businessKey),
                    RequestStatus.DeliveredRequestStatus
            );

            ReportManager.log("Step 4: Validate that the request status is 'Delivered'");
            new MyTasksPage(driver)
                    .validateThatRequestStatusIsDelivered();

        } finally {
            ReportManager.log("Final Step: Logout RHD user from the system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/171703")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/171694")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "RHD User Can Cancel The Request and Booked Quantity is released")
    @Description("Validate that RHD User Can Cancel The Request and Booked Quantity is released")
    public void rhdUserCanCancelRequestAddedByInboundUser() throws InterruptedException, ParseException {
        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        try {
            ReportManager.log("Step 1: Create a new ASN RHD request as prerequisite");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createRhdRequest(RhdRequestDetailsParams);
            baseFunctions.validateQuantitiesAreBookedAfterSubmission(RhdRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();

            ReportManager.log("Step 2: Logout ASN supplier and approve request as Inbound user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
            baseFunctions.approveRhdRequestByInboundUser(inboundEmail, inboundPassword, businessKey);

            ReportManager.log("Step 3: Login with RHD user credentials");
            new LoginPage(driver)
                    .userLogin(rhdEmail, rhdPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 4: Cancel the request by RHD user and validate success message");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForRHDUser(businessKey)
                    .cancelRequest()
                    .validateThatSuccessfulMessageIsDisplayedSuccessfully()
                    .validateThatRequestIsCanceled();

            ReportManager.log("Step 5: Logout RHD user and login again as ASN supplier");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(asnEmail, asnPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 6: Open NUPCO POs page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 7: Wait up to 3 minutes for booking quantity integration reflection");
            helper.WaitForIntegrationReflection(Duration.ofMinutes(3));

            ReportManager.log("Step 8: Filter POs using RHD customer details and open PO details page");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(rhdRegion, rhdCustomerName, rhdSloc, baseFunctions.getPONumber());

            ReportManager.log("Step 9: Validate booking quantity is released after RHD cancellation");
            int updatedBookingQty = new PoDetailsPage(driver).readBookedQtyValue();
            new PoDetailsPage(driver)
                    .ValidateNewBookingQtyValueAfterRequestRejection(baseFunctions.getPoInitialBookedQuantity(), updatedBookingQty);

        } finally {
            ReportManager.log("Final Step: Ensure logout from the system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/125330")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "RHD User Can physically Received The Request Which approved By InBound User")
    @Description("Validate that RHD User Can physically Received The Request Which approved By InBound User")
    public void rhdUserCanPhysicallyReceivedRequestApprovedByInboundUser() throws InterruptedException, ParseException {
        purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
        try {
            ReportManager.log("Step 1: Create a new ASN RHD request as a prerequisite");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createRhdRequest(RhdRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();

            ReportManager.log("Step 2: Logout as ASN supplier and approve the request as Inbound user");
            new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
            baseFunctions.approveRhdRequestByInboundUser(inboundEmail, inboundPassword, businessKey);

            ReportManager.log("Step 3: Login with RHD user credentials");
            new LoginPage(driver)
                    .userLogin(rhdEmail, rhdPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 4: Search for the created task and perform physical receiving");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForRHDUser(businessKey)
                    .userCanPhysicallyReceivedRhdRequest(DateValues.today);

            ReportManager.log("Step 5: Wait until request status becomes 'Physically Received' in the database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    asnConnectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(businessKey),
                    RequestStatus.physicallyReceivedStatus
            );
            ReportManager.log("Step 6: Refresh the page to reflect the updated status");
            helper.RefreshPage(driver);

            ReportManager.log("Step 7: Validate that the request status is updated to 'Physically Received'");
            new MyTasksPage(driver)
                    .validateTheRequestStatus(RequestStatus.physicallyReceivedStatus);

        } finally {
            ReportManager.log("Final Step: Ensure user logout to clean up the session");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118132")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118137")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "ASN supplier can Draft RHD request then submit it")
    @Description("Validate that the ASN supplier can Draft RHD request then submit it")
    public void submitDraftedRhdRequest() throws InterruptedException {
        try {
            ReportManager.log("Step 1: Connect to DB and fetch active RHD PO with remaining quantities");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            PONumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(asnConnectionString, purchaseOrderManagerSqlQueries.getPoNumber())
                    .get(2);

            ReportManager.log("Step 2: Login with ASN Supplier user");
            new LoginPage(driver)
                    .userLogin(asnEmail, asnPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Open 'NUPCO POs' from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter POs by RHD Customer details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(rhdRegion, rhdCustomerName, rhdSloc, PONumber);


            ReportManager.log("Step 5: Select line items and initiate ASN request creation");
            new PoDetailsPage(driver)
                    .startAsnRequestCreation();

            ReportManager.log("Step 6: Fill ASN Request form and save as Draft");
            draftedBusinessKey = new AsnNewRequestPage(driver)
                    .createNewAsnRequest(
                            rhdInvoiceNumber,
                            rhdDeliveryTime,
                            rhdPalletsNumber,
                            rhdBatchNumber,
                            rhdQuantity,
                            DateValues.manufacturingDateMedium,
                            DateValues.expiryDateMedium,
                            DateValues.deliveryDateMedium)
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();

            ReportManager.log("Step 7: Open 'My Requests' from Launchpad");
            new LaunchPadPage(driver)
                    .openLaunchpadMenu()
                    .openPageFromLaunchpad(myRequests_launchpadIcon);

            ReportManager.log("Step 8: Search and open drafted request in My Requests list");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 9: Validate that request status is 'DRAFTED'");
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
                    asnConnectionString,
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

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118133")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118135")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "ASN supplier can Draft empty RHD request then delete it")
    @Description("Validate that the ASN supplier can Draft empty RHD request then delete it")
    public void draftEmptyRhdRequest() {
        try {
            ReportManager.log("Step 1: Connect to DB and fetch an active RHD PO with remaining quantities");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            PONumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(asnConnectionString, purchaseOrderManagerSqlQueries.getPoNumber())
                    .getFirst();

            ReportManager.log("Step 2: Login with ASN Supplier user");
            new LoginPage(driver)
                    .userLogin(asnEmail, asnPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Open 'NUPCO POs' from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Open PO details filtered by RHD customer details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(rhdRegion, rhdCustomerName, rhdSloc, PONumber);

            ReportManager.log("Step 5: Select line items and initiate ASN request creation");
            new PoDetailsPage(driver)
                    .startAsnRequestCreation();

            ReportManager.log("Step 6: Draft empty ASN request without filling data and capture Business Key");
            draftedBusinessKey = new AsnNewRequestPage(driver)
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();

            ReportManager.log("Step 7: Open 'My Requests' page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequests_launchpadIcon);

            ReportManager.log("Step 8: Search for drafted request in 'My Requests' and open it");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 9: Validate drafted request status is 'DRAFTED' and delete it");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED)
                    .ClickOnDeleteDraftButton();

            ReportManager.log("Step 10: Confirm draft deletion and validate it no longer appears in the list");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .ValidateDeletedDraftNotInRequests();

        } finally {
            ReportManager.log("Final Step: Logout from system to ensure session cleanup");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118134")
    @Test(groups = {"Regression", "Shipping_Asn"}, description = "Check that the ASN supplier can draft drafted RHD request")
    @Description("Check that the ASN supplier can draft drafted RHD request")
    public void draftDraftedRhdRequest() {

        try {
            ReportManager.log("Step 1: Connect to DB and fetch active RHD PO with remaining quantities");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            PONumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(asnConnectionString, purchaseOrderManagerSqlQueries.getPoNumber())
                    .getFirst();

            ReportManager.log("Step 2: Login with ASN Supplier user");
            new LoginPage(driver)
                    .userLogin(asnEmail, asnPassword)
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Open 'NUPCO POs' from Launchpad");
            new LaunchPadPage(driver)
                    .openLaunchpadMenu()
                    .openPageFromLaunchpad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter POs by RHD Customer details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(rhdRegion, rhdCustomerName, rhdSloc, PONumber);


            ReportManager.log("Step 5: Select line items and initiate ASN request");
            new PoDetailsPage(driver)
                    .selectFirstPoLineItems()
                    .clickOnRequestAsnButton();

            ReportManager.log("Step 6: Fill ASN Request form and save as Draft");
            draftedBusinessKey = new AsnNewRequestPage(driver)
                    .createNewAsnRequest(
                            rhdInvoiceNumber,
                            rhdDeliveryTime,
                            rhdPalletsNumber,
                            rhdBatchNumber,
                            rhdQuantity,
                            DateValues.manufacturingDateMedium,
                            DateValues.expiryDateMedium,
                            DateValues.deliveryDateMedium)
                    .clickDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster()
                    .getDraftedBusinessKey();

            ReportManager.log("Step 7: Open 'My Requests' from Launchpad");
            new LaunchPadPage(driver)
                    .openLaunchpadMenu()
                    .openPageFromLaunchpad(myRequests_launchpadIcon);

            ReportManager.log("Step 8: Search and open drafted request in My Requests list");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(draftedBusinessKey)
                    .openRequestDetails(draftedBusinessKey);

            ReportManager.log("Step 9: Validate that request status is 'DRAFTED'");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED);

            ReportManager.log("Step 10: Redraft the drafted ASN request");
            new AsnNewRequestPage(driver)
                    .clickDraftButton();

            ReportManager.log("Step 11: Validate the request status remains DRAFTED after re-drafting");
            new RequestDetailsPage(driver)
                    .validateRequestStatus(RequestStatus.DRAFTED);

        } finally {
            ReportManager.log("Final Step: Logout from the site to close the session cleanly");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118146")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "Check that Number of Pallets are reduced after requests creation")
    @Description("Check that Pallets are booked successfully")
    public void validatePalletsAreBookedSuccessfully() throws InterruptedException {
        try {
            ReportManager.log("Step 1: Connect to DB and fetch an active RHD PO having remaining quantities");
            purchaseOrderManagerSqlQueries = new PurchaseOrderManagerSqlQueries();
            PONumber = purchaseOrderManagerSqlQueries
                    .getPurchaseOrderList(asnConnectionString, purchaseOrderManagerSqlQueries.getPoNumber())
                    .getFirst();

            ReportManager.log("Step 2: Login with ASN Supplier user");
            new LoginPage(driver)
                    .userLoggedIntoSiteSuccessfully(asnEmail, asnPassword);

            ReportManager.log("Step 3: Open 'NUPCO POs' page from Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 4: Filter PO list with RHD Customer details and open the PO details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(rhdRegion, rhdCustomerName, rhdSloc, PONumber);

            ReportManager.log("Step 5: Select PO line items and initiate ASN request creation");
            new PoDetailsPage(driver)
                    .startAsnRequestCreation();

            ReportManager.log("Step 6: Fill and submit ASN request");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage.fillAndSubmitASNRequest(
                    rhdInvoiceNumber,
                    rhdDeliveryTime,
                    rhdPalletsNumber,
                    rhdBatchNumber,
                    rhdQuantity,
                    false,
                    rhdOrderNote,
                    rhdTruckValue,
                    DateValues.manufacturingDateMedium,
                    DateValues.expiryDateMedium,
                    DateValues.deliveryDateMedium
            );

            String businessKey = asnNewRequestPage.getBusinessKey();
            ReportManager.log("Captured business key for created ASN request: " + businessKey);

            ReportManager.log("Step 7: Wait until request status becomes 'NEW' in database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    asnConnectionString,
                    purchaseOrderManagerSqlQueries.getStatusQuery(businessKey),
                    RequestStatus.NEW
            );
            ReportManager.log("Step 8: Reopen 'NUPCO POs' page to validate booking reflection");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 9: Filter PO list again to open same PO details");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(rhdRegion, rhdCustomerName, rhdSloc, PONumber);

            ReportManager.log("Step 10: Select line items again and initiate another ASN request for validation");
            new PoDetailsPage(driver)
                    .startAsnRequestCreation();

            ReportManager.log("Step 11: Open ASN Request page and fill Delivery Date and Time");
            asnNewRequestPage.openAsnRequestPageAndFillDeliveryDateAndTime(
                    rhdInvoiceNumber,
                    rhdDeliveryTime,
                    DateValues.deliveryDateMedium
            );

            ReportManager.log("Step 12: Validate that pallets were booked correctly after submission");
            int numOfPallets = Integer.parseInt(asnNewRequestPage.getNumOfPallets());
            int availablePallets = Integer.parseInt(asnNewRequestPage.readAvailableNumOfPallets());
            int requestedQty = Integer.parseInt(rhdQuantity);
            asnNewRequestPage.ValidateNewNumOfPalletsValue(numOfPallets, availablePallets, requestedQty);

            ReportManager.log(" Validation completed successfully — pallets are booked and reflected correctly.");

        } finally {
            ReportManager.log("Final Step: Ensure user logout from system for cleanup");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118141")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "Check that ASN supplier can change Invoice Number in RHD after request is approved")
    @Description("Check that ASN Supplier can change Invoice Number")
    public void validateSupplierCanChangeInvoiceNumber() throws InterruptedException, ParseException {
        try {
            ReportManager.log("Step 1: Create new RHD ASN request");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createRhdRequest(RhdRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();
            ReportManager.log("Captured business key for the created ASN request: " + businessKey);

            ReportManager.log("Step 2: Logout from supplier and login as Inbound user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(inboundEmail, inboundPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 3: Inbound user assigns and approves the ASN request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .taskDetailsPageForInboundUser(businessKey)
                    .userApproveAssignedRequest();

            ReportManager.log("Step 4: Logout inbound user and login as ASN Supplier again");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(asnEmail, asnPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 5: Open 'My Requests' and locate the approved ASN request");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(myRequests_launchpadIcon);
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 6: Update the invoice number and validate it is saved successfully");
            new RequestDetailsPage(driver)
                    .inputInvNumber(rhdUpdatedInvoiceNumber)
                    .updateIRNumber()
                    .validateInvNumberMessageDisplayed()
                    .validateInvNumberisUpdated(rhdUpdatedInvoiceNumber);

            ReportManager.log(" Validation successful — Supplier was able to update invoice number post-approval.");

        } finally {
            ReportManager.log("Final Step: Ensure system cleanup by logging out the active user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/180070")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/118171")
    @Test(groups = {"Regression", "Shipping_RHD"}, description = "Check that note and truck number are displayed in my requests and in inbound user review task as entered by supplier")
    @Description("Validate that note and truck number are reflected successfully")
    public void validateNoteAndTruckNumberReflectedSuccessfully() throws InterruptedException, ParseException {
        try {

            ReportManager.log("Step 1 → Create a new ASN request as a prerequisite");
            baseFunctions = new BaseFunctions(driver);
            baseFunctions.createRhdRequest(RhdRequestDetailsParams);
            String businessKey = baseFunctions.getBusinessKey();

            ReportManager.log("Step 2 → Opening ASN request from My Requests to verify details...");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 3 → Validating Truck Number and Notes visibility for ASN user in my requests...");
            new AsnNewRequestPage(driver)
                    .ValidateThatTheValueOfTrucksIsDisplayedForAsn(rhdTruckValue)
                    .ValidateThatTheDataOfNoteIsDisplayed(rhdOrderNote);

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
                    .ValidateThatTheValueOfTrucksIsDisplayedForInbound(rhdTruckValue)
                    .ValidateThatTheDataOfNoteIsDisplayed(rhdOrderNote);
            ReportManager.logDiscrete(" Test Passed: Truck Number and Note fields are displayed correctly for both ASN and Inbound users in RHD order.");

    } catch (Exception e) {
        ReportManager.logDiscrete(" Test Failed: " + e.getMessage());
        throw e;
    } finally {
        ReportManager.log(" Cleanup → Logging out any logged-in user...");
        new LoginPage(driver).userCanLogoutFromSiteSuccessfully();
    }
    }

    // endregion

    // region Configurations

    @BeforeClass(groups = {"Regression", "Shipping_RHD", "RhdRequestCreation"})
    public void beforeClass() {
        usersDetailsTestData = new SHAFT.TestData.JSON("UserCredentials.Json");
        filterOptionsTestData = new SHAFT.TestData.JSON("StorageLocationsFilters.json");
        rhdRequestDetailsTestData = new SHAFT.TestData.JSON("AsnRequestDetails.json");
        launchpadIcons = new SHAFT.TestData.JSON("LaunchpadIconsName.json");
        asnConnectionString = usersDetailsTestData.getTestData("asnSupplier_PurchasingDBConnectionString");
        asnEmail = usersDetailsTestData.getTestData("asnSupplier_email");
        asnPassword = usersDetailsTestData.getTestData("asnSupplier_password");
        inboundEmail = usersDetailsTestData.getTestData("Inbound_email");
        inboundPassword = usersDetailsTestData.getTestData("Inbound_password");
        rhdEmail = usersDetailsTestData.getTestData("RHD_email");
        rhdPassword = usersDetailsTestData.getTestData("RHD_password");
        nupcoPOs_launchpadIcon = launchpadIcons.getTestData("NUPCO_POs");
        myRequests_launchpadIcon = launchpadIcons.getTestData("My_Requests");
        rhdRegion = filterOptionsTestData.getTestData("RHD_Region");
        rhdCustomerName = filterOptionsTestData.getTestData("RHD_CustomerName");
        rhdSloc = filterOptionsTestData.getTestData("RHD_SLOC");
        rhdInvoiceNumber = rhdRequestDetailsTestData.getTestData("invoiceNum");
        rhdUpdatedInvoiceNumber = rhdRequestDetailsTestData.getTestData("updateInvoiceNum");
        rhdDeliveryTime = rhdRequestDetailsTestData.getTestData("deliveryTime");
        rhdPalletsNumber = rhdRequestDetailsTestData.getTestData("palletsNum");
        rhdBatchNumber = rhdRequestDetailsTestData.getTestData("batchNum");
        rhdQuantity = rhdRequestDetailsTestData.getTestData("qty");
        rhdOrderNote = rhdRequestDetailsTestData.getTestData("noteText");
        rhdTruckValue = rhdRequestDetailsTestData.getTestData("truckValue");
        printProformaFileName = rhdRequestDetailsTestData.getTestData("printProformaFileName");
        formNumberValue = rhdRequestDetailsTestData.getTestData("form1NumberValue");
        pdfFilePath = "dummy.pdf";

        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        RhdRequestDetailsParams = new InboundRequestsParams
                (
                        asnConnectionString,
                        asnEmail,
                        asnPassword,
                        nupcoPOs_launchpadIcon,
                        rhdRegion,
                        rhdCustomerName,
                        rhdSloc,
                        rhdInvoiceNumber,
                        rhdDeliveryTime,
                        rhdPalletsNumber,
                        rhdBatchNumber,
                        rhdQuantity,
                        RequestStatus.NEW, true, rhdOrderNote,
                        rhdTruckValue,
                        DateValues.manufacturingDateMedium,
                        DateValues.expiryDateMedium,
                        DateValues.deliveryDateMedium
                );

    }

    @BeforeMethod(groups = {"Regression", "Shipping_RHD", "RhdRequestCreation"})
    public void beforeMethod() {
        driver = new SHAFT.GUI.WebDriver();
        new LoginPage(driver)
                .navigateToLoginPage();
    }

    @AfterMethod(groups = {"Regression", "Shipping_RHD", "RhdRequestCreation"})
    public void afterMethod() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    //endregion
}
