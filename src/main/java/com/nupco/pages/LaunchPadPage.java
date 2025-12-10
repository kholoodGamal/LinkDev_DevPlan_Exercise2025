package com.nupco.pages;

import com.shaft.driver.SHAFT;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

import static com.nupco.utils.helper.clickUsingJavaScript;
import static com.nupco.utils.helper.scrollIntoWebElement;

public class LaunchPadPage {

    //region Variables

    private SHAFT.GUI.WebDriver driver;
    WebDriverWait wait;
    private final String e_servicesPage = System.getProperty("e-servicesPageURL");


    //endregion

    //region Locators

    private final By launchpadMenuLocator = By.xpath("//button[contains(@id, 'react-aria')]");// Old UI : By.xpath("//button[@data-testid='HeaderApplications-ToggleShowBtn']");
    private final By launchpadIconPartialLocator = By.xpath("//div[@data-testid='HeaderApplications-LunchPad']");
    private final By e_services = By.xpath("//a[normalize-space()='View All E-Servcies']");


    //endregion

    // Constructor
    public LaunchPadPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    //region Actions/steps (Methods)

    @Step("Open the Launchpad Menu")
    public LaunchPadPage openLaunchpadMenu() {

        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(launchpadMenuLocator)
                .click(launchpadMenuLocator);
        return this;
    }

    @Step("Open the {iconTitle} Page from the Launchpad")
    public LaunchPadPage openPageFromLaunchpad(String iconTitle) {
        By iconLocator = By.xpath("//div[@class='launchpad-dropdown-content']//div[contains(text(), '" + iconTitle + "')]");
        WebDriverWait wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(10));

        try {
            // Ensure element is visible in DOM
            WebElement launchpadIcon = wait.until(ExpectedConditions.visibilityOfElementLocated(iconLocator));
            scrollIntoWebElement(driver.getDriver(), launchpadIcon);
            wait.until(ExpectedConditions.elementToBeClickable(launchpadIcon)).click();

        } catch (ElementClickInterceptedException e) {
            // Re-find the element in case of DOM refresh or stale element
            WebElement launchpadIcon = wait.until(ExpectedConditions.presenceOfElementLocated(iconLocator));
            scrollIntoWebElement(driver.getDriver(), launchpadIcon);

            try {
                wait.until(ExpectedConditions.elementToBeClickable(launchpadIcon)).click();
            } catch (Exception ex) {
                // Final fallback with JavaScript click
                clickUsingJavaScript(driver.getDriver(), launchpadIcon);
            }
        }

        return this;
    }

    @Step("Open the {iconTitle} Page from the Launchpad")
    public LaunchPadPage openTheSpecificPageFromLaunchPad(String iconTitle) {
        openLaunchpadMenu();
        openPageFromLaunchpad(iconTitle);
        return this;
    }

    @Step("click on view all e-services")
    public LaunchPadPage clickOnViewAllE_Services() {
        try {
            driver.element().click(e_services);
        } catch (ElementClickInterceptedException e) {
            wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(e_services));
            wait.until(ExpectedConditions.elementToBeClickable(e_services));
        }

        return this;
    }

    @Step("Open the E-Services Page from the Launchpad")
    public LaunchPadPage openTheE_ServicesTap() {
        openLaunchpadMenu();
        clickOnViewAllE_Services();
        return this;
    }


    //endregion
    @Step("Validate That User Navigated To E-services Page")
    public void validateThatUserNavigatedToE_ServicesPage() {

        String originalTab = driver.browser().getWindowHandle();

        driver.browser().waitUntilNumberOfWindowsToBe(2);
        List<String> windowHandles = driver.browser().getWindowHandles();
        String newTabHandle = "";
        for (String handle : windowHandles) {
            if (!handle.equals(originalTab)) {
                newTabHandle = handle;
                break;
            }
        }
        driver.browser().switchToWindow(newTabHandle);
        driver.browser().assertThat().url().isEqualTo(e_servicesPage);
    }

}
