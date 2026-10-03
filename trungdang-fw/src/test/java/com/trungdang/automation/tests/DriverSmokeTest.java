package com.trungdang.automation.tests;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DriverSmokeTest extends BaseTest {

    @Test
    public void shouldStartChromeDriverAndOpenTestApplication() {
        WebDriver driver = getDriver();
        String baseUrl = getConfig().getBaseUrl();

        driver.get(baseUrl);

        Assert.assertFalse(
                driver.getWindowHandle().isBlank(),
                "Chrome session should have a window handle."
        );
        Assert.assertTrue(
                driver.getCurrentUrl().startsWith(baseUrl),
                "Chrome should open the configured base URL."
        );
    }
}
