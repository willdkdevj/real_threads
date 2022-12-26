## Conceito aplicado em projeto sobre o uso de Thread via comunicação TCP/IP
> Será visto o conceito de concorrência de threads via comunicação via rede (java.util.concurrent - Java 5)

[![Design Badge](https://img.shields.io/badge/-GitHub-blue?style=flat-square&logo=GitHub&logoColor=white&link=https://refactoring.guru/design-patterns)](https://refactoring.guru/design-patterns)


<img align="right" width="400" height="250" src="https://github.com/willdkdevj/real_threads/blob/master/assets/demo.png">

## Descrição da Aplicação
A ideia principal é que um cliente se conecte com o servidor, enviando um comando, e baseado nesse comando o servidor execute uma tarefa. Esta tarifa será executada através de comunicação remota, utilizando o **protocolo HTTP**. 
Utilizaremos aqui um protocolo que realiza os processos em segundo plano, ou seja, por baixo do HTTP, o **protocolo TCP**. Ele será responsável *por enviarmos e recebermos dados*.

## Sobre o protocolo TCP
**O TCP** somente garante que os dados serão transferidos na rede de maneira confiável. mas ele não define como será este vocabulário. O TCP é apenas um **protocolo de transporte**, diferente do protocolo HTTP, que é um protocolo de aplicação, que tem esse vocabulário.
Assim como existe o IP para identificar uma máquina, a porta é a solução para identificar diversos clientes em uma máquina. Esta porta é um número de 2 bytes, varia de 0 a 65535. Se todas as portas de uma máquina estiverem ocupadas, não é possível se conectar a ela enquanto nenhuma for liberada. Então, além do IP, também é preciso saber a porta!

### Criando o Próprio Vocabulário 
Será utilizado as classes do **Socket**, que em segundo plano utiliza o  protocolo TCP e abstrae grande parte da complexidade da conexão. Além disso, do lado do servidor será criado um *Server Socket*, desta forma, este Server Socket será executado na máquina local, na porta liberada pelo servidor (porta de entrada), onde ao iniciar a conexão é criada novo trafego, ao liberar uma porta especifica para acontecer este diálogo.
Um socket é um ponto final de uma comunicação, desta maneira, teremos um socket no lado do cliente e um outro socket no lado do servidor. Os dois irão se comunicar usando o TCP.

```java
public class ServidorDeTarefas {

    public static void main(String[] args) {
        try {
            System.out.println("---------Iniciando Conexão----------");
            ServerSocket serverSocket = new ServerSocket(12345);

            while (true) {
                Socket accept = serverSocket.accept();
                System.out.println("Aceitando novo cliente na porta " + accept.getPort());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao iniciar o Socket: " + e.getMessage());
        }
    }
}
```

Já o cliente será utilizado o *Socket* para passar o IP da máquina e a porta, para estabelecermos conexão.
```java
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
```

Utilizaremos uma classe auxiliadora para estas tarefas que ajudará na distribuição das tarefas encaminhadas pelos clientes, desta forma, será iniciada uma ***Thread*** para cada cliente no lado do servidor.
```java
public class DistribuirTarefas implements Runnable {

    private Socket socket;

    public DistribuirTarefas(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Distribuindo as tarefas para o cliente " +  socket);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

```
Agora é possível estabelecer conexões através de vários clientes, pois existirá um laço infinito que aceitará uma nova conexão socket.
O interessante é que a porta muda para cada cliente. Enquanto usamos a porta 12345 para criar a conexão inicial, toda comunicação a partir desse momento é feita com uma porta dedicada pra cada cliente.

```xml
---- Iniciando Servidor ----
Aceitando novo cliente na porta 61430
Aceitando novo cliente na porta 61432
```

<img align="middle" width="400" height="200" src="https://github.com/willdkdevj/real_threads/blob/master/assets/modelo1.png">

A partir desse **ServerSocket** é possível aceitar conexões, através do método accept. O método *accept()* permite devolver o ponto final da comunicação, referente ao que foi informado anteriormente, que o socket é um ponto final de comunicação, desta forma, o ServerSocket retorna um socket ativo.
> No lado do servidor devemos utilizar para cada cliente uma thread, pois o método accept() da classe ServerSocket é bloqueante



## Agradecimentos
Obrigado por ter acompanhado aos meus esforços em aplicar os conceitos do Design Patterns ao Projeto :octocat:

Como diria um velho mestre:
> *"Cedo ou tarde, você vai aprender, assim como eu aprendi, que existe uma diferença entre CONHECER o caminho e TRILHAR o caminho."*
>
> *Morpheus - The Matrix*
> 