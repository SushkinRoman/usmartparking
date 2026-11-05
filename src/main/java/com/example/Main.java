package com.example;

import com.example.emulator.UltrasonicSensorEmulator;
import com.example.server.ParkingServer;
import com.example.service.SignalProcessingService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Запуск системы 'Умная парковка' (Лабораторная работа №2)");

        SignalProcessingService service = new SignalProcessingService();
        ParkingServer server = new ParkingServer(service);

        try {
            // Запуск HTTP сервера
            server.start(8080);

            // Запуск эмулятора датчика в отдельном потоке
            Thread emulatorThread = new Thread(new UltrasonicSensorEmulator());
            emulatorThread.setDaemon(true);
            emulatorThread.start();

            System.out.println("Система готова. Откройте в браузере: http://localhost:8080");

        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
        }
    }
}