package com.trungdang.automation.tests;

import com.trungdang.automation.testdata.TestDataReader;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SmokeTest extends BaseTest {

    private static final String URL_DATA_FILE = "test-data/url.json";

    @Test
    public void shouldStartChromeAndOpenTestApplication() {
        String loginUrl = TestDataReader.readByKey(
                URL_DATA_FILE,
                "loginUrl",
                String.class
        );
        WebDriver driver = getDriver();

        driver.get(loginUrl);

        Assert.assertFalse(
                driver.getWindowHandle().isBlank(),
                "Chrome session should have a window handle."
        );
        Assert.assertTrue(
                driver.getCurrentUrl().startsWith(loginUrl),
                "Chrome should open the URL from " + URL_DATA_FILE + "."
        );
    }
}
