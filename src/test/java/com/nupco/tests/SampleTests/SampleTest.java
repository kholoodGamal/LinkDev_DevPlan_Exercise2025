package com.nupco.tests.SampleTests;

import com.shaft.driver.SHAFT;
import io.qameta.allure.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Epic("Automation Exercise")
@Feature("samples")
@Story("sample code")
public class SampleTest {

    SHAFT.GUI.WebDriver driver;
    SHAFT.TestData.JSON testData;

    By searchBox = By.name("q");
    By resultStats = By.id("result-stats");

    @TmsLink("TC-1234")
    @Test(description = "Test01")
    @Description("Given that I created a TC")
    public void test() {
        driver.browser().navigateToURL("https://www.google.com/");
        driver.verifyThat().browser().title().isEqualTo("Google").perform();
        driver.element().type(searchBox, testData.getTestData("searchQuery"))
                .keyPress(searchBox, Keys.ENTER);
        driver.assertThat().element(resultStats).text().doesNotEqual("")
                .withCustomReportMessage("Check that result stats is not empty").perform();
    }

    @Test(description = "Test02")
    @Description("Given that I uploaded a file")
    public void test2() {
        driver.browser().navigateToURL("https://in-stag.nupco.com/Shipping/shipping-postGR?orderId=237a3608-e6bf-4300-a791-d6b5b7baeb8e&taskId=df314dda-7f6a-4483-d2de-08dcb68d89c9&trxId=36bbbd78-2299-47c9-82d7-24b509851862");
        //By fileuploader = By.xpath("(//div[@class = 'mb-5']//div[@class = 'uploader-block']//input)[1]");
        //driver.element().type(fileuploader, "D:\\iNUBCO\\Automation\\Nupco.ShaftFramework\\src\\test\\resources\\testDataFiles\\1586317961.pdf");
        WebDriver dri = driver.getDriver();
        WebElement inputFile = dri.findElement(By.xpath("(//div[@class = 'mb-5']//div[@class = 'uploader-block']//input)[1]"));
        inputFile.sendKeys("D:\\iNUBCO\\Automation\\Nupco.ShaftFramework\\src\\test\\resources\\testDataFiles\\1586317961.pdf");
        WebElement inputFile1 = dri.findElement(By.xpath("(//div[@class = 'mb-5']//div[@class = 'uploader-block']//input)[2]"));
        inputFile.sendKeys("D:\\iNUBCO\\Automation\\Nupco.ShaftFramework\\src\\test\\resources\\testDataFiles\\1586317961.pdf");

    }


    @BeforeClass
    public void beforeClass() {
        driver = new SHAFT.GUI.WebDriver();
        testData = new SHAFT.TestData.JSON("simpleJSON.json");
    }

    @AfterClass(alwaysRun = true)
    public void afterClass(){
        driver.quit();
    }

}
