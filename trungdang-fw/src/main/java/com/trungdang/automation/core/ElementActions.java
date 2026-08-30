package com.trungdang.automation.core;

import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Performs common element interactions with an appropriate explicit wait.
 */
public class ElementActions {

    private ElementActions() {
    }

    public static void click(By locator) {
        WaitManager.waitForClickable(locator).click();
    }

    public static void type(By locator, String text) {
        Objects.requireNonNull(text, "Text to enter must not be null.");

        WebElement element = WaitManager.waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    public static String getText(By locator) {
        return WaitManager.waitForVisible(locator).getText();
    }

    public static boolean isDisplayed(By locator) {
        return WaitManager.waitForVisible(locator).isDisplayed();
    }
}
