package com.seatsniper;

public class Main {
    private static final String ERROR_DURING_SETUP = "An error occured during the set up process. Did you properly enter your information?";

    public static void main(String[] args) {
        Sniper sniper = new Sniper();

        try {
            sniper.setUp();
            sniper.login();
            sniper.authenticate();
            sniper.determineEligibility();
            sniper.navigateToScheduleBuilder();
            sniper.selectTerm();
            sniper.initializeEnrollmentLoop();
            sniper.startEnrollmentLoop();
        } catch (InterruptedException e) {
            System.err.println(ERROR_DURING_SETUP + "\n" + e.getMessage());
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println(ERROR_DURING_SETUP + "\n" + e.getMessage());
            e.printStackTrace();
        } finally {
            sniper.selfDestruct();
        }
    }
}
