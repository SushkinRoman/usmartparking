package com.example.model;

public class SensorData {
    private String spaceId;
    private double rawValue;
    private double filteredValue;
    private String status; // "СВОБОДНО", "ЗАНЯТО", "ЗАБРОНИРОВАНО"
    private long timestamp;

    public SensorData() {}

    public SensorData(String spaceId, double rawValue, double filteredValue, String status) {
        this.spaceId = spaceId;
        this.rawValue = rawValue;
        this.filteredValue = filteredValue;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    // Геттеры и сеттеры
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
    public double getRawValue() { return rawValue; }
    public void setRawValue(double rawValue) { this.rawValue = rawValue; }
    public double getFilteredValue() { return filteredValue; }
    public void setFilteredValue(double filteredValue) { this.filteredValue = filteredValue; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}