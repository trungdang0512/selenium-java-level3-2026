package com.trungdang.automation.pages;

import com.trungdang.automation.core.BrowserActions;
import com.trungdang.automation.core.ElementActions;
import org.openqa.selenium.By;

/**
 * Shared page-object operations for all application pages.
 */
public class BasePage {

    protected void open(String url) {
        BrowserActions.navigateTo(url);
    }

    protected void click(By locator) {
        ElementActions.click(locator);
    }

    protected void type(By locator, String text) {
        ElementActions.type(locator, text);
    }

    protected String getText(By locator) {
        return ElementActions.getText(locator);
    }

    protected boolean isDisplayed(By locator) {
        return ElementActions.isDisplayed(locator);
    }
}
