package com.trungdang.automation.pages;

import com.trungdang.automation.core.UiElement;
import com.trungdang.automation.model.User;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    private final UiElement userNameTextBox = new UiElement(
            By.xpath("//input[@id='username']")
    );
    private final UiElement passwordTextBox = new UiElement(
            By.xpath("//input[@id='password']")
    );
    private final UiElement loginBtn = new UiElement(
            By.xpath("//button[contains(@class, 'woocommerce-form-login__submit')]")
    );
    private final UiElement userNavigation = new UiElement(
            By.xpath("//div[contains(@class, 'woocommerce-MyAccount-navigation-wrapper')]")
    );

    /**
     * Fills in the username and password fields.
     */
    public void enterLoginInfo(User user) {
        userNameTextBox.type(user.getUsername());
        passwordTextBox.type(user.getPassword());
    }

    /**
     * Clicks the login button.
     */
    public void clickLoginButton() {
        loginBtn.click();
    }

    /**
     * Logs in with the supplied user account.
     */
    public void loginWithAccount(User user) {
        enterLoginInfo(user);
        clickLoginButton();
    }

    /**
     * Checks whether the account navigation is visible after login.
     */
    public boolean isUserNavigationDisplayed() {
        return userNavigation.isDisplayed();
    }
}
