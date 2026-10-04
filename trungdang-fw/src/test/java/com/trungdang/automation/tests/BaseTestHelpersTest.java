package com.trungdang.automation.tests;

import com.trungdang.automation.config.FrameworkConfig;
import com.trungdang.automation.core.UiElement;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Verifies lazy UI helpers without running the inherited browser setup. */
public class BaseTestHelpersTest {
    @Test
    public void helpersRejectAccessOutsideAnActiveSession() {
        BaseTest base = new BaseTest();
        Assert.expectThrows(IllegalStateException.class, base::getWaitManager);
        Assert.expectThrows(IllegalStateException.class, base::getUiAssertions);
        base.tearDown();
        Assert.expectThrows(IllegalStateException.class, base::getWaitManager);
        Assert.expectThrows(IllegalStateException.class, base::getUiAssertions);
    }

    @Test
    public void helpersFollowCurrentDriverAndTimeoutInsteadOfCachingOldSession() {
        HelperHarness base = new HelperHarness();
        By locator = By.id("missing");
        for (int timeoutMillis : new int[] {1, 2}) {
            AtomicInteger lookups = new AtomicInteger();
            base.currentDriver = (WebDriver) Proxy.newProxyInstance(WebDriver.class.getClassLoader(),
                    new Class<?>[] {WebDriver.class}, (proxy, method, args) -> {
                        if (method.getName().equals("findElements")) {
                            lookups.incrementAndGet();
                            return List.of();
                        }
                        if (method.getName().equals("toString")) return "HelperDriver";
                        throw new UnsupportedOperationException(method.getName());
                    });
            base.currentConfig = new FrameworkConfig("custom", true, "about:blank",
                    Duration.ofMillis(timeoutMillis));
            var waits = base.getWaitManager();
            Assert.assertSame(waits.waitFor(locator, driver -> driver), base.currentDriver);
            TimeoutException timeout = Assert.expectThrows(TimeoutException.class,
                    () -> waits.waitFor(locator, driver -> false));
            Assert.assertTrue(timeout.getMessage().contains("after " + timeoutMillis + " ms"));
            var assertions = base.getUiAssertions();
            AssertionError failure = Assert.expectThrows(AssertionError.class,
                    () -> assertions.assertPresent(new UiElement(waits, locator)));
            Assert.assertTrue(failure.getMessage().contains("after " + timeoutMillis + " ms"));
            Assert.assertTrue(lookups.get() > 0, "Assertions must use the current driver");
        }
    }

    private static final class HelperHarness extends BaseTest {
        private WebDriver currentDriver;
        private FrameworkConfig currentConfig;

        @Override
        protected WebDriver getDriver() {
            return currentDriver;
        }

        @Override
        protected FrameworkConfig getConfig() {
            return currentConfig;
        }
    }
}
