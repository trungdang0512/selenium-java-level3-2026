package com.trungdang.automation.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Reads JSON test data from files available on the test classpath.
 *
 * <p>Resource paths are relative to {@code src/test/resources}.
 */
public class TestDataReader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestDataReader() {
    }

    /**
     * Reads a complete JSON resource and converts it to the requested Java type.
     *
     * @param <T> the type returned after JSON conversion
     * @param resourcePath the resource path relative to {@code src/test/resources}
     * @param dataType the Java type used for JSON conversion
     * @return the converted test-data object
     */
    public static <T> T read(String resourcePath, Class<T> dataType) {
        Objects.requireNonNull(dataType, "Test-data type must not be null.");

        try (InputStream input = openResource(resourcePath)) {
            return OBJECT_MAPPER.readValue(input, dataType);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Cannot read test-data resource: " + resourcePath,
                    exception
            );
        }
    }

    private static InputStream openResource(String resourcePath) {
        String checkedPath = Objects.requireNonNull(
                resourcePath,
                "Test-data resource path must not be null."
        );
        InputStream input = TestDataReader.class.getClassLoader()
                .getResourceAsStream(checkedPath);

        if (input == null) {
            throw new IllegalArgumentException(
                    "Test-data resource not found: " + checkedPath
            );
        }

        return input;
    }
}
