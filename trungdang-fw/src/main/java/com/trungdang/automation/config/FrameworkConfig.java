package com.trungdang.automation.config;

import java.util.Locale;
import java.util.Objects;

public class FrameworkConfig {

    private static final boolean DEFAULT_HEADLESS = false;
    private static final String DEFAULT_BASE_URL = "https://demo.testarchitect.com/";

    private final String browser;
    private final boolean headless;
    private final String baseUrl;

    public FrameworkConfig(String browser) {
        this(browser, DEFAULT_HEADLESS, DEFAULT_BASE_URL);
    }

    public FrameworkConfig(String browser, boolean headless) {
        this(browser, headless, DEFAULT_BASE_URL);
    }

    public FrameworkConfig(String browser, boolean headless, String baseUrl) {
        this.browser = Objects.requireNonNull(browser, "Browser must not be null.")
                .trim().toLowerCase(Locale.ROOT);
        if (this.browser.isEmpty()) {
            throw new IllegalArgumentException("Browser must not be blank.");
        }
        this.headless = headless;
        this.baseUrl = Objects.requireNonNull(baseUrl, "Base URL must not be null.");
    }

    public String getBrowser() {
        return browser;
    }

    public boolean isHeadless() {
        return headless;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
