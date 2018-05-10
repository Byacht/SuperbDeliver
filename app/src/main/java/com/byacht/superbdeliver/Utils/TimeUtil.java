package com.byacht.superbdeliver.Utils;

import android.text.format.Time;

public class TimeUtil {

    public static String getCurrentTime(){

        Time time = new Time("GMT+8:00");
        time.set(System.currentTimeMillis() + 8 * 60 * 60 * 1000);
        int year = time.year;
        String month = formateTime(time.month + 1);
        String day = formateTime(time.monthDay);
        String minute = formateTime(time.minute);
        String hour = formateTime(time.hour);
        String sec = formateTime(time.second);
        StringBuilder sb = new StringBuilder();
        sb.append(year)
                .append("-")
                .append(month)
                .append("-")
                .append(day)
                .append(" ")
                .append(hour)
                .append(":")
                .append(minute)
                .append(":")
                .append(sec);
        return sb.toString();
    }

    private static String formateTime(int number){
        String time = String.valueOf(number);
        if (String.valueOf(time).length() == 1){
            return "0" + time;
        } else {
            return time;
        }
    }
}
