package vn.edu.iuh.fit.week05.frontend.utils;

import java.time.LocalTime;

public class Greeting {

    public static String getGreeting() {
        int currentHour = LocalTime.now().getHour();
        if (currentHour < 12) {
            return "Good morning";
        } else if (currentHour < 18) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }
}