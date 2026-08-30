package com.trungdang.automation.core;

import com.trungdang.automation.driver.DriverManager;
import java.util.Objects;

/**
 * Performs browser-level navigation and reads browser state.
 */
public class BrowserActions {

    private BrowserActions() {
    }

    public static void navigateTo(String url) {
        String targetUrl = Objects.requireNonNull(url, "URL must not be null.").trim();

        if (targetUrl.isEmpty()) {
            throw new IllegalArgumentException("URL must not be blank.");
        }

        DriverManager.getDriver().navigate().to(targetUrl);
        WaitManager.waitForPageReady();
    }

    public static void refresh() {
        DriverManager.getDriver().navigate().refresh();
        WaitManager.waitForPageReady();
    }

    public static void goBack() {
        DriverManager.getDriver().navigate().back();
        WaitManager.waitForPageReady();
    }

    public static void goForward() {
        DriverManager.getDriver().navigate().forward();
        WaitManager.waitForPageReady();
    }

    public static String getCurrentUrl() {
        return DriverManager.getDriver().getCurrentUrl();
    }

    public static String getTitle() {
        return DriverManager.getDriver().getTitle();
    }
}
