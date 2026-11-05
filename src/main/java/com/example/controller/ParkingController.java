package com.example.controller;

import com.example.model.ParkingSnapshot;
import com.example.model.SensorData;
import com.example.service.SignalProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin(origins = "*")
public class ParkingController {

    @Autowired
    private SignalProcessingService service;

    // 1. Прием данных от эмулятора датчика
    @PostMapping("/sensor")
    public SensorData receiveSensorData(@RequestBody Map<String, Object> payload) {
        String spaceId = (String) payload.get("spaceId");
        double rawValue = ((Number) payload.get("rawValue")).doubleValue();
        return service.processSignal(spaceId, rawValue);
    }

    // 2. Получить текущее состояние всех мест (для отрисовки сетки 6x6)
    @GetMapping("/map")
    public Map<String, SensorData> getParkingMap() {
        return service.getBuffer();
    }

    // 3. НОВЫЙ ЭНДПОИНТ: Получить историю изменений загруженности для графика
    @GetMapping("/history")
    public List<ParkingSnapshot> getParkingHistory() {
        return service.getHistory();
    }
}