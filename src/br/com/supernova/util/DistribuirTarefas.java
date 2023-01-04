package br.com.supernova.util;

import br.com.supernova.servidor.ServidorDeTarefas;

import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class DistribuirTarefas implements Runnable {

    private Socket socket;
    private ServidorDeTarefas servidorDeTarefas;
    private ExecutorService threadPool;

    public DistribuirTarefas(ServidorDeTarefas servidor, Socket socket, ExecutorService threadPool) {
        this.socket = socket;
        this.servidorDeTarefas = servidor;
        this.threadPool = threadPool;
    }

    @Override
    public void run() {

        try {

            System.out.println("Distribuindo as tarefas para o cliente " + socket);

            Scanner entradaCliente = new Scanner(socket.getInputStream());

            PrintStream saidaCliente = new PrintStream(socket.getOutputStream());

            processarRequisicaoCliente(entradaCliente, saidaCliente);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void processarRequisicaoCliente(Scanner entradaCliente, PrintStream saidaCliente) {
        while (entradaCliente.hasNextLine()) {
            String comando = entradaCliente.nextLine();
            System.out.println("----- Comando Recebido: " + comando + "-----");

            switch (comando) {
                case "c1":
                    // confirmação do o cliente
                    saidaCliente.println("Confirmação do comando c1");
                    break;
                case "c2":
                    saidaCliente.println("Confirmação do comando c2");
                    break;
                default:
                    saidaCliente.println("Comando não encontrado");
            }

            System.out.println(comando);
        }

        saidaCliente.close();
        entradaCliente.close();
    }

}
