package com.example.model;

public class SensorData {
    private String spotId;          // ID места (например, "A-01")
    private double rawDistance;     // Сырое расстояние в см (от датчика)
    private double filteredDistance;// Расстояние после EMA-фильтрации
    private String status;          // FREE, OCCUPIED, RESERVED
    private long timestamp;

    public SensorData(String spotId, double rawDistance, double filteredDistance, String status) {
        this.spotId = spotId;
        this.rawDistance = rawDistance;
        this.filteredDistance = filteredDistance;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    // Геттеры и сеттеры
    public String getSpotId() { return spotId; }
    public double getRawDistance() { return rawDistance; }
    public double getFilteredDistance() { return filteredDistance; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
}