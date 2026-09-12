package com.trungdang.automation.core;

import com.trungdang.automation.config.ConfigManager;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitManager {

    public static final Duration DEFAULT_TIMEOUT = ConfigManager.load().getWaitTimeout();

    private final WebDriver driver;
    private final Duration timeout;

    public WaitManager(WebDriver driver) {
        this(driver, DEFAULT_TIMEOUT);
    }

    public WaitManager(WebDriver driver, Duration timeout) {
        this.driver = Objects.requireNonNull(driver, "WebDriver must not be null.");
        this.timeout = requirePositiveTimeout(timeout);
    }

    public WebElement waitForVisible(By locator) {
        By checkedLocator = requireLocator(locator);

        return waitUntil(
                checkedLocator,
                "be visible",
                currentDriver -> {
                    WebElement element = currentDriver.findElement(checkedLocator);
                    return element.isDisplayed() ? element : null;
                }
        );
    }

    public void waitForVisibleAndExecute(By locator, Consumer<WebElement> action) {
        By checkedLocator = requireLocator(locator);
        Consumer<WebElement> checkedAction = Objects.requireNonNull(
                action,
                "Element action must not be null."
        );

        waitUntil(
                checkedLocator,
                "be visible and complete the action",
                currentDriver -> {
                    WebElement element = currentDriver.findElement(checkedLocator);

                    if (!element.isDisplayed()) {
                        return false;
                    }

                    checkedAction.accept(element);
                    return true;
                }
        );
    }

    public <T> T waitForVisibleAndGet(By locator, Function<WebElement, T> action) {
        By checkedLocator = requireLocator(locator);
        Function<WebElement, T> checkedAction = Objects.requireNonNull(
                action,
                "Element action must not be null."
        );

        return waitUntil(
                checkedLocator,
                "be visible and return the requested value",
                currentDriver -> {
                    WebElement element = currentDriver.findElement(checkedLocator);

                    if (!element.isDisplayed()) {
                        return null;
                    }

                    return checkedAction.apply(element);
                }
        );
    }

    public void waitForClickableAndExecute(By locator, Consumer<WebElement> action) {
        By checkedLocator = requireLocator(locator);
        Consumer<WebElement> checkedAction = Objects.requireNonNull(
                action,
                "Element action must not be null."
        );

        waitUntil(
                checkedLocator,
                "be clickable and complete the action",
                currentDriver -> {
                    WebElement element = currentDriver.findElement(checkedLocator);

                    if (!element.isDisplayed() || !element.isEnabled()) {
                        return false;
                    }

                    checkedAction.accept(element);
                    return true;
                }
        );
    }

    public boolean isVisible(By locator) {
        try {
            waitForVisible(locator);
            return true;
        } catch (TimeoutException exception) {
            return false;
        }
    }

    /**
     * Waits until a user-defined condition returns a non-null value or {@code true}.
     *
     * @param locator locator included in timeout details
     * @param condition custom condition evaluated by Selenium
     * @param <T> value returned when the condition succeeds
     * @return the successful condition result
     */
    public <T> T waitFor(By locator, Function<WebDriver, T> condition) {
        By checkedLocator = requireLocator(locator);
        Function<WebDriver, T> checkedCondition = Objects.requireNonNull(
                condition,
                "Wait condition must not be null."
        );

        return waitUntil(
                checkedLocator,
                "match the custom condition",
                checkedCondition
        );
    }

    static By requireLocator(By locator) {
        return Objects.requireNonNull(locator, "Element locator must not be null.");
    }

    static Duration requirePositiveTimeout(Duration timeout) {
        Duration checkedTimeout = Objects.requireNonNull(timeout, "Timeout must not be null.");

        if (checkedTimeout.isZero() || checkedTimeout.isNegative()) {
            throw new IllegalArgumentException("Timeout must be greater than zero.");
        }

        return checkedTimeout;
    }

    private <T> T waitUntil(
            By locator,
            String expectedCondition,
            Function<WebDriver, T> condition
    ) {
        try {
            return new WebDriverWait(driver, timeout)
                    .ignoring(StaleElementReferenceException.class)
                    .until(condition);
        } catch (TimeoutException exception) {
            throw new TimeoutException(
                    "Timed out after " + timeout.toMillis()
                            + " ms waiting for element " + locator
                            + " to " + expectedCondition + ".",
                    exception
            );
        }
    }
}
