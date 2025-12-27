package com.seatsniper.pages;

import org.openqa.selenium.By;

public class ScheduleBuilderPage extends BasePage {
    private static final String TERM_CODE_PREFIX = "3";
    private static final String TERM_CODE_SPRING = "10";
    private static final String TERM_CODE_SUMMER = "20";
    private static final String TERM_CODE_FALL = "30";
    private static final String TERM_CODE_WINTER = "40";
    
    private static final String TERM_XPATH_TEMPLATE = "//div[@id='welcomeTerms']//div[@data-term='%s']//a[contains(@class, 'term-card-title')]";
    
    private static final String ERROR_INVALID_TERM_SEASON = "Invalid term season: %s. Expected: Spring, Summer, Fall, or Winter";
    private static final String ERROR_TERM_FORMAT = "Invalid term format. Expected format: 'YYYY Season' (e.g., '2026 Spring')";

    private By selectedTermLocator;

    public void setTerm(String term) {
        String termCode = calculateTermCode(term);
        selectedTermLocator = By.xpath(String.format(TERM_XPATH_TEMPLATE, termCode));
    }

    private String calculateTermCode(String term) {
        int spaceIndex = term.indexOf(" ");
        
        if (spaceIndex == -1 || spaceIndex == 0 || spaceIndex == term.length() - 1) {
            throw new IllegalArgumentException(ERROR_TERM_FORMAT);
        }

        String year = term.substring(0, spaceIndex);
        String season = term.substring(spaceIndex + 1).trim();
        
        validateYear(year);
        
        String seasonCode = getSeasonCode(season);
        
        return TERM_CODE_PREFIX + year + seasonCode;
    }

    private void validateYear(String year) {
        if (year.length() != 4) {
            throw new IllegalArgumentException(ERROR_TERM_FORMAT);
        }
        
        try {
            Integer.valueOf(year);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ERROR_TERM_FORMAT, e);
        }
    }

    private String getSeasonCode(String season) {
        return switch (season) {
            case "Spring" -> TERM_CODE_SPRING;
            case "Summer" -> TERM_CODE_SUMMER;
            case "Fall" -> TERM_CODE_FALL;
            case "Winter" -> TERM_CODE_WINTER;
            default -> throw new IllegalArgumentException(
                String.format(ERROR_INVALID_TERM_SEASON, season)
            );
        };
    }

    public void clickSelectedTerm() {
        if (isDisplayed(selectedTermLocator)) {
            click(selectedTermLocator);
        }
    }
}
