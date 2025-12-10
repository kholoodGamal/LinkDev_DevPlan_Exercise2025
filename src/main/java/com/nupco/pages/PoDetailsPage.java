package com.nupco.pages;

import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import com.shaft.validation.Validations;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;

import static com.nupco.utils.helper.*;

public class PoDetailsPage {

    //region Variables

    private SHAFT.GUI.WebDriver driver;
    //endregion

    //region Locators
    private final By requestASNButton = By.xpath("//button[normalize-space()='Request blanket ASN' or normalize-space()='Request ASN']");
    private final By requestDDASNButton = By.xpath("//button[text()='Request Direct Delivery ASN']");
    private final By firstLineItem = By.xpath("(//tbody//input[@class='check-input' and @type='checkbox'])[position() = '1']");
    private final By firstLineItemBookedQty = By.xpath("(//td[contains(@data-title, 'BOOKED')])[1]");
    private final By firstLineItemRemainingQty = By.xpath("(//td[@data-title = 'REMAINING QTY'])[position() = '1']");
    private final By escapeAnnouncementButton = By.xpath("//div[@class='modal-header']/span");

    //endregion

    // Constructor
    public PoDetailsPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    //region Actions/steps (Methods)

    @Step("Select first PO line item")
    public PoDetailsPage selectFirstPoLineItems(){

        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(firstLineItem)
                .click(firstLineItem);

        return this;

    }

    @Step("Start ASN Request Creation")
    public PoDetailsPage startAsnRequestCreation() {
        selectFirstPoLineItems();
        clickOnRequestAsnButton();
        return this ;
    }

    @Step("Click on Request ASN Button")
    public PoDetailsPage clickOnRequestAsnButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(requestASNButton)
                .click(requestASNButton);

        return this;
    }

    @Step("Click on Request DD ASN Button")
    public PoDetailsPage clickOnRequestDDAsnButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(requestDDASNButton)
                .click(requestDDASNButton);

        return this;
    }

    public int readBookedQtyValue() throws ParseException {
        WebDriverWait wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(30));

        try {
            // Wait for the table cell to appear and be visible
            wait.until(ExpectedConditions.presenceOfElementLocated(firstLineItemBookedQty));
            wait.until(ExpectedConditions.visibilityOfElementLocated(firstLineItemBookedQty));

            // Get the text and convert it
            String bookedQtyText = driver.element().getText(firstLineItemBookedQty);
            if (bookedQtyText == null || bookedQtyText.isBlank()) {
                throw new IllegalStateException("Booked Qty text is empty or null.");
            }

            Number number = NumberFormat.getNumberInstance().parse(bookedQtyText);
            int value = number.intValue();
            ReportManager.logDiscrete("Booked Qty value read successfully: " + value);
            return value;

        } catch (TimeoutException e) {
            throw new AssertionError("Timed out waiting for BOOKED QTY cell to appear. Locator: " + firstLineItemBookedQty, e);
        } catch (NoSuchElementException e) {
            throw new AssertionError("Could not find BOOKED QTY cell even after waiting.", e);
        } catch (ParseException e) {
            throw new AssertionError("Failed to parse Booked Qty value. Ensure it’s numeric and formatted correctly.", e);
        }
    }


    @Step("Read Remaining Qty value")
    public int readRemainingQtyValue() throws ParseException {
        return convertStringOfNumberWithCommasToInteger(driver, firstLineItemRemainingQty);
    }

    @Step("Click on escape Announcement Button")
    public PoDetailsPage clickEscapeAnnouncementButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(escapeAnnouncementButton)
                .click(escapeAnnouncementButton);
        return this;
    }

    //endregion

    //region Validations

    @Step("Validate the Booking Qty is updated with the new booked value")
    public PoDetailsPage ValidateNewBookingQtyValue(int initialBookingQty, int updatedBookingQty, int Qty){
        Validations.verifyThat()
                .number(updatedBookingQty)
                .isEqualTo(initialBookingQty + Qty)
                .perform();

        return this;
    }
    @Step("Validate the Booking Qty is deducted with the new booked value After Rejection Or Cancellation of Request")
    public PoDetailsPage    ValidateNewBookingQtyValueAfterRequestRejection(int initialBookingQty, int bookingQtyAfterRejection){
        Validations.verifyThat()
                .number(bookingQtyAfterRejection)
                .isEqualTo(initialBookingQty)
                .perform();

        return this;
    }

    @Step("Validate the Remaining Qty is updated with the new booked value")
    public PoDetailsPage ValidateNewRemainingQtyValue(int initialRemainingQty, int updatedRemainingQty, int Qty){
        Validations.verifyThat()
                .number(updatedRemainingQty)
                .isEqualTo(initialRemainingQty - Qty)
                .perform();

        return this;
    }


    //endregion
}
