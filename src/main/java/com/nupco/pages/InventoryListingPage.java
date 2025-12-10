package com.nupco.pages;

import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.nupco.utils.helper.isElementPresent;
import static com.nupco.utils.helper.selectClassification;

public class InventoryListingPage {

    // region Variables

    private SHAFT.GUI.WebDriver driver;


    // endregion

    //region Locators

    private final By classificationMenuButton = By.xpath("//button[@data-testid='classification-btn']");
    private final By inventory7thItemCheckbox = By.xpath("(//input[@class = 'false check-input'])[7]");
    private final By availableQuantityOf7thItem = By.xpath("(//td[@data-title='Available'])[7]");
    private final By bookedQuantityOf7thItem = By.xpath("(//td[@data-title='Booked'])[7]");
    private final By stockQuantityOf7thItem = By.xpath("(//td[@data-title='Stock'])[7]");
    private final By genericCodeOf7thItem = By.xpath("(//td[@data-title='Generic Code'])[7]");
    private final By bookedQuantityOf1stItem = By.xpath("(//td[@data-title='Booked'])[1]");
    private final By availableQuantityOf1stItem = By.xpath("(//td[@data-title='Available'])[1]");
    private final By stockQuantityOf1stItem = By.xpath("(//td[@data-title='Stock'])[1]");
    private final By createOutboundButton = By.xpath("//button[normalize-space()='Create Outbound']");
    private final By inventoryError = By.xpath("//h3[normalize-space(text())='an error happened']");
    private final By searcheField = By.xpath("//input[@name='search']");

    double bookedQuantity, availableQuantity, stockQuantity;
    String genericCode;

    // endregion

    // Constructor
    public InventoryListingPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    // region Actions/steps (Methods)


    public double getBookedQuantity() {
        return bookedQuantity;
    }

    public double getAvailableQuantity() {
        return availableQuantity;
    }

    public double getStockQuantity() {
        return stockQuantity;
    }

    public String getGenericCode() {
        return genericCode;
    }

    @Step("Select The Classification {String}")
    public InventoryListingPage selectInventoryItemClassification(String classificationValue) {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(classificationMenuButton)
                .click(classificationMenuButton);
        selectClassification(driver, classificationValue);

        driver.element().click(By.tagName("body"));

        return this;
    }

    @Step("Select an inventory item from the list")
    public InventoryListingPage selectInventoryItemUnderClassification() {
        getDetailsOf7thItem();


        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(inventory7thItemCheckbox)
                .click(inventory7thItemCheckbox)
                .click(createOutboundButton);


        return this;
    }


    @Step("Get generic code and booked and available quantities of 7th item in the list")
    public InventoryListingPage getDetailsOf7thItem() {
        if (isElementPresent(inventoryError, driver) && driver.element().isElementDisplayed(inventoryError)) {
            driver.browser().refreshCurrentPage();
        }

        driver.element().waitUntilPresenceOfAllElementsLocatedBy(availableQuantityOf7thItem);
        bookedQuantity = Double.parseDouble(driver.element().getText(bookedQuantityOf7thItem). replace(",",""));
        availableQuantity = Double.parseDouble(driver.element().getText(availableQuantityOf7thItem).replace(",","") );
        stockQuantity = Double.parseDouble(driver.element().getText(stockQuantityOf7thItem).replace(",","") );
        genericCode = driver.element().getText(genericCodeOf7thItem).trim() ;
        ReportManager.log("Item details: Generic code= "+genericCode+ ",available= "+ availableQuantity +" , Booked= "+ bookedQuantity);

        return this;
    }

    @Step("Search for item with generic code and get its quantities")
    public InventoryListingPage SearchItemAndGetDetails(String genericCode) {
        driver.element().waitToBeReady(searcheField).type(searcheField,genericCode).keyPress(searcheField, Keys.ENTER);

        bookedQuantity = Double.parseDouble(driver.element().getText(bookedQuantityOf1stItem). replace(",",""));
        availableQuantity = Double.parseDouble(driver.element().getText(availableQuantityOf1stItem).replace(",","") );
        availableQuantity = Double.parseDouble(driver.element().getText(availableQuantityOf1stItem).replace(",","") );
        stockQuantity = Double.parseDouble(driver.element().getText(stockQuantityOf1stItem).replace(",","") );
        ReportManager.log("Item quantities: available= "+ availableQuantity +" , Booked= "+ bookedQuantity);

        return this;
    }

    // endregion
}
