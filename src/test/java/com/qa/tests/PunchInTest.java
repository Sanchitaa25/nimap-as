package com.qa.tests;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class PunchInTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    private final String VALID_USER = "shirolesanchita@gmail.com";
    private final String VALID_PASS = "Sanchita@25";

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.geolocation", 1);
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        js = (JavascriptExecutor) driver;
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // 1. Login
        driver.get("https://test.fieldforceconnect.com/auth/login");

        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@type='email' or @type='text' or contains(@placeholder,'Email') or contains(@placeholder,'User')]")
        ));
        emailInput.clear();
        emailInput.sendKeys(VALID_USER);

        WebElement passwordInput = driver.findElement(
            By.xpath("//input[@type='password' or contains(@placeholder,'Password')]")
        );
        passwordInput.clear();
        passwordInput.sendKeys(VALID_PASS);

        WebElement loginBtn = driver.findElement(
            By.xpath("//button[@type='submit' or contains(text(),'Login') or contains(text(),'Sign In')]")
        );
        js.executeScript("arguments[0].click();", loginBtn);

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));
    }

    @Test
    public void testAutomateTimesheetPunchInPunchOut() {
        pause(3000);

        // 1. Navigate to Attendance module
        WebElement attendanceNav = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//span[normalize-space()='Attendance'] | //a[contains(@href,'attendance')]")
        ));
        js.executeScript("arguments[0].click();", attendanceNav);
        pause(2000);

        // 2. Click "Add New" button to open modal
        WebElement addNewBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Add New')] | //a[contains(.,'Add New')]")
        ));
        js.executeScript("arguments[0].click();", addNewBtn);

        // 3. Wait for the modal dialog to be visible
        WebElement modalHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(),'Update TimeSheet')]")
        ));

        // 4. Fill each field explicitly inside the dialog container
        // Punch In Date
        WebElement inDate = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(),'Punch In Date')]/ancestor::div[contains(@class,'form-group') or contains(@class,'mat-form-field') or position()<=3]//input | (//mat-dialog-container//input)[1] | (//*[contains(@class,'modal')]//input)[1]")
        ));
        setAngularInputValue(inDate, "2026-09-04");

        // Punch In Time
        WebElement inTime = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(),'Punch In Time')]/ancestor::div[contains(@class,'form-group') or contains(@class,'mat-form-field') or position()<=3]//input | (//mat-dialog-container//input)[2] | (//*[contains(@class,'modal')]//input)[2]")
        ));
        setAngularInputValue(inTime, "09:30 AM");

        // Punch Out Date
        WebElement outDate = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(),'Punch Out Date')]/ancestor::div[contains(@class,'form-group') or contains(@class,'mat-form-field') or position()<=3]//input | (//mat-dialog-container//input)[3] | (//*[contains(@class,'modal')]//input)[3]")
        ));
        setAngularInputValue(outDate, "2026-09-04");

        // Punch Out Time
        WebElement outTime = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(),'Punch Out Time')]/ancestor::div[contains(@class,'form-group') or contains(@class,'mat-form-field') or position()<=3]//input | (//mat-dialog-container//input)[4] | (//*[contains(@class,'modal')]//input)[4]")
        ));
        setAngularInputValue(outTime, "06:30 PM");

        // 5. Reason for Claim
        try {
            WebElement reasonField = driver.findElement(
                By.xpath("//*[contains(text(),'Reason')]/ancestor::div//input | //input[contains(@placeholder,'Reason')] | //textarea")
            );
            reasonField.clear();
            reasonField.sendKeys("Automated Punch Attendance Record");
        } catch (Exception ignored) {}

        pause(1000);

        // 6. Click Save button
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[normalize-space()='Save'] | //button[contains(.,'Save')]")
        ));
        js.executeScript("arguments[0].click();", saveBtn);

        // 7. Validate toast message
        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'toast') or contains(@class,'alert') or contains(@class,'snackbar') or contains(@role,'alert')]")
        ));

        String toastMessage = toast.getText().trim();
        System.out.println("Timesheet Submission Toast Message: " + toastMessage);
        Assert.assertTrue(toast.isDisplayed(), "Toast message was not displayed.");
    }
    private void setAngularInputValue(WebElement element, String value) {
        String script = 
            "var el = arguments[0];" +
            "var val = arguments[1];" +
            "el.focus();" +
            "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "nativeInputValueSetter.call(el, val);" +
            "el.dispatchEvent(new Event('input', { bubbles: true }));" +
            "el.dispatchEvent(new Event('change', { bubbles: true }));" +
            "el.dispatchEvent(new Event('blur', { bubbles: true }));";
        js.executeScript(script, element, value);
    }
    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}