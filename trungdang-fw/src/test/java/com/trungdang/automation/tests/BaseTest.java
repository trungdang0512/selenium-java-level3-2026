package com.trungdang.automation.tests;

import com.trungdang.automation.config.ConfigManager;
import com.trungdang.automation.config.FrameworkConfig;
import com.trungdang.automation.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {

    private final ThreadLocal<DriverManager> driverManagers = new ThreadLocal<>();
    private final ThreadLocal<FrameworkConfig> configs = new ThreadLocal<>();

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        FrameworkConfig config = ConfigManager.load();
        DriverManager driverManager = new DriverManager();

        configs.set(config);
        driverManagers.set(driverManager);
        driverManager.start(config);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager driverManager = driverManagers.get();

        try {
            if (driverManager != null) {
                driverManager.quit();
            }
        } finally {
            driverManagers.remove();
            configs.remove();
        }
    }

    protected WebDriver getDriver() {
        DriverManager driverManager = driverManagers.get();

        if (driverManager == null) {
            throw new IllegalStateException(
                    "DriverManager is not initialized for the current thread. "
                            + "Test setup must run first."
            );
        }

        return driverManager.getDriver();
    }

    protected FrameworkConfig getConfig() {
        FrameworkConfig config = configs.get();

        if (config == null) {
            throw new IllegalStateException(
                    "FrameworkConfig is not initialized for the current thread. "
                            + "Test setup must run first."
            );
        }

        return config;
    }
}
