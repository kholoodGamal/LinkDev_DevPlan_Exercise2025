package com.nupco.pages;

import com.nupco.utils.DatePickerHelper;
import com.nupco.utils.DateValues;
import com.shaft.cli.FileActions;
import com.shaft.driver.SHAFT;
import com.shaft.gui.element.internal.ElementActionsHelper;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import static com.nupco.utils.helper.*;

public class MyTasksPage {
    private SHAFT.GUI.WebDriver driver;
    DatePickerHelper datePicker;
    private String rejectReason = "Supplier - Cancelled or Rescheduled Appointment";
    private String rgrRejectReason = "Contact RHD to create GR, ASN is booked.";
    private String requestEditReason = "Request Edit: Some fields values need to be updated";
    private String rejectionReason = "Rejected: Some fields values need to be updated";
    private String RhdCancelReason = "Before Arrival - Supplier - Cancelled or Rescheduled Appointment";
    private String selectedDeliveryTime = "01:00 am";
    private String tasksPageUrl = System.getProperty("tasksPageURL");

    public static ElementActionsHelper elementActionsHelper = new ElementActionsHelper(false);

    WebDriverWait wait;
    String rgrManagerTaskLocator = "//span[normalize-space()='Review']";


    private final By myTasksOptions = By.xpath("//div[contains(text(),'My Task')]");
    private final By space = By.xpath("//label[contains(@class,'page-tab')][.//span[normalize-space()='Space']]");
    private final By searchBar = By.xpath("//input[@type='search']");
    private final By noSearchResultMessage = By.xpath("//h5[normalize-space()='No Results Found']");
    private final By assignButton = By.id("dropdown-basic");
    private final By assignToMeChoice = By.xpath("//a[normalize-space()='Assign to me']");
    private final By firstElementInGrid = By.xpath("//tbody//tr//td[1]");
    private final By reviewByinBoundUser = By.xpath("//span[normalize-space()='ReviewByInboundUser']");
    private final By reviewByRhdUser = By.xpath("//span[normalize-space()='ReviewByRHDUser']");
    private final By reviewByRgrManagerUser = By.xpath("//span[normalize-space()='Review']");
    private final By approveButton = By.xpath("//button[normalize-space()='Approve' and not(@disabled)]");
    private final By rejectButton = By.xpath("//button[normalize-space()='Reject']");
    private final By rejectList = By.xpath("//select[@placeholder='Please provide your feedback']");
    private final By submitButton = By.xpath("//button[normalize-space()='Submit' and not(@disabled)]");
    private final By submitOnConfirmationPopUp = By.xpath("//button[@class='saveModal']");
    private final By submitSuccessMessage = By.xpath("//h2[normalize-space()='Success']");
    private final By submitFailMessage = By.xpath("//h2[normalize-space()='Failed']");
    private final By approveText = By.xpath("//p[normalize-space()='Approved']");
    private final By rejectText = By.xpath("//p[normalize-space()='Rejected']");
    private final By deliveredText = By.xpath("//p[normalize-space()='Delivered']");
    private final By taskInternalStatus = By.xpath("(//span[normalize-space()='Status']/following-sibling::p)[1]");
    private final By cancelledText = By.xpath("//p[normalize-space()='Cancelled']");
    private final By deliveryDate = By.xpath("//div[@class = 'react-datepicker__input-container']//input");
    private final By actualDeliveryDate = By.xpath("//div[@class='inputValidation']//div[@class='react-datepicker-wrapper']//input");
    private final By postGrButton = By.xpath("//button[normalize-space()='Post GR']");
    private final By cancelButton = By.xpath("//button[normalize-space()='Cancel']");
    private final By physicallyReceivedButton = By.xpath("//button[@data-testid = 'physicallyReceivedAction']");
    private final By feedbackTextField = By.xpath("//textarea[contains(@placeholder,'Please provide your feedback')]");
    private final By discardAllChanges = By.xpath("//button[normalize-space()='Discard all changes']");
    private final By form1NumberTextField = By.xpath("//input[@placeholder='Value']");
    private final By deliveryDateText = By.xpath("//div[contains(text(),'Delivery Date must be greater than today')]");
    private final By dueDateText = By.xpath("//div[contains(text(), \"Due Date isn't exist, please try again\")]");
    private final By requestToEditButton = By.xpath("//button[@class = 'editRequestBtn']");
    private final By availableQtyLoading = By.id("//span[@class = 'loader']");
    private final By editDeliveryDate = By.xpath("//div[@class='inputValidation']//button[@class='shipment-editBtn'][normalize-space()='Edit']");
    private final By editDeliveryTime = By.xpath("//div[@class='shipment-item shipment-item-withoutIcon delivery-time']//div//button[@class='shipment-editBtn'][normalize-space()='Edit']");
    private final By deliveryTimeList = By.xpath("(//select[@class='form-select'])[1]");
    private final By requestedQtyField = By.xpath("//input[contains(@id, 'qutInput')]");
    private final By secondReview = By.xpath("//span[normalize-space()='Second Review']");
    private final By addItemCommentButton = By.xpath("//button[contains(@class,'btn-add-item-comment')]");
    private final By noteOrCommentContent = By.xpath("//textarea[@rows='3' and @aria-label='maximum height' and (contains(@placeholder,'description') or contains(@placeholder,'notes'))]");
    private final By saveOrSubmitButtonOfCommentOrNote = By.xpath("//button[@class='saveModal' and (normalize-space(text())='Save' or normalize-space(text())='Submit')]");
    private final By requestUrgencyList = By.name("requesturgency");
    private final By toastMessage = By.xpath("//*[contains(@class,'Toastify__toast')]");


    public MyTasksPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver.getDriver(), Duration.ofMinutes(1));
    }


    public WebElement getMyTasks() {
        return driver.getDriver().findElement(myTasksOptions);
    }

    public WebElement getDeliveredText() {
        return driver.getDriver().findElement(deliveredText);
    }

    public WebElement getCancelledText() {
        return driver.getDriver().findElement(cancelledText);
    }

    public WebElement getApprovedText() {
        return driver.getDriver().findElement(approveText);
    }

    public WebElement getRejectText() {
        return driver.getDriver().findElement(rejectText);
    }


    public WebElement getDiscardAllChanges() {
        return driver.getDriver().findElement(discardAllChanges);
    }


    public WebElement getSpacePage() {
        return driver.getDriver().findElement(space);
    }

    public WebElement getAssignToMeChoice() {
        return driver.getDriver().findElement(assignToMeChoice);
    }

    public WebElement getAssignButton() {
        return driver.getDriver().findElement(assignButton);
    }

    public WebElement gettable() {
        return driver.getDriver().findElement(firstElementInGrid);
    }

    public WebElement getTextInsideFirstElementToInbound() {
        return gettable().findElement(reviewByinBoundUser);
    }

    public WebElement getTextInsideFirstElementToRHd() {
        return gettable().findElement(reviewByRhdUser);
    }

    public WebElement getTextInsideFirstElementToRgrManager() {
        return gettable().findElement(reviewByRgrManagerUser);
    }

    public WebElement getActualDeliveryDate() {
        return driver.getDriver().findElement(actualDeliveryDate);
    }


    public WebElement getSubmitButton() {
        return gettable().findElement(submitButton);
    }

    public WebElement getApproveButton() {
        return gettable().findElement(approveButton);
    }

    public WebElement getRejectButton() {
        return gettable().findElement(rejectButton);
    }

    public WebElement getSubmitOnConfirmationPopUp() {
        return driver.getDriver().findElement(submitOnConfirmationPopUp);
    }

    public WebElement getDueDateText() {
        return driver.getDriver().findElement(dueDateText);
    }

    public WebElement getTextInsideFirstElementToInboundSecReview() {
        return gettable().findElement(secondReview);
    }

    @Step("Open My Tasks Page After User Clicks On My Tasks")
    public MyTasksPage openMyTasksPage() {
        driver.browser().navigateToURL(tasksPageUrl);
        return this;
    }


    @Step("Open the Space tab")
    public MyTasksPage openSpacePage() {
        WebDriver driverInstance = driver.getDriver();
        wait = new WebDriverWait(driverInstance, Duration.ofSeconds(180));

        By tabsContainer = By.xpath("//*[contains(@class,'page-tab')]");
        By spaceTab = By.xpath("//*[contains(@class,'page-tab')]//*[translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='space']");

        try {
            ReportManager.log(" Step 1: Navigate directly to My Tasks");
            ReportManager.logDiscrete("🌐 Navigating directly to: " + tasksPageUrl);
            driverInstance.navigate().to(tasksPageUrl);

            ReportManager.log(" Wait for page and framework to load");
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState=='complete'"));
            driver.browser().waitForLazyLoading();

            ReportManager.log(" Step 2: Wait for the tabs container to appear");
            ReportManager.logDiscrete("⌛ Waiting for tabs container to appear...");
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tabsContainer));

            ReportManager.logDiscrete(" Tabs container detected.");

            ReportManager.log("Step 3: Click on the Space tab");
            WebElement space = wait.until(ExpectedConditions.elementToBeClickable(spaceTab));
            scrollIntoWebElement(driverInstance, space);
            clickUsingJavaScript(driverInstance, space);

            ReportManager.logDiscrete(" Clicked on 'Space' tab successfully.");

            ReportManager.log("Step 4: Wait for content to hydrate after click");
            driver.browser().waitForLazyLoading();

        } catch (TimeoutException e) {
            String html = driverInstance.getPageSource();
            String snippet = html.length() > 1000 ? html.substring(0, 1000) : html;
            ReportManager.logDiscrete(" Tabs container or 'Space' tab never appeared. Snapshot:\n" + snippet);
            throw new AssertionError(" Failed to open 'Space' tab after navigating to My Tasks. Verify if the /tasks page structure changed.", e);
        } catch (Exception e) {
            throw new AssertionError(" Unexpected failure while opening 'Space' tab: " + e.getMessage(), e);
        }

        return this;
    }

    @Step("Search Bar In Space Page Is Filled With Data ")
    public MyTasksPage searchInSearchBar(String searchText) {
        driver.browser().waitForLazyLoading();
        driver.element().click(searchBar)
                .type(searchBar, searchText);
        return this;
    }

    @Step("User Assign From It Request To Him")
    public MyTasksPage userAssignTaskToHim(String searchText) {
        driver.browser().waitForLazyLoading();
        int searchTrials = 5;

        while (searchTrials != 0) {
            try {
                driver.element().waitUntilNumberOfElementsToBe(assignButton, 1);
            } catch (AssertionError e) {
                driver.element()
                        .type(searchBar, searchText);
                driver.element().waitUntilNumberOfElementsToBe(assignButton, 1);
            }

            searchTrials--;
        }

        getAssignButton().click();
        driver.element().hover(assignToMeChoice);
        clickUsingJavaScript(driver.getDriver(), getAssignToMeChoice());
        driver.element().waitUntilPresenceOfAllElementsLocatedBy(noSearchResultMessage);
        driver.browser().refreshCurrentPage();
        return this;
    }

    @Step("RHD User Opens The Request Details")
    public MyTasksPage openTaskDetailsPageForRHDUser(String businessKey) {
        return openTaskDetails(businessKey,
                "ReviewByRHDUser",
                this::getTextInsideFirstElementToRHd);
    }

    @Step("InBound User Opens The Request Details")
    public MyTasksPage taskDetailsPageForInboundUser(String businessKey) {
        return openTaskDetails(businessKey,
                "ReviewByInboundUser",
                this::getTextInsideFirstElementToInbound);
    }

    @Step("HA User Opens The Request Details")
    public MyTasksPage openTaskDetailsPageForHA(String businessKey) {
        return openTaskDetails(businessKey,
                "First Review",
                this::getTextInsideFirstElementToInbound);
    }

    @Step("SO User Opens The task Details")
    public MyTasksPage openTaskDetailsPageForSO(String businessKey) {
        return openTaskDetails(businessKey,
                "Request Edit",
                this::getTextInsideFirstElementToInbound);
    }

    private MyTasksPage openTaskDetails(String businessKey,
                                        String taskType,
                                        Supplier<WebElement> fallbackElementSupplier) {
        try {
            // Row that matches both task type and business key
            String rowXPath = String.format(
                    "//tbody/tr[td[normalize-space()='%s'] and td[normalize-space()='%s']]",
                    taskType, businessKey
            );
            String taskLocatorSuffixXPath = String.format(
                    "//span[normalize-space()='%s']",
                    taskType
            );
            By target = By.xpath(rowXPath + taskLocatorSuffixXPath);

            // Primary path: wait for the specific task cell/link then click
            elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), target, "");
            driver.element().click(target);

        } catch (StaleElementReferenceException
                 | NoSuchElementException
                 | TimeoutException
                 | ElementClickInterceptedException e) {

            // Fallback: resolve element via supplier and wait until it's clickable
            WebDriverWait wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(10));
            WebElement fallback = fallbackElementSupplier.get();
            wait.until(ExpectedConditions.elementToBeClickable(fallback));
            try {
                fallback.click();
            } catch (Exception clickEx) {
                // Last-resort JS click
                clickUsingJavaScript(driver.getDriver(), fallback);
            }
        }
        return this;
    }

    @Step("InBound/HA User Can Approve The Request Assign")
    public MyTasksPage userApproveAssignedRequest() {
        if (isElementPresent(deliveryDateText, driver)) {
            if (driver.element().isElementDisplayed(deliveryDateText)) {
                driver.element().waitToBeInvisible(deliveryDateText);
            }
        }
        //wait.until(ExpectedConditions.invisibilityOfElementLocated(availableQtyLoading));
        while (!wait.until(ExpectedConditions.invisibilityOfElementLocated(availableQtyLoading))) {
            driver.browser().refreshCurrentPage();
        }

        // Perform the loop while the submit button is displayed
        while (true) {
            try {
                // Check if the submit button is displayed
                if (getApproveButton().isDisplayed()) {
                    // Perform the necessary actions
                    driver.element().click(approveButton);
                    getSubmitButton().click();
                    wait.until(ExpectedConditions.or(
                            ExpectedConditions.visibilityOfElementLocated(submitSuccessMessage),
                            ExpectedConditions.visibilityOfElementLocated(submitFailMessage)));
                    driver.browser().refreshCurrentPage();
                } else {
                    // Break out of the loop if the submit button is no longer displayed
                    break;
                }
            } catch (NoSuchElementException e) {
                // Handle cases where the button is not present and exit the loop
                System.out.println("Submit button is no longer present: " + e.getMessage());
                break;
            }
        }

        // Continue with the next steps after exiting the loop
        return this;
    }

    @Step("User Can Approve The Request Assigned in case adding new items")
    public MyTasksPage haUserApproveAssignedRequest() {
        wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(30));

        try {
            ReportManager.log("Waiting for any loaders to disappear...");
            if (isElementPresent(deliveryDateText, driver) && driver.element().isElementDisplayed(deliveryDateText)) {
                driver.element().waitToBeInvisible(deliveryDateText);
            }
            wait.until(ExpectedConditions.invisibilityOfElementLocated(availableQtyLoading));
            ReportManager.log("Start approval loop (if multiple approvals exist)");

            while (true) {
                try {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(approveButton));

                    ReportManager.log("Scroll safely into view");
                    driver.element().scrollToElement(approveButton);

                    ReportManager.log("Wait until not obstructed");
                    wait.until(ExpectedConditions.elementToBeClickable(approveButton));

                    try {
                        driver.element().click(approveButton);
                        ReportManager.log("Clicked Approve button successfully");
                    } catch (ElementClickInterceptedException e) {
                        ReportManager.log("Normal click intercepted — retrying with JS click");
                        driver.element().clickUsingJavascript(approveButton);
                    }

                    wait.until(ExpectedConditions.elementToBeClickable(submitButton));
                    driver.element().scrollToElement(submitButton);


                    try {
                        driver.element().click(submitButton);
                        ReportManager.log("Clicked Submit button successfully");
                    } catch (ElementClickInterceptedException e) {
                        ReportManager.log("Submit click intercepted — retrying with JS click");
                        driver.element().clickUsingJavascript(submitButton);
                    }

                    ReportManager.log("Wait for feedback");
                    wait.until(ExpectedConditions.or(
                            ExpectedConditions.visibilityOfElementLocated(submitSuccessMessage),
                            ExpectedConditions.visibilityOfElementLocated(submitFailMessage)
                    ));

                    driver.browser().refreshCurrentPage();

                } catch (TimeoutException | NoSuchElementException e) {
                    ReportManager.log("No more Approve buttons found, exiting loop.");
                    break;
                }
            }

        } catch (Exception e) {
            ReportManager.log("Failed to approve assigned request: " + e.getMessage());
            e.printStackTrace();
        }

        return this;
    }

    @Step("RHD User Can Submit The Request")
    public MyTasksPage rhdUserSubmitRequest() {
        driver.element().waitToBeReady(submitButton, true)
                .clickUsingJavascript(submitButton);
        getSubmitOnConfirmationPopUp().click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(submitSuccessMessage));
        driver.browser().refreshCurrentPage();
        return this;
    }

    public MyTasksPage rejectRequestAssign() throws InterruptedException {

        wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(10));
        int maxRetries = 10;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(rejectButton)).click();
            } catch (Exception e) {
                driver.element().clickUsingJavascript(rejectButton);
            }
            if (isElementPresent(rejectList, driver)) {
                break;
            }
            if (isElementPresent(toastMessage, driver)) {
                System.out.println("Toast detected. Waiting to disappear...");
                wait.until(ExpectedConditions.invisibilityOfElementLocated(toastMessage));
            }

            Thread.sleep(1000);
        }

        if (!isElementPresent(rejectList, driver)) {
            throw new RuntimeException("Reject reason list never appeared after retries.");
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(rejectList));
        driver.element().select(rejectList, rejectReason);
        WebElement submitBtn = getSubmitButton();
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(submitSuccessMessage));

        driver.browser().refreshCurrentPage();

        return this;
    }


    @Step("User Can Change Delivery Date and Delivery Time")
    public MyTasksPage changeDeliveryDateAndTime(LocalDate updatedDate) {
        datePicker = new DatePickerHelper(driver);
        driver.element().click(editDeliveryDate);
        datePicker.setDateForCalendar(actualDeliveryDate, updatedDate);
        driver.element()
                .click(editDeliveryTime)
                .select(deliveryTimeList, selectedDeliveryTime);

        return this;
    }

    @Step("User Can Change Request Urgency Type")
    public MyTasksPage changeRequestUrgency(String requestType) {
        driver.element().scrollToElement(requestUrgencyList)
                .waitToBeReady(requestUrgencyList).
                select(requestUrgencyList, requestType);
        return this;
    }

    @Step("HA User Can Change Delivery Date Of OutBound Request")
    public LocalDate changeDeliveryDate(LocalDate updatedDate) {
        datePicker = new DatePickerHelper(driver);
        driver.element().scrollToElement(deliveryDate)
                .waitToBeReady(deliveryDate);
        datePicker.setDateForCalendar(deliveryDate, updatedDate);
        String actualDateStr = driver.element()
                .getText(deliveryDate)
                .trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate actualSelectedDate = LocalDate.parse(actualDateStr, formatter);

        ReportManager.logDiscrete("Actual selected delivery date: " + actualSelectedDate);
        return actualSelectedDate;
    }

    @Step("RHD User Can Cancel The Request Assign")
    public MyTasksPage cancelRequest() {
        driver.element().click(cancelButton)
                .select(rejectList, RhdCancelReason);
        // .type(feedbackTextField, feedback);
        getSubmitOnConfirmationPopUp().click();
        return this;
    }

    @Step("User Can post GR")
    public MyTasksPage userCanPostGr(String value, String filePath) throws InterruptedException {

        datePicker = new DatePickerHelper(driver);
        elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), actualDeliveryDate, "");
        try {
            datePicker.setDateForCalendar(actualDeliveryDate, DateValues.today);
        } catch (NoSuchElementException | ElementClickInterceptedException | IllegalStateException e) {
            elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), actualDeliveryDate, "");
            datePicker.setDateForCalendar(actualDeliveryDate, DateValues.today);
        }

        elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), postGrButton, "");
        driver.element().click(postGrButton);

        getDiscardAllChanges().click();
        driver.element().type(form1NumberTextField, value);

        WebElement firstUploader, secondUploader;
        try {

            firstUploader = driver.getDriver().findElement(By.xpath("//*[@id='POD']//input[@type='file']"));
            firstUploader.sendKeys(FileActions.getInstance().getAbsolutePath(SHAFT.Properties.paths.testData(), filePath));
            Thread.sleep(10000);

            secondUploader = driver.getDriver().findElement(By.xpath("//*[@id='SupplierInvoice']//input[@type='file']"));
            secondUploader.sendKeys(FileActions.getInstance().getAbsolutePath(SHAFT.Properties.paths.testData(), filePath));
            Thread.sleep(10000);

        } catch (NoSuchElementException | StaleElementReferenceException e) {
            firstUploader = driver.getDriver().findElement(By.xpath("//*[@id='POD']//input[@type='file']"));
            firstUploader.sendKeys(FileActions.getInstance().getAbsolutePath(SHAFT.Properties.paths.testData(), filePath));
            Thread.sleep(10000);

            secondUploader = driver.getDriver().findElement(By.xpath("//*[@id='SupplierInvoice']//input[@type='file']"));
            secondUploader.sendKeys(FileActions.getInstance().getAbsolutePath(SHAFT.Properties.paths.testData(), filePath));
            Thread.sleep(10000);

        }

        return this;
    }

    @Step("User Can physically Received the RHD request")
    public MyTasksPage userCanPhysicallyReceivedRhdRequest(LocalDate updatedDate) {
        datePicker = new DatePickerHelper(driver);
        driver.element().click(actualDeliveryDate);
        datePicker.setDateForCalendar(actualDeliveryDate, updatedDate);
        driver.element().click(physicallyReceivedButton);
        getSubmitButton().click();

        return this;
    }

    @Step("InBound User Opens The Request Details")
    public MyTasksPage openTaskDetailsPageForRgrManager(String businessKey) {
        try {

            // Create dynamic XPath to locate the row with the dynamic value
            String AssignedTasksRowXPath = String.format("//tbody/tr[td[normalize-space()='Review'] and td[normalize-space()='%s']]",
                    businessKey);

            // Combine the locators to target the task name in the specific row
            String combinedLocator = AssignedTasksRowXPath + rgrManagerTaskLocator;

            elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), By.xpath(combinedLocator), "");

            // Click on the task name element
            driver.element().click(By.xpath(combinedLocator));


        } catch (StaleElementReferenceException e) {
            wait.until(ExpectedConditions.visibilityOf(getTextInsideFirstElementToRgrManager()));
            getTextInsideFirstElementToRgrManager().click();
        }
        return this;
    }

    @Step("RGR manager Can Approve The Assigned Request")
    public MyTasksPage rgrManagerApproveRequest() {
        driver.element().waitUntilPresenceOfAllElementsLocatedBy(approveButton);
        elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), approveButton, "");
        driver.element().click(approveButton);

        getSubmitButton().click();
        driver.browser().refreshCurrentPage();

        return this;
    }

    @Step("RGR manager Can Reject The Assigned Request")
    public MyTasksPage rgrManagerRejectRequest() {
        elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), rejectButton, "");
        driver.element()
                .click(rejectButton)
                .select(rejectList, rgrRejectReason);

        getSubmitButton().click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(submitSuccessMessage));
        driver.browser().refreshCurrentPage();

        return this;
    }

    @Step("Reviewer user can Request to edit the outbound request")
    public MyTasksPage userRequestToEditOutbound() {
        wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(30));

        try {
            ReportManager.log("Wait for any lazy loading or initial animation");
            driver.browser().waitForLazyLoading();

            ReportManager.log(" Robust wait for loader disappearance (handle reappearing loaders)");
            By mainLoader = By.xpath("//span[@class='loader']");
            int retryCount = 5;

            while (retryCount-- > 0) {
                try {
                    if (isElementPresent(mainLoader, driver)) {
                        ReportManager.logDiscrete("Waiting for main loader to disappear...");
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(mainLoader));
                    }
                    break;
                } catch (TimeoutException e) {
                    if (retryCount > 0) {
                        ReportManager.logDiscrete("Loader still visible. Retrying (" + retryCount + " more attempts)...");
                    } else {
                        ReportManager.logDiscrete("Loader did not disappear after multiple attempts.");
                    }
                }
            }

            ReportManager.log(" Wait for quantity loader if exists");
            if (isElementPresent(availableQtyLoading, driver)) {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(availableQtyLoading));
            }

            ReportManager.log(" Wait for Request-to-Edit button, scroll, and click");
            ReportManager.logDiscrete("Waiting for 'Request to Edit' button...");
            driver.element().waitUntilPresenceOfAllElementsLocatedBy(requestToEditButton);
            elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), requestToEditButton, "");
            driver.element().scrollToElement(requestToEditButton).click(requestToEditButton);

            ReportManager.log(" Fill reason");
            ReportManager.logDiscrete("Typing edit request reason...");
            driver.element().type(feedbackTextField, requestEditReason);

            ReportManager.log("Submit the request");
            getSubmitButton().click();
            ReportManager.logDiscrete("Clicked Submit button successfully.");

        } catch (TimeoutException e) {
            ReportManager.logDiscrete("Timeout waiting for loader or button: " + e.getMessage());
            driver.browser().refreshCurrentPage();
        } catch (Exception e) {
            ReportManager.logDiscrete("Unexpected error while requesting edit: " + e.getMessage());
            e.printStackTrace();
        }
        driver.browser().refreshCurrentPage();
        return this;
    }

    @Step("User Can Add Item Level Note")
    public MyTasksPage haUserAddItemLevelNote(String itemNoteText) {
        driver.element().scrollToElement(addItemCommentButton)
                .click(addItemCommentButton)
                .type(noteOrCommentContent, itemNoteText)
                .click(saveOrSubmitButtonOfCommentOrNote);

        return this;
    }
    @Step("User Can Edit Item Level Note")
    public MyTasksPage haUserEditItemLevelNote(String updatedItemNoteText) {
        driver.element().scrollToElement(addItemCommentButton)
                .click(addItemCommentButton)
                .clear(noteOrCommentContent)
                .type(noteOrCommentContent, updatedItemNoteText)
                .click(saveOrSubmitButtonOfCommentOrNote);

        return this;
    }


    @Step("HA user edits the outbound request Quantity")
    public MyTasksPage haUserEditQtyOfOutboundRequest(String updatedQty) {
        WebDriverWait wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(20));
        int retries = 5;
        boolean success = false;

        while (retries-- > 0 && !success) {
            try {
                // Wait for any loader/overlay to disappear
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loading")));
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//span[@class='loader']")));

                WebElement qtyField = driver.getDriver().findElement(By.xpath("//input[contains(@id, 'qutInput')]"));

                // Scroll to element and ensure visibility
                ((JavascriptExecutor) driver.getDriver()).executeScript("arguments[0].scrollIntoView(true);", qtyField);
                wait.until(ExpectedConditions.elementToBeClickable(qtyField));

                // Clear & type new value using JS (bypasses intercept issues)
                ((JavascriptExecutor) driver.getDriver()).executeScript("arguments[0].value='';", qtyField);
                qtyField.sendKeys(updatedQty);

                ReportManager.logDiscrete("Quantity updated successfully to: " + updatedQty);
                success = true;

            } catch (ElementClickInterceptedException e) {
                ReportManager.logDiscrete("Click intercepted — likely due to overlay. Retrying (" + (3 - retries) + "/3)");
                driver.browser().refreshCurrentPage();
            } catch (TimeoutException e) {
                ReportManager.logDiscrete("Timeout waiting for loader to disappear (" + (3 - retries) + "/3)");
            } catch (Exception e) {
                ReportManager.logDiscrete("Unexpected error while editing quantity: " + e.getMessage());
            }
        }

        if (!success) {
            throw new RuntimeException("Failed to edit quantity field after multiple attempts.");
        }

        return this;
    }

    @Step("Hospital approver Can Reject The Assigned Request")
    public MyTasksPage HospitalApproverRejectRequest() {
        driver.element().waitUntilPresenceOfAllElementsLocatedBy(rejectButton);
        while (!wait.until(ExpectedConditions.invisibilityOfElementLocated(availableQtyLoading))) {
            driver.browser().refreshCurrentPage();
        }
        elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), rejectButton, "");
        driver.element()
                .click(rejectButton)
                .type(feedbackTextField, rejectionReason)
                .click(submitOnConfirmationPopUp);
        driver.browser().refreshCurrentPage();
        return this;
    }


    // region Validations

    @Step("Validate That Request Is Approved Successfully")
    public void validateThatRequestStatusIsApproved() {
        driver.browser().refreshCurrentPage();
        driver.element().waitToBeReady(approveText, true)
                .scrollToElement(approveText)
                .assertThat(approveText).text().equalsIgnoringCaseSensitivity("Approved");
    }

    @Step("Validate That Request Is Rejected Successfully")
    public void validateThatRequestStatusIsRejected() {
        driver.browser().refreshCurrentPage();
        driver.element().waitToBeReady(rejectText, true)
                .scrollToElement(rejectText)
                .assertThat(rejectText).text().equalsIgnoringCaseSensitivity("Rejected");
        Assert.assertTrue(getRejectText().getText().contains("Rejected"), "Asseration Passed");
    }

    @Step("Validate That Request Is Delivered Successfully")
    public void validateThatRequestStatusIsDelivered() {
        driver.element().waitToBeReady(deliveredText, true)
                .scrollToElement(deliveredText)
                .assertThat(deliveredText).text().equalsIgnoringCaseSensitivity("Delivered");
        Assert.assertTrue(getDeliveredText().getText().contains("Delivered"), "Asseration Passed");
    }

    @Step("Validate That Request Is Cancelled Successfully")
    public void validateThatRequestIsCanceled() {
        driver.element().waitToBeReady(cancelledText, true)
                .scrollToElement(cancelledText)
                .assertThat(cancelledText).text().equalsIgnoringCaseSensitivity("Cancelled");
        Assert.assertTrue(getCancelledText().getText().contains("Cancelled"), "Asseration Passed");
    }

    @Step("Validate That Successful Message Is Displayed Successfully")
    public MyTasksPage validateThatSuccessfulMessageIsDisplayedSuccessfully() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(submitSuccessMessage));
        driver.element().assertThat(submitSuccessMessage).isVisible();
        driver.browser().refreshCurrentPage();
        return this;
    }


    @Step("Validate That Request status Is {expectedStatus}")
    public void validateTheRequestStatus(String expectedStatus) {
        driver.element().waitToBeReady(taskInternalStatus, true)
                .scrollToElement(taskInternalStatus)
                .assertThat(taskInternalStatus).text().equalsIgnoringCaseSensitivity(expectedStatus);
    }


    @Step("Validate That Delivery Time is Changed Successfully")
    public void validateThatDeliveryTimeIsChangedSuccessfully() {
        driver.element().getText(deliveryTimeList).equalsIgnoreCase(selectedDeliveryTime);
    }

    @Step("Validate The Updated Requested Qty Value")
    public MyTasksPage validateTheUpdatedRequestedQtyValue(String requestedQty) {
        driver.element().waitToBeReady(requestedQtyField, true)
                .scrollToElement(requestedQtyField)
                .assertThat(requestedQtyField).text().equalsIgnoringCaseSensitivity(requestedQty);

        return this;
    }

    @Step("Validate The Request Type Value")
    public MyTasksPage validateTheRequestEmergencyValue(String expectedType) {
        WebDriver driverInstance = driver.getDriver();
        wait = new WebDriverWait(driverInstance, Duration.ofSeconds(20));

        String actualSelected = null;

        for (int i = 0; i < 10; i++) {
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(requestUrgencyList));
                Select dropdown = new Select(driverInstance.findElement(requestUrgencyList));
                actualSelected = dropdown.getFirstSelectedOption().getText().trim();
                break;
            } catch (StaleElementReferenceException e) {
                ReportManager.logDiscrete("StaleElementReferenceException occurred. Retrying... attempt " + (i + 1));
                driver.browser().refreshCurrentPage().waitForLazyLoading();
            }
        }

        if (actualSelected == null) {
            Assert.fail("Unable to retrieve selected request type after multiple retries!");
        }

        ReportManager.logDiscrete("Selected Request Type: " + actualSelected);
        Assert.assertEquals(actualSelected, expectedType, "Incorrect selected Request Type!");

        return this;
    }

    @Step("Validate The Updated Delivery Date")
    public MyTasksPage validateTheUpdatedDeliveryDateSetByHA(LocalDate actualSelectedDate) {
        driver.element().waitToBeReady(deliveryDate, true)
                .scrollToElement(deliveryDate)
                .assertThat(deliveryDate).text().equalsIgnoringCaseSensitivity(actualSelectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        return this;
    }

    @Step("InBound User Opens The Request Details for Second Review Return Outbound")
    public MyTasksPage taskDetailsPageForInboundUserSecondReview(String businessKey) {
        return openTaskDetails(businessKey,
                "Second Review",
                this::getTextInsideFirstElementToInboundSecReview);
    }
    // endregion
}