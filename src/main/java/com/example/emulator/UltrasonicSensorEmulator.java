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
        String spotId = "A-01"; // Эмулируем датчик на месте A-01
        boolean isCarPresent = false;

        System.out.println("[ЭМУЛЯТОР] Запуск эмуляции ультразвукового датчика HC-SR04 для места " + spotId);

        while (true) {
            try {
                // Имитация изменения ситуации: машина заезжает или уезжает
                if (random.nextDouble() < 0.1) { // 10% шанс изменения состояния
                    isCarPresent = !isCarPresent;
                }

                // Генерация сырого сигнала (в см)
                // Если машина есть: ~30 см + шум. Если нет: ~150 см + шум.
                double baseDistance = isCarPresent ? 30.0 : 150.0;
                double noise = random.nextGaussian() * 5.0; // Гауссовский шум датчика
                double rawDistance = Math.max(2.0, baseDistance + noise); // Мин. расстояние 2 см

                // Создаем JSON через Map
                Map<String, Object> payload = new HashMap<>();
                payload.put("spot", spotId);
                payload.put("distance", rawDistance);

                String jsonPayload = gson.toJson(payload);


                // Отправка на сервер
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(serverUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                client.send(request, HttpResponse.BodyHandlers.discarding());

                System.out.printf("[ЭМУЛЯТОР] Место: %s, Сырой сигнал: %.2f см, Машина: %b%n",
                        spotId, rawDistance, isCarPresent);

                // Дискретизация: опрос каждые 2 секунды
                Thread.sleep(2000);

            } catch (Exception e) {
                System.err.println("[ЭМУЛЯТОР] Ошибка: " + e.getMessage());
            }
        }
    }
}