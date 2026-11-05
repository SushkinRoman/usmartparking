package com.example;

import com.example.emulator.ParkingSensorEmulator;
import com.example.service.SignalProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private SignalProcessingService service;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Запуск системы 'Умная парковка' (Лабораторная работа №2)");
        System.out.println("Сервер запущен. Откройте в браузере: http://localhost:8080/login.html");

        // 1. Запуск эмулятора ультразвукового датчика (HC-SR04) для места A-01
        Thread emulatorThread = new Thread(new ParkingSensorEmulator(service));
        emulatorThread.setDaemon(true);
        emulatorThread.start();

        // 2. Запуск эмулятора циклического изменения статуса для места A-03
        Thread a03Thread = new Thread(() -> {
            int step = 0;
            while (true) {
                try {
                    Thread.sleep(5000); // 5 секунд между изменениями
                    if (step == 0) {
                        service.setManualStatus("A-03", "ЗАБРОНИРОВАНО");
                        step = 1;
                    } else if (step == 1) {
                        service.setManualStatus("A-03", "ЗАНЯТО");
                        step = 2;
                    } else {
                        service.setManualStatus("A-03", "СВОБОДНО");
                        step = 0;
                    }
                } catch (InterruptedException e) {
                    System.out.println("[ЭМУЛЯТОР A-03] Поток остановлен");
                    break;
                }
            }
        });
        a03Thread.setDaemon(true);
        a03Thread.start();
    }
}