package com.example.server;

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
import java.util.concurrent.ConcurrentHashMap;

public class ParkingServer {
    private final SignalProcessingService service;
    private final Gson gson = new Gson();
    private final Map<String, String> users = new ConcurrentHashMap<>();

    public ParkingServer(SignalProcessingService service) {
        this.service = service;
        users.put("test", "123");
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API маршруты
        server.createContext("/api/sensor", new SensorHandler());
        server.createContext("/api/spots", new SpotsHandler());
        server.createContext("/api/chart", new ChartHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());

        // Маршрутизация HTML страниц
        server.createContext("/", new WebPageHandler("map.html"));          // Главная теперь карта
        server.createContext("/map.html", new WebPageHandler("map.html"));   // Явный маршрут карты
        server.createContext("/stats.html", new WebPageHandler("stats.html")); // НОВЫЙ маршрут статистики
        server.createContext("/login.html", new WebPageHandler("login.html"));
        server.createContext("/register.html", new WebPageHandler("register.html"));

        server.setExecutor(null);
        server.start();
        System.out.println("[СЕРВЕР] Запущен на порту " + port);
    }

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = gson.fromJson(body, Map.class);

                if (users.containsKey(data.get("username")) && users.get(data.get("username")).equals(data.get("password"))) {
                    sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Вход выполнен\"}");
                } else {
                    sendResponse(exchange, 401, "{\"success\": false, \"message\": \"Неверный логин или пароль\"}");
                }
            }
        }
    }

    class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = gson.fromJson(body, Map.class);

                if (users.containsKey(data.get("username"))) {
                    sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Пользователь уже существует\"}");
                } else {
                    users.put(data.get("username"), data.get("password"));
                    sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Регистрация успешна\"}");
                }
            }
        }
    }

    class ChartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendResponse(exchange, 200, gson.toJson(service.getOccupancyHistory()));
        }
    }

    class SensorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, Object> request = gson.fromJson(body, Map.class);

                String spot = request.get("spot").toString();
                double distance = ((Number) request.get("distance")).doubleValue();

                SensorData result = service.processSignal(spot, distance);
                sendResponse(exchange, 200, gson.toJson(result));
            }
        }
    }

    class SpotsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = gson.toJson(service.getBuffer().values());
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.getResponseBody().close();
        }
    }

    class WebPageHandler implements HttpHandler {
        private final String fileName;
        public WebPageHandler(String fileName) { this.fileName = fileName; }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
            if (is == null) { exchange.sendResponseHeaders(404, -1); return; }
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String contentType = fileName.endsWith(".html") ? "text/html; charset=UTF-8" : "text/plain";
            exchange.getResponseHeaders().set("Content-Type", contentType);
            byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, htmlBytes.length);
            exchange.getResponseBody().write(htmlBytes);
            exchange.getResponseBody().close();
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }
}