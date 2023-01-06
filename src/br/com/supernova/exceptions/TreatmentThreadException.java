package br.com.supernova.exceptions;

public class TreatmentThreadException implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("Exceção na thread " + t.getName() + ": " + e.getMessage());
    }
}
