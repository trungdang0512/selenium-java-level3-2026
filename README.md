# Selenium Java Framework

A simple Selenium WebDriver framework for writing TestNG UI tests with Java.

The framework currently runs tests on local Google Chrome. It manages browser configuration, driver creation, and driver cleanup so test classes can focus on test steps.

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

Chrome runs in headless mode by default:

```powershell
mvn test
```

To watch the test run in a visible Chrome window:

```powershell
mvn -Dheadless=false test
```

## Write a test

Create a TestNG class under:

```text
trungdang-fw/src/test/java/com/trungdang/automation/tests/
```

Use `DriverManager` in the TestNG lifecycle:

```java
package com.trungdang.automation.tests;

import com.trungdang.automation.config.ConfigManager;
import com.trungdang.automation.config.FrameworkConfig;
import com.trungdang.automation.driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExampleTest {

    private DriverManager driverManager;

    @BeforeMethod
    public void setUp() {
        FrameworkConfig config = ConfigManager.load();
        driverManager = new DriverManager();
        driverManager.start(config);
    }

    @Test
    public void shouldOpenExamplePage() {
        WebDriver driver = driverManager.getDriver();

        driver.get("https://example.com");

        Assert.assertEquals(driver.getTitle(), "Example Domain");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driverManager != null) {
            driverManager.quit();
        }
    }
}
```

Run only this test:

```powershell
mvn -Dtest=ExampleTest test
```

## Understand the test lifecycle

The example follows three steps:

1. `ConfigManager.load()` reads the browser settings.
2. `DriverManager.start(config)` creates the Chrome session before the test.
3. `DriverManager.quit()` closes Chrome after the test, even when the test fails.

Always call `quit()` from an `@AfterMethod(alwaysRun = true)` method. This prevents unused Chrome processes from remaining on the computer.

## Configure the browser

Pass configuration with Maven `-D` properties:

| Property | Default | Supported values | Example |
|---|---|---|---|
| `browser` | `chrome` | `chrome` | `-Dbrowser=chrome` |
| `headless` | `true` | `true`, `false` | `-Dheadless=false` |

Examples:

```powershell
# Default Chrome configuration
mvn test

# Visible Chrome window
mvn -Dheadless=false test

# Explicit browser and display settings
mvn -Dbrowser=chrome -Dheadless=true test
```

Invalid values fail before the browser starts. For example, `-Dheadless=yes` is rejected because the supported values are only `true` and `false`.

## Framework API

### `ConfigManager`

Loads the current system properties and returns a `FrameworkConfig` object:

```java
FrameworkConfig config = ConfigManager.load();
```

### `FrameworkConfig`

Stores the browser selected for the test and whether it should run headlessly:

```java
config.getBrowser();
config.isHeadless();
```

### `DriverManager`

Controls one WebDriver lifecycle:

```java
driverManager.start(config);
WebDriver driver = driverManager.getDriver();
driverManager.quit();
```

- `start(config)` creates the browser.
- `getDriver()` returns the running WebDriver.
- `quit()` closes the browser and clears the stored driver.

Do not call `start(config)` twice on the same `DriverManager` without calling `quit()` first.

### `WebDriverFactory`

Creates the Selenium WebDriver requested by `DriverManager`. Test classes normally do not need to call this class directly.

## Project structure

```text
selenium-java-level3-2026/
|-- README.md
`-- trungdang-fw/
    |-- pom.xml
    `-- src/
        |-- main/java/com/trungdang/automation/
        |   |-- config/
        |   |   |-- BrowserType.java
        |   |   |-- ConfigManager.java
        |   |   `-- FrameworkConfig.java
        |   `-- driver/
        |       |-- DriverManager.java
        |       `-- WebDriverFactory.java
        `-- test/java/com/trungdang/automation/tests/
            `-- DriverSmokeTest.java
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
