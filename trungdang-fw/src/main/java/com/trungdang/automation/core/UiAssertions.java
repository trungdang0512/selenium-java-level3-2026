package com.trungdang.automation.core;

import com.trungdang.automation.driver.DriverManager;
import java.time.Duration;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Assertion helpers that wait for UI conditions before failing.
 */
public class UiAssertions {

    private static final long RETRY_WAIT_SECONDS = 5;

    private UiAssertions() {
    }

    /** Waits until the element is visible. */
    public static void assertDisplayed(UiElement element) {
        By locator = requireElement(element).getLocator();
        waitUntil(
                ExpectedConditions.visibilityOfElementLocated(locator),
                "Expected element to be displayed: " + locator
        );
    }

    /** Waits until the element text exactly matches the expected text. */
    public static void assertTextEquals(UiElement element, String expectedText) {
        By locator = requireElement(element).getLocator();
        String checkedText = Objects.requireNonNull(expectedText, "Expected text must not be null.");

        waitUntil(
                ExpectedConditions.textToBe(locator, checkedText),
                "Expected element " + locator + " to have text: " + checkedText
        );
    }

    /** Waits until the element text contains the expected text. */
    public static void assertTextContains(UiElement element, String expectedText) {
        By locator = requireElement(element).getLocator();
        String checkedText = Objects.requireNonNull(expectedText, "Expected text must not be null.");

        waitUntil(
                ExpectedConditions.textToBePresentInElementLocated(locator, checkedText),
                "Expected element " + locator + " to contain text: " + checkedText
        );
    }

    /** Waits until the current URL contains the expected value. */
    public static void assertUrlContains(String expectedUrlPart) {
        String checkedUrlPart = Objects.requireNonNull(
                expectedUrlPart,
                "Expected URL part must not be null."
        );

        waitUntil(
                ExpectedConditions.urlContains(checkedUrlPart),
                "Expected current URL to contain: " + checkedUrlPart
        );
    }

    /** Rejects a missing UI element before Selenium starts waiting. */
    private static UiElement requireElement(UiElement element) {
        return Objects.requireNonNull(element, "UI element must not be null.");
    }

    /** Waits once for an assertion condition and reports a clear assertion failure on timeout. */
    private static <T> void waitUntil(ExpectedCondition<T> condition, String failureMessage) {
        try {
            createWait().until(condition);
        } catch (TimeoutException exception) {
            AssertionError assertionError = new AssertionError(
                    failureMessage + ". Condition was not met within "
                            + RETRY_WAIT_SECONDS + " seconds."
            );
            assertionError.initCause(exception);
            throw assertionError;
        }
    }

    /** Creates one wait using the configured assertion timeout. */
    private static WebDriverWait createWait() {
        Duration timeout = Duration.ofSeconds(RETRY_WAIT_SECONDS);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        wait.pollingEvery(Duration.ofMillis(200));
        return wait;
    }

}
