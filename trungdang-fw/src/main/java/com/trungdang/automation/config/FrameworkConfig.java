package com.trungdang.automation.config;

import java.time.Duration;
import java.util.Objects;

public class FrameworkConfig {

    private final BrowserType browser;
    private final boolean headless;
    private final Duration waitTimeout;

    public FrameworkConfig(BrowserType browser, boolean headless, Duration waitTimeout) {
        this.browser = Objects.requireNonNull(browser, "Browser must not be null.");
        this.headless = headless;
        this.waitTimeout = Objects.requireNonNull(waitTimeout, "Wait timeout must not be null.");

        if (waitTimeout.isZero() || waitTimeout.isNegative()) {
            throw new IllegalArgumentException("Wait timeout must be greater than zero.");
        }
    }

    public BrowserType getBrowser() {
        return browser;
    }

    public boolean isHeadless() {
        return headless;
    }

    public Duration getWaitTimeout() {
        return waitTimeout;
    }
}
