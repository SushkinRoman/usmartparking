package com.example.model;

public class ParkingSnapshot {
    private String timestamp;
    private int freeCount;
    private int occupiedCount;
    private int reservedCount;

    public ParkingSnapshot(String timestamp, int freeCount, int occupiedCount, int reservedCount) {
        this.timestamp = timestamp;
        this.freeCount = freeCount;
        this.occupiedCount = occupiedCount;
        this.reservedCount = reservedCount;
    }

    // Геттеры
    public String getTimestamp() { return timestamp; }
    public int getFreeCount() { return freeCount; }
    public int getOccupiedCount() { return occupiedCount; }
    public int getReservedCount() { return reservedCount; }
}