package com.simplyhealth;

public class User {
    private String firstName;
    private String surname;
    private double goalWeight;
    private double currentHeight;
    private double goalCalories;
    private Boolean useMetric;

    public User(String firstName, String surname, double goalWeight, double currentHeight, double goalCalories, Boolean useMetric) {
        this.firstName = firstName;
        this.surname = surname;
        this.goalWeight = goalWeight;
        this.currentHeight = currentHeight;
        this.goalCalories = goalCalories;
        this.useMetric = useMetric;
    }

    public User() {}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public double getGoalWeight() {
        return goalWeight;
    }

    public void setGoalWeight(double goalWeight) {
        this.goalWeight = goalWeight;
    }

    public double getCurrentHeight() {
        return currentHeight;
    }

    public void setCurrentHeight(double currentHeight) {
        this.currentHeight = currentHeight;
    }

    public double getGoalCalories() {
        return goalCalories;
    }

    public void setGoalCalories(double goalCalories) {
        this.goalCalories = goalCalories;
    }

    public Boolean getUseMetric() {
        return useMetric;
    }

    public void setUseMetric(Boolean useMetric) {
        this.useMetric = useMetric;
    }
}
