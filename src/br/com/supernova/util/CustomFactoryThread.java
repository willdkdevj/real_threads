package br.com.supernova.util;

import br.com.supernova.exceptions.treatmentThreadException;

import java.util.concurrent.ThreadFactory;

public class CustomFactoryThread implements ThreadFactory {
    private static Integer number = 1;
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "Server Thread-" + number);
        number++;

        thread.setUncaughtExceptionHandler(new treatmentThreadException());

        return thread;
    }
}
