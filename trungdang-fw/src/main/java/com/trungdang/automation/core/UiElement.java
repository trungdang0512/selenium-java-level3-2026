package com.trungdang.automation.core;

import java.time.Duration;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * A locator-backed UI control whose actions find the element again on every
 * wait attempt. The associated {@link WaitManager} supplies both the driver
 * and the timeout used by this control.
 */
public class UiElement {

    private final By locator;
    private final WaitManager waitManager;

    /**
     * Creates an element with a new wait manager using the configured default timeout.
     *
     * @param driver the driver used to locate the element
     * @param locator the locator that identifies the element
     * @throws NullPointerException if {@code driver} or {@code locator} is null
     */
    public UiElement(WebDriver driver, By locator) {
        this(new WaitManager(driver), locator);
    }

    /**
     * Creates an element with a new wait manager using the supplied timeout.
     *
     * @param driver the driver used to locate the element
     * @param locator the locator that identifies the element
     * @param timeout the maximum time allowed for each operation
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if {@code timeout} is zero or negative
     */
    public UiElement(WebDriver driver, By locator, Duration timeout) {
        this(new WaitManager(driver, timeout), locator);
    }

    /**
     * Creates an element that uses an existing wait manager. The injected
     * manager owns the driver and timeout and may be shared by several elements.
     *
     * @param waitManager the wait manager used by all element operations
     * @param locator the locator that identifies the element
     * @throws NullPointerException if an argument is null
     */
    public UiElement(WaitManager waitManager, By locator) {
        this.waitManager = Objects.requireNonNull(
                waitManager,
                "WaitManager must not be null."
        );
        this.locator = WaitManager.requireLocator(locator);
    }

    /**
     * Clicks the first matching element after it becomes displayed and enabled.
     * The element is located again when a stale-element failure is retried.
     *
     * @throws org.openqa.selenium.TimeoutException if the element does not become clickable
     */
    public void click() {
        waitManager.waitForClickableAndExecute(locator, element -> element.click());
    }

    /**
     * Clears and types into the first matching visible element. The complete
     * callback may run again if the element becomes stale during the operation.
     *
     * @param text the text to enter
     * @throws NullPointerException if {@code text} is null
     * @throws org.openqa.selenium.TimeoutException if the element does not become visible
     */
    public void type(String text) {
        String checkedText = Objects.requireNonNull(text, "Text must not be null.");

        waitManager.waitForVisibleAndExecute(
                locator,
                element -> {
                    element.clear();
                    element.sendKeys(checkedText);
                }
        );
    }

    /**
     * Returns the text of the first matching visible element.
     *
     * @return the element text
     * @throws org.openqa.selenium.TimeoutException if the element does not become visible
     */
    public String getText() {
        return waitManager.waitForVisibleAndGet(locator, element -> element.getText());
    }

    /**
     * Checks whether the first matching element becomes visible before the timeout.
     *
     * @return {@code true} when the element becomes visible; otherwise {@code false}
     */
    public boolean isDisplayed() {
        return waitManager.isVisible(locator);
    }

    By getLocator() {
        return locator;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "UiElement{" + locator + '}';
    }
}
