package br.com.supernova.util;

import br.com.supernova.commands.Command1Simple;
import br.com.supernova.commands.CommandC2BD;
import br.com.supernova.commands.CommandC2WS;
import br.com.supernova.servidor.ServidorDeTarefas;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DistribuirTarefas implements Runnable {

    private Socket socket;
    private ServidorDeTarefas servidorDeTarefas;
    private ExecutorService threadPool;
    private BlockingQueue<String> queueCommands;

    public DistribuirTarefas(ServidorDeTarefas servidor, BlockingQueue<String> queue, Socket socket, ExecutorService threadPool) {
        this.socket = socket;
        this.servidorDeTarefas = servidor;
        this.threadPool = threadPool;
        this.queueCommands = queue;
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
                    Command1Simple simple = new Command1Simple(saidaCliente);
                    this.threadPool.submit(simple);
                    break;
                case "c2":
                    saidaCliente.println("Confirmação do comando c2");

                    /* Criando os dois comandos para serem processados pelo servidor (Webservice, Banco de Dados) */
                    CommandC2WS ws = new CommandC2WS(saidaCliente);
                    CommandC2BD bd = new CommandC2BD(saidaCliente);

                    /* Encaminhando os comandos para a Pool e obtendo o retorno futuro */
                    Future<String> futureWS = this.threadPool.submit(ws);
                    Future<String> futureBD = this.threadPool.submit(bd);

                    TreatmentThreadsCallable treatmentThreadsCallable = new TreatmentThreadsCallable(futureWS, futureBD, saidaCliente);
                    this.threadPool.submit(treatmentThreadsCallable);

                    break;
                case "c3":
                    /* Processo será bloqueado caso a lista fique cheia */
                    try {
                        this.queueCommands.put(comando);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    saidaCliente.println("Comando C3 adicionado na fila");
                    break;
                case "fim":
                    saidaCliente.println("Encerrando o servidor!");
                    try {
                        servidorDeTarefas.parar();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                default:
                    saidaCliente.println("Comando não encontrado");
            }

            System.out.println(comando);
        }

        saidaCliente.close();
        entradaCliente.close();
    }

}
