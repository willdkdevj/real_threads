package br.com.supernova.servidor;

import br.com.supernova.commands.CommandConsumer;
import br.com.supernova.util.CustomFactoryThread;
import br.com.supernova.util.DistribuirTarefas;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServidorDeTarefas {

    private ServerSocket servidor;
    private ExecutorService threadPool;
    private BlockingQueue<String> queueCommands;
    private AtomicBoolean isRodando;

    public ServidorDeTarefas() throws IOException {
        System.out.println("---Iniciando servidor---");
        this.servidor = new ServerSocket(12345);
        this.threadPool = Executors.newCachedThreadPool(new CustomFactoryThread());
        this.isRodando = new AtomicBoolean(Boolean.TRUE);
        this.queueCommands = new ArrayBlockingQueue<>(2); // Capacidade de processo que podem ficar na fila
        iniciarConsumidores();
    }

    /* Iniciado o processo para os consumidores */
    private void iniciarConsumidores() {
        int qtdCondumidores = 2;
        for (int i = 0; i < qtdCondumidores; i++) {
            CommandConsumer tarefa = new CommandConsumer(queueCommands);
            this.threadPool.execute(tarefa);
        }
    }

    public void rodar() throws IOException {
        while (this.isRodando.get()) {
            Socket socket = servidor.accept();
            System.out.println("Aceitando novo cliente na porta " + socket.getPort());

            DistribuirTarefas distribuirTarefas = new DistribuirTarefas(this, queueCommands, socket, threadPool);
            threadPool.execute(distribuirTarefas);
        }
    }

    public void parar() throws IOException {
        this.isRodando.set(Boolean.FALSE);
        servidor.close();
        threadPool.shutdown();
        System.out.println("Parando servidor");
    }

    public static void main(String[] args) throws IOException {
        ServidorDeTarefas servidorDeTarefas = new ServidorDeTarefas();
        servidorDeTarefas.rodar();
    }
}
