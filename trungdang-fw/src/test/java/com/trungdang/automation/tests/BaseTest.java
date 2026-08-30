package com.trungdang.automation.tests;

import com.trungdang.automation.config.ConfigManager;
import com.trungdang.automation.driver.DriverManager;
import com.trungdang.automation.pages.LoginPage;
import com.trungdang.automation.pages.MainPage;
import com.trungdang.automation.reporting.TestListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * TestNG lifecycle shared by all UI tests.
 */
@Listeners(TestListener.class)
public class BaseTest {
    MainPage mainPage = new MainPage();
    LoginPage loginPage = new LoginPage();

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        ConfigManager.load();
        DriverManager.startDriver();
        DriverManager.getDriver().manage().window().maximize();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
