package com.trungdang.automation.config;

public class ConfigManager {

    private static final String BROWSER_PROPERTY = "browser";
    private static final String HEADLESS_PROPERTY = "headless";
    private static final String BASE_URL_PROPERTY = "base.url";
    private static final String DEFAULT_BASE_URL = "https://demo.testarchitect.com/";

    private ConfigManager() {
    }

    public static FrameworkConfig load() {
        String browser = System.getProperty(BROWSER_PROPERTY, "chrome");
        boolean headless = readHeadless();
        String baseUrl = System.getProperty(BASE_URL_PROPERTY, DEFAULT_BASE_URL);

        return new FrameworkConfig(browser, headless, baseUrl);
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
