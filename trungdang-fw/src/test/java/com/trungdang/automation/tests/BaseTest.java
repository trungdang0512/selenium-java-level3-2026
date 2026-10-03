package com.trungdang.automation.tests;

import com.trungdang.automation.config.ConfigManager;
import com.trungdang.automation.config.FrameworkConfig;
import com.trungdang.automation.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/** Optional TestNG lifecycle adapter for DriverManager. */
public class BaseTest {

    private final DriverManager driverManager = new DriverManager();

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driverManager.start(ConfigManager.load());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        driverManager.quit();
    }

    protected WebDriver getDriver() {
        return driverManager.getDriver();
    }

    protected FrameworkConfig getConfig() {
        return driverManager.getConfig();
    }
}
