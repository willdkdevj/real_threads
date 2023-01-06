package br.com.supernova.commands;

import java.io.PrintStream;
import java.util.Random;
import java.util.concurrent.Callable;

public class CommandC2WS implements Callable<String> {
    private PrintStream saida;

    public CommandC2WS(PrintStream saida) {
        this.saida = saida;
    }

    @Override
    public String call() throws Exception {
        System.out.println("Servidor recebeu comando C2 - WS");
        saida.println("Processando comando C2 - WS");

        /* Simulando o tempo de processamento de um comando */
        Thread.sleep(15000);

        /* Simulando o processamento de um comando utilizando um número aleatório */
        Integer number = new Random().nextInt(100) + 1;

        System.out.println("Servidor finalizou comando C2 -WS");
        return Integer.toString(number);
    }
}
