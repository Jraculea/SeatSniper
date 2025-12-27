package com.seatsniper.pages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LoopPage extends BasePage {
    public static final String RESET_ANSI = "\u001B[0m";
    public static final String BOLD_TEXT = "\u001B[1m";
    public static final String BOLD_RED_TEXT = "\u001B[1;31m";
    public static final String BOLD_GREEN_TEXT = "\u001B[1;32m";
    public static final String BOLD_YELLOW_TEXT = "\u001B[1;33m";
    public static final String BOLD_PURPLE_TEXT = "\u001B[1;95m";
    public static final String BOLD_GRAY_TEXT = "\u001B[1;90m";
    public static final String BOLD_BLACK_TEXT = "\u001B[1;90m";

    public static final String GREEN_BACKGROUND = "\u001B[42m";
    public static final String YELLOW_BACKGROUND = "\u001B[43m";
    public static final String GRAY_BACKGROUND = "\u001B[100m";
    public static final String BLACK_BACKGROUND = "\u001B[40m";
    public static final String RED_TEXT = "\u001B[31m";
   
    
    private final By continueButton = By.xpath("//button[contains(text(), 'Continue')]");
    private final By enrollButton = By.xpath("//*[@id=\"legend_checkout\"]/input[3]");
    private final By agreeToTermsButton = By.xpath("//*[@id=\"noticePopup\"]/div/div[3]/input[1]"); 
    private final By exitCheckoutButton = By.xpath("//*[@id=\"legend_checkout\"]/input[4]");
    private final By advancedSearchButton = By.xpath("//*[@id=\"tab_selected\"]/div[8]/div[2]/button");
    private final By popupSearchBar = By.id("cb_search_term");
    private final By popupSearchButton = By.id("course-browsing-search-btn");
    private final By firstResultCheckbox = By.id("cb-result-0");
    private final By addSelectedButton = By.xpath("//*[@id=\"noticePopup\"]/div/div[3]/input[1]");
    private final By closePopupBtn = By.xpath("//*[@id=\"noticePopup\"]/div/div[3]/input[2]");

    private List<String> courseCodes;
    private Map<String, String> enrollmentStatuses;
    private Map<String, String> courseNames;
    private final int loopDuration;
    private final int loopInterval;

    public LoopPage(List<String> courseCodes, int loopDuration, int loopInterval) {
        this.courseCodes = courseCodes;
        this.loopDuration = loopDuration;
        this.loopInterval = loopInterval * 1000;
        enrollmentStatuses = HashMap.newHashMap(courseCodes.size());
        courseNames = HashMap.newHashMap(courseCodes.size());
    }

    public void startEnrollmentLoop() {
        boolean finished = false;
        long startTime = System.currentTimeMillis();
        long durationMillis = loopDuration * 1000L;

        while (!finished) {
            if (loopDuration > 0 && System.currentTimeMillis() - startTime > durationMillis) {
                System.out.println("Max time exceeded.");
                printEnrollmentStatuses();

                break;
            }

            Iterator<String> iterator = courseCodes.iterator();

            while (iterator.hasNext()) {
                String code = iterator.next();
                boolean courseExists = searchForCourse(code);

                if (!courseExists) {
                    enrollmentStatuses.put(code, BOLD_GRAY_TEXT + "UNAVAILABLE" + RESET_ANSI);
                    iterator.remove(); // O(n) but its fine since N has a max of 7 (faster than LinkedList and less costly than Sets)
                } 
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            attemptEnrollment();
            printEnrollmentStatuses();
            click(exitCheckoutButton);

            if (courseCodes.isEmpty()) {
                finished = true;

                System.out.println("Success! You were enrolled/waitlisted in all courses.\n");
            } else {
                applyCooldown();
            }
        }
    }

    private boolean searchForCourse(String courseCode) {
        hover(advancedSearchButton);
        click(advancedSearchButton);
        waitUntilVisible(popupSearchBar);

        setTextAndEnter(popupSearchBar, courseCode);
        click(popupSearchButton);

        try {
            waitUntilVisible(firstResultCheckbox);
        } catch (Exception e) {
            System.out.println(RED_TEXT + "\nCourse " + RESET_ANSI + BOLD_BLACK_TEXT + courseCode + RESET_ANSI + RED_TEXT + " not found. Did you enter the right code?\n" + RESET_ANSI);
            
            if (isDisplayed(closePopupBtn)) {
                click(closePopupBtn);
            }

            return false;
        }

        String courseNameText = find(By.id("cb_search_results")).findElement(By.xpath(".//tr[1]/td[2]")).getAttribute("innerText").trim();
        
        if (courseNameText.isBlank()) {
            courseNameText = getText(By.cssSelector("#cb_search_results .cb-course-name"));
        }

        courseNameText = courseNameText.replace("\n", " - ").trim();

        courseNames.put(courseCode, courseNameText);

        click(firstResultCheckbox);
        click(addSelectedButton);

        if (isDisplayed(closePopupBtn)) {
            click(closePopupBtn);
        }

        return true;
    }

    private void attemptEnrollment() {
        click(continueButton);
        click(enrollButton);
        click(agreeToTermsButton);

        waitUntilVisible(By.id("legend_box"));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<WebElement> courseBoxes = findMultiple(By.xpath("//div[contains(@class, 'course_box')]"));
        Iterator<String> iterator = courseCodes.iterator();

        while (iterator.hasNext()) {
            String code = iterator.next();
            boolean found = false;

            for (WebElement box : courseBoxes) {
                if (box.getText().contains(code)) {
                    found = true;

                    if (processBoxResult(box, code)) {
                        iterator.remove();
                    } else {
                        // logic for refreshing the course that failed to enroll
                        // WILL IMPLEMENT NEXT SEMESTER (incase course placement is lost due to testing errors)
                        // try {
                        //     click(By.xpath(".//button[contains(@title, 'Remove')]"));

                        //     System.out.println("Removed " + code + " from cart to refresh for next attempt.");
                        // } catch (Exception e) {
                        //     System.out.println("Could not find remove button for " + code);
                        // }
                    }

                    break;
                }
            }

            if (!found) {
                enrollmentStatuses.put(code, BOLD_GRAY_TEXT + "RESULT_NOT_FOUND" + RESET_ANSI);
            }
        }
    }

    private boolean processBoxResult(WebElement box, String code) {
        try {
            String rawText = box.getText();
            
            if (rawText.contains("Failed")) {
                String reason = "Enrollment Failed";
                int index = rawText.lastIndexOf("Failed");

                if (index != -1) {
                    reason = rawText.substring(index + 6).trim().replace("\n", " ");
                }

                enrollmentStatuses.put(code, BOLD_RED_TEXT + "FAILED: " + RESET_ANSI + reason);

                return false;
            } else {
                String status;

                if (rawText.contains("wait list")) {
                    status = extractWaitlistPosition(rawText);
                } else {
                    status = BOLD_GREEN_TEXT + "ENROLLED" + RESET_ANSI;
                }

                enrollmentStatuses.put(code, status);
            }

            return true;
        } catch (Exception e) {
            System.out.println("Extraction error for " + code + ": " + e.getMessage());
        }

        return false;
    }

    private String extractWaitlistPosition(String text) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("position number (\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return BOLD_YELLOW_TEXT + "WAIT-LISTED:" + RESET_ANSI + " Position #" + matcher.group(1);
        }

        return "STATUS_UNKNOWN";
    }

    private void printEnrollmentStatuses() {
        StringBuilder enrollmentDetails = new StringBuilder();

        String headerText = "----- CURRENT ENROLLMENT STATUS -----";
        String header = "\n" + BOLD_TEXT + headerText + RESET_ANSI + "\n\n";

        enrollmentDetails.append(header);

        for (Map.Entry<String, String> entry : enrollmentStatuses.entrySet()) {
            String code = entry.getKey();
            String status = entry.getValue();
            String name = courseNames.getOrDefault(code, "Unknown Course");

            enrollmentDetails.append(name).append(" [").append(code).append("] | ").append(status).append("\n");
        }

        String footer = "\n" + BOLD_TEXT + "-".repeat(headerText.length()) + RESET_ANSI + "\n";

        enrollmentDetails.append(footer);
        System.out.println(enrollmentDetails.toString());
    }

    private void applyCooldown() {
        try {
            int totalSleepTime = loopInterval + (int) (Math.random() * 5000);
            int secondsToWait = totalSleepTime / 1000;

            for (int i = secondsToWait; i > 0; i--) {
                String timeText = i == 1 ? "1 second" : i + " seconds";

                System.out.print("\rWaiting for " + BOLD_PURPLE_TEXT + timeText + RESET_ANSI + " before next attempt...");
                System.out.flush();

                Thread.sleep(1000);
            }

            System.out.print("\r" + " ".repeat(60) + "\r");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
