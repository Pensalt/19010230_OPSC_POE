package com.simplyhealth;

public class MealBreakdown {
    private String captureDate;
    private double calories;

    public MealBreakdown(String captureDate, double calories) {
        this.captureDate = captureDate;
        this.calories = calories;
    }

    public MealBreakdown() {
    }

    public String getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(String captureDate) {
        this.captureDate = captureDate;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }
}
