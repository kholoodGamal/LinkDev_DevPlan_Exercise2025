package com.nupco.utils;

import com.shaft.cli.FileActions;
import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import com.shaft.validation.Validations;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.nupco.pages.MyTasksPage.elementActionsHelper;

public class helper {

    public enum Classification {
        PHARMACEUTICALS("Pharmaceuticals"),
        MEDICAL_SUPPLIES("Medical Supplies"),
        MEDICAL_DEVICES("Medical Devices"),
        NON_MEDICAL("Non-Medical");

        private final String label;

        Classification(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static final By exportProformaBtn = By.xpath("//button[normalize-space()='Print Nupco Proforma Invoice']");
    public static final By exportPDFBtn = By.xpath("//button[normalize-space()='Export PDF']");
    public static final By tableRows = By.cssSelector(".inventory-table-wrapper-warehouse table tbody tr");


    public static void selectClassification(SHAFT.GUI.WebDriver driver, String classificationName) {
        String classificationOption = "//label[normalize-space()='" + classificationName + "']";
        driver.element().click(By.xpath(classificationOption));
    }

    public static void clickUsingJavaScript(WebDriver driver, WebElement element) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript("arguments[0].click();", element);
    }

    public static void scrollIntoWebElement(WebDriver driver, WebElement element) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    public static void selectDate(By calendarElement, By dayElement, Predicate<LocalDate> isValidDate, SHAFT.GUI.WebDriver driver) {
        boolean dateSelected = false;
        LocalDate currentDate = LocalDate.now();
        Predicate<LocalDate> isNotWeekday = isNotWeekday();

        while (!dateSelected) {
            try {
                // Open the calendar if not already opened
                WebElement calendarWrapper = driver.getDriver().findElement(calendarElement);
                if (!calendarWrapper.isDisplayed()) {
                    elementActionsHelper.waitForElementToBeClickable(driver.getDriver(), calendarElement, "Waiting for calendar to be clickable");
                }

                driver.element().click(calendarElement);

                // Fetch day elements dynamically to ensure the latest state of the calendar
                List<WebElement> dayList = driver.getDriver().findElements(dayElement);
                if (dayList.isEmpty()) {
                    throw new IllegalStateException("No day elements found in the calendar.");
                }

                // Iterate over days to select a valid date
                for (WebElement day : dayList) {
                    if (!day.isEnabled() || !day.isDisplayed()) {
                        continue;
                    }

                    try {
                        // Check if the day is already selected
                        String isSelected = day.getAttribute("aria-selected");
                        if ("true".equals(isSelected)) {
                            System.out.println("The date is already selected. Skipping...");
                            continue;
                        }

                        int dayValue = Integer.parseInt(day.getText());
                        LocalDate selectedDate = currentDate.withDayOfMonth(dayValue);

                        // Select the first valid date that is not already selected
                        if (isValidDate.test(selectedDate) || isNotWeekday.test(selectedDate)) {
                            day.click();
                            dateSelected = true;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        // Handle cases where day text is not numeric
                        continue;
                    }
                }

            } catch (Exception e) {
                System.out.println("Error during date selection: " + e.getMessage());
                throw new IllegalStateException("Unable to select a valid date.", e);
            }

            // Prevent infinite loops by limiting the number of retries
            if (currentDate.minusMonths(12).isAfter(LocalDate.now())) {
                throw new IllegalStateException("Unable to select a date after navigating 12 months.");
            }
        }
    }

    // Example: A utility method to determine if a date is a weekend
    public static Predicate<LocalDate> isWeekend() {
        return date -> date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7;
    }

    // Example: A utility method to determine if a date is today
    public static Predicate<LocalDate> isNotToday() {
        return date -> !date.equals(LocalDate.now());
    }

    // Example: A utility method for non-weekend, non-today dates
    public static Predicate<LocalDate> isWeekdayAndNotToday() {
        return isNotToday().and(isWeekend().negate());
    }

    public static Predicate<LocalDate> isNotWeekday() {
        return isWeekend().negate();
    }

    public static Predicate<LocalDate> isToday() {
        return date -> date.equals(LocalDate.now());
    }

    public static boolean isElementPresent(By locator, SHAFT.GUI.WebDriver driver) {
        try {
            return driver.getDriver()
                    .findElements(locator)
                    .stream()
                    .anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }


    public static void RefreshPage(SHAFT.GUI.WebDriver driver) {
        driver.browser().refreshCurrentPage();
    }

    public static void WaitForIntegrationReflection(Duration waitingDuration) throws InterruptedException {
        ReportManager.log("Wait for " + waitingDuration.getSeconds() + " seconds till SAP integration reflected");
        Thread.sleep(waitingDuration);
    }

    public static boolean waitForFileToBeDownloaded(String downloadDir, String fileName, int timeoutSeconds) {
        File file = new File(downloadDir + "/" + fileName);
        int elapsedTime = 0;

        while (elapsedTime < timeoutSeconds) {
            if (file.exists()) {
                return true; // File is downloaded
            }
            try {
                Thread.sleep(1000); // Wait for 1 second before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted while waiting for the file to download.", e);
            }
            elapsedTime++;
        }
        return false; // File was not downloaded within the timeout
    }

    public static int convertStringOfNumberWithCommasToInteger(SHAFT.GUI.WebDriver driver, By numberWithCommasLocator) throws ParseException {
        // Parse the text into a Number (Sample 9,333,023 ---> 933023)
        Number number = NumberFormat.getNumberInstance().parse(driver.element().getText(numberWithCommasLocator));

        // Convert the Number to an integer (or long if the value is large)
        return number.intValue();
    }

    public static void checkCalendarEnabledDays(SHAFT.GUI.WebDriver driver, By enabledCalenderDays, By nextMonthButton) {

        // Check for enabled days in the current month
        List<WebElement> enabledDays = driver.getDriver().findElements(enabledCalenderDays);

        if (enabledDays.isEmpty()) {
            // If no enabled days, navigate to the next month
            driver.element().click(nextMonthButton);

        }
    }

    public static void clickSubmitButton(SHAFT.GUI.WebDriver driver, By submitButton) {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(submitButton)
                .click(submitButton);
    }

    public static void fillNotesField(SHAFT.GUI.WebDriver driver, By addNoteButton, By noteTextField, By saveButton, String noteText) {
        driver.element()
                .scrollToElement(addNoteButton)
                .click(addNoteButton)
                .waitToBeReady(noteTextField)
                .type(noteTextField, noteText)
                .click(saveButton);
    }

    public static void handleLiveAvailableQtyLoader(SHAFT.GUI.WebDriver driver, By deliveryDate, By enabledCalenderDays, By nextMonthButton, By firstEnabledCalenderDay) {
        driver.browser().refreshCurrentPage();
        driver.element().click(deliveryDate); // Click to open the calendar
        checkCalendarEnabledDays(driver, enabledCalenderDays, nextMonthButton);
        driver.element().click(firstEnabledCalenderDay);//.type(requestedQtyField, requestedQty);

    }

    public static void handleLogoutWindows(SHAFT.GUI.WebDriver driver) {
        try {
            String mainWindow = driver.getDriver().getWindowHandle();
            Set<String> allWindows = driver.getDriver().getWindowHandles();

            for (String window : allWindows) {
                driver.getDriver().switchTo().window(window);
                String currentUrl = driver.getDriver().getCurrentUrl();

                if (currentUrl.contains("startSLO") || currentUrl.contains("signoff")) {
                    ReportManager.logDiscrete(" Closing sign-off window: " + currentUrl);
                    driver.getDriver().close();
                }
            }

            if (!driver.getDriver().getWindowHandles().isEmpty()) {
                driver.getDriver().switchTo().window(mainWindow);
            }
        } catch (Exception e) {
            ReportManager.logDiscrete(" No additional logout windows found or error occurred: " + e.getMessage());
        }
    }

    /**
     * Smart wait for lazy-rendered (React/Vue) elements to appear in headless mode.
     * It continuously scrolls, triggers resize/scroll events, and waits for presence.
     */
    public static boolean waitForHydratedElement(WebDriver driverInstance, By locator, int maxScrolls, int timeoutSeconds) {
        JavascriptExecutor js = (JavascriptExecutor) driverInstance;
        WebDriverWait wait = new WebDriverWait(driverInstance, Duration.ofSeconds(timeoutSeconds));

        ReportManager.logDiscrete(" Starting adaptive hydration wait for: " + locator);

        for (int i = 1; i <= maxScrolls; i++) {
            try {
                // Trigger JS hydration + scroll
                js.executeScript("""
                            window.dispatchEvent(new Event('resize'));
                            window.dispatchEvent(new Event('scroll'));
                            window.scrollBy(0, window.innerHeight / 2);
                        """);

                // Short sleep to allow DOM update
                Thread.sleep(1500);

                // Check if element is now present
                List<WebElement> elements = driverInstance.findElements(locator);
                if (!elements.isEmpty()) {
                    WebElement el = elements.get(0);
                    if (el.isDisplayed()) {
                        ReportManager.logDiscrete(" Element became visible after scroll #" + i);
                        return true;
                    } else {
                        js.executeScript("arguments[0].scrollIntoView({behavior:'instant', block:'center'});", el);
                    }
                } else {
                    ReportManager.logDiscrete(" Element not yet found (scroll #" + i + "/" + maxScrolls + ")");
                }
            } catch (Exception e) {
                ReportManager.logDiscrete(" Scroll attempt #" + i + " failed: " + e.getMessage());
            }
        }

        try {
            // Last explicit wait as fallback
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            ReportManager.logDiscrete(" Element appeared after waiting without scroll.");
            return true;
        } catch (Exception e) {
            ReportManager.logDiscrete(" Element never appeared even after hydration attempts: " + locator);
            return false;
        }
    }


    @Step("Validate that file is downloaded successfully")
    public static void validateThatFileIsDownloadedSuccessfully(String businessKey, String fileNamePrefix) {

        Path downloadFolder = Paths.get(SHAFT.Properties.paths.downloads());
        File dir = downloadFolder.toFile();

        ReportManager.logDiscrete("Download folder: " + downloadFolder);
        ReportManager.logDiscrete("Searching for file with key: " + businessKey);

        File downloadedFile = null;
        long maxWaitSeconds = 20;
        long start = System.currentTimeMillis();

        while ((System.currentTimeMillis() - start) < (maxWaitSeconds * 1000)) {

            File[] matchingFiles = dir.listFiles((d, name) ->
                    name.toLowerCase().contains(businessKey.toLowerCase()) &&
                            name.toLowerCase().endsWith(".pdf")
            );

            if (matchingFiles != null && matchingFiles.length > 0) {
                downloadedFile = matchingFiles[0];
                break;
            }
        }

        Validations.assertThat()
                .object(downloadedFile != null)
                .isTrue()
                .withCustomReportMessage(
                        "Validate that a PDF file containing '" + businessKey + "' was downloaded to: " + downloadFolder)
                .perform();
    }


    @Step("Validate That PDF File Is Downloaded Successfully")
    public static void exportPDFAndValidateFileDownloaded(SHAFT.GUI.WebDriver driver, String businessKey, String fileName) {
        exportPDF(driver);
        validateThatFileIsDownloadedSuccessfully(businessKey, fileName);
    }

    @Step("Export PDF file")
    public static void exportPDF(SHAFT.GUI.WebDriver driver) {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(exportPDFBtn)
                .click(exportPDFBtn);

    }

    @Step("Export Proforma file")
    public static void exportProforma(SHAFT.GUI.WebDriver driver) {
        driver.element()
                .waitUntilPresenceOfAllElementsLocatedBy(exportProformaBtn)
                .click(exportProformaBtn);

    }

    @Step("Validate That Proforma File Is Downloaded Successfully")
    public static void exportProformaAndValidateFileDownloaded(SHAFT.GUI.WebDriver driver, String businessKey, String fileName) {
        exportProforma(driver);
        validateThatFileIsDownloadedSuccessfully(businessKey, fileName);
    }

    @Step("Select {numberOfItemsToSelect} items from the inventory table")
    public static void selectItemsAndAddToRequest(SHAFT.GUI.WebDriver driver, int numberOfItemsToSelect) {
        By inventoryTable = By.cssSelector("div.inventory-table-wrapper table.inventory-table");
        By tableCheckboxes = By.cssSelector("div.inventory-table-wrapper table.inventory-table tbody input[type='checkbox']");
        By addToRequestButton = By.xpath("//button[normalize-space()='Add to request']");

        WebDriver driverInstance = driver.getDriver();
        WebDriverWait wait = new WebDriverWait(driverInstance, Duration.ofSeconds(15));

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryTable));
            List<WebElement> checkboxes = driverInstance.findElements(tableCheckboxes);
            int total = checkboxes.size();
            ReportManager.logDiscrete("Found " + total + " checkboxes in table.");
            if (total == 0) {
                ReportManager.logDiscrete("️No checkboxes found. Table may not be loaded.");
                return;
            }
            numberOfItemsToSelect = Math.min(numberOfItemsToSelect, total);

            for (int i = 0; i < numberOfItemsToSelect; i++) {
                try {
                    WebElement checkbox = checkboxes.get(i);
                    ((JavascriptExecutor) driverInstance)
                            .executeScript("arguments[0].scrollIntoView({block:'center'});", checkbox);

                    wait.until(ExpectedConditions.elementToBeClickable(checkbox));
                    checkbox.click();
                    ReportManager.logDiscrete(" Clicked checkbox #" + (i + 1));

                } catch (StaleElementReferenceException stale) {
                    ReportManager.logDiscrete(" Checkbox #" + (i + 1) + " became stale. Retrying...");
                    checkboxes = driverInstance.findElements(tableCheckboxes);
                    checkboxes.get(i).click();
                }
            }

            driver.element().click(addToRequestButton);
            ReportManager.logDiscrete(" Successfully added selected items to request.");

        } catch (Exception e) {
            ReportManager.logDiscrete(" Failed to select items: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Step("User adds new items to the assigned request")
    public static void userAddItemsToAssignedRequest(SHAFT.GUI.WebDriver driver, int noOfItems) {

        By addNewItemsButton = By.xpath("//button[normalize-space(text())='Add New Items' or normalize-space(text())='Add New Item']");
        try {
            driver.element().click(addNewItemsButton);
            ReportManager.log(" Clicked 'Add New Items' button.");
            driver.browser().waitForLazyLoading();

            selectItemsAndAddToRequest(driver, noOfItems);
            ReportManager.log(" Successfully added new item(s) to the request.");

        } catch (Exception e) {
            ReportManager.log(" Failed to add new items. Reason: " + e.getMessage());
            e.printStackTrace();

            try {
                ReportManager.log(" Retrying 'Add New Items' action once...");
                driver.browser().refreshCurrentPage();
                driver.element().click(addNewItemsButton);
                driver.browser().waitForLazyLoading();
                selectItemsAndAddToRequest(driver, noOfItems);
            } catch (Exception retryError) {
                ReportManager.log(" Retry also failed: " + retryError.getMessage());
                retryError.printStackTrace();
            }
        }
    }

    @Step("Fill requested quantity fields for {numberOfItems} selected items (skipping first row) with value: {requestedQty}")
    public static void fillRequestedQuantities(SHAFT.GUI.WebDriver driver, int numberOfItems, String requestedQty) {
        try {
            ReportManager.log("Wait until any loader disappears");
            driver.element().waitUntil(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//span[@class='loader']")));

            By requestedQtyInputs = By.xpath("//table[contains(@class,'table-hover')]//input[contains(@class,'qut')]");
            List<WebElement> qtyFields = driver.getDriver().findElements(requestedQtyInputs);

            int totalAvailable = qtyFields.size();
            int startIndex = 1;
            int itemsToFill = Math.min(numberOfItems, totalAvailable - startIndex);

            ReportManager.log("Found " + totalAvailable + " quantity fields. Skipping first, filling next " + itemsToFill + ".");

            for (int i = startIndex; i < startIndex + itemsToFill; i++) {
                By inputLocator = By.xpath("(" + requestedQtyInputs.toString().replace("By.xpath: ", "") + ")[" + (i + 1) + "]");

                ReportManager.log("Scroll into view for field #" + (i + 1));
                driver.element().scrollToElement(inputLocator);

                ReportManager.log("Type requested quantity");
                driver.element().type(inputLocator, requestedQty);
                driver.element().keyPress(inputLocator, Keys.TAB);

                ReportManager.log("Filled requested qty for item #" + (i + 1) + " → " + requestedQty);
            }

            driver.browser().waitForLazyLoading();

        } catch (Exception e) {
            ReportManager.log("Failed to fill requested quantities. Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Step("Delete the last item from the items table")
    public static void deleteLastItemFromTable(SHAFT.GUI.WebDriver driver) {
        By checkboxes = By.cssSelector(".inventory-table-wrapper-warehouse table tbody tr td input.check-input");
        By deleteButton = By.cssSelector("button.outbound-delete-items-btn");

        WebDriver driverInstance = driver.getDriver();
        WebDriverWait wait = new WebDriverWait(driverInstance, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.visibilityOfElementLocated(tableRows));

        List<WebElement> rows = driverInstance.findElements(tableRows);
        int rowCount = rows.size();
        ReportManager.logDiscrete("Found " + rowCount + " rows in the table.");

        if (rowCount == 0) {
            ReportManager.logDiscrete(" No items found in the table to delete.");
        }

        ReportManager.log(" Select the checkbox of the last item");
        WebElement lastCheckbox = driverInstance.findElements(checkboxes).get(rowCount - 1);
        ((JavascriptExecutor) driverInstance).executeScript("arguments[0].scrollIntoView({block:'center'});", lastCheckbox);
        lastCheckbox.click();
        ReportManager.logDiscrete(" Selected last item (row " + rowCount + ") for deletion.");

        ReportManager.log(" Wait until Delete button becomes enabled");
        wait.until(ExpectedConditions.elementToBeClickable(deleteButton));
        WebElement deleteBtn = driverInstance.findElement(deleteButton);

        ReportManager.log("Click Delete");
        deleteBtn.click();
        ReportManager.logDiscrete("Clicked Delete button to remove last item.");

        ReportManager.log(" Optional: wait for table refresh");
        wait.until(ExpectedConditions.numberOfElementsToBeLessThan(tableRows, rowCount));
        ReportManager.logDiscrete(" Table updated after deletion.");
    }

    @Step("Validate the last item was deleted successfully")
    public static void validateLastItemDeleted(SHAFT.GUI.WebDriver driver, int expectedCountAfterDeletion) {
        WebDriver driverInstance = driver.getDriver();
        WebDriverWait wait = new WebDriverWait(driverInstance, Duration.ofSeconds(15));

        wait.until(ExpectedConditions.visibilityOfElementLocated(tableRows));

        List<WebElement> visibleRows = driverInstance.findElements(tableRows)
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();

        int remainingCount = visibleRows.size();
        ReportManager.logDiscrete("Visible items in table: " + remainingCount);

        if (remainingCount == 0) {
            ReportManager.logDiscrete("No items are present. Table is empty — this is incorrect!");
            Assert.fail("No items exist after deletion — table should not be empty!");
        }

        Assert.assertEquals(remainingCount, expectedCountAfterDeletion, "Last item was NOT deleted correctly!");
        ReportManager.logDiscrete("Last item deletion validated successfully");
    }
}






