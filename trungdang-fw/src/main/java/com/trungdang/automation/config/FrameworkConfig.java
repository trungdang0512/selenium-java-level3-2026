package com.trungdang.automation.config;

public class FrameworkConfig {

    private final BrowserType browser;
    private final ExecutionMode executionMode;
    private final boolean headless;
    private final String gridUrl;

    public FrameworkConfig(
            BrowserType browser,
            ExecutionMode executionMode,
            boolean headless,
            String gridUrl
    ) {
        this.browser = browser;
        this.executionMode = executionMode;
        this.headless = headless;
        this.gridUrl = gridUrl;
    }

    public BrowserType getBrowser() {
        return browser;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public boolean isHeadless() {
        return headless;
    }

    public String getGridUrl() {
        return gridUrl;
    }
}
