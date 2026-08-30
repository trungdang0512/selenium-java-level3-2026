package com.trungdang.automation.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class TestDataReader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestDataReader() {
    }

    public static <T> T read(String resourcePath, Class<T> dataType) {
        Objects.requireNonNull(resourcePath, "Test-data resource path must not be null.");
        Objects.requireNonNull(dataType, "Test-data type must not be null.");

        try (InputStream input = TestDataReader.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalArgumentException(
                        "Test-data resource not found: " + resourcePath
                );
            }

            return OBJECT_MAPPER.readValue(input, dataType);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Cannot read test-data resource: " + resourcePath,
                    exception
            );
        }
    }

    public static <T> T readByKey(String resourcePath, String key, Class<T> dataType) {
        Objects.requireNonNull(key, "Test-data key must not be null.");
        Objects.requireNonNull(dataType, "Test-data type must not be null.");

        try (InputStream input = openResource(resourcePath)) {
            JsonNode root = OBJECT_MAPPER.readTree(input);
            JsonNode value = root.get(key);

            if (value == null || value.isNull()) {
                throw new IllegalArgumentException(
                        "Test-data key not found: '" + key + "' in " + resourcePath
                );
            }

            return OBJECT_MAPPER.treeToValue(value, dataType);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Cannot read test-data resource: " + resourcePath,
                    exception
            );
        }
    }

    private static InputStream openResource(String resourcePath) {
        Objects.requireNonNull(resourcePath, "Test-data resource path must not be null.");

        InputStream input = TestDataReader.class.getClassLoader()
                .getResourceAsStream(resourcePath);
        if (input == null) {
            throw new IllegalArgumentException(
                    "Test-data resource not found: " + resourcePath
            );
        }

        return input;
    }
}
