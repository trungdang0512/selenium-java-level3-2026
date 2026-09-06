package com.trungdang.automation.pages;

import com.trungdang.automation.core.BrowserActions;

/**
 * Shared page-object operations for all application pages.
 */
public class BasePage {

    protected void open(String url) {
        BrowserActions.navigateTo(url);
    }
}
