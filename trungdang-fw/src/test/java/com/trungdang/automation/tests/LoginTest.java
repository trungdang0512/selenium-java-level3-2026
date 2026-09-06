package com.trungdang.automation.tests;

import com.trungdang.automation.core.UiAssertions;
import com.trungdang.automation.model.User;
import com.trungdang.automation.testdata.LoginDataProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(dataProvider = "validLogin", dataProviderClass = LoginDataProvider.class)
    public void logInWithValidCredentials(String loginUrl, User validUser) {
        mainPage.openMainPage(loginUrl);
        mainPage.goToLoginPage();
        loginPage.loginWithAccount(validUser);

        Assert.assertTrue(
                loginPage.isUserNavigationDisplayed(),
                "User navigation should be displayed after a successful login."
        );
    }
}
