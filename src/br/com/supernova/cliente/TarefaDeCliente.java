package br.com.supernova.cliente;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class TarefaDeCliente {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            System.out.println("-------- Realizada Conexão com Servidor --------");

            Thread threadEnviar = enviarComando(socket);
            Thread threadResposta = receberResposta(socket);

            threadEnviar.start();
            threadResposta.start();

            threadResposta.join();

            System.out.println("Fechando o socket do cliente");

            socket.close();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Erro ao processar retorno e desconectar Socket (Client): " + e.getMessage());
        }

    }

    private static Thread enviarComando(Socket socket) {
        return new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    System.out.println("--------- Será encaminhado comando --------");

                    PrintStream saida = new PrintStream(socket.getOutputStream());

                    Scanner teclado = new Scanner(System.in);
                    while ((teclado.hasNextLine())) {
                        String linha = teclado.nextLine();

                        // Forçar a parada da de input no teclasso
                        if (linha.trim().equals("")) break;

                        saida.println(linha);
                    }

                    saida.close();
                    teclado.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static Thread receberResposta(Socket socket) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("-------- Recebendo resposta do Servidor ---------");
                    Scanner respostaServidor = new Scanner(socket.getInputStream());

                    while (respostaServidor.hasNextLine()) {
                        String linha = respostaServidor.nextLine();
                        System.out.println(linha);
                    }
                    // Fechar conexão com o servidor
                    respostaServidor.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }
}
