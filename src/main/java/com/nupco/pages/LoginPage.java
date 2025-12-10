package com.nupco.pages;

import com.shaft.driver.SHAFT;
import com.shaft.gui.element.ElementActions;
import com.shaft.gui.element.internal.ElementActionsHelper;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.nupco.pages.MyTasksPage.elementActionsHelper;
import static com.nupco.utils.helper.handleLogoutWindows;
import static com.nupco.utils.helper.isElementPresent;

public class LoginPage {

    //region Variables

    private SHAFT.GUI.WebDriver driver;
    private String loginPageURL = System.getProperty("loginPageURL");
    private String stagingDashboardURL = System.getProperty("stagingDashboardURL");
    private String tasksPageUrl= System.getProperty("requestsPageURL");

    //endregion

    //region Locators

    private final By userNameLocator = By.xpath("//input[@id='username']");
    private final By passwordLocator = By.id("password");
    private final By loginButton = By.id("signOnButton");
    private final By nupcoLogo = By.xpath("//img[@alt='NUPCO']");
    private final By submitButton = By.xpath("//button[@name='login']");
    private final By clearButton = By.xpath("//button[@name='reset']");
    private final By welcomeMessageCard = By.xpath("//div[@data-testid='UserInfoCard']");
    private final By secondaryLoginButton = By.xpath("(//button[contains(@class, 'btn') and contains(@class, 'btn-secondary')])[1]");
    private final By profileIcon = By.xpath("//a[@id='avatarDropdown']//img");
    private final By logout = By.xpath("//a[text()='Logout']");
    private final By logoutButtonSSOPage = By.id("logout-input");
    //endregion

    // Constructor
    public LoginPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    public WebElement getSubmitButton() {
        return driver.getDriver().findElement(submitButton);
    }

    public WebElement getSecondaryLoginButton() {
        return driver.getDriver().findElement(secondaryLoginButton);
    }


    //region Actions/steps (Methods)

    @Step("User Can Login To Site Successfully")
    public LoginPage userLoggedIntoSiteSuccessfully(String email, String password) {
        userLogin(email, password);
        validateLoginSuccessfully();
        return this;
    }
    @Step("Navigate To Login Page")
    public LoginPage navigateToLoginPage() {
        driver.browser().navigateToURL(stagingDashboardURL);

        elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), secondaryLoginButton, "");
        driver.element().waitToBeReady(secondaryLoginButton);
        getSecondaryLoginButton().click();

        //driver.browser().navigateToURL(loginPageURL);
        return this;
    }

    @Step("Navigate To requests Page")
    public LoginPage navigateToRequestsPage() {
        driver.browser().navigateToURL(tasksPageUrl);
        return this;
    }

    @Step("User Login With Correct user name: {userName} And password : {password}")
    public LoginPage userLogin(String userName, String password) {

        if (userName == null || password == null || userName.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username or password is missing. Please verify test data (JSON keys).");
        }

        try {
            driver.element()
                    .waitToBeReady(userNameLocator)
                    .type(userNameLocator, userName);

            boolean passwordVisible = isElementPresent(passwordLocator,driver);

            if (!passwordVisible) {
                driver.element()
                        .click(submitButton)
                        .waitToBeReady(passwordLocator)
                        .typeSecure(passwordLocator, password)
                        .click(submitButton);

            } else {
                driver.element()
                        .waitToBeReady(passwordLocator)
                        .typeSecure(passwordLocator, password)
                        .click(loginButton);
            }

        } catch (ElementClickInterceptedException e) {
            ReportManager.log("Login button click was intercepted, attempting retry...");
            driver.browser().refreshCurrentPage();
            driver.element().waitToBeReady(loginButton).click(loginButton);

        } catch (Exception e) {
            ReportManager.log("Login attempt failed due to unexpected error: " + e.getMessage());
            throw e;
        }

        return this;
    }

    @Step("User Navigates to Staging Env Dashboard screen")
    public LoginPage navigateToStagEnvDashboard() {
        driver.browser().waitForLazyLoading()
                //.element().waitUntilPresenceOfAllElementsLocatedBy(logoutButtonSSOPage).browser()
                .navigateToURL(stagingDashboardURL);
        try {
            getSecondaryLoginButton().click();
        } catch (ElementClickInterceptedException e) {
            // Wait for the secondary Login Button element to be clickable
            elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), secondaryLoginButton, "");
            driver.element().waitToBeReady(secondaryLoginButton);
            getSecondaryLoginButton().click();
        }
        driver.browser().navigateToURL(stagingDashboardURL);

        return this;
    }

    @Step("User Navigates to Staging Env Dashboard directly Without Clicking on Secondary Button")
    public LoginPage navigateToStagEnvDashboardDirectly() {
        driver.browser().waitForLazyLoading()
                .navigateToURL(stagingDashboardURL);

        return this;
    }

    @Step("User Logout From Site Successfully")
    public LoginPage userCanLogoutFromSiteSuccessfully() {
        try {
            if (isElementPresent(profileIcon, driver)) {
                driver.browser().waitForLazyLoading()
                        .element().click(profileIcon)
                        .click(logout)
                        .browser().refreshCurrentPage();
                handleLogoutWindows(driver);

                WebDriverWait wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(10));
                wait.until(ExpectedConditions.presenceOfElementLocated(secondaryLoginButton));
                wait.until(ExpectedConditions.elementToBeClickable(secondaryLoginButton));
                driver.element().click(secondaryLoginButton);
                ReportManager.logDiscrete(" Logout successful");
            } else {
                ReportManager.logDiscrete(" Profile icon not found; user may already be logged out.");
            }
        } catch (Exception e) {
            ReportManager.logDiscrete(" Logout attempt failed: " + e.getMessage());
        }
        return this;
    }



    //endregion

    //region Validations

    @Step("Validate Login Successfully")
    public LoginPage validateLoginSuccessfully() {
        driver.assertThat().element(nupcoLogo).exists().perform();
        return this;
    }
    //endregion
}
