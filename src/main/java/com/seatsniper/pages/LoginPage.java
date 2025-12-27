package com.seatsniper.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

public class LoginPage extends BasePage {
    private static final String ERROR_USERNAME = "Username cannot be null or empty";
    private static final String ERROR_PASSWORD = "Password cannot be null or empty";

    private final By usernameField = By.id("CUNYfirstUsernameH");
    private final By passwordField = By.id("CUNYfirstPassword");
    private final By loginButton = By.id("submit");
    private final By errorMessage = By.className("mobile-message");
    private final By homePageLoginButton = By.id("menu-item-158900");
    private final By homePageCunyFirstButton = By.id("menu-item-90744");

    public AuthenticationPage logIntoApplication(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(ERROR_USERNAME);
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException(ERROR_PASSWORD);
        }

        navigateToLoginPage();
        enterCredentials(username, password);
        
        return clickLoginButton();
    }

    private void navigateToLoginPage() {
        waitUntilVisible(homePageLoginButton);
        hover(homePageLoginButton);

        click(homePageCunyFirstButton);
    }

    private void enterCredentials(String username, String password) {
        waitUntilVisible(usernameField);
        waitUntilVisible(passwordField);
        
        setText(usernameField, username);
        setText(passwordField, password);
    }

    private AuthenticationPage clickLoginButton() {
        click(loginButton);

        return new AuthenticationPage();
    }

    public String getErrorMessage() {
        try {
            return getText(errorMessage);
        } catch (NoSuchElementException exception) {
            return "";
        }
    }

    public boolean hasErrorMessage() {
        return isDisplayed(errorMessage) && !getErrorMessage().trim().isEmpty();
    }
}
