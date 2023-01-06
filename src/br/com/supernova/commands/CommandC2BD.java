package br.com.supernova.commands;

import java.io.PrintStream;
import java.util.Random;
import java.util.concurrent.Callable;

public class CommandC2BD implements Callable<String> {
    private PrintStream saida;

    public CommandC2BD(PrintStream saida) {
        this.saida = saida;
    }

    @Override
    public String call() throws Exception {
        System.out.println("Servidor recebeu comando C2 - BD");
        saida.println("Processando comando C2 - BD");

        /* Simulando tempo de processamento do comando */
        Thread.sleep(150000);

        /* Simulando o valor retornado pelo processamento pelo Servidor */
        Integer number =  new Random().nextInt(100) + 1;
        System.out.println("Servidor finalizou comando C2 - BD");
        return Integer.toString(number);
    }
}
