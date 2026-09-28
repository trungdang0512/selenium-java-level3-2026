package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import java.util.Objects;
import org.openqa.selenium.WebDriver;

public class WebDriverFactory {

    private WebDriverFactory() {
    }

    public static WebDriver create(FrameworkConfig config) {
        Objects.requireNonNull(
                config,
                "Framework configuration must not be null."
        );

        BrowserProvider provider = switch (config.getBrowser()) {
            case CHROME -> new ChromeProvider();
        };

        return provider.create(config);
    }
}
