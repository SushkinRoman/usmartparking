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
            server.start(8080);
            System.out.println("Сервер запущен. Откройте в браузере: http://localhost:8080/login.html");

            // 1. Запуск эмулятора датчика A-01
            Thread emulatorThread = new Thread(new UltrasonicSensorEmulator());
            emulatorThread.setDaemon(true);
            emulatorThread.start();

            // 2. Запуск эмулятора циклического изменения статуса A-03 (СВОБОДНО -> ЗАБРОНИРОВАНО -> ЗАНЯТО)
            Thread a03Simulator = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(5000); // Каждые 5 секунд
                        service.simulateA03Cycle();
                        System.out.println("[ЭМУЛЯТОР] Изменен статус места A-03");
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            a03Simulator.setDaemon(true);
            a03Simulator.start();

        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
        }
    }
}