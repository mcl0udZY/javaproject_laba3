import java.time.Instant;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class clientgenerator implements Runnable {

    private static final int MIN_SLEEP_MS = 300;
    private static final int MAX_EXTRA_SLEEP_MS = 700;
    private static final int MAX_COORD =10;

    private final BlockingQueue<clientreq> requestQueue;
    private final Random random = new Random();

    private volatile boolean running = true;
    private long nextId = 1L;

    public clientgenerator(BlockingQueue<clientreq> requestQueue){
        this.requestQueue = requestQueue;
    }

    public void shutdown(){
        running = false;
        Thread.currentThread().interrupt();
    }

    @Override
    public void run(){
        System.out.println("ClientGenerator started");
        try {
            while (running){
                clientreq request = generateRandomRequest();
                requestQueue.put(request);
                System.out.println("ClientGenerator: created " + request);
                int sleepTime = MIN_SLEEP_MS + random.nextInt(MAX_EXTRA_SLEEP_MS);
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e){
            // нормальное завершение
        }
        System.out.println("ClientGenerator stopped");
    }

    private clientreq generateRandomRequest(){
        int fromX = random.nextInt(MAX_COORD);
        int fromY = random.nextInt(MAX_COORD);
        int toX = random.nextInt(MAX_COORD);
        int toY = random.nextInt(MAX_COORD);
        long id = nextId++;
        Instant now = Instant.now();
        return new clientreq(id, fromX, fromY, toX, toY, now);
    }
}
