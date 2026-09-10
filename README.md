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

Extend `BaseTest` instead of repeating driver setup and cleanup in every test class.

## Read JSON test data

Test resources are stored under `trungdang-fw/src/test/resources/`. The smoke test reads its application URL from:

```text
src/test/resources/test-data/url.json
```

```json
{
  "loginUrl": "https://demo.testarchitect.com/"
}
```

Read one JSON value by key:

```java
String loginUrl = TestDataReader.readByKey(
        "test-data/url.json",
        "loginUrl",
        String.class
);
```

Resource paths start inside `src/test/resources`; do not include that directory in the value passed to `TestDataReader`.

Run the smoke test that opens this URL:

```powershell
mvn -Dheadless=true -Dtest=SmokeTest test
```

This test requires network access to `https://demo.testarchitect.com/`.

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

### `BaseTest`

Provides the TestNG setup and teardown shared by UI tests. Extend it and call `getDriver()` inside test methods.

### `TestDataReader`

Reads JSON files from `src/test/resources`. Use `readByKey()` for one value or `read()` to map the entire JSON file to a Java class.

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
        `-- test/
            |-- java/com/trungdang/automation/
            |   |-- testdata/
            |   |   `-- TestDataReader.java
            |   `-- tests/
            |       |-- BaseTest.java
            |       `-- SmokeTest.java
            `-- resources/test-data/
                `-- url.json
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
