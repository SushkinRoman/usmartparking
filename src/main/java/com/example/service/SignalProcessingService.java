package com.example.service;

import com.example.model.ParkingSpot;
import com.example.model.SensorData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SignalProcessingService {
    // Буфер текущих состояний всех мест парковки
    private final Map<String, ParkingSpot> buffer = new ConcurrentHashMap<>();

    // Коэффициент сглаживания EMA (alpha = 2 / (N + 1), пусть N=5)
    private final double ALPHA = 0.333;

    public SignalProcessingService() {
        // Инициализируем буфер для сетки 6x6 (36 мест)
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

        // Формула EMA из методички: EMA = (P * alpha) + (EMA_prev * (1 - alpha))
        double ema = (rawDistance * ALPHA) + (spot.getLastFilteredDistance() * (1 - ALPHA));

        // Обновляем состояние места
        spot.update(ema);

        return new SensorData(spotId, rawDistance, ema, spot.getStatus());
    }

    public Map<String, ParkingSpot> getBuffer() {
        return buffer;
    }

    // Метод для будущего модуля бронирования
    public void reserveSpot(String spotId) {
        ParkingSpot spot = buffer.get(spotId);
        if (spot != null && "FREE".equals(spot.getStatus())) {
            spot.setStatus("RESERVED");
        }
    }
}