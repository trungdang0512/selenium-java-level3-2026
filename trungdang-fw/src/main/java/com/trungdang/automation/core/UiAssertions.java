package com.trungdang.automation.core;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * UI assertions that repeatedly locate elements until a condition passes or
 * this instance's timeout expires. Stale-element failures are retried; a
 * timeout is reported as an {@link AssertionError}.
 */
public class UiAssertions {

    private final WebDriver driver;
    private final Duration timeout;

    /**
     * Creates assertions with the configured default timeout. This timeout is
     * owned by the assertion object and is independent of any {@link UiElement}
     * wait manager.
     *
     * @param driver the driver used to locate asserted elements
     * @throws NullPointerException if {@code driver} is null
     */
    public UiAssertions(WebDriver driver) {
        this(driver, WaitManager.DEFAULT_TIMEOUT);
    }

    /**
     * Creates assertions with a fixed timeout owned by this assertion object.
     *
     * @param driver the driver used to locate asserted elements
     * @param timeout the maximum time allowed for each assertion
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if {@code timeout} is zero or negative
     */
    public UiAssertions(WebDriver driver, Duration timeout) {
        this.driver = Objects.requireNonNull(driver, "WebDriver must not be null.");
        this.timeout = WaitManager.requirePositiveTimeout(timeout);
    }

    /**
     * Asserts that at least one element matching the locator exists in the DOM.
     * Visibility is not required.
     *
     * @param element the locator-backed element to assert
     * @throws NullPointerException if {@code element} is null
     * @throws AssertionError if no matching element appears before the timeout
     */
    public void assertPresent(UiElement element) {
        UiElement checkedElement = requireElement(element);
        By locator = checkedElement.getLocator();

        waitForAssertion(
                locator,
                "to be present",
                currentDriver -> !currentDriver.findElements(locator).isEmpty()
        );
    }

    /**
     * Asserts that the first matching element becomes visible.
     *
     * @param element the locator-backed element to assert
     * @throws NullPointerException if {@code element} is null
     * @throws AssertionError if the element does not become visible before the timeout
     */
    public void assertVisible(UiElement element) {
        assertThat(
                element,
                "to be visible",
                WebElement::isDisplayed
        );
    }

    /**
     * Asserts that there are no matching elements or that all matches are hidden.
     *
     * @param element the locator-backed element to assert
     * @throws NullPointerException if {@code element} is null
     * @throws AssertionError if any matching element remains visible until the timeout
     */
    public void assertNotVisible(UiElement element) {
        UiElement checkedElement = requireElement(element);
        By locator = checkedElement.getLocator();

        waitForAssertion(
                locator,
                "to be absent or not visible",
                currentDriver -> currentDriver.findElements(locator)
                        .stream()
                        .noneMatch(WebElement::isDisplayed)
        );
    }

    /**
     * Asserts that the first matching element has exactly the expected text.
     *
     * @param element the locator-backed element to assert
     * @param expectedText the complete expected text
     * @throws NullPointerException if an argument is null
     * @throws AssertionError if the text does not match before the timeout
     */
    public void assertTextEquals(UiElement element, String expectedText) {
        UiElement checkedElement = requireElement(element);
        String checkedExpectedText = Objects.requireNonNull(
                expectedText,
                "Expected text must not be null."
        );

        assertThat(
                checkedElement,
                "to have text \"" + checkedExpectedText + "\"",
                targetElement -> checkedExpectedText.equals(targetElement.getText())
        );
    }

    /**
     * Asserts that the text of the first matching element contains a value.
     *
     * @param element the locator-backed element to assert
     * @param expectedText the text fragment to find
     * @throws NullPointerException if an argument is null
     * @throws AssertionError if the text does not contain the value before the timeout
     */
    public void assertTextContains(UiElement element, String expectedText) {
        UiElement checkedElement = requireElement(element);
        String checkedExpectedText = Objects.requireNonNull(
                expectedText,
                "Expected text must not be null."
        );

        assertThat(
                checkedElement,
                "to contain text \"" + checkedExpectedText + "\"",
                targetElement -> targetElement.getText().contains(checkedExpectedText)
        );
    }

    /**
     * Asserts that an attribute of the first matching element equals a value.
     *
     * @param element the locator-backed element to assert
     * @param attributeName the non-blank attribute name
     * @param expectedValue the expected attribute value
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if {@code attributeName} is blank
     * @throws AssertionError if the attribute does not match before the timeout
     */
    public void assertAttribute(
            UiElement element,
            String attributeName,
            String expectedValue
    ) {
        UiElement checkedElement = requireElement(element);
        String checkedAttributeName = requireNonBlank(
                attributeName,
                "Attribute name must not be blank."
        );
        String checkedExpectedValue = Objects.requireNonNull(
                expectedValue,
                "Expected attribute value must not be null."
        );

        assertThat(
                checkedElement,
                "to have attribute \"" + checkedAttributeName
                        + "\" equal to \"" + checkedExpectedValue + "\"",
                targetElement -> checkedExpectedValue.equals(
                        targetElement.getAttribute(checkedAttributeName)
                )
        );
    }

    /**
     * Asserts a custom condition against the first matching element. Selenium
     * may invoke the predicate repeatedly, so it should be safe to repeat and
     * should not rely on side effects. Stale-element failures are retried.
     *
     * @param element the locator-backed element to assert
     * @param expectedCondition a non-blank description used in failure messages
     * @param condition the predicate that returns {@code true} when the assertion passes
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if {@code expectedCondition} is blank
     * @throws AssertionError if the condition does not pass before the timeout
     */
    public void assertThat(
            UiElement element,
            String expectedCondition,
            Predicate<WebElement> condition
    ) {
        UiElement checkedElement = requireElement(element);
        String checkedExpectedCondition = requireNonBlank(
                expectedCondition,
                "Expected condition must not be blank."
        );
        Predicate<WebElement> checkedCondition = Objects.requireNonNull(
                condition,
                "Assertion condition must not be null."
        );
        By locator = checkedElement.getLocator();

        waitForAssertion(
                locator,
                checkedExpectedCondition,
                currentDriver -> checkedCondition.test(currentDriver.findElement(locator))
        );
    }

    private UiElement requireElement(UiElement element) {
        return Objects.requireNonNull(element, "UiElement must not be null.");
    }

    private String requireNonBlank(String value, String message) {
        String checkedValue = Objects.requireNonNull(value, message).trim();

        if (checkedValue.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return checkedValue;
    }

    private void waitForAssertion(
            By locator,
            String expectedCondition,
            Function<WebDriver, Boolean> condition
    ) {
        try {
            new WebDriverWait(driver, timeout)
                    .ignoring(StaleElementReferenceException.class)
                    .until(condition);
        } catch (TimeoutException exception) {
            throw new AssertionError(
                    "Assertion failed after " + timeout.toMillis()
                            + " ms: expected element " + locator + " "
                            + expectedCondition + ".",
                    exception
            );
        }
    }
}
