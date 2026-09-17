package com.trungdang.automation.config;

import java.util.Objects;

public class FrameworkConfig {

    private final BrowserType browser;
    private final boolean headless;
    private final String baseUrl;

    public FrameworkConfig(BrowserType browser, boolean headless, String baseUrl) {
        this.browser = Objects.requireNonNull(browser, "Browser must not be null.");
        this.headless = headless;
        this.baseUrl = Objects.requireNonNull(baseUrl, "Base URL must not be null.");
    }

    public BrowserType getBrowser() {
        return browser;
    }

    public boolean isHeadless() {
        return headless;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
