package com.trungdang.automation.config;

import java.time.Duration;
import java.util.Locale;

/**
 * Resolves framework configuration from JVM system properties.
 *
 * <p>Supported properties are:
 * <ul>
 *   <li>{@code browser}: defaults to {@code chrome}; currently only Chrome is supported.</li>
 *   <li>{@code headless}: defaults to {@code false}; accepts {@code true} or {@code false}.</li>
 *   <li>{@code base.url}: defaults to {@code https://demo.testarchitect.com/} and is used
 *       as provided.</li>
 *   <li>{@code wait.timeout.seconds}: defaults to {@code 10}; accepts a whole number
 *       greater than zero.</li>
 * </ul>
 */
public class ConfigManager {

    private static final String BROWSER_PROPERTY = "browser";
    private static final String HEADLESS_PROPERTY = "headless";
    private static final String BASE_URL_PROPERTY = "base.url";
    private static final String DEFAULT_BASE_URL = "https://demo.testarchitect.com/";
    private static final String WAIT_TIMEOUT_SECONDS_PROPERTY = "wait.timeout.seconds";
    private static final long DEFAULT_WAIT_TIMEOUT_SECONDS = 10;

    private ConfigManager() {
    }

    /**
     * Loads and validates the current framework configuration.
     *
     * @return an immutable snapshot of the resolved configuration
     * @throws IllegalArgumentException if a supported property contains an invalid value
     */
    public static FrameworkConfig load() {
        BrowserType browser = readBrowser();
        boolean headless = readHeadless();
        String baseUrl = System.getProperty(BASE_URL_PROPERTY, DEFAULT_BASE_URL);
        Duration waitTimeout = readWaitTimeout();

        return new FrameworkConfig(browser, headless, baseUrl, waitTimeout);
    }

    private static BrowserType readBrowser() {
        String value = System.getProperty(BROWSER_PROPERTY, "chrome");

        try {
            return BrowserType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "System property 'browser' currently supports only: chrome.",
                    exception
            );
        }
    }

    private static boolean readHeadless() {
        String value = System.getProperty(HEADLESS_PROPERTY, "false").trim();

        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(
                    "System property 'headless' must be true or false."
            );
        }

        return Boolean.parseBoolean(value);
    }

    private static Duration readWaitTimeout() {
        String value = System.getProperty(
                WAIT_TIMEOUT_SECONDS_PROPERTY,
                String.valueOf(DEFAULT_WAIT_TIMEOUT_SECONDS)
        ).trim();

        try {
            long timeoutSeconds = Long.parseLong(value);

            if (timeoutSeconds <= 0) {
                throw new IllegalArgumentException(
                        "System property 'wait.timeout.seconds' must be greater than zero."
                );
            }

            return Duration.ofSeconds(timeoutSeconds);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "System property 'wait.timeout.seconds' must be a whole number greater than zero.",
                    exception
            );
        }
    }
}
