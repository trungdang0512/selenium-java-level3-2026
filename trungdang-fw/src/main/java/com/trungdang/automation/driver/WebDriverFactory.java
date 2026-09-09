package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import java.util.Objects;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public final class WebDriverFactory {

    private WebDriverFactory() {
    }

    public static WebDriver create(FrameworkConfig config) {
        FrameworkConfig checkedConfig = Objects.requireNonNull(
                config,
                "Framework configuration must not be null."
        );

        return switch (checkedConfig.getBrowser()) {
            case CHROME -> createChrome(checkedConfig);
        };
    }

    private static WebDriver createChrome(FrameworkConfig config) {
        ChromeOptions options = new ChromeOptions();

        if (config.isHeadless()) {
            options.addArguments("--headless=new");
        }

        options.addArguments("--window-size=1920,1080");
        return new ChromeDriver(options);
    }
}
