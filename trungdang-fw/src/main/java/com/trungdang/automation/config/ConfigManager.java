package com.trungdang.automation.config;

import java.util.Locale;

public class ConfigManager {

    private static final String BROWSER_PROPERTY = "browser";
    private static final String EXECUTION_MODE_PROPERTY = "execution.mode";
    private static final String HEADLESS_PROPERTY = "headless";
    private static final String GRID_URL_PROPERTY = "grid.url";

    private static FrameworkConfig config;

    private ConfigManager() {
    }

    public static synchronized FrameworkConfig load() {
        config = new FrameworkConfig(
                getEnum(BROWSER_PROPERTY, BrowserType.class, BrowserType.CHROME),
                getEnum(EXECUTION_MODE_PROPERTY, ExecutionMode.class, ExecutionMode.LOCAL),
                getBoolean(HEADLESS_PROPERTY, false),
                getProperty(GRID_URL_PROPERTY, "http://localhost:4444")
        );

        return config;
    }

    public static synchronized FrameworkConfig getConfig() {
        if (config == null) {
            return load();
        }

        return config;
    }

    private static String getProperty(String propertyName, String defaultValue) {
        String value = System.getProperty(propertyName);

        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static boolean getBoolean(String propertyName, boolean defaultValue) {
        String value = getProperty(propertyName, String.valueOf(defaultValue));

        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(
                    "System property '" + propertyName + "' must be true or false."
            );
        }

        return Boolean.parseBoolean(value);
    }

    private static <T extends Enum<T>> T getEnum(
            String propertyName,
            Class<T> enumType,
            T defaultValue
    ) {
        String value = getProperty(propertyName, defaultValue.name());

        try {
            return Enum.valueOf(enumType, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "System property '" + propertyName + "' must be one of "
                            + java.util.Arrays.toString(enumType.getEnumConstants()) + ".",
                    exception
            );
        }
    }
}
