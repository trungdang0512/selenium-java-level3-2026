package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;

/**
 * Provides browser-specific local driver creation and capabilities.
 */
interface BrowserProvider {

    WebDriver createLocalDriver(FrameworkConfig config);

    MutableCapabilities createOptions(FrameworkConfig config);
}
