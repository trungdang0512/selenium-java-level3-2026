# Selenium Java Framework

A simple Selenium WebDriver framework for writing TestNG UI tests with Java.

The framework currently runs tests on local Google Chrome. It manages browser configuration, driver creation, driver cleanup, and JSON test-data loading so test classes can focus on test steps.

## Quick start

### 1. Install the required tools

Make sure the following tools are available:

- JDK 21 or later
- Maven 3.9 or later
- Google Chrome

Check Java and Maven:

```powershell
java -version
mvn -version
```

### 2. Get the project

```powershell
git clone https://github.com/trungdang0512/selenium-java-level3-2026.git
cd selenium-java-level3-2026/trungdang-fw
```

If the project is already on your computer, open a terminal in the `trungdang-fw` directory.

### 3. Run the tests

Chrome runs in a maximized visible window by default:

```powershell
mvn test
```

To run Chrome without opening a visible window:

```powershell
mvn -Dheadless=true test
```

## Write a test

Create a TestNG class under:

```text
trungdang-fw/src/test/java/com/trungdang/automation/tests/
```

Extend `BaseTest` to reuse the framework's TestNG browser lifecycle:

```java
package com.trungdang.automation.tests;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExampleTest extends BaseTest {

    @Test
    public void shouldOpenExamplePage() {
        WebDriver driver = getDriver();

        driver.get("https://example.com");

        Assert.assertEquals(driver.getTitle(), "Example Domain");
    }
}
```

Run only this test:

```powershell
mvn -Dtest=ExampleTest test
```

## Understand the test lifecycle

Tests that extend `BaseTest` follow three steps:

1. `BaseTest.setUp()` loads the browser settings and creates Chrome before each test.
2. The test calls `getDriver()` to use the active WebDriver.
3. `BaseTest.tearDown()` closes Chrome after each test, even when the test fails.

`BaseTest` is optional. If your project already has a base test class, use a `DriverManager` directly in its lifecycle hooks:

```java
private final DriverManager driverManager = new DriverManager();

@BeforeMethod(alwaysRun = true)
public void setUp() {
    driverManager.start(ConfigManager.load());
}

@AfterMethod(alwaysRun = true)
public void tearDown() {
    driverManager.quit();
}

@Test
public void shouldOpenApplication() {
    driverManager.getDriver().get(driverManager.getConfig().getBaseUrl());
}
```

Import `DriverManager` from `com.trungdang.automation.driver`, `ConfigManager` from `com.trungdang.automation.config`, and the annotations from `org.testng.annotations`. The manager has no TestNG dependency, so other test runners can call the same API from their lifecycle hooks.

## Configure the application URL

The driver smoke test reads the application URL from `FrameworkConfig`. The default URL is:

```text
https://demo.testarchitect.com/
```

Override it with the `base.url` system property:

```powershell
mvn -Dbase.url=https://example.com -Dheadless=true -Dtest=DriverSmokeTest test
```

## Read typed JSON test data

`TestDataReader.read()` uses Jackson to convert a complete JSON resource into the requested Java type. Resource paths are relative to `src/test/resources`.

## Configure the browser

Pass configuration with Maven `-D` properties:

| Property | Default | Supported values | Example |
|---|---|---|---|
| `browser` | `chrome` | A name registered with the factory | `-Dbrowser=chrome` |
| `headless` | `false` | `true`, `false` | `-Dheadless=true` |
| `base.url` | `https://demo.testarchitect.com/` | Application URL | `-Dbase.url=https://example.com` |

Examples:

```powershell
# Default maximized Chrome window
mvn test

# Headless Chrome
mvn -Dheadless=true test

# Explicit browser and display settings
mvn -Dbrowser=chrome -Dheadless=true test

# Override the application URL
mvn -Dbase.url=https://example.com test
```

Invalid values fail before the browser starts. For example, `-Dheadless=yes` is rejected because the supported values are only `true` and `false`.

## Framework API

### `ConfigManager`

Loads the current system properties and returns a `FrameworkConfig` object:

```java
FrameworkConfig config = ConfigManager.load();
```

### `FrameworkConfig`

Stores the browser, display mode, and application base URL resolved for the test:

```java
config.getBrowser();
config.isHeadless();
config.getBaseUrl();
```

### `DriverManager`

Controls one WebDriver lifecycle and its configuration per thread. Create a manager with `new DriverManager()` and reuse that instance:

```java
driverManager.start(config);
WebDriver driver = driverManager.getDriver();
driverManager.quit();
```

- `start(config)` creates the browser.
- `getDriver()` returns the current thread's running WebDriver.
- `getConfig()` returns the configuration used to start that browser.
- `quit()` closes the current thread's browser and removes both driver and configuration, even if closing fails. Calling it without an active browser is safe.

Call all lifecycle methods on the same thread and manager instance. One manager can serve concurrent test threads; each thread must call `quit()` in teardown. Do not call `start(config)` twice on the same thread without calling `quit()` first. Accessing driver or configuration before startup or after cleanup throws `IllegalStateException`.

### `WebDriverFactory`

Looks up the configured browser in its `BrowserRegistry` and delegates driver creation to that provider. `new WebDriverFactory()` uses an independent registry with Chrome registered; `new WebDriverFactory(registry)` uses your registry. A missing provider or a null driver returned by a provider fails before the manager stores session state.

### `BrowserRegistry`

Maps normalized browser names to providers. `new BrowserRegistry()` starts empty; `BrowserRegistry.withDefaults()` registers Chrome. Names are trimmed and compared in lowercase. Blank names, null providers, and duplicate registrations are rejected. Each registry is independent, so different suites can register the same name with different providers.

### `BrowserProvider`

Defines `WebDriver create(FrameworkConfig config)`. Every call must return a new, non-null driver. A provider shared by parallel tests must support concurrent calls; keep mutable browser options and drivers local to each invocation. If startup fails after acquiring resources, the provider must clean them up before throwing.

### `ChromeProvider`

Owns the Chrome-specific options and creates `ChromeDriver`. In visible mode, it maximizes the browser after startup. Headless mode uses Chrome's default viewport. Additional browsers are supplied through registered providers without modifying `WebDriverFactory`. Chrome is the only built-in provider.

### `BaseTest`

Provides the TestNG setup and teardown shared by UI tests. Extend it and call `getDriver()` or `getConfig()` inside test methods.

### `TestDataReader`

Reads complete JSON resources from `src/test/resources` and delegates typed deserialization to Jackson through `read()`.

## Register a custom browser provider

Create a class implementing `BrowserProvider` in your own project, then register an instance before tests start. The following runnable setup example registers a custom name for the existing Chrome provider:

```java
BrowserRegistry registry = BrowserRegistry.withDefaults();
registry.register("custom-chrome", new ChromeProvider());

WebDriverFactory factory = new WebDriverFactory(registry);
DriverManager manager = new DriverManager(factory);
FrameworkConfig config = new FrameworkConfig("custom-chrome", true);

try {
    manager.start(config);
    manager.getDriver().get(config.getBaseUrl());
} finally {
    manager.quit();
}
```

Import the driver classes from `com.trungdang.automation.driver` and `FrameworkConfig` from `com.trungdang.automation.config`. Replace `new ChromeProvider()` with your provider to supply a different implementation. The framework does not depend on that custom class.

For TestNG, keep the configured manager as a field of your own base test class, call `manager.start(ConfigManager.load())` in `@BeforeMethod`, and call `manager.quit()` in `@AfterMethod(alwaysRun = true)`. Configure `-Dbrowser=custom-chrome` after registering that name. A system property selects a provider; it does not register one. The bundled `BaseTest` uses its own default manager and registry.

Finish registrations before starting parallel tests. Registration rejects replacement of existing names. Each manager/factory created with default constructors has its own registry; configure and pass the same factory to the manager that will run the tests.

Browser names in `FrameworkConfig` are now strings: use `new FrameworkConfig("chrome")` instead of `BrowserType.CHROME`. Direct factory callers now use an instance: `new WebDriverFactory().create(config)`.

## Verify provider extension

Run the focused contract tests without launching Chrome:

```powershell
mvn -Dtest=BrowserExtensionTest test
```

These tests cover independent registries, custom configuration, provider selection, invalid registrations, startup/cleanup failures, and concurrent manager state using fake drivers. They do not prove real browser or Grid compatibility. `DriverManagerTest` exercises two real Chrome sessions without extending `BaseTest`; `DriverSmokeTest` exercises the bundled TestNG adapter.
## Project structure

```text
selenium-java-level3-2026/
|-- README.md
`-- trungdang-fw/
    |-- pom.xml
    `-- src/
        |-- main/java/com/trungdang/automation/
        |   |-- config/
        |   |   |-- ConfigManager.java
        |   |   `-- FrameworkConfig.java
        |   `-- driver/
        |       |-- BrowserProvider.java
        |       |-- BrowserRegistry.java
        |       |-- ChromeProvider.java
        |       |-- DriverManager.java
        |       `-- WebDriverFactory.java
        `-- test/
            |-- java/com/trungdang/automation/
            |   |-- testdata/
            |   |   `-- TestDataReader.java
            |   `-- tests/
            |       |-- BaseTest.java
            |       |-- BrowserExtensionTest.java
            |       |-- DriverManagerTest.java
            |       `-- DriverSmokeTest.java
```

## Current browser support

| Browser | Execution | Status |
|---|---|---|
| Chrome | Local | Supported |
| Firefox | Local | Not available yet |
| Edge | Local | Not available yet |
| Chrome, Firefox, Edge | Selenium Grid | Not available yet |

## Troubleshooting

### `java` is not recognized

Install JDK 21 and set `JAVA_HOME`. Open a new terminal, then run:

```powershell
java -version
```

### `mvn` is not recognized

Install Maven and add its `bin` directory to `PATH`. Open a new terminal, then run:

```powershell
mvn -version
```

### Chrome does not start

1. Confirm that Google Chrome is installed and up to date.
2. Run the test with a visible window:

```powershell
mvn -Dheadless=false test
```

3. Read the test failure under `trungdang-fw/target/surefire-reports/`.

Selenium Manager locates or downloads a compatible ChromeDriver when the browser session starts.

### `Unable to establish loopback connection`

This error happens before the test steps run. Try running the test from a normal local terminal or from the IntelliJ Maven tool window. If it continues, check local security software, firewall rules, and the JDK used by Maven.
