package com.trungdang.automation.testdata;

import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDataReaderTest {

    private static final String URL_DATA_FILE = "test-data/url.json";
    private static final String EXPECTED_LOGIN_URL = "https://demo.testarchitect.com/";

    @Test
    public void shouldReadWholeJsonFile() {
        Map<?, ?> urlData = TestDataReader.read(URL_DATA_FILE, Map.class);

        Assert.assertEquals(urlData.get("loginUrl"), EXPECTED_LOGIN_URL);
    }

    @Test
    public void shouldReadUrlByKey() {
        String loginUrl = TestDataReader.readByKey(
                URL_DATA_FILE,
                "loginUrl",
                String.class
        );

        Assert.assertEquals(loginUrl, EXPECTED_LOGIN_URL);
    }
}
