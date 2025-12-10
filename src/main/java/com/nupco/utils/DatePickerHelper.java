package com.nupco.utils;

import com.shaft.driver.SHAFT;
import com.shaft.tools.io.ReportManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DatePickerHelper {

    private SHAFT.GUI.WebDriver driver;
    WebDriverWait wait;
    public DatePickerHelper(SHAFT.GUI.WebDriver driver) {
        this.driver = driver;
    }

    public void setDateForCalendar(By calendarInputLocator, LocalDate targetDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDate = targetDate.format(formatter);
            WebDriver webDriver = driver.getDriver();
            wait= new WebDriverWait(webDriver, Duration.ofSeconds(10));

            // 1️⃣ Click input field safely
            WebElement dateInput = wait.until(ExpectedConditions.elementToBeClickable(calendarInputLocator));
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", dateInput);
            ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", dateInput);

            // 2️⃣ Wait for calendar popup
            WebElement datePicker = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("react-datepicker")));
            WebElement monthYearLabel = datePicker.findElement(By.className("react-datepicker__current-month"));

            String displayedMonthYear = monthYearLabel.getText().replace("\u00A0", " ").trim();
            ReportManager.logDiscrete("📅 Calendar header: '" + displayedMonthYear + "'");

            // 3️⃣ Determine displayed month safely
            YearMonth displayedMonth;
            try {
                displayedMonth = YearMonth.parse(displayedMonthYear, DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
            } catch (Exception e) {
                displayedMonth = YearMonth.now();
                ReportManager.logDiscrete("⚠️ Fallback to current month-year due to parse issue: " + e.getMessage());
            }

            // 4️⃣ Calculate month difference
            YearMonth targetMonth = YearMonth.from(targetDate);
            int monthDiff = (targetMonth.getYear() - displayedMonth.getYear()) * 12 +
                    (targetMonth.getMonthValue() - displayedMonth.getMonthValue());

            // 5️⃣ Navigate to target month
            if (monthDiff != 0) {
                String navButtonXpath = monthDiff > 0
                        ? "//button[contains(@class,'react-datepicker__navigation--next')]"
                        : "//button[contains(@class,'react-datepicker__navigation--previous')]";

                for (int i = 0; i < Math.abs(monthDiff); i++) {
                    WebElement navButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(navButtonXpath)));

                    ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView({block:'center'});", navButton);
                    wait.until(ExpectedConditions.elementToBeClickable(navButton));

                    try {
                        navButton.click();
                    } catch (ElementClickInterceptedException e) {
                        ReportManager.logDiscrete("⚠️ Click intercepted, retrying with JS click...");
                        ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", navButton);
                    }

                    // short non-blocking logical pause
                    Thread.sleep(300);

                }
            }

            // 6️⃣ Select target day
            int attempts = 0;
            boolean daySelected = false;
            LocalDate currentTryDate = targetDate;

            while (!daySelected && attempts < 4) {
                int day = currentTryDate.getDayOfMonth();
                String dayXpath = String.format(
                        "//div[contains(@class,'react-datepicker__day') and not(contains(@class,'outside-month')) and text()='%d']",
                        day
                );

                List<WebElement> matchingDays = webDriver.findElements(By.xpath(dayXpath));

                if (matchingDays.isEmpty()) {
                    ReportManager.logDiscrete("❌ No day element found for " + currentTryDate);
                    break;
                }

                WebElement dayElement = matchingDays.get(0);
                String classes = dayElement.getAttribute("class");

                if (classes.contains("--disabled")) {
                    ReportManager.logDiscrete("⚠️ Day " + formattedDate + " is disabled. Trying next day...");
                    currentTryDate = currentTryDate.plusDays(1);
                    formattedDate = currentTryDate.format(formatter);
                    attempts++;
                    continue;
                }

                ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView({block:'center'});", dayElement);
                ((JavascriptExecutor) webDriver).executeScript("arguments[0].click();", dayElement);

                ReportManager.logDiscrete("✅ Selected enabled date: " + formattedDate);
                daySelected = true;
            }

            if (!daySelected) {
                throw new RuntimeException("❌ No enabled date found after " + attempts + " attempts starting from " + targetDate);
            }

        } catch (Exception e) {
            ReportManager.logDiscrete("❌ Failed to set date for " + calendarInputLocator + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }




}

