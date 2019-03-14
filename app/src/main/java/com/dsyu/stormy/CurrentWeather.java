package com.dsyu.stormy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CurrentWeather {

    private String location;
    private String icon;
    private long time;
    private long sunRiseTime;
    private long sunSetTime;
    private double temperature;
    private int uvIndex;
    private double precipChance;
    private String summary;
    private String timeZone;

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public CurrentWeather(String icon, long time, long sunRiseTime, long sunSetTime, double temperature, int uvIndex, double precipChance, String summary, String timeZone) {
        this.icon = icon;
        this.time = time;
        this.sunRiseTime = sunRiseTime;
        this.sunSetTime =sunSetTime;
        this.temperature = (temperature - 32) * 5 / 9;
        this.uvIndex = uvIndex;
        this.precipChance = precipChance;
        this.summary = summary;
        this.timeZone = timeZone;
    }

    public CurrentWeather() {
    }

    public int getIconId() {
        int iconId = R.drawable.clear_day;
        switch (icon) {
            case "clear-day":
                break;
            case "clear-night":
                iconId = R.drawable.clear_night;
                break;
            case "rain":
                iconId = R.drawable.rain;
                break;
            case "snow":
                iconId = R.drawable.snow;
                break;
            case "sleet":
                iconId = R.drawable.sleet;
                break;
            case "wind":
                iconId = R.drawable.wind;
                break;
            case "fog":
                iconId = R.drawable.fog;
                break;
            case "cloudy":
                iconId = R.drawable.cloudy;
                break;
            case "partly-cloudy-day":
                iconId = R.drawable.partly_cloudy;
                break;
            case "partly-cloudy-night":
                iconId = R.drawable.cloudy_night;
                break;
        }
        return  iconId;
    }
    public long getTime() {
        return time;
    }

    public  String getFormattedTime() {
        SimpleDateFormat formatter = new SimpleDateFormat( "h:mm a" );
        formatter.setTimeZone( TimeZone.getTimeZone( timeZone ) );
        Date dateTime = new Date(time * 1000);
        return formatter.format( dateTime );
    }

    public long getSunRiseTime() {
        return sunRiseTime;
    }

    public void setSunRiseTime(long sunRiseTime) {
        this.sunRiseTime = sunRiseTime;
    }

    public long getSunSetTime() {
        return sunSetTime;
    }

    public void setSunSetTime(long sunSetTime) {
        this.sunSetTime = sunSetTime;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(int uvIndex) {
        this.uvIndex = uvIndex;
    }

    public double getPrecipChance() {
        return precipChance;
    }

    public void setPrecipChance(double precipChance) {
        this.precipChance = precipChance;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

}
