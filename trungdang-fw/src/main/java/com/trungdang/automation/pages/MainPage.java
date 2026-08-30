package com.trungdang.automation.pages;

import org.openqa.selenium.By;

public class MainPage extends BasePage {

    private final By loginLink = By.xpath("//div[contains(@class, 'login-link')]/a");

    /** Opens the main page at the supplied URL. */
    public void openMainPage(String url) {
        open(url);
    }

    /** Opens the login page from the main page. */
    public void goToLoginPage() {
        click(loginLink);
    }
}
