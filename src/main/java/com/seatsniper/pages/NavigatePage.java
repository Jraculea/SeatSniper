package com.seatsniper.pages;

import java.util.Set;
import java.util.Collections;

import org.openqa.selenium.By;

public class NavigatePage extends BasePage {
    private static final long ENROLLMENT_PAGE_WAIT_MS = 3000;
    private static final String TERM_SUFFIX = " Term";
    private static final String TERM_XPATH_TEMPLATE = "//*[contains(text(), '%s')]";
    private final By studentCenterButton = By.id("win0divPTNUI_LAND_REC_GROUPLET$0");
    private final By scheduleBuilderButton = By.id("win0groupletPTNUI_LAND_REC_GROUPLET$12");
    private final By coursePlanningButton = By.id("win0divPTNUI_LAND_REC_GROUPLET$13");
    private final By enrollmentAppointmentButton = By.xpath("//span[contains(text(), 'Enrollment Appointment')]/ancestor::a");
    private final By upcomingAppointmentDate = By.id("ENRL_START$0");
    private final By returnButton = By.id("PT_WORK_PT_BUTTON_BACK$IMG");

    public String getEnrollmentAppointmentDate(String courseTerm) throws InterruptedException {
        navigateToEnrollmentAppointments();
        selectTermForAppointment(courseTerm);
        
        return retrieveAppointmentDate();
    }

    private void navigateToEnrollmentAppointments() throws InterruptedException {
        click(coursePlanningButton);
        
        waitUntilVisible(enrollmentAppointmentButton);

        try {
            click(enrollmentAppointmentButton);
        } catch (Exception e) {
            click(enrollmentAppointmentButton);
        }

        Thread.sleep(ENROLLMENT_PAGE_WAIT_MS);
    }

    private void selectTermForAppointment(String courseTerm) {
        String termText = courseTerm + TERM_SUFFIX;
        By termButton = By.xpath(String.format(TERM_XPATH_TEMPLATE, termText));
        
        click(termButton);
    }

    private String retrieveAppointmentDate() {
        waitUntilVisible(upcomingAppointmentDate);

        return getText(upcomingAppointmentDate);
    }

    public void toStudentCenter() {
        click(studentCenterButton);
    }

    public ScheduleBuilderPage toScheduleBuilder() {
        click(scheduleBuilderButton);
        
        return new ScheduleBuilderPage();
    }

    public void backToStudentCenter() {
        click(returnButton);
    }

    /////
    
    public Set<String> getAutomaticCourseCodes() {
        return Collections.emptySet(); //change later
    }
}
