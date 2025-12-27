package com.seatsniper.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    
    protected static WebDriver driver;
    protected static WebDriverWait wait;

    public static void setDriver(WebDriver driverPassed) {
        driver = driverPassed;
        wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    protected WebElement find(By locator) {
        return driver.findElement(locator);
    }

    protected List<WebElement> findMultiple(By locator) {
        return driver.findElements(locator);
    }

    protected void setText(By locator, String text) {
        WebElement element = find(locator);

        element.clear();
        element.sendKeys(text);
    }

    protected void setTextAndEnter(By locator, String text) {
        WebElement element = find(locator);

        element.clear();
        element.sendKeys(text);
        element.sendKeys(Keys.ENTER);
    }

    protected void hover(By locator) {
        WebElement element = find(locator);
        Actions actions = new Actions(driver);

        actions.moveToElement(element).perform();
    }

    protected void click(By locator) {
        waitUntilClickable(locator);
        find(locator).click();
    }

    protected void waitUntilVisible(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitUntilClickable(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected String getText(By locator) {
        try {
            return find(locator).getText();
        } catch (NoSuchElementException exception) {
            return "";
        }
    }

    protected boolean isDisplayed(By locator) {
        try {
            return find(locator).isDisplayed();
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    protected void waitForInvisibility(By locator) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));

            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            // If the loader wasn't there or already gone, just continue
        }
    }

    protected void executeJavaScript(String script, Object... args) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(script, args);
    }
}
