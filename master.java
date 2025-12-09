import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class master{

    private static final int TAXI_COUNT = 3;
    private static final long SIMULATION_TIME_MS = 20_000L;

    public static void main(String[] args) throws InterruptedException{
        BlockingQueue<clientreq> requestQueue = new LinkedBlockingQueue<>();

        List<taxi> taxis = new ArrayList<>();
        dispatc dispatcher = new dispatc(requestQueue, taxis);

        for (int i = 0; i < TAXI_COUNT;i++){
            // стартовые координаты просто разные для наглядности
            taxi taxi = new taxi(i + 1,i * 2, i * 2, dispatcher);
            taxis.add(taxi);
        }

        Thread dispatcherThread = new Thread(dispatcher, "dispatcher");
        dispatcherThread.start();

        List<Thread> taxiThreads = new ArrayList<>();
        for (taxi taxi : taxis){
            Thread t = new Thread(taxi,"taxi-" + taxi.getId());
            taxiThreads.add(t);
            t.start();
        }

        clientgenerator generator = new clientgenerator(requestQueue);
        Thread generatorThread = new Thread(generator, "client-generator");
        generatorThread.start();

        // даём системе поработать
        Thread.sleep(SIMULATION_TIME_MS);

        System.out.println("=== Stopping simulation ===");
        generator.shutdown();
        dispatcher.shutdown();
        generatorThread.interrupt();
        dispatcherThread.interrupt();
        for (taxi taxi : taxis){
            taxi.shutdown();
        }
        for (Thread t : taxiThreads){
            t.interrupt();
        }

        generatorThread.join();
        dispatcherThread.join();
        for (Thread t : taxiThreads){
            t.join();
        }

        System.out.println("=== Simulation finished ===");
    }
}
