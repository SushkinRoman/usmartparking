package com.example.emulator;

import com.example.service.SignalProcessingService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

public class ParkingSensorEmulator implements Runnable {
    private final SignalProcessingService service;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Random random = new Random();

    // Вариант 1 из методички
    private final double[] A = {1.0, 0.5, 0.25};
    private final double[] F = {0.1, 0.1, 0.4};

    public ParkingSensorEmulator(SignalProcessingService service) {
        this.service = service;
    }

    @Override
    public void run() {
        System.out.println("[ЭМУЛЯТОР HC-SR04] Запущен для места A-01");
        while (true) {
            try {
                Thread.sleep(3000); // Дискретизация каждые 3 секунды
                double t = System.currentTimeMillis() / 1000.0;

                // Аддитивная модель гармоник + шум (имитация HC-SR04)
                double signal = 0;
                for (int i = 0; i < A.length; i++) {
                    signal += A[i] * Math.sin(2 * Math.PI * F[i] * t);
                }
                double noise = random.nextGaussian() * 0.5;
                double rawDistance = 50.0 + (signal * 10.0) + noise; // Базовое расстояние 50 см

                // Отправка на сервер
                String json = String.format(
                        "{\"spaceId\":\"A-01\", \"rawValue\":%.2f}", rawDistance
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/parking/sensor"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                client.send(request, HttpResponse.BodyHandlers.discarding());

            } catch (Exception e) {
                System.err.println("[ЭМУЛЯТОР] Ошибка: " + e.getMessage());
            }
        }
    }
}