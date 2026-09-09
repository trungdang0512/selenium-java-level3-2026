package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import org.openqa.selenium.WebDriver;

public class DriverManager {

    private WebDriver driver;

    public void start(FrameworkConfig config) {
        if (driver != null) {
            throw new IllegalStateException("WebDriver is already running.");
        }

        driver = WebDriverFactory.create(config);
    }

    public WebDriver getDriver() {
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver is not running. Call start() first."
            );
        }

        return driver;
    }

    public void quit() {
        if (driver == null) {
            return;
        }

        try {
            driver.quit();
        } finally {
            driver = null;
        }
    }
}
