package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import java.util.Objects;
import org.openqa.selenium.WebDriver;

/**
 * Manages one browser and its configuration per thread, independently of a test runner.
 * Call start, accessors, and quit on the same thread and the same manager instance.
 */
public class DriverManager {

    private final ThreadLocal<WebDriver> drivers = new ThreadLocal<>();
    private final ThreadLocal<FrameworkConfig> configs = new ThreadLocal<>();

    private final WebDriverFactory factory;

    public DriverManager() {
        this(new WebDriverFactory());
    }

    public DriverManager(WebDriverFactory factory) {
        this.factory = Objects.requireNonNull(factory, "WebDriver factory must not be null.");
    }

    public void start(FrameworkConfig config) {
        if (drivers.get() != null) {
            throw new IllegalStateException("WebDriver is already running for the current thread.");
        }

        WebDriver driver = factory.create(config);
        drivers.set(driver);
        configs.set(config);
    }

    public WebDriver getDriver() {
        WebDriver driver = drivers.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver is not running for the current thread. Call start() first."
            );
        }
        return driver;
    }

    public FrameworkConfig getConfig() {
        FrameworkConfig config = configs.get();
        if (config == null) {
            throw new IllegalStateException(
                    "Framework configuration is not available for the current thread. Call start() first."
            );
        }
        return config;
    }

    /** Closes the current thread's browser and removes its state, even if quit fails. */
    public void quit() {
        WebDriver driver = drivers.get();
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            drivers.remove();
            configs.remove();
        }
    }
}
