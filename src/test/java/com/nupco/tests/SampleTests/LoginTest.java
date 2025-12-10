package com.nupco.tests.SampleTests;

import com.nupco.pages.LoginPage;
import com.shaft.driver.SHAFT;
import io.qameta.allure.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("NUPCO Automation scripts")
@Feature("Login feature")
@Story("Login")
public class LoginTest {

    // region Variables
    private SHAFT.GUI.WebDriver driver;
    private SHAFT.TestData.JSON testData;

    //endregion

    // region Test Cases
    @TmsLink("TC-ID-1220")
    @Test(groups = {"Regression", "Sanity"}, description = "Login With Correct Email and Password Test")
    @Description("the user should be Logged in successfully to the system")
    public void loginWithCorrectEmailAndPassword() {
        new LoginPage(driver)
                .navigateToLoginPage()
                .userLogin(testData.getTestData("asnSupplier_email"), testData.getTestData("asnSupplier_password"))
                .navigateToStagEnvDashboard()
                .validateLoginSuccessfully()

        ;

    }


    // endregion

    //region Configurations

    @BeforeClass
    public void beforeClass() {
        testData = new SHAFT.TestData.JSON("UserCredentials.Json");
    }

    @BeforeMethod
    public void beforeMethod() {

        driver = new SHAFT.GUI.WebDriver();

    }

    @AfterMethod
    public void afterMethod() {
        driver.quit();
    }

    //endregion
}
