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
 * Assertion helpers that retry in three five-second wait windows.
 */
public class UiAssertions {

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_WAIT_SECONDS = 5;

    private UiAssertions() {
    }

    /** Waits until the element is visible. */
    public static void assertDisplayed(By locator) {
        By checkedLocator = requireLocator(locator);
        retryUntil(
                ExpectedConditions.visibilityOfElementLocated(checkedLocator),
                "Expected element to be displayed: " + checkedLocator
        );
    }

    /** Waits until the element text exactly matches the expected text. */
    public static void assertTextEquals(By locator, String expectedText) {
        By checkedLocator = requireLocator(locator);
        String checkedText = Objects.requireNonNull(expectedText, "Expected text must not be null.");

        retryUntil(
                ExpectedConditions.textToBe(checkedLocator, checkedText),
                "Expected element " + checkedLocator + " to have text: " + checkedText
        );
    }

    /** Waits until the element text contains the expected text. */
    public static void assertTextContains(By locator, String expectedText) {
        By checkedLocator = requireLocator(locator);
        String checkedText = Objects.requireNonNull(expectedText, "Expected text must not be null.");

        retryUntil(
                ExpectedConditions.textToBePresentInElementLocated(checkedLocator, checkedText),
                "Expected element " + checkedLocator + " to contain text: " + checkedText
        );
    }

    /** Waits until the current URL contains the expected value. */
    public static void assertUrlContains(String expectedUrlPart) {
        String checkedUrlPart = Objects.requireNonNull(
                expectedUrlPart,
                "Expected URL part must not be null."
        );

        retryUntil(
                ExpectedConditions.urlContains(checkedUrlPart),
                "Expected current URL to contain: " + checkedUrlPart
        );
    }

    /** Retries one assertion condition up to the configured fixed attempt count. */
    private static <T> void retryUntil(ExpectedCondition<T> condition, String failureMessage) {
        TimeoutException lastTimeout = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                createAttemptWait().until(condition);
                return;
            } catch (TimeoutException exception) {
                lastTimeout = exception;
            }
        }

        AssertionError assertionError = new AssertionError(
                failureMessage + ". Condition was not met after " + MAX_ATTEMPTS
                        + " attempts of " + RETRY_WAIT_SECONDS + " seconds each."
        );
        assertionError.initCause(lastTimeout);
        throw assertionError;
    }

    /** Creates one five-second wait window for an assertion attempt. */
    private static WebDriverWait createAttemptWait() {
        Duration timeout = Duration.ofSeconds(RETRY_WAIT_SECONDS);
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), timeout);
        wait.pollingEvery(Duration.ofMillis(200));
        return wait;
    }

    /** Rejects a missing locator before Selenium starts waiting. */
    private static By requireLocator(By locator) {
        return Objects.requireNonNull(locator, "Locator must not be null.");
    }
}
