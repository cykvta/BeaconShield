package icu.cykuta.beaconshield.utils;

public class Date {
    public static String secondsToTime(int seconds) {
        seconds = Math.max(seconds, 0);
        int days = seconds / 86400;
        int hours = (seconds % 86400) / 3600;
        int minutes = ((seconds % 86400) % 3600) / 60;
        int sec = ((seconds % 86400) % 3600) % 60;
        return days + "d " + hours + "h " + minutes + "m " + sec + "s";
    }
}
