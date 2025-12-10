package com.nupco.pages;

import com.shaft.driver.SHAFT;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class BlanketPOListPage {


    //region Variables

    private SHAFT.GUI.WebDriver driver;

    //endregion

    //region Locators

    private final By blanketLabel = By.xpath("//h2[normalize-space()='Blankets']");

    private final By searchPOTXT = By.xpath("//input[@name='searchPO']");
    private final By poList = By.xpath("//table[@class='po-table  ']//td[@data-title='Nupco PO No']/p");



    //endregion

    // Constructor
    public BlanketPOListPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    //region Actions/steps (Methods)

    @Step("Validate if page is loaded correctly")
    public BlanketPOListPage validateIsPageLoaded() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(blanketLabel);
        return this;
    }


    @Step("Search with PO number {poNumber}")
    public BlanketPOListPage searchWithPONumber(String poNumber) {

        driver.element()
                .type(searchPOTXT, poNumber)
                .keyPress(searchPOTXT, Keys.ENTER);

        return this;
    }

    @Step("Open the PO details after Search")
    public BlanketPOListPage openPoDetailsAfterSearch() {

        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(poList)
                //.waitToBeReady(poList, true)
                .waitUntilNumberOfElementsToBe(poList, 1)
                .click(poList);
        return this;
    }

    @Step("Open PO Details")
    public BlanketPOListPage openPoDetailsPage(String region, String customerName, String storageLocation, String poNumber) {

        validateIsPageLoaded();
        validateBlanketPoListIsNotEmpty();
        searchWithPONumber(poNumber);
        validateBlanketPoListIsNotEmpty();
        openPoDetailsAfterSearch();

        return this;
    }

    //endregion

    //region Validations

    @Step("Validate The PO list is not empty")
    public BlanketPOListPage validateBlanketPoListIsNotEmpty() {

        driver.element().waitUntilPresenceOfAllElementsLocatedBy(poList);

        int poListItemsCount = driver.element().getElementsCount(poList);
        if (poListItemsCount == 0) {
            throw new IllegalStateException(
                    "The PO list is Empty");
        }

        return this;
    }

    //endregion
}
