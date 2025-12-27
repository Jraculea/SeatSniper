package com.seatsniper.pages;

import org.jboss.aerogear.security.otp.Totp;
import org.openqa.selenium.By;

public class AuthenticationPage extends BasePage {
    private static final long INITIAL_WAIT_MS = 6000;
    private static final long AUTHENTICATOR_SWITCH_WAIT_MS = 3000;
    
    private static final String ERROR_AUTHENTICATOR_NOT_FOUND = "The provided authenticator name: \"%s\" is not a valid option registered to your CUNYfirst account.";
    private static final String AUTHENTICATOR_LINK_PREFIX = "Enter OTP from device ";
    private static final String AUTHENTICATOR_OPTIONS_BUTTON_TEXT = "Return to All Options";
    private static final String AUTHENTICATOR_SUBSTRING_KEY = "phone ";

    private final By defaultAuthenticatorLocator = By.xpath("//label[@for='otpValue|input']//span");
    private final By authenticatorOptionsButton = By.linkText(AUTHENTICATOR_OPTIONS_BUTTON_TEXT);
    private final By changeAuthenticatorHeader = By.id("loginForm");
    private final By tokenField = By.id("otpValue|input");
    private final By verifyButton = By.id("_oj1|text");

    public NavigatePage authenticateLogin(String authenticatorName, String authenticationKey) throws InterruptedException {
        Thread.sleep(INITIAL_WAIT_MS);
        
        selectAuthenticatorIfNeeded(authenticatorName);
        
        Thread.sleep(AUTHENTICATOR_SWITCH_WAIT_MS);
        
        enterOtpToken(authenticationKey);
        
        return new NavigatePage();
    }

    private void selectAuthenticatorIfNeeded(String authenticatorName) {
        String defaultAuthenticator = find(defaultAuthenticatorLocator).getText();
        defaultAuthenticator = defaultAuthenticator.substring(defaultAuthenticator.indexOf(AUTHENTICATOR_SUBSTRING_KEY) + AUTHENTICATOR_SUBSTRING_KEY.length()).trim();

        if (!authenticatorName.equals(defaultAuthenticator)) {
            switchAuthenticator(authenticatorName);
        }
    }

    private void switchAuthenticator(String authenticatorName) {
        click(authenticatorOptionsButton);

        waitUntilVisible(changeAuthenticatorHeader);

        By targetAuthenticatorLocator = By.linkText(AUTHENTICATOR_LINK_PREFIX + authenticatorName);

        if (isDisplayed(targetAuthenticatorLocator)) {
            click(targetAuthenticatorLocator);
        } else {
            throw new IllegalArgumentException(
                String.format(ERROR_AUTHENTICATOR_NOT_FOUND, authenticatorName)
            );
        }
    }

    private void enterOtpToken(String authenticationKey) {
        Totp totp = new Totp(authenticationKey);
        String currentToken = totp.now();

        //System.out.println(totp.toString());

        waitUntilVisible(tokenField);
        setText(tokenField, currentToken);

        click(verifyButton);
    }
}
