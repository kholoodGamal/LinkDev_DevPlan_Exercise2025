package com.nupco.pages;

import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.nupco.utils.helper.*;

public class OutboundNewRequestPage {

    // region Variables

    private SHAFT.GUI.WebDriver driver;
    String draftedBusinessKey;
    WebDriverWait wait ;

    // endregion

    //region Locators

    private final By deliveryDate = By.xpath("//div[@class = 'react-datepicker__input-container']//input");
    private final By requestedQtyField = By.xpath("(//input[contains(@id, 'qutInput')])[1]");
    private final By enabledCalenderDays = By.xpath("//div[@class='react-datepicker__week']//div[@aria-disabled='false']"); //Get whole enabled dates
    private final By firstEnabledCalenderDay = By.xpath("(//div[@class='react-datepicker__week']//div[@aria-disabled='false'])[1]"); //Get the first enabled date
    private final By nextMonthButton = By.xpath("//button[@aria-label='Next Month']"); // Calendar next month button
    private final By submitOutboundButton = By.xpath("//button[@class='submitBtn']");
    private final By resubmitOutboundButton = By.xpath("//button[@class='btn btn-primary']");
    private final By validationErrorMessage = By.xpath("//div[@class='validationMsg']/i[@class='bi bi-exclamation-circle-fill']");
    private final By draftButton = By.xpath("//button[@class='draftBtn']");
    private final By draftedRequestBusinessKey = By.xpath("(//div[contains(., 'The Request Saved As Draft Successfully')])[last()]");
    private final By availableQtyLoading = By.id("//span[@class = 'loader']");
    private final By addNewItem = By.xpath("//button[@class='editBtn btn d-flex align-items-center false btn btn-primary']");
    private final By addItemCommentButton = By.xpath("//button[contains(@class,'btn-add-item-comment')]");
    private final By addNoteButton = By.xpath("//button[contains(@class,'addNoteBtn')]");
    private final By noteOrCommentContent = By.xpath("//textarea[@rows='3' and @aria-label='maximum height' and (contains(@placeholder,'description') or contains(@placeholder,'notes'))]");
    private final By saveOrSubmitButtonOfCommentOrNote=By.xpath("//button[@class='saveModal' and (normalize-space(text())='Save' or normalize-space(text())='Submit')]");



    // endregion

    // Constructor
    public OutboundNewRequestPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(30));
    }

    // region Actions/steps (Methods)

    @Step("Fill outbound request Data Without submitting")
    public OutboundNewRequestPage fillOutboundRequestData(
            String requestedQty, Boolean fillOptionalNotesFields,
            String OrderNoteText, String ItemNoteText) {

        driver.element().click(deliveryDate);
        checkCalendarEnabledDays(driver, enabledCalenderDays, nextMonthButton);
        driver.element().click(firstEnabledCalenderDay);

        int retries = 5;
        while (retries-- > 0) {
            if (isElementPresent(validationErrorMessage, driver)) {
                ReportManager.logDiscrete("Validation warning detected. Retrying to handle live loader...");

                handleLiveAvailableQtyLoader(driver, deliveryDate, enabledCalenderDays,
                        nextMonthButton, firstEnabledCalenderDay);
            }
            driver.element()
                    .waitUntilPresenceOfAllElementsLocatedBy(requestedQtyField)
                    .waitToBeReady(requestedQtyField)
                    .type(requestedQtyField, requestedQty);
            if (!isElementPresent(validationErrorMessage, driver)) {
                break;
            }

            if (retries == 0) {
                ReportManager.logDiscrete("Max retries reached. Validation error still present.");
            }
        }

        if (fillOptionalNotesFields) {
            fillOptionalFields(OrderNoteText, ItemNoteText);
        }

        return this;
    }


    @Step("Fill & submit outbound request")
    public OutboundNewRequestPage fillAndSubmitOutboundRequest(
            String requestedQty, Boolean fillOptionalNotesFields,
            String OrderNoteText, String ItemNoteText) {
        fillOutboundRequestData(requestedQty ,fillOptionalNotesFields,OrderNoteText,ItemNoteText);
        clickSubmitButton(driver ,submitOutboundButton);

        return this;
    }



    @Step("Create new OutBound request with optional fields")
    public OutboundNewRequestPage fillOptionalFields(String noteText, String itemNoteText) {

        driver.element().click(addItemCommentButton)
                .type(noteOrCommentContent,itemNoteText)
                .click(saveOrSubmitButtonOfCommentOrNote)
                .scrollToElement(addNoteButton)
                .click(addNoteButton)
                .type(noteOrCommentContent,noteText)
                .click(saveOrSubmitButtonOfCommentOrNote);

        return this;
    }


    @Step("Click on Draft button")
    public OutboundNewRequestPage clickDraftButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(draftButton)
                .click(draftButton);
        return this;
    }
    @Step("Get the request business Key")
    public OutboundNewRequestPage catchDraftedRequestBusinessKeyFromToaster() {
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
    @Step("Click on Submit button")
    public OutboundNewRequestPage clickOnSubmitButtonInCaseSubmittedDraftedRequest() {
        //driver.browser().refreshCurrentPage()
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(submitOutboundButton)
                .scrollToElement(submitOutboundButton)
                .waitToBeReady(submitOutboundButton)
                .click(submitOutboundButton);
        return this;
    }

    public String getDraftedBusinessKey() {
        return draftedBusinessKey;
    }

    @Step("Edit the Requested Qty field and resubmit the request")
    public OutboundNewRequestPage editAndReSubmitOutboundRequest(String requestedQty) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(availableQtyLoading));
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(requestedQtyField)
                .clear(requestedQtyField)
                .type(requestedQtyField, requestedQty);
        clickSubmitButton(driver ,resubmitOutboundButton);

        return this;
    }

 // endregion

}



