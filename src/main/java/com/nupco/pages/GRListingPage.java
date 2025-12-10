package com.nupco.pages;

import com.shaft.driver.SHAFT;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class GRListingPage {

    // region Variables

    private SHAFT.GUI.WebDriver driver;


    // endregion

    //region Locators

    private final By searchBox = By.xpath("//input[@placeholder='Search Starts With...']");
    private final By createReqBtn = By.xpath("//button[@class='button-component button-component--white-color']");
    //private final By grRadioBtn = By.xpath("//tbody/tr[1]/td[1]/input[1]");
    private final By grRadioBtn = By.xpath("//input[@type = 'radio']");
    private final By grTable = By.xpath("//table[@class='inventory-table gr-list-table']");

    // endregion

    // Constructor
    public GRListingPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    // region Actions/steps (Methods)

    @Step("Search for {GR} and create request")
    public GRListingPage initiateReturnOutboundRequest(String GR) {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(grTable)
                .click(searchBox)
                .type(searchBox, GR)
                .keyPress(searchBox, Keys.ENTER);
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(grTable)
                .click(grRadioBtn)
                .click(createReqBtn);
        return this;
    }


    // endregion
}
