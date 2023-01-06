package br.com.supernova.util;

import br.com.supernova.exceptions.TreatmentThreadException;

import java.util.concurrent.ThreadFactory;

public class CustomFactoryThread implements ThreadFactory {
    private static Integer number = 1;
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "Server Thread-" + number);
        number++;

        thread.setUncaughtExceptionHandler(new TreatmentThreadException());

        return thread;
    }
}
