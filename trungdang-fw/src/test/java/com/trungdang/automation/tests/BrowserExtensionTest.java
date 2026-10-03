package com.trungdang.automation.tests;

import com.trungdang.automation.config.FrameworkConfig;
import com.trungdang.automation.driver.BrowserProvider;
import com.trungdang.automation.driver.BrowserRegistry;
import com.trungdang.automation.driver.ChromeProvider;
import com.trungdang.automation.driver.DriverManager;
import com.trungdang.automation.driver.WebDriverFactory;
import java.lang.reflect.Proxy;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Exercises extension and lifecycle contracts without launching an external browser. */
public class BrowserExtensionTest {

    @Test
    public void delegatesToCustomProviderWithOriginalConfiguration() {
        FrameworkConfig config = new FrameworkConfig(" Custom ", true, "about:blank");
        AtomicInteger closed = new AtomicInteger();
        WebDriver expected = fakeDriver(closed, false);
        BrowserRegistry registry = new BrowserRegistry();
        registry.register("CUSTOM", actual -> {
            Assert.assertSame(actual, config);
            return expected;
        });
        DriverManager manager = new DriverManager(new WebDriverFactory(registry));
        try {
            manager.start(config);
            Assert.assertSame(manager.getDriver(), expected);
            Assert.assertSame(manager.getConfig(), config);
        } finally {
            manager.quit();
        }
        Assert.assertEquals(closed.get(), 1);
        assertEmpty(manager);
    }

    @Test
    public void registriesAreIndependentAndDuplicateNamesAreRejected() {
        BrowserRegistry first = BrowserRegistry.withDefaults();
        BrowserRegistry second = BrowserRegistry.withDefaults();
        BrowserProvider provider = config -> fakeDriver(new AtomicInteger(), false);
        first.register(" Custom ", provider);
        Assert.assertSame(first.getProvider("CUSTOM"), provider);
        Assert.assertTrue(second.getProvider("chrome") instanceof ChromeProvider);
        Assert.expectThrows(IllegalArgumentException.class, () -> first.register("custom", provider));
        Assert.assertSame(first.getProvider("custom"), provider);
        IllegalArgumentException error = Assert.expectThrows(
                IllegalArgumentException.class, () -> second.getProvider("custom"));
        Assert.assertTrue(error.getMessage().contains("custom"));
        Assert.assertTrue(error.getMessage().contains("chrome"));
        second.register("custom", config -> fakeDriver(new AtomicInteger(), false));
        Assert.assertNotSame(first.getProvider("custom"), second.getProvider("custom"));
    }

    @Test
    public void rejectsInvalidRegistrationAndConfiguration() {
        BrowserRegistry registry = new BrowserRegistry();
        BrowserProvider provider = config -> fakeDriver(new AtomicInteger(), false);
        Assert.expectThrows(IllegalArgumentException.class, () -> registry.register("  ", provider));
        Assert.expectThrows(NullPointerException.class, () -> registry.register(null, provider));
        Assert.expectThrows(NullPointerException.class, () -> registry.register("custom", null));
        Assert.expectThrows(IllegalArgumentException.class, () -> new FrameworkConfig("  "));
        Assert.expectThrows(NullPointerException.class, () -> new FrameworkConfig(null));
        registry.register("custom", provider);
        Assert.assertSame(registry.getProvider("custom"), provider);
    }

    @Test
    public void missingOrFailingProvidersLeaveNoActiveState() {
        BrowserRegistry registry = new BrowserRegistry();
        DriverManager manager = new DriverManager(new WebDriverFactory(registry));
        FrameworkConfig config = new FrameworkConfig("custom");
        Assert.expectThrows(IllegalArgumentException.class, () -> manager.start(config));
        assertEmpty(manager);
        registry.register("custom", actual -> { throw new IllegalStateException("startup failed"); });
        Assert.expectThrows(IllegalStateException.class, () -> manager.start(config));
        assertEmpty(manager);
        registry.register("null-driver", actual -> null);
        Assert.expectThrows(NullPointerException.class,
                () -> manager.start(new FrameworkConfig("null-driver")));
        assertEmpty(manager);
        manager.quit();
    }

    @Test
    public void quitFailureClearsStateAndAllowsRestart() {
        AtomicInteger closed = new AtomicInteger();
        BrowserRegistry registry = new BrowserRegistry();
        registry.register("custom", config -> fakeDriver(closed, true));
        DriverManager manager = new DriverManager(new WebDriverFactory(registry));
        for (int i = 0; i < 2; i++) {
            manager.start(new FrameworkConfig("custom"));
            Assert.expectThrows(IllegalStateException.class, manager::quit);
            assertEmpty(manager);
            manager.quit();
        }
        Assert.assertEquals(closed.get(), 2);
    }

    @Test
    public void sharedManagerIsolatesConcurrentThreads() throws Exception {
        AtomicInteger created = new AtomicInteger();
        AtomicInteger closed = new AtomicInteger();
        BrowserRegistry registry = new BrowserRegistry();
        registry.register("custom", config -> {
            created.incrementAndGet();
            return fakeDriver(closed, false);
        });
        DriverManager manager = new DriverManager(new WebDriverFactory(registry));
        CyclicBarrier started = new CyclicBarrier(2);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> exerciseSession(manager, started, "first"));
            var second = executor.submit(() -> exerciseSession(manager, started, "second"));
            Assert.assertNotSame(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            Assert.assertEquals(created.get(), 2);
            Assert.assertEquals(closed.get(), 2);
            assertEmpty(manager);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static WebDriver exerciseSession(DriverManager manager, CyclicBarrier started,
                                            String name) throws Exception {
        FrameworkConfig config = new FrameworkConfig("custom", true, "about:blank#" + name);
        try {
            manager.start(config);
            WebDriver driver = manager.getDriver();
            started.await(10, TimeUnit.SECONDS);
            Assert.assertSame(manager.getDriver(), driver);
            Assert.assertSame(manager.getConfig(), config);
            Assert.expectThrows(IllegalStateException.class, () -> manager.start(config));
            Assert.assertSame(manager.getDriver(), driver);
            return driver;
        } finally {
            manager.quit();
            assertEmpty(manager);
        }
    }

    private static void assertEmpty(DriverManager manager) {
        Assert.expectThrows(IllegalStateException.class, manager::getDriver);
        Assert.expectThrows(IllegalStateException.class, manager::getConfig);
    }

    private static WebDriver fakeDriver(AtomicInteger closed, boolean failOnQuit) {
        return (WebDriver) Proxy.newProxyInstance(
                WebDriver.class.getClassLoader(), new Class<?>[] {WebDriver.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("quit")) {
                        closed.incrementAndGet();
                        if (failOnQuit) {
                            throw new IllegalStateException("quit failed");
                        }
                        return null;
                    }
                    if (method.getName().equals("toString")) {
                        return "FakeWebDriver@" + System.identityHashCode(proxy);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
