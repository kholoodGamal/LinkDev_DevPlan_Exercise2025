package com.nupco.pages;

import com.nupco.utils.DatePickerHelper;
import com.nupco.utils.ShelfLife;
import com.nupco.utils.helper;
import com.shaft.cli.FileActions;
import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import com.shaft.validation.Validations;
import io.qameta.allure.Step;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nupco.pages.MyTasksPage.elementActionsHelper;
import static com.nupco.utils.helper.*;

public class AsnNewRequestPage {

    //region Variables

    private SHAFT.GUI.WebDriver driver;
    private String businessKey, draftedBusinessKey, numOfPallets;

    public String getBusinessKey() {
        return businessKey;
    }

    public String getDraftedBusinessKey() {
        return draftedBusinessKey;
    }

    public String getNumOfPallets() {
        return numOfPallets;
    }


    //endregion

    //region Locators

    private final By submitButton = By.xpath("//button[@data-testid='btnSubmitAsn']");
    private final By draftButton = By.xpath("//button[@data-testid='btnDraftAsn']");
    private final By invoiceNumber = By.cssSelector("input.batches-inputs[placeholder='Invoice Number']");
    private final By deliveryTime = By.xpath("//select[@class='no-value   form-select']");
    private final By deliveryDate = By.xpath("//div[@class = 'form-select delivery-date inputValidation false  ']//input");
    private final By nextMonthButton = By.xpath("//button[@aria-label='Next Month']");
    private final By noOfPalletsText = By.xpath("//input[@class='no-pallet']");
    private final By availNoOfPallets = By.xpath("//span[@class='pallets-num']");
    private final By tradeCodeRowExpander = By.xpath("//button[@class='btn border' and @data-testid='showAccordion']");
    private final By batchRowExpander = By.xpath("(//button[@class='btn border'])[2]");
    private final By batchNumber = By.xpath("//input[@placeholder='Batch Number']");
    private final By quantity = By.xpath("//input[@class='batches-inputs quantity-input  ']");
    private final By enabledCalenderDays = By.xpath("//div[@class='react-datepicker__week']//div[@aria-disabled='false']"); //Get whole enabled dates
    private final By firstEnabledCalenderDay = By.xpath("(//div[@class='react-datepicker__week']//div[@aria-disabled='false'])[1]"); //Get the first enabled date
    private final By requestBusinessKey = By.xpath("//div[@class='modal-content']//span[@class='request-numb']");
    private final By draftedRequestBusinessKey = By.xpath("(//div[@role='alert' and @class='Toastify__toast-body']//div)[2]");
    private final By backToMyRequestsButton = By.xpath("(//button[@class = 'btns modal-actions'])[1]");
    private final By rgrForm1Number = By.xpath("//input[@class = 'batches-inputs' and @placeholder = 'Form 1 Number']");
    private final By rgrDeliveryDate = By.xpath("//div[@class = 'form-select delivery-date inputValidation false']//div[@class = 'react-datepicker__input-container']//input");
    private final By rgrInvoiceDate = By.xpath("//div[@class = 'form-select delivery-date inputValidation invalidInput']//div[@class = 'react-datepicker__input-container']//input");
    private final By rgrProofOfDeliveryFile = By.xpath("//*[@id='POD']//input[@type='file']");
    private final By rgrSupplierInvoiceFile = By.xpath("//*[@id='SupplierInvoice']//input[@type='file']");
    private final By rgrReference = By.xpath("//*[@id='reference']//input[@type='file']");
    private final By rgrSubmitButton = By.xpath("//button[@class='submitBtn']");
    private final By rgrDraftButton = By.xpath("//button[@class='defaultBtn mx-2']");
    private final By truckNumber = By.xpath("//input[@placeholder='Value']");
    private final By displayedTruckNumberForInbound = By.cssSelector("div[class='info-value'] span");
    private final By addNoteButton = By.xpath("//button[normalize-space()='Add Notes']");
    private final By noteTextField = By.xpath("//textarea[@placeholder='Please provide your notes']");
    private final By displayedNoteText = By.xpath("//p[contains(@class, 'displayNote-text')]");
    private final By saveButton = By.xpath("//button[@class='saveModal']");


    //endregion

    // Constructor
    public AsnNewRequestPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }


    //region Actions/steps (Methods)
    @Step("Fill & submit ASN request then return the business key")
    public AsnNewRequestPage fillAndSubmitASNRequest(String invoiceNum, String deliveryTimeVal, String palletsNum, String batchNum,
                                                     String qty, Boolean fillOptionalFields, String noteText, String value, LocalDate manufacturingDateValue, LocalDate expiryDateValue, LocalDate deliveryDateValue) {
        createNewAsnRequest(invoiceNum, deliveryTimeVal, palletsNum, batchNum, qty, manufacturingDateValue, expiryDateValue, deliveryDateValue);
        if (fillOptionalFields) {
            fillOptionalFields(noteText, value);
            clickSubmitButton();
            copyRequestBusinessKey();
            BackToMyRequests();
        } else {
            clickSubmitButton();
            copyRequestBusinessKey();
            BackToMyRequests();
        }
        return this;
    }
    @Step("Fill & submit DD_ASN request then return the business key")
    public AsnNewRequestPage fillAndSubmitDd_ASNRequest(String invoiceNum, String deliveryTimeVal, String batchNum,
                                                     String qty, Boolean fillOptionalFields, String noteText, String value, LocalDate manufacturingDateValue, LocalDate expiryDateValue, LocalDate deliveryDateValue) {
        createNewDDAsnRequest(invoiceNum, deliveryTimeVal, batchNum, qty, manufacturingDateValue, expiryDateValue, deliveryDateValue);
        if (fillOptionalFields) {
            fillOptionalFields(noteText, value);
            clickSubmitButton();
            copyRequestBusinessKey();
            BackToMyRequests();
        } else {
            clickSubmitButton();
            copyRequestBusinessKey();
            BackToMyRequests();
        }
        return this;
    }

    @Step("Fill & submit RGR request")
    public AsnNewRequestPage fillAndSubmitRgrRequest(String form1Num, String filename, Boolean fillOptionalFields, String noteText) throws InterruptedException {
        createNewRgrRequest(form1Num,filename);
        if (fillOptionalFields) {
            fillNotesField(driver,addNoteButton,noteTextField,saveButton,noteText);
            ClickOnRgrRequestSubmitButton();
            copyRequestBusinessKey();
            BackToMyRequests();
        } else {
            ClickOnRgrRequestSubmitButton();
            copyRequestBusinessKey();
            BackToMyRequests();
        }
        return this;
    }

    @Step("Create new ASN request with Mandatory fields")
    public AsnNewRequestPage fillOptionalFields(String noteText, String value) {
        driver.element().scrollToElement(truckNumber)
                .type(truckNumber, value)
                .scrollToElement(addNoteButton)
                .click(addNoteButton)
                .waitToBeReady(noteTextField)
                .type(noteTextField, noteText)
                .click(saveButton);

        return this;
    }

    @Step("Create new ASN request with mandatory fields")
    public AsnNewRequestPage createNewAsnRequest(
            String invoiceNum,
            String deliveryTimeVal,
            String palletsNum,
            String batchNum,
            String qty,
            LocalDate manufacturingDateValue,
            LocalDate expiryDateValue,
            LocalDate deliveryDateValue) {

        By loader = By.xpath("//*[contains(@class,'loader') or contains(@class,'spinner')]");

        ReportManager.log("Step 0: Wait for ASN page and form to load");
        driver.browser().waitForLazyLoading();

        driver.element()
                .waitUntil(ExpectedConditions.invisibilityOfElementLocated(loader));

        ReportManager.log("Step 1: Enter invoice number and open Delivery Date calendar");
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(invoiceNumber)
                .type(invoiceNumber, invoiceNum)
                .click(deliveryDate);

        ReportManager.log("Step 2: Validate shelf life");
        double shelfLifePercent = ShelfLife.calculateShelfLifePercentage(manufacturingDateValue, expiryDateValue, deliveryDateValue);
        ReportManager.logDiscrete(String.format("Shelf life: %.2f%% (Delivery Date: %s)", shelfLifePercent, deliveryDateValue));
        ShelfLife.ensureValidShelfLife(manufacturingDateValue, expiryDateValue, deliveryDateValue);

        DatePickerHelper datePicker = new DatePickerHelper(driver);
        datePicker.setDateForCalendar(deliveryDate, deliveryDateValue);

        ReportManager.log("Step 3: Select delivery time");
        driver.element()
                .waitToBeReady(deliveryTime)
                .select(deliveryTime, deliveryTimeVal);

        ReportManager.log("Step 4: Wait for available pallets to update");
        numOfPallets = driver.element()
                .waitForTextToChange(availNoOfPallets, "0")
                .getText(availNoOfPallets);

        ReportManager.log("Step 5: Fill pallets, batch, and quantity details");
        driver.element()
                .type(noOfPalletsText, palletsNum)
                .click(tradeCodeRowExpander)
                .click(batchRowExpander)
                .type(batchNumber, batchNum)
                .type(quantity, qty);

        ReportManager.log("Step 6: Set Manufacturing and Expiry Dates");
        datePicker.setDateForCalendar(GetCalendarByTitle("MANUFACTURING DATE"), manufacturingDateValue);
        datePicker.setDateForCalendar(GetCalendarByTitle("EXPIRY DATE"), expiryDateValue);

        ReportManager.log("✅ ASN request form filled successfully with validated shelf life data");
        return this;
    }



    @Step("Get the request business Key")
    public AsnNewRequestPage copyRequestBusinessKey() {
        businessKey = driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(requestBusinessKey).
                getText(requestBusinessKey);
        return this;
    }

    @Step("Get the request business Key")
    public AsnNewRequestPage catchDraftedRequestBusinessKeyFromToaster() {
        // Wait for the toaster message and get its text
        String toasterMsg = driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(draftedRequestBusinessKey)
                .getText(draftedRequestBusinessKey)
                .trim();

        ReportManager.logDiscrete("📢 Toaster message: " + toasterMsg);

        // Regex pattern to extract business key (handles 3–6 digits in the last section)
        Pattern pattern = Pattern.compile("(\\d{4}-\\d{6}-\\d{3,6})");
        Matcher matcher = pattern.matcher(toasterMsg);

        if (matcher.find()) {
            draftedBusinessKey = matcher.group(1);
            ReportManager.logDiscrete("✅ Captured business key: " + draftedBusinessKey);
        } else {
            throw new IllegalStateException("❌ No business key found in toaster message: " + toasterMsg);
        }

        return this;
    }

    @Step("Click on 'Back To My Requests' button")
    public AsnNewRequestPage BackToMyRequests() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(backToMyRequestsButton)
                .click(backToMyRequestsButton);
        return this;
    }

    @Step("Click on Submit button")
    public AsnNewRequestPage clickSubmitButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(submitButton)
                .click(submitButton);
        return this;
    }

    @Step("Click on Draft button")
    public AsnNewRequestPage clickDraftButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(draftButton)
                .click(draftButton);
        return this;
    }

    @Step("Read The available number of pallets on a specific date")
    public String readAvailableNumOfPallets() {

        return driver.element()
                .waitForTextToChange(availNoOfPallets, "0")
                .getText(availNoOfPallets);
    }

    @Step("Open ASN request and fill delivery date and Time")
    public AsnNewRequestPage openAsnRequestPageAndFillDeliveryDateAndTime(String invoiceNum, String deliveryTimeVal,LocalDate deliveryDateValue) {

        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(invoiceNumber)
                .type(invoiceNumber, invoiceNum)
                .click(deliveryDate);
        DatePickerHelper datePicker = new DatePickerHelper(driver);
        datePicker.setDateForCalendar(deliveryDate, deliveryDateValue);
        driver.element().select(deliveryTime, deliveryTimeVal);

        return this;
    }

    @Step("Create new DD ASN request with Mandatory fields")
    public AsnNewRequestPage createNewDDAsnRequest(
            String invoiceNum,
            String deliveryTimeVal,
            String batchNum,
            String qty,
            LocalDate manufacturingDateValue,
            LocalDate expiryDateValue,
            LocalDate deliveryDateValue) {

        ReportManager.log("Step 1: Enter invoice number and open Delivery Date calendar");
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(invoiceNumber)
                .type(invoiceNumber, invoiceNum)
                .click(deliveryDate);

        ReportManager.log("Step 2: Validate shelf life");
        double shelfLifePercent = ShelfLife.calculateShelfLifePercentage(manufacturingDateValue, expiryDateValue, deliveryDateValue);
        ReportManager.logDiscrete(String.format("Shelf life: %.2f%% (Delivery Date: %s)", shelfLifePercent, deliveryDateValue));
        ShelfLife.ensureValidShelfLife(manufacturingDateValue, expiryDateValue, deliveryDateValue);

        DatePickerHelper datePicker = new DatePickerHelper(driver);
        datePicker.setDateForCalendar(deliveryDate, deliveryDateValue);


        ReportManager.log(" Step 3: Select delivery time");
        driver.element()
                .waitToBeReady(deliveryTime)
                .select(deliveryTime, deliveryTimeVal);

        ReportManager.log(" Step 4: Fill batch, and quantity details");
        driver.element()
                .click(tradeCodeRowExpander)
                .click(batchRowExpander)
                .type(batchNumber, batchNum)
                .type(quantity, qty);

        ReportManager.log(" Step 5: Set Manufacturing and Expiry Dates");
        datePicker.setDateForCalendar(GetCalendarByTitle("MANUFACTURING DATE"), manufacturingDateValue);
        datePicker.setDateForCalendar(GetCalendarByTitle("EXPIRY DATE"), expiryDateValue);

        return this;
    }

    @Step("Create new RGR based on DD ASN request with Mandatory fields")
    public AsnNewRequestPage createNewRgrRequest(String form1Num, String fileName) throws InterruptedException {

        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(rgrForm1Number);
        elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), rgrForm1Number, "");
        driver.element()
                .clear(rgrForm1Number)
                .typeSecure(rgrForm1Number, form1Num)
                .keyPress(rgrForm1Number, Keys.ENTER)
                .click(rgrDeliveryDate); // Open the delivery date calendar widget

        checkCalendarEnabledDays();// Related to selecting to Delivery date
        driver.element()
                .click(firstEnabledCalenderDay) //click on the first enabled date
                .click(rgrInvoiceDate);

        checkCalendarEnabledDays();// Related to selecting to Invoice date
        driver.element()
                .click(firstEnabledCalenderDay)
                .typeFileLocationForUpload(rgrProofOfDeliveryFile, FileActions.getInstance().getAbsolutePath(SHAFT.Properties.paths.testData(), fileName));
        Thread.sleep(10000);

        driver.element().typeFileLocationForUpload(rgrSupplierInvoiceFile, FileActions.getInstance().getAbsolutePath(SHAFT.Properties.paths.testData(), fileName));
        Thread.sleep(10000);

        return this;
    }

    @Step("Click on RGR request Submit button")
    public AsnNewRequestPage ClickOnRgrRequestSubmitButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(rgrSubmitButton)
                .click(rgrSubmitButton);
        return this;
    }

    @Step("Click on RGR request Draft button")
    public AsnNewRequestPage ClickOnRgrRequestDraftButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(rgrDraftButton)
                .click(rgrDraftButton);
        return this;
    }

    //endregion

    // region Helpers

    /**
     * Gets the calendar locator by its title. (will be replaced once Dev add elements identifiers )
     *
     * @param title the title of the calendar (MANUFACTURING DATE or EXPIRY DATE )
     * @return the calendar locator
     */
    public By GetCalendarByTitle(String title) {
        List<WebElement> headers = driver.getDriver().findElements(By.xpath("//table[@class='batches-holder']//tr//th"));

        int columnIndex = -1;

        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getText().equalsIgnoreCase(title)) {
                columnIndex = i + 1;
                break;
            }
        }

        if (columnIndex == -1) {
            throw new NoSuchElementException("Column with title '" + title + "' not found");
        }

        return By.xpath("//table[@class='batches-holder']//tr//td[" + columnIndex
                + "]//input");
    }


    /**
     * Go to Next month if the current displayed month Doesn't have enabled days
     * Sample: running on 28/11/2024, The upcoming displayed dates 29,30 both are weekends
     */
    public void checkCalendarEnabledDays() {

        // Check for enabled days in the current month
        List<WebElement> enabledDays = driver.getDriver().findElements(enabledCalenderDays);

        if (enabledDays.isEmpty()) {
            // If no enabled days, navigate to the next month
            driver.element().click(nextMonthButton);

        }
    }

    //endregion


    // region Validations
    @Step("Validate the Number of pallets is updated after request creation")
    public AsnNewRequestPage ValidateNewNumOfPalletsValue(int initialNumOfPallets, int updatedNumOfPallets, int requestNumOfPallets) {
        Validations.verifyThat()
                .number(updatedNumOfPallets)
                .isEqualTo(initialNumOfPallets - requestNumOfPallets)
                .perform();

        return this;
    }

    @Step("Validate the Number of pallets is updated after request rejected By inbound")
    public AsnNewRequestPage ValidateNumOfPalletsAreUpdatedAfterInboundRejection(int initialNumOfPallets, int updatedNumOfPallets) {
        Validations.verifyThat()
                .number(updatedNumOfPallets)
                .isEqualTo(initialNumOfPallets)
                .perform();

        return this;
    }

    @Step("Validate that the Value of Trucks is displayed and match what already inserted")
    public AsnNewRequestPage ValidateThatTheValueOfTrucksIsDisplayedForAsn(String insertedNumber) {
        By truckNumber = By.xpath("//input[@placeholder='Value']");

        for (int i = 0; i < 3; i++) {
            try {
                driver.element().waitUntilPresenceOfAllElementsLocatedBy(truckNumber);
                driver.element().assertThat(truckNumber).text().isEqualTo(insertedNumber);
                break; // Exit loop if successful
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                driver.element().switchToIframe(By.xpath("//iframe[@id='frame_id']"));
                driver.element().assertThat(truckNumber).text().isEqualTo(insertedNumber);
                driver.element().switchToDefaultContent();

            }
        }

        return this;
    }

    @Step("Validate that the Value of Trucks is displayed and match what already inserted")
    public AsnNewRequestPage ValidateThatTheValueOfTrucksIsDisplayedForInbound(String insertedNumber) {
        driver.element().assertThat(displayedTruckNumberForInbound).text().isEqualTo(insertedNumber);
        return this;
    }

    @Step("Validate that the data of note is displayed and match what already inserted")
    public AsnNewRequestPage ValidateThatTheDataOfNoteIsDisplayed(String noteData) {
        driver.element().waitUntilPresenceOfAllElementsLocatedBy(displayedNoteText)
                .scrollToElement(displayedNoteText);
        String actualText = driver.element().getText(displayedNoteText).trim();
        String expectedText = noteData.trim();
        Assertions.assertEquals(expectedText, actualText, "Validation failed: Text does not match");
        return this;
    }
// endregion
}
