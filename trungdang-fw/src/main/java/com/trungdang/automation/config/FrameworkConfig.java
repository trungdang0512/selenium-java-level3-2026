package com.trungdang.automation.config;

import java.time.Duration;
import java.util.Objects;

/**
 * Stores the resolved settings used by one framework configuration snapshot.
 */
public class FrameworkConfig {

    private static final boolean DEFAULT_HEADLESS = false;
    private static final String DEFAULT_BASE_URL = "https://demo.testarchitect.com/";
    private static final Duration DEFAULT_WAIT_TIMEOUT = Duration.ofSeconds(10);

    private final BrowserType browser;
    private final boolean headless;
    private final String baseUrl;
    private final Duration waitTimeout;

    /**
     * Creates a configuration with the default headless mode, base URL, and wait timeout.
     *
     * @param browser browser used for the test
     * @throws NullPointerException if browser is null
     */
    public FrameworkConfig(BrowserType browser) {
        this(browser, DEFAULT_HEADLESS, DEFAULT_BASE_URL, DEFAULT_WAIT_TIMEOUT);
    }

    /**
     * Creates a configuration with the default base URL and wait timeout.
     *
     * @param browser browser used for the test
     * @param headless whether the browser runs without a visible window
     * @throws NullPointerException if browser is null
     */
    public FrameworkConfig(BrowserType browser, boolean headless) {
        this(browser, headless, DEFAULT_BASE_URL, DEFAULT_WAIT_TIMEOUT);
    }

    /**
     * Creates a configuration with the default wait timeout.
     *
     * @param browser browser used for the test
     * @param headless whether the browser runs without a visible window
     * @param baseUrl application URL used by tests
     * @throws NullPointerException if browser or baseUrl is null
     */
    public FrameworkConfig(BrowserType browser, boolean headless, String baseUrl) {
        this(browser, headless, baseUrl, DEFAULT_WAIT_TIMEOUT);
    }

    /**
     * Creates a configuration snapshot.
     *
     * @param browser browser used for the test
     * @param headless whether the browser runs without a visible window
     * @param baseUrl application URL used by tests
     * @param waitTimeout default timeout owned by waits created from this configuration
     * @throws NullPointerException if browser, baseUrl, or waitTimeout is null
     * @throws IllegalArgumentException if waitTimeout is zero or negative
     */
    public FrameworkConfig(
            BrowserType browser,
            boolean headless,
            String baseUrl,
            Duration waitTimeout
    ) {
        this.browser = Objects.requireNonNull(browser, "Browser must not be null.");
        this.headless = headless;
        this.baseUrl = Objects.requireNonNull(baseUrl, "Base URL must not be null.");
        this.waitTimeout = Objects.requireNonNull(waitTimeout, "Wait timeout must not be null.");

        if (waitTimeout.isZero() || waitTimeout.isNegative()) {
            throw new IllegalArgumentException("Wait timeout must be greater than zero.");
        }
    }

    /**
     * Returns the configured browser.
     *
     * @return configured browser
     */
    public BrowserType getBrowser() {
        return browser;
    }

    /**
     * Returns whether the browser runs headlessly.
     *
     * @return {@code true} for headless execution
     */
    public boolean isHeadless() {
        return headless;
    }

    /**
     * Returns the configured application URL.
     *
     * @return application base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the default timeout used by wait-aware APIs.
     *
     * @return positive wait timeout
     */
    public Duration getWaitTimeout() {
        return waitTimeout;
    }
}
