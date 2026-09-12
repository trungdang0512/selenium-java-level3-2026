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

public class UiAssertions {

    private final WebDriver driver;
    private final Duration timeout;

    public UiAssertions(WebDriver driver) {
        this(driver, WaitManager.DEFAULT_TIMEOUT);
    }

    public UiAssertions(WebDriver driver, Duration timeout) {
        this.driver = Objects.requireNonNull(driver, "WebDriver must not be null.");
        this.timeout = WaitManager.requirePositiveTimeout(timeout);
    }

    public void assertPresent(UiElement element) {
        UiElement checkedElement = requireElement(element);
        By locator = checkedElement.getLocator();

        waitForAssertion(
                locator,
                "to be present",
                currentDriver -> !currentDriver.findElements(locator).isEmpty()
        );
    }

    public void assertVisible(UiElement element) {
        assertThat(
                element,
                "to be visible",
                WebElement::isDisplayed
        );
    }

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
