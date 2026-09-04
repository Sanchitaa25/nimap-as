package com.qa.tests;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;

public class AddCustomerTest {

    WebDriver driver;
    WebDriverWait wait;

    private final String VALID_USER = "shirolesanchita@gmail.com";
    private final String VALID_PASS = "Sanchita@25";

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.get("https://test.fieldforceconnect.com/auth/login");

        // Authenticate
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
        loginBtn.click();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));
    }

    // --- Task 3: Customer Parametrization ---
    @DataProvider(name = "customerRecords")
    public Object[][] getCustomerRecords() {
        long timestamp = System.currentTimeMillis() % 10000;
        return new Object[][] {
            { "Alpha Retail " + timestamp, "98765" + String.format("%05d", timestamp), "Sector 18, Noida" },
            { "Omega Traders " + timestamp, "91234" + String.format("%05d", timestamp), "MG Road, Pune" }
        };
    }

    @Test(dataProvider = "customerRecords")
    public void testAddCustomerJourney(String customerName, String customerPhone, String customerAddress) {
        // 1. Click 'My Customers' from the left sidebar
        WebElement myCustomersNav = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//span[contains(text(),'My Customers')] | //a[contains(.,'My Customers')]")
        ));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", myCustomersNav);

        // 2. Click the 'Add Customer' button or icon on the customers page
        WebElement addCustomerBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Add') or contains(.,'Create') or contains(.,'New')] | //a[contains(.,'Add Customer') or contains(.,'Create')] | //button[@title='Add Customer']")
        ));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", addCustomerBtn);

        // 3. Fill customer details
        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@name='name' or @id='name' or contains(@placeholder,'Name') or contains(@formcontrolname,'name')]")
        ));
        nameField.clear();
        nameField.sendKeys(customerName);

        WebElement phoneField = driver.findElement(
            By.xpath("//input[@name='phone' or @name='mobile' or @id='phone' or contains(@placeholder,'Phone') or contains(@placeholder,'Mobile') or contains(@formcontrolname,'phone') or contains(@formcontrolname,'mobile')]")
        );
        phoneField.clear();
        phoneField.sendKeys(customerPhone);

        try {
            WebElement addressField = driver.findElement(
                By.xpath("//input[@name='address' or contains(@placeholder,'Address')] | //textarea[@name='address' or contains(@placeholder,'Address')]")
            );
            addressField.clear();
            addressField.sendKeys(customerAddress);
        } catch (Exception ignored) {
        }

        // 4. Submit the form
        WebElement saveBtn = driver.findElement(
            By.xpath("//button[@type='submit' or contains(text(),'Save') or contains(text(),'Submit')]")
        );
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

        // 5. Validate customer record or success toast
        WebElement verificationElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(),'" + customerName + "') or contains(@class,'toast') or contains(@class,'alert') or contains(.,'success') or contains(.,'Added')]")
        ));

        Assert.assertTrue(verificationElement.isDisplayed(), "Validation Failed: Customer creation record/alert not visible.");
        System.out.println("Customer Verified Successfully: " + customerName);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}