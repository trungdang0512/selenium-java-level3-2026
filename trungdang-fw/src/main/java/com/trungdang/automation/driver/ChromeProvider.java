package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Provides Chrome-specific driver creation and options.
 */
final class ChromeProvider implements BrowserProvider {

    @Override
    public WebDriver createLocalDriver(FrameworkConfig config) {
        return new ChromeDriver(createOptions(config));
    }

    @Override
    public ChromeOptions createOptions(FrameworkConfig config) {
        ChromeOptions options = new ChromeOptions();

        if (config.isHeadless()) {
            options.addArguments("--headless=new");
        }

        return options;
    }
}
