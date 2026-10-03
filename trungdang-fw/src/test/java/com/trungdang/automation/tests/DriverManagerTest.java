package com.trungdang.automation.tests;

import com.trungdang.automation.config.FrameworkConfig;
import com.trungdang.automation.driver.DriverManager;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Verifies direct use without inheriting BaseTest, including concurrent sessions. */
public class DriverManagerTest {

    private final DriverManager manager = new DriverManager();
    private final CyclicBarrier started = new CyclicBarrier(2);
    private final Set<String> sessionIds = ConcurrentHashMap.newKeySet();

    @DataProvider(name = "sessions", parallel = true)
    public Object[][] sessions() {
        return new Object[][] {{"first"}, {"second"}};
    }

    @Test(dataProvider = "sessions")
    public void isolatesSessionsAndCleansUpWithoutBaseTest(String name) throws Exception {
        FrameworkConfig config = new FrameworkConfig("chrome", true, "about:blank#" + name);
        Assert.expectThrows(IllegalStateException.class, manager::getDriver);
        Assert.expectThrows(IllegalStateException.class, manager::getConfig);
        try {
            manager.start(config);
            WebDriver driver = manager.getDriver();
            Assert.assertTrue(sessionIds.add(((RemoteWebDriver) driver).getSessionId().toString()));
            started.await(60, TimeUnit.SECONDS);
            Assert.assertSame(manager.getDriver(), driver);
            Assert.assertSame(manager.getConfig(), config);
            Assert.expectThrows(IllegalStateException.class, () -> manager.start(config));
            Assert.assertSame(manager.getDriver(), driver);
            driver.get(config.getBaseUrl());
            Assert.assertEquals(driver.getCurrentUrl(), config.getBaseUrl());
        } finally {
            manager.quit();
        }
        Assert.expectThrows(IllegalStateException.class, manager::getDriver);
        Assert.expectThrows(IllegalStateException.class, manager::getConfig);
        manager.quit();
    }

    @Test
    public void failedStartLeavesNoConfiguration() {
        DriverManager independentManager = new DriverManager();
        Assert.expectThrows(NullPointerException.class, () -> independentManager.start(null));
        Assert.expectThrows(IllegalStateException.class, independentManager::getDriver);
        Assert.expectThrows(IllegalStateException.class, independentManager::getConfig);
        independentManager.quit();
    }
}
