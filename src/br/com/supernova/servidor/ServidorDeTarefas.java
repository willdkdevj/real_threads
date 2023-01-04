package br.com.supernova.servidor;

import br.com.supernova.util.CustomFactoryThread;
import br.com.supernova.util.DistribuirTarefas;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServidorDeTarefas {

    private ServerSocket servidor;
    private ExecutorService threadPool;
    private AtomicBoolean isRodando;

    public ServidorDeTarefas() throws IOException {
        System.out.println("---Iniciando servidor---");
        this.servidor = new ServerSocket(12345);
        this.threadPool = Executors.newCachedThreadPool(new CustomFactoryThread());
        this.isRodando = new AtomicBoolean(Boolean.TRUE);

    }

    public void rodar() throws IOException {
        while (this.isRodando.get()){
            Socket socket = servidor.accept();
            System.out.println("Aceitando novo cliente na porta " +socket.getPort());

            DistribuirTarefas distribuirTarefas = new DistribuirTarefas(this, socket, threadPool);
            threadPool.execute(distribuirTarefas);
        }
    }

    public void parar() throws IOException {
        this.isRodando.set(Boolean.FALSE);
        servidor.close();
        threadPool.shutdown();
    }

    public static void main(String[] args) throws IOException {
        ServidorDeTarefas servidorDeTarefas = new ServidorDeTarefas();
        servidorDeTarefas.rodar();
        servidorDeTarefas.parar();
    }
}
