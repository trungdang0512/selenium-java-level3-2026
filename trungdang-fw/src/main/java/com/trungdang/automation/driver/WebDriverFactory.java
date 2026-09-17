package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import java.util.Objects;
import org.openqa.selenium.WebDriver;

public class WebDriverFactory {

    private WebDriverFactory() {
    }

    public static WebDriver create(FrameworkConfig config) {
        FrameworkConfig checkedConfig = Objects.requireNonNull(
                config,
                "Framework configuration must not be null."
        );

        BrowserProvider provider = switch (checkedConfig.getBrowser()) {
            case CHROME -> new ChromeProvider();
        };

        return provider.create(checkedConfig);
    }
}
