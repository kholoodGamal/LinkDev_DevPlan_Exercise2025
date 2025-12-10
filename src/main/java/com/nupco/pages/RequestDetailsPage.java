package com.nupco.pages;

import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import com.shaft.validation.Validations;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.time.Duration;
import java.util.List;

public class RequestDetailsPage {

    //region Variables

    private SHAFT.GUI.WebDriver driver;
    WebDriverWait driverWait;
    Wait<SHAFT.GUI.WebDriver> wait;
    SoftAssert softAssert;


    //endregion

    //region Locators
    private final By collapsedStatus = By.cssSelector("button.accordion-button.collapsed");
    private final By deleteDraftButton = By.xpath("//button[normalize-space()='Delete Draft']");
    private final By cancelButton = By.xpath("//button[@data-testid = 'handleCancelRgrAction']");
    private final By cancellationConfirmButton = By.xpath("//button[@class='submitBtn']");
    private final By requestRgrButton = By.xpath("//button[@data-testid = 'requestRgrAction']");
    private final By rgrRequestsStatus = By.xpath("//p[contains(@class, 'status')]");
    private final By rgrDeleteDraftButton = By.xpath("//button[@class='rejectBtn defaultBtn']");
    private final By changeIRBtn = By.xpath("//button[normalize-space()='Update IR number']");
    private final By invNumber = By.xpath("//input[@placeholder='Invoice Number']");
    private final By invNumberUpdated = By.xpath("//input[@title='supplier Invoice']");
    private final By inNumSuccessMessage = By.xpath("//h3[normalize-space()='Your Request Has Been Sent']");
    private final By cancelOutboundBtn = By.xpath("//button[@class='rejectBtn']");
    private final By confirmCancelOutboundBtn = By.xpath("//button[@class='submitBtn']");
    private final By cancellationConfirmPopup = By.xpath("//div[@class ='Toastify__toast-body']");
    private final By draftButton = By.xpath("//button[@class='draftBtn' or @class='defaultBtn']");
    private final By addItemCommentButton = By.xpath("(//button[contains(@class,'btn-add-item-comment')])[1]");
    private final By displayedNote = By.xpath("//p[@class='displayNote-text']");
    private final By CommentContent = By.xpath("//textarea[@rows='3' and @aria-label='maximum height' and (contains(@placeholder,'description') or contains(@placeholder,'notes'))]");
    private final By cancelButtonOfItemNote = By.xpath("//button[@class='cancelModal' and text()='Cancel']");

    //endregion

    // Constructor
    public RequestDetailsPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
        driverWait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(30));
        wait =
                new FluentWait<>(driver)
                        .withTimeout(Duration.ofMinutes(2))
                        .pollingEvery(Duration.ofSeconds(2))
                        .ignoring(StaleElementReferenceException.class);
        softAssert = new SoftAssert();
    }

    //region Validations
    @Step("Validate request status is {expectedStatus}")
    public RequestDetailsPage validateRequestStatus(String expectedStatus) {
        WebDriver driverInstance = driver.getDriver();
        driverWait = new WebDriverWait(driverInstance, Duration.ofSeconds(30));

        try {
            WebElement accordionBtn = driverInstance.findElement(collapsedStatus);
            // Click only if not already expanded
            if ("false".equals(accordionBtn.getAttribute("aria-expanded"))) {
                accordionBtn.click();
                ReportManager.logDiscrete("✅ Expanded Status accordion.");
            } else {
                ReportManager.logDiscrete("Status accordion already expanded.");
            }
        } catch (NoSuchElementException e) {
            ReportManager.logDiscrete("⚠️ Status accordion button not found, maybe already expanded.");
        }

        // 2️⃣ Status element XPath
        By statusLocator = By.xpath(
                "//p[@data-testid='requestStatus']" +
                        "|//span[contains(@class,'statusLabel')]" +
                        "|//p[contains(@class,'custom-badge') and ancestor::div[p[text()='Status']]]" +
                        "|//p[contains(@class,'col-value') and contains(@class,'custom-badge') and ancestor::div[p[text()='Status']]]" +
                        "|//p[contains(@class,'status') and ancestor::div[p[text()='Status']]]"
        );

        String actualStatus = "";

        // 3️⃣ Try Selenium visible element first
        try {
            driverWait.until(ExpectedConditions.visibilityOfElementLocated(statusLocator));
            for (WebElement el : driverInstance.findElements(statusLocator)) {
                if (el.isDisplayed()) {
                    String text = el.getText().trim();
                    if (!text.isEmpty() && !text.equals("-")) {
                        actualStatus = text;
                        break;
                    }
                }
            }
        } catch (TimeoutException ignored) {
            ReportManager.logDiscrete("⚠️ Selenium could not locate visible status element.");
        }

        // 4️⃣ JS fallback (works even if collapsed or hidden)
        if (actualStatus.isEmpty()) {
            try {
                actualStatus = (String) ((JavascriptExecutor) driverInstance)
                        .executeScript(
                                "return document.evaluate(" +
                                        "'//div[p[text()=\"Status\"]]/p[contains(@class,\"custom-badge\")]', " +
                                        "document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)" +
                                        ".singleNodeValue?.textContent.trim() || '';"
                        );
                ReportManager.logDiscrete("🔹 JS fallback retrieved status: " + actualStatus);
            } catch (Exception ignored) {
                ReportManager.logDiscrete("⚠️ JS fallback failed to retrieve status.");
            }
        }

        // 5️⃣ Normalize and assert
        String normalizedExpected = expectedStatus.replaceAll("\\s+", "").toLowerCase();
        String normalizedActual = actualStatus.replaceAll("\\s+", "").toLowerCase();

        ReportManager.logDiscrete("Expected Status: [" + expectedStatus + "]");
        ReportManager.logDiscrete("Actual Status: [" + actualStatus + "]");

        Assert.assertEquals(normalizedActual, normalizedExpected,
                "❌ Status validation failed! (Normalized comparison)");

        return this;
    }


    @Step("Validate submitted businessKey {businessKey} equal the Drafted businessKey {DraftedBusinessKey}")
    public RequestDetailsPage validateSubmittedBusinessKeySameDrafted(String businessKey, String DraftedBusinessKey) {
        Validations.assertThat()
                .object(businessKey)
                .isEqualTo(DraftedBusinessKey)
                .perform();

        return this;
    }

    @Step("Validate RGR request status is {expectedStatus}")
    public RequestDetailsPage validateRgrRequestStatus(String expectedStatus) {
        WebElement requestsStatusElement = wait.until(driver -> {
            WebElement e = driver.getDriver().findElement(rgrRequestsStatus);
            return e.isDisplayed() ? e : null;  // Ensures element is displayed
        });

        softAssert.assertEquals(requestsStatusElement.getText(), expectedStatus);

        return this;
    }


    @Step("Validate RGR request button is not Displayed")
    public RequestDetailsPage validateRgrRequestButtonIsNotDisplayed() {
        Validations.assertThat()
                .element(driver.getDriver(), requestRgrButton)
                .doesNotExist()
                .perform();
        return this;
    }

    //endregion

    // region Steps
    @Step("Click on Delete Draft button")
    public RequestDetailsPage ClickOnDeleteDraftButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(deleteDraftButton)
                .click(deleteDraftButton);
        return this;
    }

    @Step("Click on Cancel button")
    public RequestDetailsPage ClickOnCancelButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(cancelButton)
                .click(cancelButton);
        return this;
    }

    @Step("Click on confirmation button on the Cancel popup")
    public RequestDetailsPage ClickOnCancellationConfirmButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(cancellationConfirmButton)
                .click(cancellationConfirmButton);
        return this;
    }

    @Step("Click on Request RGR button")
    public RequestDetailsPage ClickOnRequestRgrButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(requestRgrButton)
                .click(requestRgrButton);
        return this;
    }

    @Step("Click on RGR Delete Draft button")
    public RequestDetailsPage ClickOnRgrDeleteDraftButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(rgrDeleteDraftButton)
                .click(rgrDeleteDraftButton);
        return this;
    }

    @Step("Input Invoice Number")
    public RequestDetailsPage inputInvNumber(String inputInvNum) {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(invNumber)
                .clear(invNumber)
                .type(invNumber, inputInvNum);
        return this;
    }

    @Step("Press Update IR Number Button")
    public RequestDetailsPage updateIRNumber() {
        driver.element().waitUntilPresenceOfAllElementsLocatedBy(changeIRBtn).click(changeIRBtn);
        return this;
    }

    @Step("Validate That invoice number success message is displayed")
    public RequestDetailsPage validateInvNumberMessageDisplayed() {
        driverWait.until(ExpectedConditions.visibilityOfElementLocated(inNumSuccessMessage));
        driver.element().assertThat(inNumSuccessMessage).isVisible();
        driver.browser().refreshCurrentPage();
        return this;
    }

    @Step("Validate that invoice number is updated")
    public RequestDetailsPage validateInvNumberisUpdated(String inputInvNum) {
        String invoiceNum = wait.until(driver -> driver.getDriver().findElement(invNumberUpdated).getAttribute("value"));
        Validations.assertThat()
                .object(invoiceNum)
                .isEqualTo(inputInvNum)
                .perform();

        return this;
    }

    @Step("Validate That The item comment have data is inserted")
    public RequestDetailsPage validateItemCommentHaveDataInsertedBefore(String itemComment) {
        driver.element().scrollToElement(addItemCommentButton)
                .click(addItemCommentButton)
                .assertThat(CommentContent).text().equalsIgnoringCaseSensitivity(itemComment)
                .withCustomReportMessage("Validate comment contains expected text: " + itemComment)
                .perform();
        driver.element().click(cancelButtonOfItemNote);

        return this;
    }

    @Step("Validate That The Note Section have data is inserted")
    public RequestDetailsPage validateThatNoteSectionHaveDataInsertedBefore(String noteData) {
        driver.element().scrollToElement(displayedNote)
                .assertThat(displayedNote).text().equalsIgnoringCaseSensitivity(noteData).perform();
        return this;
    }
    @Step("Validate That The File Is Uploaded And Displayed Successfully")
    public RequestDetailsPage ValidateThatTheFileIsUploadedAndDisplayedSuccessfullyInFilesSection(String fileName) {

        By fileNameLocator = By.xpath(String.format("//a[contains(@title, '%s')]", fileName));
        driver.element()
                .scrollToElement(fileNameLocator)
                .assertThat(fileNameLocator)
                .exists()
                .perform();

        driver.element()
                .assertThat(fileNameLocator)
                .text()
                .contains(fileName) // contains to avoid case issues
                .perform();

        return this;
    }

    @Step("Cancel Outbound/Return outbound Request")
    public RequestDetailsPage cancelOutboundAndReturnOutboundRequest() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(cancelOutboundBtn)
                .click(cancelOutboundBtn)
                .waitUntilPresenceOfAllElementsLocatedBy(confirmCancelOutboundBtn)
                .click(confirmCancelOutboundBtn)
                .waitUntilPresenceOfAllElementsLocatedBy(cancellationConfirmPopup);
        return this;
    }

    @Step("Click on Draft button")
    public RequestDetailsPage clickDraftButton() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(draftButton)
                .click(draftButton);
        return this;
    }

    //endregion

}
