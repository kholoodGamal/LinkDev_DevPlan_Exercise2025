package com.nupco.pages;

import com.shaft.driver.SHAFT;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.nupco.utils.helper.*;

public class ReturnOutboundNewRequestPage {

    // region Variables

    private SHAFT.GUI.WebDriver driver;
    String draftedBusinessKey;
    WebDriverWait wait;

    // endregion

    //region Locators

    private final By deliveryDate = By.xpath("//div[@class = 'react-datepicker__input-container']//input");
    private final By returnReasonInput = By.xpath("//select[contains(@class,'color-gray form-select form-select-lg')]");
    private final By requestedQtyField = By.xpath("(//input[@title= 'Returned Qty'])[1]");
    private final By enabledCalenderDays = By.xpath("//div[@class='react-datepicker__week']//div[@aria-disabled='false']"); //Get whole enabled dates
    private final By firstEnabledCalenderDay = By.xpath("(//div[@class='react-datepicker__week']//div[@aria-disabled='false'])[1]"); //Get the first enabled date
    private final By nextMonthButton = By.xpath("//button[@aria-label='Next Month']"); // Calendar next month button
    private final By submitRequestButton = By.xpath("//button[@class='submitBtn']");
    private final By validationErrorMessage = By.xpath("//div[@class='validationMsg']/i[@class='bi bi-exclamation-circle-fill']");
    private final By draftButton = By.xpath("//button[normalize-space()='Draft']");
    private final By draftedRequestBusinessKey = By.xpath("(//div[@role='alert' and @class='Toastify__toast-body']//div)[2]");
    private final By disableRequestedQtyField = By.xpath("(//input[contains(@class,'dtd-qty-table__tbody-qty-input') and @disabled])[1]");
    private final By addNoteButton = By.xpath("//button[contains(@class,'addNoteBtn')]");
    private final By noteContent = By.xpath("//textarea[@rows='3' and @aria-label='maximum height' and (contains(@placeholder,'description') or contains(@placeholder,'notes'))]");
    private final By submitButtonOfNote = By.xpath("//button[@class='saveModal' and (normalize-space(text())='Save' or normalize-space(text())='Submit')]");
    private final By uploadFile = By.xpath("//input[@type='file']");


    // endregion

    // Constructor
    public ReturnOutboundNewRequestPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(30));
    }

    // region Actions/steps (Methods)
    @Step("Fill return outbound request Data Without submitting")
    public ReturnOutboundNewRequestPage fillReturnOutboundRequestData(
            String requestedQty,
            String returnReason,
            Boolean fillOptionalNotesFields,
            String OrderNoteText,
            String filePath) {

        driver.element().click(deliveryDate);
        checkCalendarEnabledDays(driver, enabledCalenderDays, nextMonthButton);
        driver.element().click(firstEnabledCalenderDay)
                .click(returnReasonInput).select(returnReasonInput, returnReason);

        // Wait and fix available Qty loader BEFORE submit
        int retries = 5;
        while (retries-- > 0) {
            if (isElementPresent(disableRequestedQtyField, driver)) {
                handleLiveAvailableQtyLoader(driver, deliveryDate, enabledCalenderDays,
                        nextMonthButton, firstEnabledCalenderDay);

                // Small pause to allow DOM refresh
                driver.browser().waitForLazyLoading(); // or use sleep(500) if no lazy load helper

                // Re-wait for element to appear fresh
                driver.element()
                        .click(returnReasonInput).select(returnReasonInput, returnReason);
            } else {
                break;
            }
        }

        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(requestedQtyField)
                .waitToBeReady(requestedQtyField)
                .clear(requestedQtyField).type(requestedQtyField, requestedQty);

        if (fillOptionalNotesFields) {
            fillOptionalFields(OrderNoteText, filePath);
        }
        return this;
    }

    @Step("Fill & submit return outbound request then return the business key")
    public ReturnOutboundNewRequestPage fillAndSubmitReturnOutboundRequest(
            String requestedQty, String returnReason, Boolean fillOptionalNotesFields,
            String OrderNoteText, String filePath) {
        fillReturnOutboundRequestData(requestedQty, returnReason, fillOptionalNotesFields, OrderNoteText,filePath);
        clickSubmitButton(driver, submitRequestButton);

        return this;
    }


    @Step("Fill & Draft Return outbound request then return the business key")
    public ReturnOutboundNewRequestPage fillAndDraftReturnOutboundRequest(
            String requestedQty, String returnReason, Boolean fillOptionalNotesFields,
            String OrderNoteText, String filePath) {
        fillReturnOutboundRequestData(requestedQty, returnReason, fillOptionalNotesFields, OrderNoteText,filePath);
        clickDraftButton();

        return this;
    }


    @Step("Get the return outbound request business Key")
    public ReturnOutboundNewRequestPage catchDraftedRequestBusinessKeyFromToaster() {
        //wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(30));
        wait.until(ExpectedConditions.presenceOfElementLocated(draftedRequestBusinessKey));
        // Wait for toaster and get its full text
        String fullMessage = driver.element().getText(draftedRequestBusinessKey);

        // Extract only the business key using regex
        draftedBusinessKey = fullMessage.replaceFirst(".*\\s(\\d{4}-\\d{6}-\\d{4,6})$", "$1");

        // Debug log
        System.out.println("Extracted Business Key: " + draftedBusinessKey);

        return this;
    }

    public String getDraftedBusinessKey() {
        return draftedBusinessKey;
    }

    @Step("Create new Return OutBound request with optional note field")
    public ReturnOutboundNewRequestPage fillOptionalFields(String noteText, String filePath) {
        driver.browser().waitForLazyLoading()
                .element().scrollToElement(uploadFile)
                .typeFileLocationForUpload(uploadFile, filePath)
                .scrollToElement(addNoteButton)
                .click(addNoteButton)
                .type(noteContent, noteText)
                .click(submitButtonOfNote);

        return this;
    }


    @Step("Click on Draft button")
    public ReturnOutboundNewRequestPage clickDraftButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(draftButton)
                .click(draftButton);
        return this;
    }


}


// endregion

// region Actions/steps (Methods)

