# Conceito aplicado em projeto sobre o uso de Thread via comunicação TCP/IP
> Será visto o conceito de concorrência de threads via comunicação via rede (java.util.concurrent - Java 5)

[![Java Badge](https://img.shields.io/badge/-Java-blue?style=flat-square&logo=GitHub&logoColor=white&link=https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html)](https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html)


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

## Reuso de Threads (Pool)
O objetivo agora é que o cliente envie comandos ao servidor, onde baseado nestes comandos são realizadas tarefas. Desta forma, na classe **DistribuirTarefas** que é uma classe auxiliar de **ServidorDeTarefas**, será utilizado a classe *InputStream* a fim de envidar um comando digitado no terminal, instanciando a classe *Scanner* a fim de conseguir interagir com este comando inserido.
```java
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
```

Já no lado do cliente, realizaremos a caminho inverso, ao invés de enviar iremos receber a devolutiva do servidor sobre o status do comando enviado. Para isso, utilizaremos a classe *OutputStream* a fim de obter esta devolutiva e instanciar a classe *PrintStream* para permitir manusear o conteúdo para apresentar no console.
```java
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
```

Cada *Thread* é mapeada para uma **Thread Nativa** do Sistema Operacional, o que gera um custo na sua criação, desta forma, é necessário ficar atento na quantidade de threads que nossas aplicações disponibilizam para permitir uma melhor administração dos recursos. 

A biblioteca do Java *Concurrent* nos permite utilizar de um recurso nomeado como ***Pool*** para gerenciar e administrar estas instâncias de threads, assim como, limitá-las. Além disso, quando não temos ideia de quão escalável está o processo de criação de threads para atender a demanda podemos utilizar o método [newCachedThreadPool()] da classe estática ***Executors*** que automaticamente é atribuida esta função a JVM administrar a quantidade de instâncias de threads criadas.
> Obs: Também podemos inserir um número fixo de threads a serem criadas utilizando outro método de Executors, seu nome é newFixedThreadPool(Integer number), onde como argumento passamos o valor de threads limite da piscina (pool).

Utilizando um pool, mesmo sendo de modo estático, podemos definir uma quantidade para atender a distribuição de tarefas. Ao analisarmos que não estamos conseguindo atender ao número de requisições, ou mesmo que o número de requisições tem alta variança, podemos utilizar um cache e deixar por conta da JVM.
> Obs: Além de utilizarmos um cachê ou informar um valor limite para o pool, também podemos criar uma instância única para a piscina ao utilizar o método newSingleThreadExecutor() que cria uma única thread.

Este gerenciador de instâncias ficará no lado do servidor, logicamente, pois ele que será responsável por tratar as requisições dos clientes e realizar as tratativas para responde-los.
```java
    public ServidorDeTarefas() throws IOException {
        System.out.println("---- Iniciando servidor ----");
        this.servidor = new ServerSocket(12345);
        this.threadPool = Executors.newCachedThreadPool();
    }
```

A instância da classe *Executors* ao invocar os métodos para criação da Pool, geram um objeto de ***ExecutorService**, na qual não é mais necessário criarmos instância de Threads para passarmos nossos Runnables para sua execução, mas sim, utilizar o método [execute()] para disponibilizar uma tarefa para uma thread de um pool's de threads.

ExecutorService é uma interface e a classe Executors devolve uma implementação do pool, através dos métodos mencionados em cima. Também podemos dizer que a classe Executors é uma fábrica de pools!

### Concorrência de Threads
Como mencionado, teremos em nossa aplicação a comunicação entre cliente/servidor, onde a partir de uma ação haverá uma reação correspondente a fim de informar seu estado. Mas para fazer este processo o cliente também terá que possuir threads internas para envio e recebimento, pois a ação de enviar bloqueia a ação de receber, desta maneira, deverá haver processos distintos para a realização dos mesmos.

<img align="middle" width="400" height="250" src="https://github.com/willdkdevj/real_threads/blob/master/assets/concorrencia.png">

Desta forma, foram criados os métodos **enviarComando** e **receberResposta** que são classes anônimas da ***interface Runnable***. Mas ainda temos que informar a thread principal (a thread do Maain()) que deverá aguardar a finalização do processo das threads de envio e recebimento, antes de seu encerramento, ao invocar o método close(), na qual utilizamos o método [join()]  que permite a thread aguarde até a outra thread conclua o seu processo.

O real problema é o seguinte, estamos inicializando cada thread corretamente, mas o **Socket** é fechado utilizando o método close() na thread principal (main). No momento que é iniciado o processo de envio e recebimento dos dados pelas threads internas (auxiliares), neste processo, é capaz que a thread [main] já tenha encerrado o Socket! 

Já a solução e a seguinte, quando a thread main executa o método join da thread que está realizando um procedimento, ela sabe que precisa aguardar o retorno da execução da thread. Portanto, a thread main ficará esperando até a outra thread acabe.

### Compartilhamento de Variável
Quando Threads necessitam de compartilhar de um valor presente em uma variável é necessário utilizar um recurso do pacote [java.util.concurrent]. Este problema existe devido ao cachê que a Thread cria para alocar variáveis locais, aproveitando do cachê da CPU, sendo que, a alteração do estado da variável está sendo aplicada a memória principal.

Desta forma, o recurso presente do pacote *Concurrent* é o atributo **volatile**, na qual define que a thread não deve criar um cachê para a variável que está manuseando, mas sim, utilizar a variável presente na memória principal. Desta forma, todas as threads que forem instanciadas e que em suas classes existam este atributos, não importa quantas sejam, todas elas irão buscar da memória principal.

<img align="middle" width="400" height="250" src="https://github.com/willdkdevj/real_threads/blob/master/assets/thread_memoria.png">

Outro possibilidade é utilizar as classes ***Atomic*** como tipo para variável, que também faz parte do pacote *Concurrent*, nela estão dispostos métodos para inserir e buscar os valores correspondentes ao tipo da classe, onde possuem a mesma funcionalidade da volatile de compartilhar o conteúdo da variável em memória com todas as threads que a declarem.
```java
public class ServidorDeTarefas {

    private ServerSocket servidor;
    private ExecutorService threadPool;
    private AtomicBoolean isRodando;

    public ServidorDeTarefas() throws IOException {
        System.out.println("---Iniciando servidor---");
        this.servidor = new ServerSocket(12345);
        this.threadPool = Executors.newCachedThreadPool();
        this.isRodando = new AtomicBoolean(Boolean.TRUE);
    }

    public void rodar() throws IOException {
        while (this.isRodando.get()){
            Socket socket = servidor.accept();
            System.out.println("Aceitando novo cliente na porta " +socket.getPort());

            DistribuirTarefas distribuirTarefas = new DistribuirTarefas(socket, this);
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
```

## Tratamento de Exceções
É muito importante estarmos cientes que nossas aplicações podem apresentar comportamentos que não eram esperados, estes comportamentos tem no nome de *exceção*. Mas além de nossas aplicações a própria Thread pode lançar uma exceção, onde caso ocorra o lançamento por lá e nós não realizamos o tratamento no método que sobreescrevemos, no caso, o método [run()] de **Runnable**, esta exceção será lançada para o usuário sem o seu tratamento adequado de legibilidade.

Desta forma, devemos utilizar a estrutura de *try/catch* a fim de realizar a tratativa de um código que pode lançar uma exceção para o usuário. Mas realizar esta tratativa em todo método que sobreescrevermos não é a única alternativa.

Uma das alternativas é utilizar o método [setUncaughtExceptionHandler()] da classe ***Thread*** para receber um objeto que realizará a tratativa de exceção, na qual implementa a interface UncaughtExceptionHandler, para que seja possível personalizar uma exceção, toda vez que for encontrada, tratada para o usuário.
```java
    private static Thread receberResposta(Socket socket) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("-------- Recebendo resposta do Servidor ---------");
                Scanner respostaServidor = new Scanner(socket.getInputStream());

                while (respostaServidor.hasNextLine()) {
                    String linha = respostaServidor.nextLine();
                    System.out.println(linha);
                }
                // Fechar conexão com o servidor
                respostaServidor.close();
            }
        });

        thread.setUncaughtExceptionHandler(new treatmentThreadException());

        return thread;
    }
```

Feito isso, é necessário desenvolver esta classe que implementará a interface **UncaughtExceptionHandler**, na qual teremos que sobreescrever o método [uncaughtExcetion(Thread t, Throwable e)] a fim de personalizar nossa tratativa de devolutiva.
```java
    public class treatmentThreadException implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("Exceção na thread " + t.getName() + ": " + e.getMessage());
    }
}
```

Agora, e se tivermos utilizando o recurso de *Pool* para que a JVM gerencie as threads que forem criadas? Neste caso, precisamos ter acesso a esta "fabrica de threads" na qual a JVM irá administrar para que nós consigamos personaliza-la e passar os atributos necessários para que as threads que sejam criadas tenham os parâmetros necessários para realizar esta tratativa. Abaixo segue como foi realizado esta tratativa para permitir acessar a *fábrica* e realizar as tratativas.
```java
    public ServidorDeTarefas() throws IOException {
        System.out.println("---Iniciando servidor---");
        this.servidor = new ServerSocket(12345);
        this.threadPool = Executors.newCachedThreadPool(new CustomFactoryThread());
        this.isRodando = new AtomicBoolean(Boolean.TRUE);

    }
```

Para isso, foi passado como parãmetro do método newCach a classe personalizada que desenvolvemos nomeada como *CustomFactoryThread*, na qual implementa a interface **ThreadFactory**, sendo sobreescrito o método [newThread()] a fim de permitirmos criar e parametrizar a mesma com os valores necessários.
```java
    public class CustomFactoryThread implements ThreadFactory {
        private static Integer number = 1;
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Server Thread-" + number);
            number++;
            
            thread.setUncaughtExceptionHandler(new treatmentThreadException());
            
            return thread;
        }
    }
```

Neste caso, só utilizamos o construtor da Thread para passar o *Runnable* que será passado a ela e inserirmos um nome para as threads que serão geradas no lado do servidor. Além disso, com a thread instanciada, invocamos o método setUncaugetExceptionHandler(), para passarmos nossa *Exception* personalizada.

## Retornos no Futuro (Callable)
Até agora só vimos Threads não devolvendo qualquer tipo de valor, pois elas são *Runnable* e seu retorno é nulo (*void*). Além de retornar algum tipo de valor, as threads deveriam ter um tempo de trabalho, isso quer dizer que elas não ficariam em execução até a conclusão de seus trabalhos, elas teriam um tempo determinado para conclusão, caso contrário, a JVM derrubaria sua execução. Ou seja, como estamos chamando um *web service*, algum tipo de problema pode ocorrer, como trafego de rede, comunicação via TCP, desvio de rotas, entre outros podem aparecer.

A alternativa possível para obtermos retorno de threads que executam tarefas é a utilização da interface ***Callable***, também do pacote do *Concurrent*.
```java
public class ComandoC2WS implements Callable<String> {

    @Override

    public String call() throws Exception {
    }
}
```

Notem que através do *Generics* ele já nos obriga a especificar o tipo de retorno a ser devolvido pela thread. Além disso, não utilizamos mais o método **run()** como em Runnable, agora estamos utilizando o método **call()** que deve retornar o mesmo tipo especificado na implementação da classe.

Também para executar uma nova thread em nosso *Pool* não utilizaremos mais o comando **execute** passando a instância da classe que implementa *Callable*, agora utilizaremos o comando **submit** continuando passar esta a instância de classe.
```java
/* O nosso pool de threads */
Executor executor = Executors.newCachedThreadPool(); 
/* *Usando submit que recebe um Callable */
Future<String> future = executor.submit(new ComandoC2BD(saida));
```

Mas agora tem algo de diferente, pois o comando *execute* não nos devolvia nada, pois seu retorno é nulo (*void*), mas o comando **submit** nos devolve um tipo como valor, e seu nome é ***Future***.

A ideia do *Future* é que já exista um retorno para a thread desde que invocado o método **get()**. mas este retorno só será possível acessa-lo quando estiver pronto. O nome *Future* soa estranho, mas faz todo sentido se pensarmos que a tarefa passada para o *pool* será executada em algum momento no futuro. No entanto, o método **submit()** não bloqueia a execução e podemos submeter quantas tarefas quisermos.
```java

```

Então, o que será feito? Será criada uma nova thread que receberá o retorno estas duas threads<Callable>, mas com um diferencial. Ela não retornará nada! Mas será responsável pelo retorno das suas e seu tempo de execução.
<img align="middle" width="400" height="250" src="https://github.com/willdkdevj/real_threads/blob/master/assets/commandc2.png">

O método **get()** desta classe *Future* bloqueia a thread e aguarda o resultado de seu processo. Portanto, é por isso que será criada uma nova thread (TratamentoThreadsCallable) a fim de obter o retorno destas outras threads, mas tembém fornecer um tempo limitado para sua execução. Isso é útil quando não podemos garantir que a execução realmente termine com sucesso.
```java
public class TratamentoThreadsCallable implements Callable<Void> {

    private Future<String> futureWS;
    private Future<String> futureBD;
    private PrintStream saida;

    public TratamentoThreadsCallable(Future<String> futureWS,
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

            this.saidaCliente.println("Resultado do comando c2: " + numeroMagico + ", " + numeroMagico2);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {

            System.out.println("Timeout: Cancelando a execução do comando c2");

            /* Caso ocorra exceção é reportado e é forçado o encerramento das threads em execução (cancel(true)) */
            this.saidaCliente.println("Timeout na execução do comando c2");
            this.futureWS.cancel(true);
            this.futureBD.cancel(true);
        }

        System.out.println("Finalizou TratamentoThreadsCallable");

        return null; //esse Callable não tem retorno, por isso null
    }
}
```


> Obs: Nesse caso, não seria necessário utilizar o ***Callable***, pois a tarefa vai unir os resultados e devolver na saída do cliente nenhum retorno. Desta forma, poderíamos utilizar a interface ***Runnable***, mas como existe um *Generics* do tipo ***Void*** que tem a característica de devolver um valor nulo, foi utilizado para continuar com a prática da interface Callable.


<img align="middle" width="400" height="340" src="https://github.com/willdkdevj/real_threads/blob/master/assets/structure.png">

Ufa! E este é o fim do projeto de Thread para simular um ambiente de Client/Servidor.

## Agradecimentos
Obrigado por ter acompanhado aos meus esforços ao aplicar o conceito de threads na comunicação entre cliente/servidor. :octocat:

Como diria um velho mestre:
> *"Cedo ou tarde, você vai aprender, assim como eu aprendi, que existe uma diferença entre CONHECER o caminho e TRILHAR o caminho."*
>
> *Morpheus - The Matrix*
> 