package com.example.service;

import com.example.model.ParkingSpot;
import com.example.model.SensorData;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SignalProcessingService {
    private final Map<String, ParkingSpot> buffer = new ConcurrentHashMap<>();
    private final double ALPHA = 0.333;

    // История для графика (последние 20 замеров)
    private final Queue<Integer> occupancyHistory = new LinkedList<>();

    public SignalProcessingService() {
        char[] rows = {'A', 'B', 'C', 'D', 'E', 'F'};
        for (char row : rows) {
            for (int i = 1; i <= 6; i++) {
                String id = row + "-" + String.format("%02d", i);
                buffer.put(id, new ParkingSpot(id));
            }
        }
    }

    public synchronized SensorData processSignal(String spotId, double rawDistance) {
        ParkingSpot spot = buffer.get(spotId);
        if (spot == null) return null;

        double ema = (rawDistance * ALPHA) + (spot.getLastFilteredDistance() * (1 - ALPHA));
        spot.update(ema);

        updateChartHistory();
        return new SensorData(spotId, rawDistance, ema, spot.getStatus());
    }

    // Метод для эмуляции смены статусов A-03 (СВОБОДНО -> ЗАБРОНИРОВАНО -> ЗАНЯТО)
    public void simulateA03Cycle() {
        ParkingSpot spot = buffer.get("A-03");
        if (spot != null) {
            if ("СВОБОДНО".equals(spot.getStatus())) {
                spot.setStatus("ЗАБРОНИРОВАНО");
            } else if ("ЗАБРОНИРОВАНО".equals(spot.getStatus())) {
                spot.setStatus("ЗАНЯТО");
            } else {
                spot.setStatus("СВОБОДНО");
                spot.update(150.0); // Сброс дистанции
            }
        }
    }

    private void updateChartHistory() {
        long occupiedCount = buffer.values().stream().filter(s -> "ЗАНЯТО".equals(s.getStatus())).count();
        occupancyHistory.add((int) occupiedCount);
        if (occupancyHistory.size() > 20) {
            occupancyHistory.poll();
        }
    }

    public Map<String, ParkingSpot> getBuffer() { return buffer; }
    public Queue<Integer> getOccupancyHistory() { return occupancyHistory; }
}