package br.com.supernova.util;

import java.io.PrintStream;
import java.util.concurrent.*;

public class TreatmentThreadsCallable implements Callable<Void> {

    private Future<String> futureWS;
    private Future<String> futureBD;
    private PrintStream saida;

    public TreatmentThreadsCallable(Future<String> futureWS,
                                    Future<String> futureBanco, PrintStream saidaCliente) {
        this.futureWS = futureWS;
        this.futureBD = futureBanco;
        this.saida = saidaCliente;
    }

    /* Não queremos devolver nada, então usamos um tipo que representa nada: Void */
    public Void call() {

        System.out.println("Aguardando resultados do future WS e BD");

        try {
            /* As Threads devolvem números aleatório a fim de simular o retorno de serviços externos */
            String numeroMagico = this.futureWS.get(20, TimeUnit.SECONDS);
            String numeroMagico2 = this.futureBD.get(20, TimeUnit.SECONDS);

            this.saida.println("Resultado do comando c2: " + numeroMagico + ", " + numeroMagico2);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {

            System.out.println("Timeout: Cancelando a execução do comando c2");

            /* Caso ocorra exceção é reportado e é forçado o encerramento das threads em execução (cancel(true)) */
            this.saida.println("Timeout na execução do comando c2");
            this.futureWS.cancel(true);
            this.futureBD.cancel(true);
        }

        System.out.println("Finalizou TratamentoThreadsCallable");

        return null; //esse Callable não tem retorno, por isso null
    }
}
