package br.com.supernova.servidor;

import br.com.supernova.util.DistribuirTarefas;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorDeTarefas {

    public static void main(String[] args) {
        try {
            System.out.println("---------Iniciando Conexão----------");
            ServerSocket serverSocket = new ServerSocket(12345);

            while (true) {
                Socket accept = serverSocket.accept();
                System.out.println("Aceitando novo cliente na porta " + accept.getPort());
                DistribuirTarefas distribuirTarefas = new DistribuirTarefas(accept);
                new Thread(distribuirTarefas).start();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao iniciar o Socket: " + e.getMessage());
        }
    }
}
