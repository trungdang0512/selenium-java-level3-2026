package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import java.util.Objects;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeProvider implements BrowserProvider {

    @Override
    public WebDriver create(FrameworkConfig config) {
        FrameworkConfig checkedConfig = Objects.requireNonNull(
                config,
                "Framework configuration must not be null."
        );
        ChromeOptions options = new ChromeOptions();

        if (checkedConfig.isHeadless()) {
            options.addArguments("--headless=new");
        }

        WebDriver driver = new ChromeDriver(options);

        if (!checkedConfig.isHeadless()) {
            driver.manage().window().maximize();
        }

        return driver;
    }
}
