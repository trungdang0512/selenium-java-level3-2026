package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import org.openqa.selenium.WebDriver;

/** Creates browser sessions without depending on a test runner or BaseTest. */
public interface BrowserProvider {

    /**
     * Creates a new, non-null driver for each call. Implementations must support concurrent
     * calls if used by parallel tests, and clean up any resources acquired before a failure.
     */
    WebDriver create(FrameworkConfig config);
}
