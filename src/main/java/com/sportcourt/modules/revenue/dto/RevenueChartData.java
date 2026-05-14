package com.sportcourt.modules.revenue.dto;

public class RevenueChartData {
    private String[] labels;
    private double[] currentValues;
    private double[] previousValues;

    public RevenueChartData(String[] labels, double[] currentValues, double[] previousValues) {
        this.labels = labels;
        this.currentValues = currentValues;
        this.previousValues = previousValues;
    }

    public String[] getLabels() { return labels; }
    public double[] getCurrentValues() { return currentValues; }
    public double[] getPreviousValues() { return previousValues; }
}
