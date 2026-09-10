package com.trungdang.automation.tests;

import com.trungdang.automation.config.ConfigManager;
import com.trungdang.automation.config.FrameworkConfig;
import com.trungdang.automation.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

    private DriverManager driverManager;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        FrameworkConfig config = ConfigManager.load();
        driverManager = new DriverManager();
        driverManager.start(config);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driverManager != null) {
            driverManager.quit();
        }
    }

    protected WebDriver getDriver() {
        if (driverManager == null) {
            throw new IllegalStateException(
                    "DriverManager is not initialized. Test setup must run first."
            );
        }

        return driverManager.getDriver();
    }
}
