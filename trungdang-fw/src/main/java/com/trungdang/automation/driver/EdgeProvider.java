package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

/**
 * Provides Edge-specific driver creation and options.
 */
final class EdgeProvider implements BrowserProvider {

    @Override
    public WebDriver createLocalDriver(FrameworkConfig config) {
        return new EdgeDriver(createOptions(config));
    }

    @Override
    public EdgeOptions createOptions(FrameworkConfig config) {
        EdgeOptions options = new EdgeOptions();

        if (config.isHeadless()) {
            options.addArguments("--headless=new");
        }

        return options;
    }
}
