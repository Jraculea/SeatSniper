package com.seatsniper.utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.seatsniper.UserData;

public final class ConfigurationLoader {
    private static final Logger LOGGER = Logger.getLogger(ConfigurationLoader.class.getName());
    
    private static final String CONFIG_FILE_PATH = "src\\main\\resources\\user_config.properties";
    
    private static final String KEY_DRIVER_PATH = "driverPath";
    private static final String KEY_BROWSER_PATH = "browserPath";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AUTHENTICATOR_NAME = "authenticator_name";
    private static final String KEY_AUTHENTICATION_KEY = "authentication_key";
    private static final String KEY_INTERVAL = "interval";
    private static final String KEY_TERM = "term";
    private static final String KEY_COURSE_CODES = "courseCodes";
    private static final String KEY_DURATION = "maxDuration";
    
    private static final int MIN_RECOMMENDED_INTERVAL = 30;
    private static final int MAX_COURSE_CODES = 7;
    private static final String EMAIL_DOMAIN = "@login.cuny.edu";
    private static final Pattern TERM_PATTERN = Pattern.compile("(\\d{4})\\s+(Spring|Summer|Fall|Winter)", Pattern.CASE_INSENSITIVE);
    private static final String COURSE_CODE_SEPARATOR = ",";
    
    private static final String ERROR_CONFIG_FILE_NOT_FOUND = "Configuration file not found: %s";
    private static final String ERROR_DRIVER_PATH_INVALID = "The driver path provided is invalid.";
    private static final String ERROR_BROWSER_PATH_INVALID = "The browser path provided is invalid.";
    private static final String ERROR_USERNAME_MISSING = "Username is missing in config file.";
    private static final String ERROR_PASSWORD_MISSING = "Password is missing in config file.";
    private static final String ERROR_AUTHENTICATOR_NAME_MISSING = "Authenticator Method Name is missing in config file.";
    private static final String ERROR_AUTHENTICATION_KEY_MISSING = "Authenticator Secret Key is missing in config file.";
    private static final String ERROR_NUM_INVALID = "Numbers must be valid (positive integers or 0 if duration).";
    private static final String ERROR_TERM_MISSING = "No course term provided. Expected format: '2026_Fall', '2027_Spring', etc.";
    private static final String ERROR_TERM_INVALID = "Invalid course term. Expected format '2026_Fall', '2027_Spring', etc.";
    private static final String ERROR_TERM_YEAR_INVALID = "Please enter an available term. If you suspect this is an error, report it.";
    private static final String ERROR_COURSE_CODES_MISSING = "No course codes provided (at least 1 is required). Check the 'ReadMe' to learn how to find course codes.";
    private static final String ERROR_COURSE_CODES_EMPTY = "Course codes list is empty after parsing. Make sure you entered them correctly.";
    private static final String ERROR_COURSE_CODES_MAX = "Maximum of " + MAX_COURSE_CODES + " course codes allowed.";
    private static final String WARNING_INTERVAL_LOW = "An interval value less than the recommended was entered. If any problems arise, consider using the recommended value.";
    private static final String WARNING_DURATION_INVALID = "Duration provided is not a positive integer. Using the default value for an infinite duration.";
    
    private ConfigurationLoader() {
        throw new AssertionError("Utility class should not be instantiated.");
    }
    
    public static UserData loadData() throws IOException {
        Properties properties = loadProperties();
        
        String driverPath = validateAndGetDriverPath(properties);
        String browserPath = validateAndGetBrowserPath(properties);

        String username = validateAndGetUsername(properties);
        String password = validateAndGetPassword(properties);
        String authenticatorName = validateAndGetAuthenticatorName(properties);
        String authenticationKey = validateAndGetAuthenticationKey(properties);
        String term = validateAndGetTerm(properties);
        Set<String> courseCodes = validateAndGetCourseCodes(properties);
        int interval = validateAndGetInterval(properties);
        int duration = validateAndGetDuration(properties);
        
        return new UserData(driverPath, browserPath, username, password, authenticatorName, authenticationKey, term, courseCodes, interval, duration);
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        Path configPath = Paths.get(CONFIG_FILE_PATH);
        
        if (!Files.exists(configPath)) {
            throw new FileNotFoundException(String.format(ERROR_CONFIG_FILE_NOT_FOUND, CONFIG_FILE_PATH));
        }
        
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(new java.io.InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException error) {
            throw new IOException("Failed to read configuration file: " + error.getMessage(), error);
        }
        
        return properties;
    }

    private static String validateAndGetDriverPath(Properties properties) {
        String driverPath = getProperty(properties, KEY_DRIVER_PATH);

        if (driverPath == null || driverPath.trim().isBlank()) {
            throw new IllegalArgumentException(ERROR_DRIVER_PATH_INVALID);
        }

        return driverPath.trim();
    }

    private static String validateAndGetBrowserPath(Properties properties) {
        String browserPath = getProperty(properties, KEY_BROWSER_PATH);

        if (browserPath == null || browserPath.trim().isBlank()) {
            throw new IllegalArgumentException(ERROR_BROWSER_PATH_INVALID);
        }

        return browserPath.trim();
    }
    
    private static String validateAndGetUsername(Properties properties) {
        String username = getProperty(properties, KEY_USERNAME);

        if (username == null || username.trim().isBlank()) {
            throw new IllegalArgumentException(ERROR_USERNAME_MISSING);
        }

        return username.trim() + EMAIL_DOMAIN;
    }

    private static String validateAndGetPassword(Properties properties) {
        String password = getProperty(properties, KEY_PASSWORD);

        if (password == null || password.trim().isBlank()) {
            throw new IllegalArgumentException(ERROR_PASSWORD_MISSING);
        }

        return password.trim();
    }

    private static String validateAndGetAuthenticatorName(Properties properties) {
        String authenticatorName = getProperty(properties, KEY_AUTHENTICATOR_NAME);

        if (authenticatorName == null || authenticatorName.trim().isBlank()) {
            throw new IllegalArgumentException(ERROR_AUTHENTICATOR_NAME_MISSING);
        }

        return authenticatorName.trim();
    }

    private static String validateAndGetAuthenticationKey(Properties properties) {
        String authenticationKey = getProperty(properties, KEY_AUTHENTICATION_KEY);

        if (authenticationKey == null || authenticationKey.trim().isBlank()) {
            throw new IllegalArgumentException(ERROR_AUTHENTICATION_KEY_MISSING);
        }

        return authenticationKey.trim();
    }

    private static int validateAndGetInterval(Properties properties) {
        String intervalString = getProperty(properties, KEY_INTERVAL);

        int interval = parseNumberString(intervalString);
        
        if (interval < MIN_RECOMMENDED_INTERVAL) {
            LOGGER.warning(WARNING_INTERVAL_LOW);
        }
        
        return interval;
    }

    private static int validateAndGetDuration(Properties properties) {
        String durationString = getProperty(properties, KEY_DURATION);

        int duration = parseNumberString(durationString);

        if (duration < 0) {
            LOGGER.warning(WARNING_DURATION_INVALID);

            return 0;
        }

        return duration;
    }

    private static String validateAndGetTerm(Properties properties) {
        String termRaw = getProperty(properties, KEY_TERM);

        if (termRaw == null || termRaw.trim().isBlank()) {
            throw new IllegalArgumentException(ERROR_TERM_MISSING);
        }
        
        String term = termRaw.trim();

        validateTermFormat(term);
        
        return term;
    }
    
    private static void validateTermFormat(String term) {
        Matcher matcher = TERM_PATTERN.matcher(term);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(ERROR_TERM_INVALID);
        }
        
        int termYear = Integer.parseInt(matcher.group(1));
        int currentYear = Year.now().getValue();
        
        if (termYear < currentYear || termYear > currentYear + 1) {
            throw new IllegalArgumentException(ERROR_TERM_YEAR_INVALID);
        }
    }

    private static Set<String> validateAndGetCourseCodes(Properties properties) {
        String courseCodesStr = getProperty(properties, KEY_COURSE_CODES);

        if (courseCodesStr == null || courseCodesStr.trim().isBlank()) {
            throw new IllegalArgumentException(ERROR_COURSE_CODES_MISSING);
        }
        
        Set<String> courseCodes = parseCourseCodes(courseCodesStr);
        
        if (courseCodes.isEmpty()) {
            throw new IllegalArgumentException(ERROR_COURSE_CODES_EMPTY);
        }
        if (courseCodes.size() > MAX_COURSE_CODES) {
            throw new IllegalArgumentException(ERROR_COURSE_CODES_MAX);
        }
        
        return courseCodes;
    }

    private static Set<String> parseCourseCodes(String courseCodesStr) {
        return Stream.of(courseCodesStr.split(COURSE_CODE_SEPARATOR))
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
    }
    
    private static int parseNumberString(String numberString) {
        try {
            return Integer.parseInt(numberString.trim());
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(ERROR_NUM_INVALID, error);
        }
    }
    
    private static String getProperty(Properties properties, String key) {
        return properties.getProperty(key);
    }
}
