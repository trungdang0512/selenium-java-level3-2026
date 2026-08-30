package com.trungdang.automation.core;

import com.trungdang.automation.driver.DriverManager;
import java.time.Duration;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Provides reusable explicit waits for common web-element states.
 */
public class WaitManager {

    private static final long ELEMENT_WAIT_SECONDS = 15;

    private WaitManager() {
    }

    public static WebElement waitForPresence(By locator) {
        return createWait().until(ExpectedConditions.presenceOfElementLocated(requireLocator(locator)));
    }

    public static WebElement waitForVisible(By locator) {
        return createWait().until(ExpectedConditions.visibilityOfElementLocated(requireLocator(locator)));
    }

    public static WebElement waitForClickable(By locator) {
        return createWait().until(ExpectedConditions.elementToBeClickable(requireLocator(locator)));
    }

    public static boolean waitForInvisible(By locator) {
        return createWait().until(ExpectedConditions.invisibilityOfElementLocated(requireLocator(locator)));
    }

    /** Waits until the current document has finished loading. */
    public static void waitForPageReady() {
        createWait().until(driver -> "complete".equals(
                ((JavascriptExecutor) driver).executeScript("return document.readyState")
        ));
    }

    private static WebDriverWait createWait() {
        Duration timeout = Duration.ofSeconds(ELEMENT_WAIT_SECONDS);
        return new WebDriverWait(DriverManager.getDriver(), timeout);
    }

    private static By requireLocator(By locator) {
        return Objects.requireNonNull(locator, "Locator must not be null.");
    }
}
