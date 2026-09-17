package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import org.openqa.selenium.WebDriver;

public interface BrowserProvider {

    WebDriver create(FrameworkConfig config);
}
