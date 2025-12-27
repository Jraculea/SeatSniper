package com.seatsniper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.seatsniper.utilities.ConfigurationLoader;
import com.seatsniper.pages.BasePage;
import com.seatsniper.pages.LoginPage;
import com.seatsniper.pages.AuthenticationPage;
import com.seatsniper.pages.NavigatePage;
import com.seatsniper.pages.ScheduleBuilderPage;
import com.seatsniper.pages.LoopPage;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Sniper {
    private static final String STARTING_PAGE_URL = "https://www.cuny.edu/about/administration/offices/cis/cunyfirst/";
    private static final String SITE_DATE_FORMAT = "MMMM d, yyyy\nh:mma";
    private static final ZoneId ENROLLMENT_TIMEZONE = ZoneId.of("America/New_York");

    private static final String ERROR_APPOINTMENT_DATE = "Your enrollment appointment must be within 24 hours for the process to be scheduled. Try again at a later time.";
    private static final String MESSAGE_START_AUTOMATION = "\n\nStarting the auto-enrollment section. Please report any encountered errors.";

    protected UserData userData;
    protected WebDriver driver;
    protected BasePage basePage;
    protected LoginPage loginPage;
    protected AuthenticationPage authenticationPage;
    protected NavigatePage navigatePage;
    protected ScheduleBuilderPage scheduleBuilderPage;
    protected List<String> localCourseCodes;
    protected LoopPage loopPage;

    public void setUp() throws IOException {
        userData = ConfigurationLoader.loadData();

        System.setProperty("webdriver.chrome.driver", userData.getDriverPath());

        ChromeOptions options = new ChromeOptions();
        options.setBinary(userData.getBrowserPath());
        options.addArguments("--start-maximized");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu ");
        options.addArguments("--no-sandbox");
        //options.addArguments("--headless=new");

        driver = new ChromeDriver(options);
        driver.get(STARTING_PAGE_URL);

        BasePage.setDriver(driver);

        loginPage = new LoginPage();
    }
    
    public void login() {
        authenticationPage = loginPage.logIntoApplication(userData.getUsername(), userData.getPassword());
    }

    public void authenticate() throws InterruptedException {
        navigatePage = authenticationPage.authenticateLogin(userData.getAuthenticatorName(), userData.getAuthenticationKey());
    }

    public void determineEligibility() throws InterruptedException {
        navigatePage.toStudentCenter();

        ///// Move this code to be under the check for empty course codes, that way a proper wait can be added before going back to the Student Center page /////

        String enrollmentDate = navigatePage.getEnrollmentAppointmentDate(userData.getCourseTerm());

        ZonedDateTime appointmentTime = parseAppointmentTime(enrollmentDate);
        ZonedDateTime currentTime = ZonedDateTime.now(ENROLLMENT_TIMEZONE);
        ZonedDateTime windowStartTime = appointmentTime.minus(24, ChronoUnit.HOURS);

        boolean mustWait = false;

        if (!currentTime.isAfter(windowStartTime)) {
            throw new InterruptedException(ERROR_APPOINTMENT_DATE);
        } else {
            mustWait = !currentTime.isAfter(appointmentTime);
        }

        //System.out.println("Wait before process: " + mustWait);

        //

        if (userData.getCourseCodes().isEmpty()) {
            //method to navigate back to student center
            //method to navigate to degree works
            //method to check courses based on ratemyprofessor, and add them to the HashSet
            System.out.println("\nNo course codes were entered.\nSupport coming soon for automatic course addition via RateMyProfessor.");

            selfDestruct();

            return;
        }

        navigatePage.backToStudentCenter();
    }

    public void navigateToScheduleBuilder() {
        scheduleBuilderPage = navigatePage.toScheduleBuilder();
    }

    private void switchTabs(int index) {
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());

        if (index < 0 || index >= tabs.size()) {
            return;
            //throw new IndexOutOfBoundsException("Invalid index provided for the ArrayList tabs");
        }

        driver.switchTo().window(tabs.get(index));
        //driver.close();
        //driver.switchTo().window(tabs.get(0));
    }

    public void selectTerm() {
        switchTabs(1);

        scheduleBuilderPage.setTerm(userData.getCourseTerm());
        scheduleBuilderPage.clickSelectedTerm();
    }

    public void initializeEnrollmentLoop() {
        System.out.println(MESSAGE_START_AUTOMATION);

        localCourseCodes = new ArrayList<>(userData.getCourseCodes());
        loopPage = new LoopPage(localCourseCodes, userData.getDuration(), userData.getInterval());
    }

    public void startEnrollmentLoop() throws InterruptedException {
        Thread.sleep(5000);
        loopPage.startEnrollmentLoop();
    }

    public void selfDestruct() {
        driver.quit();
    }
    
    private ZonedDateTime parseAppointmentTime(String siteDateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SITE_DATE_FORMAT, Locale.ENGLISH);
        LocalDateTime localDateTime = LocalDateTime.parse(siteDateString.trim(), formatter);

        return localDateTime.atZone(ENROLLMENT_TIMEZONE);
    }
}
