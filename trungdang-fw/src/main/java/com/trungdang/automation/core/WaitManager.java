package com.trungdang.automation.core;

import com.trungdang.automation.driver.DriverManager;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
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

    /** Waits for a visible element and retries the action if the element becomes stale. */
    public static <T> T waitForVisibleAndExecute(By locator, Function<WebElement, T> action) {
        return waitAndExecute(
                ExpectedConditions.visibilityOfElementLocated(requireLocator(locator)),
                action
        );
    }

    /** Waits for a clickable element and retries the action if the element becomes stale. */
    public static <T> T waitForClickableAndExecute(By locator, Function<WebElement, T> action) {
        return waitAndExecute(
                ExpectedConditions.elementToBeClickable(requireLocator(locator)),
                action
        );
    }

    /** Waits until the current document has finished loading. */
    public static void waitForPageReady() {
        createWait().until(driver -> "complete".equals(
                ((JavascriptExecutor) driver).executeScript("return document.readyState")
        ));
    }

    private static WebDriverWait createWait() {
        Duration timeout = Duration.ofSeconds(ELEMENT_WAIT_SECONDS);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        wait.ignoring(StaleElementReferenceException.class);
        return wait;
    }

    private static <T> T waitAndExecute(
            ExpectedCondition<WebElement> elementCondition,
            Function<WebElement, T> action
    ) {
        Objects.requireNonNull(action, "Element action must not be null.");

        return createWait().until(driver -> {
            WebElement element = elementCondition.apply(driver);

            if (element == null) {
                return null;
            }

            return action.apply(element);
        });
    }

    /** Validates a locator for wait and assertion helpers in the core package. */
    static By requireLocator(By locator) {
        return Objects.requireNonNull(locator, "Locator must not be null.");
    }
}
