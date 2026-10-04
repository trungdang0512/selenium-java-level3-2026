package com.trungdang.automation.core;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Performs explicit waits with one WebDriver and one fixed timeout.
 *
 * <p>Each poll locates elements again and retries
 * {@link StaleElementReferenceException}. A manager never replaces its driver or
 * timeout after construction.
 */
public class WaitManager {

    /**
     * Default timeout used when callers do not supply one explicitly.
     */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final WebDriver driver;
    private final Duration timeout;

    /**
     * Creates a manager that owns the default timeout.
     *
     * @param driver driver used for every wait
     * @throws NullPointerException if driver is null
     */
    public WaitManager(WebDriver driver) {
        this(driver, DEFAULT_TIMEOUT);
    }

    /**
     * Creates a manager that owns the supplied timeout.
     *
     * @param driver driver used for every wait
     * @param timeout fixed timeout used by this manager
     * @throws NullPointerException if driver or timeout is null
     * @throws IllegalArgumentException if timeout is zero or negative
     */
    public WaitManager(WebDriver driver, Duration timeout) {
        this.driver = Objects.requireNonNull(driver, "WebDriver must not be null.");
        this.timeout = requirePositiveTimeout(timeout);
    }

    /**
     * Waits for the first element matching the locator to be displayed.
     *
     * @param locator locator evaluated on each poll
     * @return the displayed element
     * @throws NullPointerException if locator is null
     * @throws TimeoutException if no matching element becomes visible in time
     */
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

    /**
     * Waits for a visible element and executes an action inside the wait.
     *
     * <p>The action may run more than once when a stale element causes a retry.
     *
     * @param locator locator evaluated on each poll
     * @param action action performed on the first visible matching element
     * @throws NullPointerException if locator or action is null
     * @throws TimeoutException if visibility and the action do not complete in time
     */
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

    /**
     * Waits for a displayed and enabled element and executes an input action.
     *
     * <p>A transient invalid element state is retried until the action succeeds
     * or the timeout expires. The element is located again on every attempt.
     *
     * @param locator locator evaluated on each poll
     * @param action input action performed on the first interactable matching element
     * @throws NullPointerException if locator or action is null
     * @throws TimeoutException if the element does not become ready for the action in time
     */
    public void waitForInteractableAndExecute(By locator, Consumer<WebElement> action) {
        By checkedLocator = requireLocator(locator);
        Consumer<WebElement> checkedAction = Objects.requireNonNull(
                action,
                "Element action must not be null."
        );

        waitUntil(
                checkedLocator,
                "be ready for input and complete the action",
                currentDriver -> {
                    WebElement element = currentDriver.findElement(checkedLocator);

                    if (!element.isDisplayed() || !element.isEnabled()) {
                        return false;
                    }

                    try {
                        checkedAction.accept(element);
                        return true;
                    } catch (InvalidElementStateException exception) {
                        return false;
                    }
                }
        );
    }

    /**
     * Waits for a visible element and returns a value produced from it.
     *
     * <p>The action may run more than once when a stale element causes a retry.
     * Returning {@code null} continues waiting. Boolean {@code false} is returned
     * as a valid value rather than being treated as an unsatisfied wait condition.
     *
     * @param locator locator evaluated on each poll
     * @param action function applied to the first visible matching element
     * @param <T> returned value type
     * @return the first non-null value produced by the action
     * @throws NullPointerException if locator or action is null
     * @throws TimeoutException if no non-null value is produced in time
     */
    public <T> T waitForVisibleAndGet(By locator, Function<WebElement, T> action) {
        By checkedLocator = requireLocator(locator);
        Function<WebElement, T> checkedAction = Objects.requireNonNull(
                action,
                "Element action must not be null."
        );

        WaitValue<T> result = waitUntil(
                checkedLocator,
                "be visible and return the requested value",
                currentDriver -> {
                    WebElement element = currentDriver.findElement(checkedLocator);

                    if (!element.isDisplayed()) {
                        return null;
                    }

                    T value = checkedAction.apply(element);
                    return value == null ? null : new WaitValue<>(value);
                }
        );

        return result.value();
    }

    /**
     * Waits for a displayed and enabled element and executes an action inside the wait.
     *
     * <p>The action may run more than once when a stale element or temporary
     * click interception causes a retry. On the first interception, a JavaScript-capable
     * driver attempts to scroll the element to the viewport center once per invocation.
     * The next poll locates the element again and retries the action within the same wait.
     * Drivers without JavaScript support continue retrying without scrolling. A stale
     * element during scrolling is retried; other script failures propagate to the caller.
     *
     * @param locator locator evaluated on each poll
     * @param action action performed on the first clickable matching element
     * @throws NullPointerException if locator or action is null
     * @throws TimeoutException if clickability and the action do not complete in time
     */
    public void waitForClickableAndExecute(By locator, Consumer<WebElement> action) {
        By checkedLocator = requireLocator(locator);
        Consumer<WebElement> checkedAction = Objects.requireNonNull(
                action,
                "Element action must not be null."
        );

        waitUntil(
                checkedLocator,
                "be clickable and complete the action",
                new Function<WebDriver, Boolean>() {
                    private boolean scrollAttempted;

                    @Override
                    public Boolean apply(WebDriver currentDriver) {
                        WebElement element = currentDriver.findElement(checkedLocator);
                        if (!element.isDisplayed() || !element.isEnabled()) {
                            return false;
                        }
                        try {
                            checkedAction.accept(element);
                            return true;
                        } catch (ElementClickInterceptedException exception) {
                            if (!scrollAttempted && currentDriver instanceof JavascriptExecutor executor) {
                                scrollAttempted = true;
                                executor.executeScript(
                                        "arguments[0].scrollIntoView({block: 'center', inline: 'center', behavior: 'instant'});",
                                        element
                                );
                            }
                            return false;
                        }
                    }
                }
        );
    }

    /**
     * Reports whether the first matching element becomes visible before the timeout.
     *
     * @param locator locator evaluated on each poll
     * @return {@code true} when an element becomes visible; {@code false} on timeout
     * @throws NullPointerException if locator is null
     */
    public boolean isVisible(By locator) {
        try {
            waitForVisible(locator);
            return true;
        } catch (TimeoutException exception) {
            return false;
        }
    }

    /**
     * Repeatedly evaluates a custom condition until it succeeds or the timeout expires.
     *
     * <p>The callback may run multiple times and should tolerate repeated evaluation.
     * Returning {@code null} or Boolean {@code false} continues waiting. Any other
     * non-null value, or Boolean {@code true}, completes the wait. A stale element
     * failure is ignored and retried.
     *
     * @param locator locator included in timeout diagnostics
     * @param condition condition evaluated with this manager's driver
     * @param <T> type returned by the condition
     * @return the value that satisfied the condition
     * @throws NullPointerException if locator or condition is null
     * @throws TimeoutException if the condition does not succeed before the timeout
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

    private record WaitValue<T>(T value) {
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
