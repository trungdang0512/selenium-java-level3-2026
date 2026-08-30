package com.trungdang.automation.reporting;

import io.qameta.allure.Allure;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Collects diagnostic artifacts whenever a TestNG test fails.
 */
public final class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable failure = result.getThrowable();
        String details = failure == null ? "No failure details were provided." : failure.toString();

        Allure.addAttachment("Failure details", "text/plain", details, ".txt");
        ScreenshotService.attachFailureArtifacts();
    }
}
