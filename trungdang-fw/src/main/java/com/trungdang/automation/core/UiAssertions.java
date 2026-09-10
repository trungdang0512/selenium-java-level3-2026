package com.trungdang.automation.core;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
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

    public void assertVisible(UiElement element) {
        UiElement checkedElement = requireElement(element);
        By locator = checkedElement.getLocator();

        waitForAssertion(
                locator,
                "to be visible",
                currentDriver -> currentDriver.findElement(locator).isDisplayed()
        );
    }

    public void assertTextEquals(UiElement element, String expectedText) {
        UiElement checkedElement = requireElement(element);
        String checkedExpectedText = Objects.requireNonNull(
                expectedText,
                "Expected text must not be null."
        );
        By locator = checkedElement.getLocator();
        AtomicReference<String> lastActualText = new AtomicReference<>("<not found>");

        waitForAssertion(
                locator,
                "to have text \"" + checkedExpectedText + "\"",
                currentDriver -> {
                    String actualText = currentDriver.findElement(locator).getText();
                    lastActualText.set(actualText);
                    return checkedExpectedText.equals(actualText);
                },
                () -> ", but its last text was \"" + lastActualText.get() + "\""
        );
    }

    private UiElement requireElement(UiElement element) {
        return Objects.requireNonNull(element, "UiElement must not be null.");
    }

    private void waitForAssertion(
            By locator,
            String expectedCondition,
            Function<WebDriver, Boolean> condition
    ) {
        waitForAssertion(locator, expectedCondition, condition, () -> "");
    }

    private void waitForAssertion(
            By locator,
            String expectedCondition,
            Function<WebDriver, Boolean> condition,
            Supplier<String> failureDetails
    ) {
        try {
            new WebDriverWait(driver, timeout)
                    .ignoring(StaleElementReferenceException.class)
                    .until(condition);
        } catch (TimeoutException exception) {
            throw new AssertionError(
                    "Assertion failed after " + timeout.toMillis()
                            + " ms: expected element " + locator + " "
                            + expectedCondition + failureDetails.get() + ".",
                    exception
            );
        }
    }
}
