package com.simplyhealth;

import java.util.Date;

public class DailyWeightInfo {

    private String captureDate;
    private double weight;

    public DailyWeightInfo(String captureDate, double weight) {
        this.captureDate = captureDate;
        this.weight = weight;
    }

    public DailyWeightInfo() {}

    public String getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(String captureDate) {
        this.captureDate = captureDate;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
