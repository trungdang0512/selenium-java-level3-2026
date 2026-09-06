package com.trungdang.automation.pages;

import com.trungdang.automation.core.UiElement;
import org.openqa.selenium.By;

public class MainPage extends BasePage {

    private final UiElement loginLink = new UiElement(
            By.xpath("//div[contains(@class, 'login-link')]/a")
    );

    /** Opens the main page at the supplied URL. */
    public void openMainPage(String url) {
        open(url);
    }

    /** Opens the login page from the main page. */
    public void goToLoginPage() {
        loginLink.click();
    }
}
