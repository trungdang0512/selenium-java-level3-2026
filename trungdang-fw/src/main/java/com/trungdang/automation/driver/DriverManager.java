package com.trungdang.automation.driver;

import com.trungdang.automation.config.ConfigManager;
import com.trungdang.automation.config.FrameworkConfig;
import org.openqa.selenium.WebDriver;

/**
 * Owns one WebDriver instance for each test thread.
 */
public class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void startDriver() {
        startDriver(ConfigManager.getConfig());
    }

    public static void startDriver(FrameworkConfig config) {
        if (DRIVER.get() != null) {
            throw new IllegalStateException("A WebDriver is already running for this thread.");
        }

        DRIVER.set(WebDriverFactory.createDriver(config));
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();

        if (driver == null) {
            throw new IllegalStateException(
                    "No WebDriver is running for this thread. Call startDriver() first."
            );
        }

        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();

        if (driver == null) {
            return;
        }

        try {
            driver.quit();
        } finally {
            DRIVER.remove();
        }
    }
}
