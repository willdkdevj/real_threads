package br.com.supernova.commands;

import java.io.PrintStream;

public class Command1Simple implements Runnable {
    private PrintStream saida;

    public Command1Simple(PrintStream saida) {
        this.saida = saida;
    }

    @Override
    public void run() {
        System.out.println("Executando comando C1");

        try{
            /* Simulando algo demorado */
            Thread.sleep(20000);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }

        System.out.println("Comando C1 executado com sucesso!");
    }
}
