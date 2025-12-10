package com.nupco.pages;

import com.shaft.driver.SHAFT;
import com.shaft.gui.element.internal.ElementActionsHelper;
import com.shaft.tools.io.ReportManager;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.testng.Assert;

import static com.nupco.utils.helper.isElementPresent;

public class MyRequestListPage {

    // region Variables

    private SHAFT.GUI.WebDriver driver;
    private ElementActionsHelper elementActionsHelper = new ElementActionsHelper(false);


    // endregion

    // region Locators
    private final By myRequestsList = By.xpath("//div[@id='drawer-container']");
    private final By myRequestsSearchBox = By.xpath("//input[@placeholder='Search anything ..']");
    private final By businessKeyLink = By.xpath("//td[@class='blue-color']//span");
    private final By noSearchResultMessage = By.xpath("//h5[normalize-space()='No Results Found']");

    // endregion

    // Constructor
    public MyRequestListPage(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    // region Actions/steps (Methods)

    @Step("open The Request {businessKey} from My Requests")
    public MyRequestListPage openTheRequestFromMyRequests(String businessKey) {
        SearchForBusinessKeyOnMyRequests(businessKey);
        openRequestDetails(businessKey);
        return this;
    }

    @Step("Search For the Business Key {businessKey} on My Requests")
    public MyRequestListPage SearchForBusinessKeyOnMyRequests(String businessKey) {
        int maxRetries = 5;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            ReportManager.logDiscrete("Searching for: " + businessKey + " (Attempt " + attempt + ")");
            driver.element().click(myRequestsSearchBox)
                    .clear(myRequestsSearchBox)
                    .type(myRequestsSearchBox, businessKey);

            if (!isElementPresent(noSearchResultMessage, driver)){
                ReportManager.logDiscrete("Results found successfully.");
                return this;
            }
            ReportManager.logDiscrete("No results found — retrying...");
            driver.browser().refreshCurrentPage()
                    .waitForLazyLoading();
        }
        Assert.fail("No results found after " + maxRetries + " attempts for search text: " + businessKey);
        return this;
    }


    @Step("Click On the  Business Key  link")
    public void openRequestDetails(String searchText) {
        driver.browser().waitForLazyLoading();
        elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), businessKeyLink, "");

        int searchTrials = 3;

        while (searchTrials != 0) {
            try {
                driver.element().waitUntilNumberOfElementsToBe(businessKeyLink, 1);
            } catch (AssertionError e) {
                driver.browser().refreshCurrentPage()
                        .element()
                        .type(myRequestsSearchBox, searchText);
                driver.element().waitUntilNumberOfElementsToBe(businessKeyLink, 1);
            }
            searchTrials--;
        }

        driver.element()
                .click(businessKeyLink);
    }

    // endregion

    // region Validation

    @Step("Validate The deleted draft request isn't displayed on my requests")
    public void ValidateDeletedDraftNotInRequests() {
        driver.element().waitUntilPresenceOfAllElementsLocatedBy(noSearchResultMessage);
    }

    // endregion
}
