package com.nupco.pages;

import com.shaft.driver.SHAFT;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class E_ServicesPage {

    //region Variables
    private SHAFT.GUI.WebDriver driver;
    WebDriverWait wait;


    // Constructor
    public E_ServicesPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    //region Locators

    private final By mainSearchBar = By.xpath("//input[@placeholder='Search for an e-service']");
    private final By poSearchBar = By.xpath("//input[@placeholder='Search...']");
    private final By openPoDetails = By.xpath("(//p[@class='p-0 m-0 clickable cursor-pointer'])[1]");



    @Step("Open the {service} Page from the Launchpad")
    public E_ServicesPage searchAndOpenTargetedService(String service) {
        driver.element().type(mainSearchBar, service)
                .keyPress(mainSearchBar, Keys.ENTER);

        WebElement targetService = driver.getDriver().findElement(By.xpath("//div[@title='" + service + "']"));

        try {
            targetService.click();
        } catch (ElementClickInterceptedException e) {
            WebDriverWait wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOf(targetService));
            wait.until(ExpectedConditions.elementToBeClickable(targetService));

            // Scroll to the element
            JavascriptExecutor js = (JavascriptExecutor) driver.getDriver();
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", targetService);

            // Try clicking again
            try {
                targetService.click();
            } catch (ElementClickInterceptedException e2) {
                js.executeScript("arguments[0].click();", targetService);
            }
        }
        return this;
    }


    @Step("Search and open the creation page pf a specific Po")
    public E_ServicesPage startBlanketAsnCreationPage(String po){
        driver.element().type(poSearchBar, po)
                .keyPress(poSearchBar, Keys.ENTER)
                .waitUntilPresenceOfAllElementsLocatedBy(openPoDetails)
                .click(openPoDetails);
        return this ;
    }



}
