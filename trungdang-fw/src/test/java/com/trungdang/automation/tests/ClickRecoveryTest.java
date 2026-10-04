package com.trungdang.automation.tests;

import com.trungdang.automation.core.UiElement;
import java.lang.reflect.Proxy;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ElementClickInterceptedException;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests the real wait loop with controlled browser responses. */
public class ClickRecoveryTest {
    @Test
    public void successfulClickDoesNotScroll() {
        Scenario s = new Scenario();
        s.ui().click();
        Assert.assertEquals(s.scrolls, 0);
        Assert.assertEquals(s.clicks, 1);
    }

    @Test
    public void interceptedClickScrollsThenFindsFreshElement() {
        Scenario s = new Scenario();
        s.covered = true;
        s.ui().click();
        Assert.assertEquals(s.scrolls, 1);
        Assert.assertEquals(s.clicks, 2);
        Assert.assertEquals(s.finds, 2);
        Assert.assertNotSame(s.clicked, s.scrolled);
    }

    @Test
    public void staleDuringScrollRetriesWithFreshElement() {
        Scenario s = new Scenario();
        s.covered = true;
        s.staleOnScroll = true;
        s.ui().click();
        Assert.assertEquals(s.scrolls, 1);
        Assert.assertEquals(s.finds, 2);
        Assert.assertNotSame(s.clicked, s.scrolled);
    }

    @Test
    public void staleOnNextClickAfterScrollIsRetried() {
        Scenario s = new Scenario();
        s.covered = true;
        s.staleOnSecondClick = true;
        s.ui().click();
        Assert.assertEquals(s.scrolls, 1);
        Assert.assertEquals(s.clicks, 3);
        Assert.assertEquals(s.finds, 3);
    }

    @Test
    public void persistentOverlayTimesOutWithoutRepeatedScrolling() {
        Scenario s = new Scenario();
        s.covered = true;
        s.permanent = true;
        Assert.expectThrows(TimeoutException.class,
                () -> new UiElement(s.driver(), By.id("target"), Duration.ofMillis(40)).click());
        Assert.assertEquals(s.scrolls, 1);
        Assert.assertTrue(s.clicks >= 2);
    }

    @Test
    public void eachClickGetsItsOwnRecoveryAttempt() {
        Scenario s = new Scenario();
        UiElement element = s.ui();
        s.covered = true;
        element.click();
        s.covered = true;
        element.click();
        Assert.assertEquals(s.scrolls, 2);
        Assert.assertEquals(s.clicks, 4);
    }

    @Test
    public void unrelatedScriptErrorsAreNotHidden() {
        Scenario s = new Scenario();
        s.covered = true;
        s.scriptFailure = true;
        Assert.expectThrows(JavascriptException.class, () -> s.ui().click());
        Assert.assertEquals(s.scrolls, 1);
        Assert.assertEquals(s.clicks, 1);
    }

    private static final class Scenario {
        boolean covered;
        boolean permanent;
        boolean staleOnScroll;
        boolean staleOnSecondClick;
        boolean scriptFailure;
        int finds;
        int clicks;
        int scrolls;
        WebElement scrolled;
        WebElement clicked;

        UiElement ui() {
            return new UiElement(driver(), By.id("target"), Duration.ofSeconds(3));
        }

        WebDriver driver() {
            return (WebDriver) Proxy.newProxyInstance(WebDriver.class.getClassLoader(),
                    new Class<?>[] {WebDriver.class, JavascriptExecutor.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "findElement":
                                finds++;
                                return element();
                            case "executeScript":
                                scrolls++;
                                String script = (String) args[0];
                                Assert.assertTrue(script.contains("scrollIntoView"));
                                Assert.assertTrue(script.contains("block: 'center'"));
                                Assert.assertFalse(script.contains(".click("));
                                scrolled = (WebElement) ((Object[]) args[1])[0];
                                if (scriptFailure) throw new JavascriptException("script failed");
                                if (!permanent) covered = false;
                                if (staleOnScroll) throw new StaleElementReferenceException("replaced during scroll");
                                return null;
                            case "toString": return "RecoveryDriver";
                            default: throw new UnsupportedOperationException(method.getName());
                        }
                    });
        }

        WebElement element() {
            return (WebElement) Proxy.newProxyInstance(WebElement.class.getClassLoader(),
                    new Class<?>[] {WebElement.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "isDisplayed":
                            case "isEnabled": return true;
                            case "click":
                                clicks++;
                                clicked = (WebElement) proxy;
                                if (covered) throw new ElementClickInterceptedException("overlay");
                                if (staleOnSecondClick && clicks == 2)
                                    throw new StaleElementReferenceException("replaced after scroll");
                                return null;
                            case "toString": return "RecoveryElement";
                            default: throw new UnsupportedOperationException(method.getName());
                        }
                    });
        }
    }
}
