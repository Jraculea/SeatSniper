package com.seatsniper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class UserData {
    private final String driverPath;
    private final String browserPath;
    private final String username;
    private final String password;
    private final String authenticatorName;
    private final String authenticationKey;
    private final String courseTerm;
    private final Set<String> courseCodes;
    private final int interval;
    private final int duration;
    
    public UserData(
            String driverPath,
            String browserPath,
            String username,
            String password,
            String authenticatorName,
            String authenticationKey,
            String courseTerm,
            Set<String> courseCodes,
            int interval,
            int duration
    ) {
        this.driverPath = Objects.requireNonNull(driverPath, "Driver path cannot be null");
        this.browserPath = Objects.requireNonNull(browserPath, "Browser path cannot be null");
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.password = Objects.requireNonNull(password, "Password cannot be null");
        this.authenticatorName = Objects.requireNonNull(authenticatorName, "Authenticator name cannot be null");
        this.authenticationKey = Objects.requireNonNull(authenticationKey, "Authentication key cannot be null");
        this.courseTerm = Objects.requireNonNull(courseTerm, "Course term cannot be null");
        
        if (interval <= 0) {
            throw new IllegalArgumentException("Interval must be a positive integer, got: " + interval);
        }
        if (duration < 0) {
            throw new IllegalArgumentException("Max duration must be a positive integer or 0, got: " + duration);
        }

        this.interval = interval;
        this.duration = duration;
        
        this.courseCodes = courseCodes != null 
                ? Collections.unmodifiableSet(new HashSet<>(courseCodes))
                : Collections.emptySet();
    }

    public String getDriverPath() {
        return driverPath;
    }

    public String getBrowserPath() {
        return browserPath;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public String getAuthenticationKey() {
        return authenticationKey;
    }

    public Set<String> getCourseCodes() {
        return courseCodes;
    }

    public String getCourseTerm() {
        return courseTerm;
    }

    public int getInterval() {
        return interval;
    }

    public int getDuration() {
        return duration;
    }

    public boolean hasCourseCode(String courseCode) {
        return courseCodes.contains(courseCode);
    }

    public int getCourseCodeCount() {
        return courseCodes.size();
    }

    public boolean hasNoCourseCodes() {
        return courseCodes.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        UserData userData = (UserData) obj;

        return interval == userData.interval
                && duration == userData.duration
                && Objects.equals(driverPath, userData.driverPath)
                && Objects.equals(browserPath, userData.browserPath)
                && Objects.equals(username, userData.username)
                && Objects.equals(password, userData.password)
                && Objects.equals(authenticatorName, userData.authenticatorName)
                && Objects.equals(authenticationKey, userData.authenticationKey)
                && Objects.equals(courseCodes, userData.courseCodes)
                && Objects.equals(courseTerm, userData.courseTerm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            driverPath,
            browserPath,
            username,
            password,
            authenticatorName,
            authenticationKey, 
            courseCodes,
            courseTerm,
            interval,
            duration
        );
    }

    @Override
    public String toString() {
        return "UserData{ " +
                "driverPath='" + driverPath + '\'' +
                ", browserPath='" + browserPath + '\'' +
                ", username='" + username + '\'' +
                ", authenticatorName='" + authenticatorName + '\'' +
                ", courseTerm='" + courseTerm + '\'' +
                ", interval=" + interval +
                ", duration=" + duration +
                ", courseCodeCount=" + courseCodes.size() +
                " }";
    }
}
