package com.example.service;

import com.example.model.ParkingSnapshot;
import com.example.model.SensorData;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SignalProcessingService {

    // Буфер текущего состояния всех мест
    private final Map<String, SensorData> buffer = new HashMap<>();

    // История для графика загруженности
    private final List<ParkingSnapshot> history = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 25; // Храним последние 25 снимков

    private final double ALPHA = 2.0 / (1 + 5); // N=5 для EMA

    @PostConstruct
    public void initGrid() {
        char[] rows = {'A', 'B', 'C', 'D', 'E', 'F'};
        for (char row : rows) {
            for (int col = 1; col <= 6; col++) {
                String id = row + "-" + String.format("%02d", col);
                buffer.put(id, new SensorData(id, 50.0, 50.0, "СВОБОДНО"));
            }
        }
        recordHistory(); // Записываем начальное состояние
    }

    public synchronized SensorData processSignal(String spaceId, double rawValue) {
        SensorData prev = buffer.getOrDefault(spaceId, new SensorData(spaceId, 50.0, 50.0, "СВОБОДНО"));

        // Фильтрация EMA
        double ema = (rawValue * ALPHA) + (prev.getFilteredValue() * (1 - ALPHA));

        // Логика определения статуса
        String newStatus = "СВОБОДНО";
        if ("ЗАБРОНИРОВАНО".equals(prev.getStatus())) {
            newStatus = "ЗАБРОНИРОВАНО";
        } else if (ema < 40.0) {
            newStatus = "ЗАНЯТО";
        }

        SensorData newData = new SensorData(spaceId, rawValue, ema, newStatus);
        buffer.put(spaceId, newData);

        // Обновляем историю для графика при каждом изменении данных
        recordHistory();

        return newData;
    }

    // Метод для ручного изменения статуса (например, для эмулятора A-03)
    public synchronized void setManualStatus(String spaceId, String status) {
        SensorData data = buffer.get(spaceId);
        if (data != null) {
            data.setStatus(status);
            buffer.put(spaceId, data);
            recordHistory(); // Также обновляем график
        }
    }

    private void recordHistory() {
        int free = 0, occupied = 0, reserved = 0;
        for (SensorData data : buffer.values()) {
            if ("СВОБОДНО".equals(data.getStatus())) free++;
            else if ("ЗАНЯТО".equals(data.getStatus())) occupied++;
            else if ("ЗАБРОНИРОВАНО".equals(data.getStatus())) reserved++;
        }

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        history.add(new ParkingSnapshot(time, free, occupied, reserved));

        // Ограничиваем размер истории, чтобы график был читаемым
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    public Map<String, SensorData> getBuffer() { return buffer; }
    public List<ParkingSnapshot> getHistory() { return history; }
}