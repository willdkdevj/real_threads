package br.com.supernova.cliente;

import javax.imageio.IIOException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TarefaDeCliente {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            System.out.println("-------- Realizada Conexão com Servidor --------");

            Scanner teclado = new Scanner(System.in);
            teclado.nextLine();
            socket.close();
        } catch (IOException e){
            throw new RuntimeException("Erro ao iniciar Socket (Client): " + e.getMessage());
        }

    }
}
