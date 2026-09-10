# Selenium Java Framework

A simple Selenium WebDriver framework for writing TestNG UI tests with Java.

The framework currently runs tests on local Google Chrome. It manages browser configuration, lifecycle, and JSON test-data loading. It also provides waited element actions and retries UI assertions while the page is changing.

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

import com.trungdang.automation.core.UiAssertions;
import com.trungdang.automation.core.UiElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

public class ExampleTest extends BaseTest {

    @Test
    public void shouldOpenExamplePage() {
        WebDriver driver = getDriver();
        driver.get("https://example.com");

        UiElement heading = new UiElement(driver, By.tagName("h1"));
        UiAssertions assertions = new UiAssertions(driver);

        assertions.assertVisible(heading);
        assertions.assertTextEquals(heading, "Example Domain");
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

Run the driver smoke test that starts Chrome and opens this URL:

```powershell
mvn -Dheadless=true -Dtest=DriverSmokeTest test
```

This test requires network access to `https://demo.testarchitect.com/`.

## Configure the browser

Pass configuration with Maven `-D` properties:

| Property | Default | Supported values | Example |
|---|---|---|---|
| `browser` | `chrome` | `chrome` | `-Dbrowser=chrome` |
| `headless` | `false` | `true`, `false` | `-Dheadless=true` |

Examples:

```powershell
# Default maximized Chrome window
mvn test

# Headless Chrome
mvn -Dheadless=true test

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

Selects the `BrowserProvider` requested by `DriverManager`. Test classes normally do not need to call this class directly.

### `BrowserProvider`

Defines the common method used to create a WebDriver from `FrameworkConfig`.

### `ChromeProvider`

Owns the Chrome-specific options and creates `ChromeDriver`. In visible mode, it maximizes the browser after startup. Headless mode uses Chrome's default viewport. Adding another browser later requires another provider instead of adding browser-specific creation code to `WebDriverFactory`.

### `BaseTest`

Provides the TestNG setup and teardown shared by UI tests. Extend it and call `getDriver()` inside test methods.

### `TestDataReader`

Reads JSON files from `src/test/resources`. Use `readByKey()` for one value or `read()` to map the entire JSON file to a Java class.

### `UiElement`

Stores a Selenium `By` locator and finds the element again when an action runs. This avoids keeping an old `WebElement` after the page changes.

```java
UiElement username = new UiElement(driver, By.id("username"));
UiElement signInButton = new UiElement(driver, By.id("sign-in"));

username.type("framework-user");
signInButton.click();
String buttonText = signInButton.getText();
boolean buttonIsDisplayed = signInButton.isDisplayed();
```

`click()` waits for a displayed and enabled element. `type()` and `getText()` wait for a visible element. The default timeout is 10 seconds.

### `UiAssertions`

Retries an assertion until it passes or reaches the timeout:

```java
UiAssertions assertions = new UiAssertions(driver);

assertions.assertVisible(signInButton);
assertions.assertTextEquals(signInButton, "Sign in");
```

A failed assertion reports the locator, expected condition, timeout, and last observed text when available.

### `WaitManager`

Centralizes explicit waits and retries an element action when Selenium reports that the DOM replaced the element. Most tests use it indirectly through `UiElement`.

Use a custom timeout when a page needs a different wait time:

```java
UiElement message = new UiElement(
        driver,
        By.id("message"),
        Duration.ofSeconds(5)
);
```

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
        |   |-- core/
        |   |   |-- UiAssertions.java
        |   |   |-- UiElement.java
        |   |   `-- WaitManager.java
        |   `-- driver/
        |       |-- BrowserProvider.java
        |       |-- ChromeProvider.java
        |       |-- DriverManager.java
        |       `-- WebDriverFactory.java
        `-- test/
            |-- java/com/trungdang/automation/
            |   |-- testdata/
            |   |   `-- TestDataReader.java
            |   `-- tests/
            |       |-- BaseTest.java
            |       `-- DriverSmokeTest.java
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
