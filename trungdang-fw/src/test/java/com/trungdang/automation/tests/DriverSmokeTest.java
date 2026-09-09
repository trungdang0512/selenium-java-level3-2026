package com.trungdang.automation.tests;

import com.trungdang.automation.config.ConfigManager;
import com.trungdang.automation.config.FrameworkConfig;
import com.trungdang.automation.driver.DriverManager;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DriverSmokeTest {

    private DriverManager driverManager;

    @BeforeMethod
    public void setUp() {
        FrameworkConfig config = ConfigManager.load();
        driverManager = new DriverManager();
        driverManager.start(config);
    }

    @Test
    public void shouldStartChromeSession() {
        String windowHandle = driverManager.getDriver().getWindowHandle();

        Assert.assertFalse(windowHandle.isBlank(), "Chrome session should have a window handle.");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driverManager != null) {
            driverManager.quit();
        }
    }
}
