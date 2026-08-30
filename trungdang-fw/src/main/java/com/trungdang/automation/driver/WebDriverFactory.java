package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Creates WebDriver instances from framework configuration.
 */
public class WebDriverFactory {

    private WebDriverFactory() {
    }

    public static WebDriver createDriver(FrameworkConfig config) {
        Objects.requireNonNull(config, "Framework configuration must not be null.");

        return switch (config.getExecutionMode()) {
            case LOCAL -> createLocalDriver(config);
            case REMOTE -> createRemoteDriver(config);
        };
    }

    private static WebDriver createLocalDriver(FrameworkConfig config) {
        return switch (config.getBrowser()) {
            case CHROME -> new ChromeDriver(createChromeOptions(config));
            case FIREFOX -> new FirefoxDriver(createFirefoxOptions(config));
            case EDGE -> new EdgeDriver(createEdgeOptions(config));
        };
    }

    private static WebDriver createRemoteDriver(FrameworkConfig config) {
        return new RemoteWebDriver(getGridUrl(config), createOptions(config));
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

    private static MutableCapabilities createOptions(FrameworkConfig config) {
        return switch (config.getBrowser()) {
            case CHROME -> createChromeOptions(config);
            case FIREFOX -> createFirefoxOptions(config);
            case EDGE -> createEdgeOptions(config);
        };
    }

    private static ChromeOptions createChromeOptions(FrameworkConfig config) {
        ChromeOptions options = new ChromeOptions();

        if (config.isHeadless()) {
            options.addArguments("--headless=new");
        }

        return options;
    }

    private static FirefoxOptions createFirefoxOptions(FrameworkConfig config) {
        FirefoxOptions options = new FirefoxOptions();

        if (config.isHeadless()) {
            options.addArguments("-headless");
        }

        return options;
    }

    private static EdgeOptions createEdgeOptions(FrameworkConfig config) {
        EdgeOptions options = new EdgeOptions();

        if (config.isHeadless()) {
            options.addArguments("--headless=new");
        }

        return options;
    }
}
