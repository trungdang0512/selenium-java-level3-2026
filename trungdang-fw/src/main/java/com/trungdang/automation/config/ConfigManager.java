package com.trungdang.automation.config;

import java.util.Locale;

public class ConfigManager {

    private static final String BROWSER_PROPERTY = "browser";
    private static final String HEADLESS_PROPERTY = "headless";

    private ConfigManager() {
    }

    public static FrameworkConfig load() {
        BrowserType browser = readBrowser();
        boolean headless = readHeadless();

        return new FrameworkConfig(browser, headless);
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
}
