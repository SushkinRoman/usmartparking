package com.example.model;

public class ParkingSpot {
    private String spotId;
    private String status; // "FREE", "OCCUPIED", "RESERVED"
    private double lastFilteredDistance;

    public ParkingSpot(String spotId) {
        this.spotId = spotId;
        this.status = "FREE";
        this.lastFilteredDistance = 150.0; // Изначально место свободно (далеко)
    }

    public void update(double filteredDistance) {
        this.lastFilteredDistance = filteredDistance;
        // Логика определения занятости: если расстояние < 50 см, место занято
        if (filteredDistance < 50.0 && !"RESERVED".equals(this.status)) {
            this.status = "OCCUPIED";
        } else if (!"RESERVED".equals(this.status)) {
            this.status = "FREE";
        }
    }

    public String getSpotId() { return spotId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; } // Для ручного бронирования в будущем
    public double getLastFilteredDistance() { return lastFilteredDistance; }
}