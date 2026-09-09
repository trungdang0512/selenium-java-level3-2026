package com.trungdang.automation.config;

import java.util.Objects;

public final class FrameworkConfig {

    private final BrowserType browser;
    private final boolean headless;

    public FrameworkConfig(BrowserType browser, boolean headless) {
        this.browser = Objects.requireNonNull(browser, "Browser must not be null.");
        this.headless = headless;
    }

    public BrowserType getBrowser() {
        return browser;
    }

    public boolean isHeadless() {
        return headless;
    }
}
