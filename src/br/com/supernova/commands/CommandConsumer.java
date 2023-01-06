package br.com.supernova.commands;

import java.util.concurrent.BlockingQueue;

public class CommandConsumer implements Runnable{
    private BlockingQueue<String> queue;

    public CommandConsumer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            String comando = null;
            while ((comando= queue.take()) != null) {
                System.out.println("Consumindo comando " + comando + ", " + Thread.currentThread().getName());
                Thread.sleep(20000);
            }
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }

    }
}
