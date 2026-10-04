package com.trungdang.automation.tests;

import com.trungdang.automation.config.ConfigManager;
import com.trungdang.automation.config.FrameworkConfig;
import com.trungdang.automation.core.UiAssertions;
import com.trungdang.automation.core.WaitManager;
import com.trungdang.automation.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/** Optional TestNG lifecycle adapter with convenience access to the current session. */
public class BaseTest {

    private final DriverManager driverManager = new DriverManager();

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driverManager.start(ConfigManager.load());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        driverManager.quit();
    }

    protected WebDriver getDriver() {
        return driverManager.getDriver();
    }

    protected FrameworkConfig getConfig() {
        return driverManager.getConfig();
    }

    /**
     * Creates a new wait manager for the current thread's driver and configured timeout.
     * Retain the returned instance locally when several elements should share it.
     *
     * @return a wait manager bound to the current session
     * @throws IllegalStateException if no browser is running on the current thread
     */
    protected WaitManager getWaitManager() {
        return new WaitManager(getDriver(), getConfig().getWaitTimeout());
    }

    /**
     * Creates new assertions for the current thread's driver and configured timeout.
     * The returned instance must not be reused after the browser session ends.
     *
     * @return assertions bound to the current session
     * @throws IllegalStateException if no browser is running on the current thread
     */
    protected UiAssertions getUiAssertions() {
        return new UiAssertions(getDriver(), getConfig().getWaitTimeout());
    }
}
