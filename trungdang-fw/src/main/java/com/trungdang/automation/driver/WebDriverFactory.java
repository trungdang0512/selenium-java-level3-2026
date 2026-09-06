package com.trungdang.automation.driver;

import com.trungdang.automation.config.BrowserType;
import com.trungdang.automation.config.FrameworkConfig;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Creates WebDriver instances from framework configuration.
 */
public class WebDriverFactory {

    private WebDriverFactory() {
    }

    public static WebDriver createDriver(FrameworkConfig config) {
        Objects.requireNonNull(config, "Framework configuration must not be null.");
        BrowserProvider provider = getProvider(config.getBrowser());

        return switch (config.getExecutionMode()) {
            case LOCAL -> provider.createLocalDriver(config);
            case REMOTE -> createRemoteDriver(config, provider);
        };
    }

    private static BrowserProvider getProvider(BrowserType browser) {
        Objects.requireNonNull(browser, "Browser type must not be null.");

        return switch (browser) {
            case CHROME -> new ChromeProvider();
            case FIREFOX -> new FirefoxProvider();
            case EDGE -> new EdgeProvider();
        };
    }

    private static WebDriver createRemoteDriver(
            FrameworkConfig config,
            BrowserProvider provider
    ) {
        return new RemoteWebDriver(getGridUrl(config), provider.createOptions(config));
    }

    private static URL getGridUrl(FrameworkConfig config) {
        try {
            return URI.create(config.getGridUrl()).toURL();
        } catch (IllegalArgumentException | MalformedURLException exception) {
            throw new IllegalArgumentException(
                    "System property 'grid.url' must be a valid URL.",
                    exception
            );
        }
    }

}
