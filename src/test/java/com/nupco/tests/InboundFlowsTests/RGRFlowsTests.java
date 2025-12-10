package com.nupco.tests.InboundFlowsTests;

import com.nupco.pages.*;
import com.nupco.sqlqueries.PurchaseOrderManagerSqlQueries;
import com.nupco.utils.*;
import com.shaft.cli.FileActions;
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

public class RGRFlowsTests {
    private SHAFT.GUI.WebDriver driver;
    private SHAFT.TestData.JSON usersDetailsTestData, filterOptionsTestData, ddAsnRequestDetailsTestData, launchpadIcons;
    InboundRequestsParams ddAsnRequestDetailsParams;
    String businessKey, asnConnectionString, rgrConnectionString, asnEmail, asnPassword, rgrManagerEmail, rgrManagerPassword, nupcoPOs_launchpadIcon, myRequests_launchpadIcon, asnRegion, asnCustomerName, asnSloc, asnInvoiceNumber, asnDeliveryTime, asnPalletsNumber, asnBatchNumber, asnQuantity, asnFeedbackData, asnTruckValue, formNumberValue, rhdRegion, rhdCustomerName, rhdSloc, pdfFilePath,noteText;
    BaseFunctions baseFunction;
    DatabaseRepository databaseRepository;
    PurchaseOrderManagerSqlQueries purchaseOrderManagerSqlQueries;
    SQLConnectionManager connectionManager;
    AsnNewRequestPage asnNewRequestPage;

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130463")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130470")
    @Test(groups = {"Regression", "Shipping_RGR"}, description = "Check that the RGR supplier can submit only one RGR request")
    @Description("Check that the RGR supplier can submit RGR request")
    public void createRgrRequest() throws ParseException, InterruptedException {

        try {
            ReportManager.log("Step 1: Create DD ASN Request (prerequisite)");
            baseFunction = new BaseFunctions(driver);
            baseFunction.createDDAsnRequest(ddAsnRequestDetailsParams);

            ReportManager.log("Step 2: Open RGR New Request page");
            new RequestDetailsPage(driver)
                    .ClickOnRequestRgrButton();

            ReportManager.log("Step 3: Fill the RGR request");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage.createNewRgrRequest(formNumberValue, pdfFilePath);
            asnNewRequestPage.ClickOnRgrRequestSubmitButton();
            asnNewRequestPage.copyRequestBusinessKey();
            asnNewRequestPage.BackToMyRequests();
            businessKey = asnNewRequestPage.getBusinessKey();

            ReportManager.log("Step 4: Wait Till Status To Be (New) in DataBase");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    rgrConnectionString,
                    purchaseOrderManagerSqlQueries.getRGRStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 5: Search for business key on My Requests list");
            new MyRequestListPage(driver).openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 6: Validate request status is New");
            new RequestDetailsPage(driver)
                    .validateRgrRequestStatus(RequestStatus.NEW)
                    .validateRgrRequestButtonIsNotDisplayed();
        } finally {
            ReportManager.log("Final Step: Logout user from system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/146092")
    @Test(groups = {"Regression", "Shipping_RGR"}, description = "Check that the RGR supplier can submit only one RGR request With Notes")
    @Description("Check that the RGR supplier can submit RGR request Having Notes")
    public void createRgrRequestHavingNotes() throws ParseException, InterruptedException {

        try {
            ReportManager.log("Step 1: Create DD ASN Request (prerequisite)");
            baseFunction = new BaseFunctions(driver);
            baseFunction.createDDAsnRequest(ddAsnRequestDetailsParams);

            ReportManager.log("Step 2: Open RGR New Request page");
            new RequestDetailsPage(driver)
                    .ClickOnRequestRgrButton();

            ReportManager.log("Step 3: Fill the RGR request With Notes");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage.fillAndSubmitRgrRequest(
                    formNumberValue,
                    pdfFilePath,
                    true,
                    noteText
                    );
            businessKey = asnNewRequestPage.getBusinessKey();

            ReportManager.log("Step 4: Wait Till Status To Be (New) in DataBase");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    rgrConnectionString,
                    purchaseOrderManagerSqlQueries.getRGRStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 5: Search for business key on My Requests list");
            new MyRequestListPage(driver).openTheRequestFromMyRequests(businessKey);

            ReportManager.log("Step 6: Validate request status is New");
            new RequestDetailsPage(driver)
                    .validateRgrRequestStatus(RequestStatus.NEW)
                    .validateRgrRequestButtonIsNotDisplayed();
        } finally {
            ReportManager.log("Final Step: Logout user from system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130461")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130462")
    @Test(groups = {"Regression", "Shipping_RGR"}, description = "Check that the RGR supplier can draft the RGR request then submit it")
    @Description("Check that the RGR supplier can draft the RGR request then submit it")
    public void draftRgrRequestThenSubmit() throws ParseException, InterruptedException {

        try {
            ReportManager.log("Step 1: Create DD ASN Request (prerequisite)");
            baseFunction = new BaseFunctions(driver);
            baseFunction.createDDAsnRequest(ddAsnRequestDetailsParams);

            ReportManager.log("Step 2: Open RGR New Request page");
            new RequestDetailsPage(driver)
                    .ClickOnRequestRgrButton();

            ReportManager.log("Step 3: Fill the RGR request and save it as Draft");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage
                    .createNewRgrRequest(formNumberValue, pdfFilePath)
                    .ClickOnRgrRequestDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster();
            String rgrDraftedBusinessKey = asnNewRequestPage.getDraftedBusinessKey();

            ReportManager.log("Step 4: Open My Requests icon from Launchpad");
            new LaunchPadPage(driver)
                    .openLaunchpadMenu()
                    .openPageFromLaunchpad(myRequests_launchpadIcon);

            ReportManager.log("Step 5: Search for drafted request business key on My Requests list");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(rgrDraftedBusinessKey)
                    .openRequestDetails(rgrDraftedBusinessKey);

            ReportManager.log("Step 6: Validate that request status is DRAFTED");
            new RequestDetailsPage(driver)
                    .validateRgrRequestStatus(RequestStatus.DRAFTED);

            ReportManager.log("Step 7: Submit the drafted RGR request");
            asnNewRequestPage
                    .ClickOnRgrRequestSubmitButton()
                    .copyRequestBusinessKey()
                    .BackToMyRequests();
            String rgrDraftedSubmittedBusinessKey = asnNewRequestPage.getBusinessKey();

            ReportManager.log("Step 8: Validate submitted business key equals drafted business key");
            new RequestDetailsPage(driver)
                    .validateSubmittedBusinessKeySameDrafted(rgrDraftedSubmittedBusinessKey, rgrDraftedBusinessKey);

            ReportManager.log("Step 9: Wait until status changes to NEW in database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    rgrConnectionString,
                    purchaseOrderManagerSqlQueries.getRGRStatusQuery(rgrDraftedSubmittedBusinessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 10: Search for submitted request business key on My Requests list");
            new MyRequestListPage(driver)
                    .openTheRequestFromMyRequests(rgrDraftedSubmittedBusinessKey);

            ReportManager.log("Step 11: Validate request status is NEW");
            new RequestDetailsPage(driver)
                    .validateRgrRequestStatus(RequestStatus.NEW);

        } finally {
            ReportManager.log("Final Step: Logout user from system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130464")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130465")
    @Test(groups = {"Regression", "Shipping_RGR"}, description = "Check that the RGR supplier can draft the empty RGR request then Delete it")
    @Description("Check that the RGR supplier can draft the empty RGR request then Delete it")
    public void draftEmptyRgrRequestThenDelete() throws ParseException, InterruptedException {

        try {
            ReportManager.log("Step 1: Create DD ASN Request (prerequisite)");
            baseFunction = new BaseFunctions(driver);
            baseFunction.createDDAsnRequest(ddAsnRequestDetailsParams);

            ReportManager.log("Step 2: Open RGR New Request page");
            new RequestDetailsPage(driver)
                    .ClickOnRequestRgrButton();

            ReportManager.log("Step 3: Draft the empty RGR request");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage
                    .ClickOnRgrRequestDraftButton()
                    .catchDraftedRequestBusinessKeyFromToaster();
            String rgrDraftedBusinessKey = asnNewRequestPage.getDraftedBusinessKey();

            ReportManager.log("Step 4: Open My Requests icon from the Launchpad");
            new LaunchPadPage(driver)
                    .openLaunchpadMenu()
                    .openPageFromLaunchpad(myRequests_launchpadIcon);

            ReportManager.log("Step 5: Search for drafted business key on My Requests list and open details");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(rgrDraftedBusinessKey)
                    .openRequestDetails(rgrDraftedBusinessKey);

            ReportManager.log("Step 6: Validate request status and delete the draft");
            new RequestDetailsPage(driver)
                    .validateRgrRequestStatus(RequestStatus.DRAFTED)
                    .ClickOnRgrDeleteDraftButton();

            ReportManager.log("Step 7: Verify the deleted draft no longer appears in My Requests");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(rgrDraftedBusinessKey)
                    .ValidateDeletedDraftNotInRequests();

        } finally {
            ReportManager.log("Final Step: Logout user from system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }


    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130466")
    @Test(groups = {"Regression", "Shipping_RGR"}, description = "Check that the RGR manager can approve the RGR request", dependsOnMethods = "createRgrRequest")
    @Description("Check that the RGR manager can approve the RGR request")
    public void approveRgrRequestByRgrManager() throws InterruptedException {

        try {
            ReportManager.log("Step 1: Login with RGR Manager user");
            new LoginPage(driver)
                    .userLogin(rgrManagerEmail, rgrManagerPassword)
                    .navigateToStagEnvDashboard()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 2: RGR Manager approves the RGR request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(businessKey)
                    .userAssignTaskToHim(businessKey)
                    .searchInSearchBar(businessKey)
                    .openTaskDetailsPageForRgrManager(businessKey)
                    .rgrManagerApproveRequest();

            ReportManager.log("Step 3: Wait until status becomes APPROVED in the database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    rgrConnectionString,
                    purchaseOrderManagerSqlQueries.getRGRStatusQuery(businessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 4: Validate the request status is APPROVED in My Tasks");
            new MyTasksPage(driver)
                    .validateTheRequestStatus(RequestStatus.APPROVED);

        } finally {
            ReportManager.log("Final Step: Logout user from system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }

    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130469")
    @TmsLink("https://devops.nupco.com/NUPCOCollection/iNUPCO/_workitems/edit/130467")
    @Test(groups = {"Regression", "Shipping_RGR"}, description = "Check that the RGR manager can reject the RGR request")
    @Description("Check that the RGR manager can reject the RGR request and the booking quantity is released")
    public void rejectRgrRequestByRgrManager() throws ParseException, InterruptedException {

        try {
            ReportManager.log("Step 1: Create DD ASN Request (prerequisite)");
            baseFunction = new BaseFunctions(driver);
            baseFunction.createDDAsnRequest(ddAsnRequestDetailsParams);
            baseFunction.validateQuantitiesAreBookedAfterSubmission(ddAsnRequestDetailsParams);
            String ddAsnBusinessKey = baseFunction.getBusinessKey();

            ReportManager.log("Step 2: Open My Requests from the Launchpad");
            new LaunchPadPage(driver)
                    .openLaunchpadMenu()
                    .openPageFromLaunchpad(myRequests_launchpadIcon);

            ReportManager.log("Step 3: Open created DD ASN request details from My Requests list");
            new MyRequestListPage(driver)
                    .SearchForBusinessKeyOnMyRequests(ddAsnBusinessKey)
                    .openRequestDetails(ddAsnBusinessKey);

            ReportManager.log("Step 4: Open RGR New Request page");
            new RequestDetailsPage(driver)
                    .ClickOnRequestRgrButton();

            ReportManager.log("Step 5: Fill and submit the RGR request");
            asnNewRequestPage = new AsnNewRequestPage(driver);
            asnNewRequestPage
                    .createNewRgrRequest(formNumberValue, pdfFilePath)
                    .ClickOnRgrRequestSubmitButton()
                    .copyRequestBusinessKey()
                    .BackToMyRequests();
            String rgrBusinessKey = asnNewRequestPage.getBusinessKey();

            ReportManager.log("Step 6: Wait until RGR status becomes NEW in the database");
            DBRequestStatus.waitForRequestStatus(
                    databaseRepository,
                    rgrConnectionString,
                    purchaseOrderManagerSqlQueries.getRGRStatusQuery(rgrBusinessKey),
                    RequestStatus.NEW
            );

            ReportManager.log("Step 7: Logout RGR Supplier user");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();

            ReportManager.log("Step 8: Login with RGR Manager user");
            new LoginPage(driver)
                    .userLogin(rgrManagerEmail, rgrManagerPassword)
                    .navigateToStagEnvDashboardDirectly()
                    .validateLoginSuccessfully();

            ReportManager.log("Step 9: RGR Manager rejects the RGR request");
            new MyTasksPage(driver)
                    .openMyTasksPage()
                    .openSpacePage()
                    .searchInSearchBar(rgrBusinessKey)
                    .userAssignTaskToHim(rgrBusinessKey)
                    .searchInSearchBar(rgrBusinessKey)
                    .openTaskDetailsPageForRgrManager(rgrBusinessKey)
                    .rgrManagerRejectRequest();

            ReportManager.log("Step 10: Validate request status is REJECTED");
            new MyTasksPage(driver)
                    .validateTheRequestStatus(RequestStatus.REJECTED);

            ReportManager.log("Step 11: Logout RGR Manager and login back with RGR Supplier");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully()
                    .userLogin(asnEmail, asnPassword);

            ReportManager.log("Step 12: Open NUPCO POs icon from the Launchpad");
            new LaunchPadPage(driver)
                    .openTheSpecificPageFromLaunchPad(nupcoPOs_launchpadIcon);

            ReportManager.log("Step 13: Wait up to 3 minutes for integration reflection");
            helper.WaitForIntegrationReflection(Duration.ofMinutes(3));

            ReportManager.log("Step 14: Open PO details using RHD customer filters");
            new POsListPage(driver)
                    .openPoDetailsPageWhetherFilterExist(
                            rhdRegion,
                            rhdCustomerName,
                            rhdSloc,
                            baseFunction.getPONumber()
                    );

            ReportManager.log("Step 15: Validate booking quantity is released after rejection");
            int updatedBookingQty = new PoDetailsPage(driver).readBookedQtyValue();
            new PoDetailsPage(driver)
                    .ValidateNewBookingQtyValueAfterRequestRejection(
                            baseFunction.getPoInitialBookedQuantity(),
                            updatedBookingQty
                    );

        } finally {
            ReportManager.log("Final Step: Logout user from system");
            new LoginPage(driver)
                    .userCanLogoutFromSiteSuccessfully();
        }
    }


    @BeforeClass(groups = {"Regression", "Shipping_RGR", "RhdRequestCreation"})
    public void beforeClass() {
        usersDetailsTestData = new SHAFT.TestData.JSON("UserCredentials.Json");
        filterOptionsTestData = new SHAFT.TestData.JSON("StorageLocationsFilters.json");
        ddAsnRequestDetailsTestData = new SHAFT.TestData.JSON("AsnRequestDetails.json");
        launchpadIcons = new SHAFT.TestData.JSON("LaunchpadIconsName.json");
        asnConnectionString = usersDetailsTestData.getTestData("asnSupplier_PurchasingDBConnectionString");
        rgrConnectionString = usersDetailsTestData.getTestData("RGRSupplier_RGRDBConnectionString");
        asnEmail = usersDetailsTestData.getTestData("asnSupplier_email");
        asnPassword = usersDetailsTestData.getTestData("asnSupplier_password");
        rgrManagerEmail = usersDetailsTestData.getTestData("RGRManager_email");
        rgrManagerPassword = usersDetailsTestData.getTestData("RGRManager_password");
        nupcoPOs_launchpadIcon = launchpadIcons.getTestData("NUPCO_POs");
        myRequests_launchpadIcon = launchpadIcons.getTestData("My_Requests");
        asnRegion = filterOptionsTestData.getTestData("ASN_Region");
        asnCustomerName = filterOptionsTestData.getTestData("ASN_CustomerName");
        asnSloc = filterOptionsTestData.getTestData("ASN_SLOC");
        asnInvoiceNumber = ddAsnRequestDetailsTestData.getTestData("invoiceNum");
        asnDeliveryTime = ddAsnRequestDetailsTestData.getTestData("deliveryTime");
        asnPalletsNumber = ddAsnRequestDetailsTestData.getTestData("palletsNum");
        asnBatchNumber = ddAsnRequestDetailsTestData.getTestData("batchNum");
        asnQuantity = ddAsnRequestDetailsTestData.getTestData("qty");
        asnFeedbackData = ddAsnRequestDetailsTestData.getTestData("feedbackData");
        noteText = ddAsnRequestDetailsTestData.getTestData("noteText");
        asnTruckValue = ddAsnRequestDetailsTestData.getTestData("truckValue");
        formNumberValue = ddAsnRequestDetailsTestData.getTestData("form1NumberValue");
        rhdRegion = filterOptionsTestData.getTestData("RHD_Region");
        rhdCustomerName = filterOptionsTestData.getTestData("RHD_CustomerName");
        rhdSloc = filterOptionsTestData.getTestData("RHD_SLOC");
        pdfFilePath = FileActions.getInstance().getAbsolutePath(SHAFT.Properties.paths.testData(), "dummy.pdf");
        connectionManager = new SQLConnectionManager();
        databaseRepository = new DatabaseRepository(connectionManager);
        ddAsnRequestDetailsParams = new InboundRequestsParams
                (
                        asnConnectionString,
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

    @BeforeMethod(groups = {"Regression", "Shipping_RGR", "RhdRequestCreation"})
    public void beforeMethod() {
        driver = new SHAFT.GUI.WebDriver();
        new LoginPage(driver)
                .navigateToLoginPage();
    }

    @AfterMethod(groups = {"Regression", "Shipping_RGR", "RhdRequestCreation"})
    public void afterMethod() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
