package com.trungdang.automation.driver;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A suite-owned collection of browser providers. Register providers before starting tests.
 * Providers may be called concurrently and must create a new driver for each invocation.
 */
public final class BrowserRegistry {

    private final Map<String, BrowserProvider> providers = new ConcurrentHashMap<>();

    /** Creates an independent registry with the built-in Chrome provider. */
    public static BrowserRegistry withDefaults() {
        BrowserRegistry registry = new BrowserRegistry();
        registry.register("chrome", new ChromeProvider());
        return registry;
    }

    public void register(String browser, BrowserProvider provider) {
        String name = normalize(browser);
        Objects.requireNonNull(provider, "Browser provider must not be null.");
        if (providers.putIfAbsent(name, provider) != null) {
            throw new IllegalArgumentException("Browser provider is already registered: " + name);
        }
    }

    public BrowserProvider getProvider(String browser) {
        String name = normalize(browser);
        BrowserProvider provider = providers.get(name);
        if (provider == null) {
            throw new IllegalArgumentException(
                    "No browser provider registered for '" + name
                            + "'. Registered browsers: " + new TreeSet<>(providers.keySet())
            );
        }
        return provider;
    }

    private static String normalize(String browser) {
        String name = Objects.requireNonNull(browser, "Browser name must not be null.")
                .trim().toLowerCase(Locale.ROOT);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Browser name must not be blank.");
        }
        return name;
    }
}
