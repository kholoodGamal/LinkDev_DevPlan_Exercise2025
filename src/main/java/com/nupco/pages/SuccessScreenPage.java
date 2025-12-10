package com.nupco.pages;

import com.shaft.cli.FileActions;
import com.shaft.driver.SHAFT;
import com.shaft.validation.Validations;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SuccessScreenPage {
    private SHAFT.GUI.WebDriver driver;
    private WebDriverWait wait;
    String businessKey;
    private final By requestBusinessKey = By.xpath("//div[@class='modal-content']//span[@class='request-numb']");
    private final By backToMyRequestsButton = By.xpath("//button[contains(@class,'btns') and contains(@class,'modal-actions') and text()='Back to Requests']");
    private final By backToMyReturnRequestsButton = By.xpath("(//button[@class = 'btns modal-actions mt-1'])[1]");
    private final By exportPDFBtn = By.xpath("//button[normalize-space()='Export PDF']");

    public SuccessScreenPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }


    @Step("Get the request business Key")
    public SuccessScreenPage copyRequestBusinessKey() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(requestBusinessKey);
        businessKey = driver.element().getText(requestBusinessKey);
        return this;
    }

    @Step("Click on 'Back To My Requests' button")
    public SuccessScreenPage BackToMyRequests() {
        wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(30));
        wait.until(ExpectedConditions.elementToBeClickable(backToMyRequestsButton));
        driver.element().click(backToMyRequestsButton);
        return this;
    }

    @Step("Click on 'Back To My Requests' on return outbound button")
    public SuccessScreenPage BackToMyReturnRequests() {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(backToMyReturnRequestsButton)
                .click(backToMyReturnRequestsButton);
        return this;
    }

    public String getBusinessKey() {
        return businessKey;
    }


}
