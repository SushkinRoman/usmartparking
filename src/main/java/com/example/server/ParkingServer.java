package com.example.server;

import com.example.model.ParkingSpot;
import com.example.model.SensorData;
import com.example.service.SignalProcessingService;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ParkingServer {
    private final SignalProcessingService service;
    private final Gson gson = new Gson();

    // Вспомогательный класс для корректного парсинга входящего JSON
    public static class SensorRequest {
        public String spot;
        public double distance;
    }

    public ParkingServer(SignalProcessingService service) {
        this.service = service;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/sensor", new SensorHandler());
        server.createContext("/api/spots", new SpotsHandler());
        server.createContext("/", new WebPageHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("[СЕРВЕР] Запущен на порту " + port);
    }

    class SensorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                    //  НАДЕЖНЫЙ ПАРСИНГ: Порядок ключей в JSON больше не важен
                    SensorRequest request = gson.fromJson(body, SensorRequest.class);

                    if (request.spot == null) {
                        throw new IllegalArgumentException("Отсутствует поле 'spot'");
                    }

                    SensorData result = service.processSignal(request.spot, request.distance);
                    String response = gson.toJson(result);

                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    exchange.getResponseBody().write(responseBytes);
                    exchange.getResponseBody().close();
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            } catch (Exception e) {
                //  ОБРАБОТКА ОШИБОК: Сервер не упадет молча, а вернет понятную ошибку
                System.err.println("[СЕРВЕР] Ошибка обработки запроса: " + e.getMessage());
                String errorResponse = "{\"error\": \"Неверный формат данных\"}";
                byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, errorBytes.length);
                exchange.getResponseBody().write(errorBytes);
                exchange.getResponseBody().close();
            }
        }
    }

    class SpotsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, ParkingSpot> spots = service.getBuffer();
            String response = gson.toJson(spots.values());

            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.getResponseBody().close();
        }
    }

    class WebPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream is = getClass().getClassLoader().getResourceAsStream("index.html");
            if (is == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, htmlBytes.length);
            exchange.getResponseBody().write(htmlBytes);
            exchange.getResponseBody().close();
        }
    }
}