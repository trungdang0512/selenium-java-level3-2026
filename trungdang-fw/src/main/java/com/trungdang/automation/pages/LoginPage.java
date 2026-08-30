package com.trungdang.automation.pages;

import com.trungdang.automation.model.User;
import java.util.Objects;
import org.openqa.selenium.By;

public class LoginPage extends BasePage{
    private final By userNameTextBox = By.xpath("//input[@id='username']");
    private final By passwordTextBox = By.xpath("//input[@id='password']");
    private final By loginBtn = By.xpath("//button[contains(@class, 'woocommerce-form-login__submit')]");
    private final By userNavigation = By.xpath(
            "//div[contains(@class, 'woocommerce-MyAccount-navigation-wrapper')]"
    );

    /** Fills in the username and password fields. */
    public void enterLoginInfo(User user) {
        type(userNameTextBox, user.getUsername());
        type(passwordTextBox, user.getPassword());
    }

    /** Clicks the login button. */
    public void clickLoginButton() {
        click(loginBtn);
    }

    /** Logs in with the supplied user account. */
    public void loginWithAccount(User user) {
        enterLoginInfo(user);
        clickLoginButton();
    }

    /** Checks whether the account navigation is visible after login. */
    public boolean isUserNavigationDisplayed() {
        return isDisplayed(userNavigation);
    }
}
