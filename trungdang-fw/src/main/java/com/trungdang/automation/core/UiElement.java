package com.trungdang.automation.core;

import java.util.Objects;
import org.openqa.selenium.By;

/**
 * Represents a UI element identified by a locator and performs waited interactions on it.
 */
public class UiElement {

    private final By locator;

    public UiElement(By locator) {
        this.locator = WaitManager.requireLocator(locator);
    }

    public void click() {
        WaitManager.waitForClickableAndExecute(locator, element -> {
            element.click();
            return true;
        });
    }

    public void type(String text) {
        Objects.requireNonNull(text, "Text to enter must not be null.");

        WaitManager.waitForVisibleAndExecute(locator, element -> {
            element.clear();
            element.sendKeys(text);
            return true;
        });
    }

    public String getText() {
        return WaitManager.waitForVisibleAndExecute(locator, element -> element.getText());
    }

    public boolean isDisplayed() {
        return WaitManager.waitForVisibleAndExecute(locator, element -> element.isDisplayed());
    }

    /** Provides the locator to assertion infrastructure in the core package. */
    By getLocator() {
        return locator;
    }
}
