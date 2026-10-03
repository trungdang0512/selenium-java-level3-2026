package com.trungdang.automation.driver;

import com.trungdang.automation.config.FrameworkConfig;
import java.util.Objects;
import org.openqa.selenium.WebDriver;

/** Creates drivers using the providers registered for this factory. */
public class WebDriverFactory {

    private final BrowserRegistry registry;

    public WebDriverFactory() {
        this(BrowserRegistry.withDefaults());
    }

    public WebDriverFactory(BrowserRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "Browser registry must not be null.");
    }

    public WebDriver create(FrameworkConfig config) {
        Objects.requireNonNull(config, "Framework configuration must not be null.");
        BrowserProvider provider = registry.getProvider(config.getBrowser());
        return Objects.requireNonNull(
                provider.create(config),
                "Browser provider '" + config.getBrowser() + "' returned a null driver."
        );
    }
}
