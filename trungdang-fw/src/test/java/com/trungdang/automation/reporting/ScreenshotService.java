package com.trungdang.automation.reporting;

import com.trungdang.automation.driver.DriverManager;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Adds browser artifacts to the current Allure test result.
 */
public final class ScreenshotService {

    private ScreenshotService() {
    }

    public static void attachFailureArtifacts() {
        WebDriver driver;

        try {
            driver = DriverManager.getDriver();
        } catch (RuntimeException exception) {
            attachUnavailable("Browser artifacts", exception);
            return;
        }

        attachScreenshot(driver);
        attachPageSource(driver);
        attachCurrentUrl(driver);
    }

    private static void attachScreenshot(WebDriver driver) {
        try {
            if (!(driver instanceof TakesScreenshot screenshotDriver)) {
                return;
            }

            byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(
                    "Failure screenshot",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );
        } catch (RuntimeException exception) {
            attachUnavailable("Screenshot", exception);
        }
    }

    private static void attachPageSource(WebDriver driver) {
        try {
            Allure.addAttachment("Page source", "text/html", driver.getPageSource(), ".html");
        } catch (RuntimeException exception) {
            attachUnavailable("Page source", exception);
        }
    }

    private static void attachCurrentUrl(WebDriver driver) {
        try {
            Allure.addAttachment("Current URL", "text/plain", driver.getCurrentUrl(), ".txt");
        } catch (RuntimeException exception) {
            attachUnavailable("Current URL", exception);
        }
    }

    private static void attachUnavailable(String artifactName, RuntimeException exception) {
        Allure.addAttachment(
                artifactName + " unavailable",
                "text/plain",
                exception.toString(),
                ".txt"
        );
    }
}
