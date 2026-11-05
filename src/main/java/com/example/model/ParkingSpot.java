package com.example.model;

public class ParkingSpot {
    private String spotId;
    private String status; // "СВОБОДНО", "ЗАНЯТО", "ЗАБРОНИРОВАНО"
    private double lastFilteredDistance;

    public ParkingSpot(String spotId) {
        this.spotId = spotId;
        this.status = "СВОБОДНО";
        this.lastFilteredDistance = 150.0;
    }

    public void update(double filteredDistance) {
        this.lastFilteredDistance = filteredDistance;
        if (filteredDistance < 50.0 && !"ЗАБРОНИРОВАНО".equals(this.status)) {
            this.status = "ЗАНЯТО";
        } else if (!"ЗАБРОНИРОВАНО".equals(this.status)) {
            this.status = "СВОБОДНО";
        }
    }

    public String getSpotId() { return spotId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getLastFilteredDistance() { return lastFilteredDistance; }
}