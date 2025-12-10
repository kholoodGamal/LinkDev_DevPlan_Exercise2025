package com.nupco.pages;

import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static com.nupco.utils.helper.isElementPresent;

public class POsListPage {


    //region Variables

    private SHAFT.GUI.WebDriver driver;
    WebDriverWait wait;

    //endregion

    //region Locators
    private final By escapeAnnouncementButton = By.xpath("//div[@class='modal-header']/span");
    private final By regionSearch = By.xpath("//input[@placeholder='Choose Region' or @placeholder='Filter by Region']");
    private final By customerSearch = By.xpath("//input[@placeholder='Filter by Customer' or @placeholder='Choose Customer']");
    private final By storageLocationSearch = By.xpath("//input[@placeholder='Filter by Storage Location' or @placeholder='Choose Storage Location']");
    private final By selectButton = By.xpath("//button[normalize-space()='Select']");
    private final By retrievePOButton = By.xpath("//button[text()='Retrieve POs']");
    private final By searchPOTXT = By.xpath("//input[@name='searchPO']");
    private final By poList = By.xpath("//td[@data-title='PO Number']//p");
    private final By cancelButton = By.xpath("//button[@class='button-component ' and normalize-space(text())='Cancel']");
    private final By storageLocations = By.xpath("//span[normalize-space(text())='Select Storage Location'] | //input[@name='storageLocation' and @placeholder='Choose Storage Location']");
    private final By selectedItem = By.xpath("(//input[@name='selectedItem'])[1]");

    // Old filter Locators Section
    private final By regionDropDown = By.xpath("//input[@name='region']");
    private final By regionDropDownSearch = By.xpath("//input[@name='regionSearch']");
    private final By regionDropDownSearchedValue = By.xpath("//input[@name='regionSearch']//..//li");
    private final By customerDropDownList = By.xpath("//input[@name='customer']");
    private final By customerDropDownSearch = By.xpath("//input[@name='customerSearch']");
    private final By customerDropDownSearchedValue = By.xpath("//input[@name='customerSearch']//..//li");
    private final By storageLocationList = By.xpath("//input[@name='storageLocation']");
    private final By storageLocationDropDownSearch = By.xpath("//input[@name='storageLocationSearch']");
    private final By storageLocationDropDownSearchedValue = By.xpath("//input[@name='storageLocationSearch']//..//li");


    //endregion

    // Constructor
    public POsListPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    //region Actions/steps (Methods)


    @Step("Filter with Region value '{region}'")
    public POsListPage escapeAnnouncementAndSelectRegion(String region) {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(escapeAnnouncementButton)
                .click(escapeAnnouncementButton)
                .click(storageLocations)
                .click(regionSearch)
                .type(regionSearch, region);

        return this;
    }

    @Step("Filter with Region value '{region}'")
    public POsListPage selectRegionWithOldFilter(String region) {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(escapeAnnouncementButton)
                .click(escapeAnnouncementButton)
                .click(regionDropDown)
                .scrollToElement(regionDropDownSearch)
                .click(regionDropDownSearch)
                .type(regionDropDownSearch, region)
                .waitUntilNumberOfElementsToBe(regionDropDownSearchedValue, 1)
                .click(regionDropDownSearchedValue);

        return this;
    }

    @Step("Filter with customer name '{customerName}'")
    public POsListPage selectCustomerName(String customerName) {

        driver.element()
                .click(customerSearch)
                .clear(customerSearch)
                .type(customerSearch, customerName);

        return this;
    }

    @Step("Filter with customer name '{customerName}'")
    public POsListPage selectCustomerNameWithOldFilter(String customerName) {

        driver.element()
                .click(customerDropDownList)
                .type(customerDropDownSearch, customerName)
                .waitUntilNumberOfElementsToBe(customerDropDownSearchedValue, 1)
                .click(customerDropDownSearchedValue);

        return this;
    }

    @Step("Filter with storage location name '{storageLocation}'")
    public POsListPage selectStorageLocationName(String storageLocation) {

        driver.element()
                .click(storageLocationSearch)
                .clear(storageLocationSearch)
                .type(storageLocationSearch, storageLocation)
                .waitUntilPresenceOfAllElementsLocatedBy(selectedItem)
                .waitUntilNumberOfElementsToBe(selectedItem, 1)
                .waitToBeReady(selectedItem)
                .click(selectedItem);
        return this;
    }

    @Step("Filter with storage location name '{storageLocation}'")
    public POsListPage selectStorageLocationNameWithOldFilter(String storageLocation) {

        driver.element()
                .click(storageLocationList)
                .type(storageLocationDropDownSearch, storageLocation)
                .waitUntilNumberOfElementsToBe(storageLocationDropDownSearchedValue, 1)
                .click(storageLocationDropDownSearchedValue);

        return this;
    }

    @Step("Click on Select button to retrieve al pos related to selected items")
    public POsListPage clickToRetrievePOs() {
        driver.element().click(selectButton);
        return this;
    }

    @Step("Click on Retrieve button of old filter to retrieve all pos related to selected items")
    public POsListPage clickOnRetrievePOButton() {
        driver.element().click(retrievePOButton);
        return this;
    }

    @Step("Search with PO number {poNumber}")
    public POsListPage searchWithPONumber(String poNumber) {

        driver.element()
                .type(searchPOTXT, poNumber)
                .keyPress(searchPOTXT, Keys.ENTER);

        return this;
    }

    @Step("Open the PO details after Search")
    public POsListPage openPoDetailsAfterSearch() {

        driver.element()
                //.waitUntilPresenceOfAllElementsLocatedBy(poList)
                .waitToBeReady(poList, true)
                .click(poList);
        return this;
    }

    @Step("Open PO Details")
    public POsListPage openPoDetailsPage(String region, String customerName, String storageLocation, String poNumber) {
        escapeAnnouncementAndSelectRegion(region);
        selectCustomerName(customerName);
        selectStorageLocationName(storageLocation);
        clickToRetrievePOs();
        validatePoListIsNotEmpty();
        searchWithPONumber(poNumber);
        validatePoListIsNotEmpty();
        openPoDetailsAfterSearch();

        return this;
    }

    @Step("Open PO Details")
    public POsListPage openPoDetailsPageWithOldFilter(String region, String customerName, String storageLocation, String poNumber) {
        selectRegionWithOldFilter(region);
        selectCustomerNameWithOldFilter(customerName);
        selectStorageLocationNameWithOldFilter(storageLocation);
        clickOnRetrievePOButton();
        validatePoListIsNotEmpty();
        searchWithPONumber(poNumber);
        validatePoListIsNotEmpty();
        openPoDetailsAfterSearch();

        return this;
    }

    @Step("Retrieve POs based on filtration (auto-detect old or new design)")
    public POsListPage openPoDetailsPageWhetherFilterExist(String region, String customerName, String storageLocation, String poNumber) {
        try {
            boolean isOldFilterVisible = false;

            ReportManager.log(" Step 1 — Wait a short time to check for the region dropdown reliably");
            wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(5));
            try {
                WebElement regionElement = wait.until(ExpectedConditions.presenceOfElementLocated(regionDropDown));
                if (regionElement.isDisplayed()) {
                    isOldFilterVisible = true;
                }
            } catch (TimeoutException ignored) {
                ReportManager.log(" Element not found — likely new design");
            }

            ReportManager.log(" Step 2 — Route to the correct flow");
            if (isOldFilterVisible) {
                ReportManager.logDiscrete("Detected OLD filter layout — proceeding with legacy flow.");
                openPoDetailsPageWithOldFilter(region, customerName, storageLocation, poNumber);
            } else {
                ReportManager.logDiscrete("Detected NEW filter layout — proceeding with updated flow.");
                openPoDetailsPage(region, customerName, storageLocation, poNumber);
            }

        } catch (Exception e) {
            ReportManager.logDiscrete("Failed to determine filter design: " + e.getMessage());
            e.printStackTrace();
        }

        return this;
    }


    //endregion

    //region Validations

    @Step("Validate The PO list is not empty (stable hydration handling)")
    public POsListPage validatePoListIsNotEmpty() {

        WebDriver driverInstance = driver.getDriver();
        By poRows = By.xpath("//table[contains(@class,'po-table')]//tbody/tr");
        By noDataMsg = By.xpath("//*[contains(.,'No data') or contains(.,'No records')]");
        By poTable = By.xpath("//div[contains(@class,'po-table-wrapper')]//table");

        WebDriverWait wait = new WebDriverWait(driverInstance, Duration.ofSeconds(90));
        JavascriptExecutor js = (JavascriptExecutor) driverInstance;

        ReportManager.log("Waiting for PO table to exist in DOM...");
        wait.until(ExpectedConditions.presenceOfElementLocated(poTable));

        ReportManager.log("Waiting for hydration: table must contain rows OR no-data message...");

        long endTime = System.currentTimeMillis() + 90000; // 90 seconds hard limit

        while (System.currentTimeMillis() < endTime) {

            // FIRST: If "No data" message is visible → break early
            if (isElementPresent(noDataMsg, driver)) {
                String msg = driver.element().getText(noDataMsg);
                throw new AssertionError("PO list is empty. UI message: " + msg);
            }

            // SECOND: Check number of actual PO rows
            int count = driver.element().getElementsCount(poRows);

            if (count > 0) {
                ReportManager.logDiscrete("PO table hydrated successfully. Rows found: " + count);
                return this;
            }

            // Trigger hydration minimally
            js.executeScript("window.dispatchEvent(new Event('scroll'));");
        }

        throw new AssertionError("PO table not hydrated: No rows loaded after 90s.");
    }


}

