package com.trungdang.automation.core;

import java.time.Duration;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class UiElement {

    private final By locator;
    private final WaitManager waitManager;

    public UiElement(WebDriver driver, By locator) {
        this(new WaitManager(driver), locator);
    }

    public UiElement(WebDriver driver, By locator, Duration timeout) {
        this(new WaitManager(driver, timeout), locator);
    }

    public UiElement(WaitManager waitManager, By locator) {
        this.waitManager = Objects.requireNonNull(
                waitManager,
                "WaitManager must not be null."
        );
        this.locator = WaitManager.requireLocator(locator);
    }

    public void click() {
        waitManager.waitForClickableAndExecute(locator, element -> element.click());
    }

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

    public String getText() {
        return waitManager.waitForVisibleAndGet(locator, element -> element.getText());
    }

    public boolean isDisplayed() {
        return waitManager.isVisible(locator);
    }

    By getLocator() {
        return locator;
    }

    @Override
    public String toString() {
        return "UiElement{" + locator + '}';
    }
}
