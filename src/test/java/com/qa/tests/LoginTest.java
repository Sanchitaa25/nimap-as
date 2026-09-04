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

public class LoginTest {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get("https://test.fieldforceconnect.com/auth/login");
    }

    // --- Task 1: Parametrization via TestNG @DataProvider ---
    @DataProvider(name = "loginData")
    public Object[][] getLoginData() {
        return new Object[][] {
            { "shirolesanchita@gmail.com", "Sanchita@25", true }
        };
    }

    @Test(dataProvider = "loginData")
    public void testLoginJourney(String username, String password, boolean isExpectedSuccess) {
        // 1. Enter Email / Username
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@type='email' or @type='text' or contains(@formcontrolname,'email') or contains(@placeholder,'Email') or contains(@placeholder,'User')]")
        ));
        emailInput.clear();
        emailInput.sendKeys(username);

        // 2. Enter Password
        WebElement passwordInput = driver.findElement(
            By.xpath("//input[@type='password' or contains(@placeholder,'Password')]")
        );
        passwordInput.clear();
        passwordInput.sendKeys(password);

        // 3. Click Login / Sign In Button
        WebElement loginBtn = driver.findElement(
            By.xpath("//button[@type='submit' or contains(text(),'Login') or contains(text(),'Sign In')]")
        );
        loginBtn.click();

        // 4. Validate successful entry to dashboard
        boolean isLoggedIn = wait.until(ExpectedConditions.or(
            ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")),
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Dashboard') or contains(text(),'Punch')]"))
        ));
        Assert.assertTrue(isLoggedIn, "Valid login failed: User could not reach dashboard.");
        System.out.println("Login Successful! Landed on: " + driver.getCurrentUrl());
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}