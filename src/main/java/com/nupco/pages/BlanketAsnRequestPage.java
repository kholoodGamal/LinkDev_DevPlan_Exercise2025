package com.nupco.pages;

import com.nupco.utils.DatePickerHelper;
import com.nupco.utils.ShelfLife;
import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import com.shaft.validation.Validations;
import io.qameta.allure.Step;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

public class BlanketAsnRequestPage {
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
    private final By invoiceNumber = By.xpath("//input[@class='batches-inputs ' and @placeholder='Invoice Number']");
    private final By deliveryTime = By.xpath("//select[@class='  form-select']");
    private final By availNoOfPallets = By.xpath("//span[@class='pallets-num']");
    private final By tradeCodeRowExpander = By.xpath("//button[@class='btn border' and @data-testid='showAccordion']");
    private final By batchRowExpander = By.xpath("(//button[@class='btn border'])[2]");
    private final By batchNumber = By.xpath("//input[@placeholder='Batch Number']");
    private final By quantity = By.xpath("//input[@class='batches-inputs quantity-input  ']");
    private final By requestBusinessKey = By.xpath("//div[@class='modal-content']//span[@class='request-numb']");
    private final By backToMyRequestsButton = By.xpath("(//button[@class = 'btns modal-actions'])[1]");
    private final By truckNumber = By.xpath("//input[@placeholder='Value']");
    private final By displayedTruckNumberForInbound = By.cssSelector("div[class='info-value'] span");
    private final By addNoteButton = By.xpath("//button[normalize-space()='Add Notes']");
    private final By noteTextField = By.xpath("//textarea[@placeholder='Please provide your notes']");
    private final By displayedNoteText = By.xpath("//p[@class='displayNote-text']/span");
    private final By saveButton = By.xpath("//button[normalize-space()='Save']");
    private final By deliveryDate = By.xpath("//div[@class = 'form-select delivery-date inputValidation false  ']//input");
    private final By shippedToDropdown = By.xpath("//div[contains(@class, 'custom-search-select')]//input[@id='react-select-2-input']");
    private final By enabledCalenderDays = By.xpath("//div[@class='react-datepicker__week']//div[@aria-disabled='false']"); //Get whole enabled dates
    private final By firstEnabledCalenderDay = By.xpath("(//div[@class='react-datepicker__week']//div[@aria-disabled='false'])[1]");
    private final By nextMonthButton = By.xpath("//button[@aria-label='Next Month']");


    //endregion

    // Constructor
    public BlanketAsnRequestPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    @Step("Fill & submit ASN request then return the business key")
    public By getDatePickerInputByIndex(int index) {
        String xpath = "(//div[contains(@class, 'react-datepicker-wrapper')]//input[@type='text'])[" + index + "]";
        return By.xpath(xpath);
    }


    //region Actions/steps (Methods)
    @Step("Fill & submit ASN request then return the business key")
    public BlanketAsnRequestPage fillAndSubmitBlanketASNRequest(String invoiceNum, String location, String deliveryTimeVal, String batchNum,
                                                                String qty, Boolean fillOptionalFields, String noteText, String value,
                                                                LocalDate manufacturingDateValue,
                                                                LocalDate expiryDateValue,
                                                                LocalDate deliveryDateValue) {
        createNewBlanketAsnRequest(invoiceNum, location, deliveryTimeVal, batchNum, qty,
                manufacturingDateValue,
                expiryDateValue,
                deliveryDateValue);
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

    @Step("Create new ASN request with Mandatory fields")
    public BlanketAsnRequestPage fillOptionalFields(String noteText, String value) {

        driver.element().scrollToElement(truckNumber)
                .type(truckNumber, value)
                .scrollToElement(addNoteButton)
                .click(addNoteButton)
                .type(noteTextField, noteText)
                .click(saveButton);


        return this;
    }

    @Step("Create new ASN request with Mandatory fields")
    public BlanketAsnRequestPage createNewBlanketAsnRequest(
            String invoiceNum,
            String location,
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

        ReportManager.log(" Step 3: Fill All Mandatory Fields");
        driver.element()
                .waitToBeReady(deliveryTime)
                .select(deliveryTime, deliveryTimeVal)
                .click(tradeCodeRowExpander)
                .click(batchRowExpander)
                .type(batchNumber, batchNum)
                .type(quantity, qty);
        ReportManager.log(" Step 4: Set Manufacturing and Expiry Dates");
        datePicker.setDateForCalendar(GetCalendarByTitle("MANUFACTURING DATE"), manufacturingDateValue);
        datePicker.setDateForCalendar(GetCalendarByTitle("EXPIRY DATE"), expiryDateValue);
        driver.element().click(shippedToDropdown)
                .type(shippedToDropdown, location)
                .waitToBeReady(shippedToDropdown)
                .keyPress(shippedToDropdown, Keys.ENTER);


        return this;
    }

    @Step("Get the request business Key")
    public BlanketAsnRequestPage copyRequestBusinessKey() {
        businessKey = driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(requestBusinessKey).
                getText(requestBusinessKey);
        return this;
    }

    @Step("Click on 'Back To My Requests' button")
    public BlanketAsnRequestPage BackToMyRequests() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(backToMyRequestsButton)
                .click(backToMyRequestsButton);
        return this;
    }

    @Step("Click on Submit button")
    public BlanketAsnRequestPage clickSubmitButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(submitButton)
                .click(submitButton);
        return this;
    }

    @Step("Click on Draft button")
    public BlanketAsnRequestPage clickDraftButton() {
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

    //endregion


    // region Validations
    @Step("Validate the Number of pallets is updated after request creation")
    public BlanketAsnRequestPage ValidateNewNumOfPalletsValue(int initialNumOfPallets, int updatedNumOfPallets, int requestNumOfPallets) {
        Validations.verifyThat()
                .number(updatedNumOfPallets)
                .isEqualTo(initialNumOfPallets - requestNumOfPallets)
                .perform();

        return this;
    }

    @Step("Validate the Number of pallets is updated after request rejected By inbound")
    public BlanketAsnRequestPage ValidateNumOfPalletsAreUpdatedAfterInboundRejection(int initialNumOfPallets, int updatedNumOfPallets) {
        Validations.verifyThat()
                .number(updatedNumOfPallets)
                .isEqualTo(initialNumOfPallets)
                .perform();

        return this;
    }

    @Step("Validate that the Value of Trucks is displayed and match what already inserted")
    public BlanketAsnRequestPage ValidateThatTheValueOfTrucksIsDisplayedForAsn(String insertedNumber) {
        driver.element().assertThat(truckNumber).text().isEqualTo(insertedNumber);
        return this;
    }

    @Step("Validate that the Value of Trucks is displayed and match what already inserted")
    public BlanketAsnRequestPage ValidateThatTheValueOfTrucksIsDisplayedForInbound(String insertedNumber) {
        driver.element().assertThat(displayedTruckNumberForInbound).text().isEqualTo(insertedNumber);
        return this;
    }

    @Step("Validate that the data of note is displayed and match what already inserted")
    public BlanketAsnRequestPage ValidateThatTheDataOfNoteIsDisplayed(String noteData) {
        String actualText = driver.element().getText(displayedNoteText).trim();
        String expectedText = noteData.trim();
        Assertions.assertEquals(expectedText, actualText, "Validation failed: Text does not match");
        return this;
    }
}
