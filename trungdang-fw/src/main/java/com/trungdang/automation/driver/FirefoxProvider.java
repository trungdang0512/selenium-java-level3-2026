package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Provides Firefox-specific driver creation and options.
 */
final class FirefoxProvider implements BrowserProvider {

    @Override
    public WebDriver createLocalDriver(FrameworkConfig config) {
        return new FirefoxDriver(createOptions(config));
    }

    @Override
    public FirefoxOptions createOptions(FrameworkConfig config) {
        FirefoxOptions options = new FirefoxOptions();

        if (config.isHeadless()) {
            options.addArguments("-headless");
        }

        return options;
    }
}
