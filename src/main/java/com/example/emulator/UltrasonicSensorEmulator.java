package com.example.emulator;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UltrasonicSensorEmulator implements Runnable {
    private final String serverUrl = "http://localhost:8080/api/sensor";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final Random random = new Random();

    @Override
    public void run() {
        // Поток 1: Эмуляция датчика A-01 (расстояние)
        Thread threadA01 = new Thread(() -> {
            String spotId = "A-01";
            boolean isCarPresent = false;
            while (true) {
                try {
                    if (random.nextDouble() < 0.1) isCarPresent = !isCarPresent;
                    double baseDistance = isCarPresent ? 30.0 : 150.0;
                    double rawDistance = Math.max(2.0, baseDistance + random.nextGaussian() * 5.0);
                    sendRequest(spotId, rawDistance);
                    Thread.sleep(2000);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        // Поток 2: Эмуляция логики A-03 (СВОБОДНО -> ЗАБРОНИРОВАНО -> ЗАНЯТО)
        Thread threadA03 = new Thread(() -> {
            // Мы отправляем фиктивные данные, а сервер сам меняет статус через simulateA03Cycle
            // Но чтобы триггернуть сервер, будем слать запросы, а сервер их перехватит или мы вызовем метод напрямую.
            // Для простоты в рамках эмулятора, мы просто шлем данные, а в Main добавим вызов simulateA03Cycle
            while(true) {
                try { Thread.sleep(5000); } catch (Exception e) {}
            }
        });

        threadA01.start();
    }

    private void sendRequest(String spotId, double rawDistance) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("spot", spotId);
            payload.put("distance", rawDistance);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
            System.out.printf("[ЭМУЛЯТОР] Место: %s, Дистанция: %.2f см%n", spotId, rawDistance);
        } catch (Exception e) {
            System.err.println("[ЭМУЛЯТОР] Ошибка: " + e.getMessage());
        }
    }
}