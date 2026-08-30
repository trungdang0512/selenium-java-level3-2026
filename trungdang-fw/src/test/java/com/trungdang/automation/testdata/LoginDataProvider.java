package com.trungdang.automation.testdata;

import com.trungdang.automation.model.User;
import org.testng.annotations.DataProvider;

public class LoginDataProvider {

    private LoginDataProvider() {
    }

    @DataProvider(name = "validLogin")
    public static Object[][] provideValidLogin() {
        String loginUrl = TestDataReader.readByKey(
                "test-data/url.json",
                "loginUrl",
                String.class
        );
        User validUser = TestDataReader.readByKey(
                "test-data/user.json",
                "validUser",
                User.class
        );

        return new Object[][]{
                {loginUrl, validUser}
        };
    }

    @DataProvider(name = "invalidLogin")
    public static Object[][] provideInvalidLogin() {
        String loginUrl = TestDataReader.readByKey(
                "test-data/url.json",
                "loginUrl",
                String.class
        );
        User invalidUser = TestDataReader.readByKey(
                "test-data/user.json",
                "invalidUser",
                User.class
        );

        return new Object[][]{
                {loginUrl, invalidUser}
        };
    }
}
