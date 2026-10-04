package com.trungdang.automation.tests;

import com.trungdang.automation.config.ConfigManager;
import com.trungdang.automation.config.FrameworkConfig;
import com.trungdang.automation.core.UiElement;
import com.trungdang.automation.core.WaitManager;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Regression checks for the review dated September 18; no real browser is started. */
public class September18ReviewTest {
    private static final By LOCATOR = By.id("review-control");

    @Test
    public void shorterConstructorsPreserveDefaultsAndExplicitTimeout() {
        FrameworkConfig minimal = new FrameworkConfig("chrome");
        Assert.assertFalse(minimal.isHeadless());
        Assert.assertEquals(minimal.getWaitTimeout(), Duration.ofSeconds(10));
        Assert.assertEquals(minimal.getBaseUrl(), "https://demo.testarchitect.com/");
        Assert.assertTrue(new FrameworkConfig("chrome", true).isHeadless());
        Assert.assertEquals(new FrameworkConfig("chrome", true, "about:blank").getBaseUrl(), "about:blank");
        Assert.assertEquals(new FrameworkConfig("custom", false, "about:blank", Duration.ofSeconds(3))
                .getWaitTimeout(), Duration.ofSeconds(3));
        Assert.expectThrows(IllegalArgumentException.class,
                () -> new FrameworkConfig("chrome", false, "about:blank", Duration.ZERO));
    }

    @Test
    public void typingWaitsUntilEnabled() {
        Control control = new Control();
        control.disabledFirst = true;
        control.ui().type("hello");
        Assert.assertTrue(control.finds >= 2);
        Assert.assertEquals(control.clears, 1);
        Assert.assertEquals(control.text, "hello");
    }

    @DataProvider(name = "inputFailures")
    public Object[][] inputFailures() {
        return new Object[][] {{"clear"}, {"sendKeys"}};
    }

    @Test(dataProvider = "inputFailures")
    public void typingRetriesWholeActionAfterInvalidState(String operation) {
        Control control = new Control();
        control.failOperation = operation;
        control.ui().type("hello");
        Assert.assertTrue(control.finds >= 2, "Retry must locate a fresh element");
        Assert.assertEquals(control.clears, 2, "Retry must clear again before typing");
        Assert.assertEquals(control.text, "hello", "Partial input must not be duplicated");
    }

    @Test
    public void clickRetriesInterceptionAndFindsAgain() {
        Control control = new Control();
        control.intercept = true;
        control.ui().click();
        Assert.assertEquals(control.clicks, 2);
        Assert.assertEquals(control.finds, 2);
    }

    @Test
    public void persistentInterceptionTimesOut() {
        Control control = new Control();
        control.intercept = true;
        control.alwaysFail = true;
        TimeoutException failure = Assert.expectThrows(TimeoutException.class,
                () -> new UiElement(control.driver(), LOCATOR, Duration.ofMillis(30)).click());
        Assert.assertTrue(control.clicks >= 2);
        Assert.assertTrue(failure.getMessage().contains("review-control"));
    }

    @Test
    public void visibleUncheckedCheckboxReturnsFalseImmediately() {
        Control control = new Control();
        Boolean selected = new WaitManager(control.driver(), Duration.ofSeconds(2))
                .waitForVisibleAndGet(LOCATOR, WebElement::isSelected);
        Assert.assertEquals(selected, Boolean.FALSE);
        Assert.assertEquals(control.finds, 1, "False is a result, not a reason to poll again");
    }

    @Test
    public void nullValueAndFalseConditionStillRetry() {
        Control control = new Control();
        WaitManager waits = new WaitManager(control.driver(), Duration.ofSeconds(2));
        String value = waits.waitForVisibleAndGet(LOCATOR, element -> control.finds == 1 ? null : "ready");
        Assert.assertEquals(value, "ready");
        int[] polls = {0};
        Assert.assertTrue(waits.waitFor(LOCATOR, driver -> ++polls[0] > 1));
        Assert.assertEquals(polls[0], 2);
    }

    @Test
    public void configurationAndClassInitializationAreVerifiedInFreshJvm() throws Exception {
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(),
                "-Xms16m", "-Xmx128m", "-XX:+UseSerialGC", "-cp", classpath,
                September18ReviewTest.class.getName()).redirectErrorStream(true).start();
        try {
            Assert.assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Isolated probe timed out");
            String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            Assert.assertEquals(process.exitValue(), 0, output);
            Assert.assertTrue(output.contains("ISOLATED_CHECKS_PASS"), output);
        } finally {
            process.destroyForcibly();
        }
    }

    /** Runs in a separate JVM so invalid properties cannot affect parallel test classes. */
    public static void main(String[] args) {
        System.setProperty("browser", "custom");
        System.setProperty("headless", "false");
        System.clearProperty("wait.timeout.seconds");
        Assert.assertEquals(ConfigManager.load().getWaitTimeout(), Duration.ofSeconds(10));
        System.setProperty("wait.timeout.seconds", " 7 ");
        Assert.assertEquals(ConfigManager.load().getWaitTimeout(), Duration.ofSeconds(7));
        for (String invalid : List.of("0", "-1", "1.5", "abc")) {
            System.setProperty("wait.timeout.seconds", invalid);
            Assert.expectThrows(IllegalArgumentException.class, ConfigManager::load);
        }
        // WaitManager has not been initialized yet in this JVM.
        System.setProperty("headless", "invalid");
        Assert.expectThrows(IllegalArgumentException.class, ConfigManager::load);
        Control control = new Control();
        WaitManager explicit = new WaitManager(control.driver(), Duration.ofMillis(100));
        Assert.assertFalse(explicit.waitForVisibleAndGet(LOCATOR, WebElement::isSelected));
        Assert.assertNotNull(new WaitManager(control.driver()));
        System.out.println("ISOLATED_CHECKS_PASS");
    }

    private static final class Control {
        int finds;
        int clears;
        int clicks;
        String text = "";
        String failOperation = "";
        boolean disabledFirst;
        boolean intercept;
        boolean alwaysFail;

        UiElement ui() {
            return new UiElement(driver(), LOCATOR, Duration.ofSeconds(3));
        }

        WebDriver driver() {
            return (WebDriver) Proxy.newProxyInstance(WebDriver.class.getClassLoader(),
                    new Class<?>[] {WebDriver.class}, (proxy, method, args) -> {
                        if (method.getName().equals("findElement")) {
                            int attempt = ++finds;
                            return element(attempt);
                        }
                        if (method.getName().equals("toString")) return "ReviewDriver";
                        throw new UnsupportedOperationException(method.getName());
                    });
        }

        WebElement element(int attempt) {
            return (WebElement) Proxy.newProxyInstance(WebElement.class.getClassLoader(),
                    new Class<?>[] {WebElement.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "isDisplayed": return true;
                            case "isEnabled": return !disabledFirst || attempt > 1;
                            case "isSelected": return false;
                            case "clear":
                                Assert.assertTrue(!disabledFirst || attempt > 1, "Typed while disabled");
                                clears++;
                                if (failOperation.equals("clear") && attempt == 1)
                                    throw new InvalidElementStateException("not ready");
                                text = "";
                                return null;
                            case "sendKeys":
                                if (failOperation.equals("sendKeys") && attempt == 1) {
                                    text += "hel";
                                    throw new InvalidElementStateException("partially entered");
                                }
                                for (CharSequence value : (CharSequence[]) args[0]) text += value;
                                return null;
                            case "click":
                                clicks++;
                                if (intercept && (alwaysFail || attempt == 1))
                                    throw new ElementClickInterceptedException("covered");
                                return null;
                            case "toString": return "ReviewElement-" + attempt;
                            default: throw new UnsupportedOperationException(method.getName());
                        }
                    });
        }
    }
}
