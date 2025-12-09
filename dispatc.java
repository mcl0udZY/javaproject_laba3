import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class dispatc implements Runnable{

    private static final long REQUEST_POLL_TIMEOUT_MS = 500L;
    private static final long NO_TAXI_SLEEP_MS = 200L;

    private final BlockingQueue<clientreq> requestQueue;
    private final List<taxi> taxis;
    private final ReentrantLock taxiLock = new ReentrantLock();

    private volatile boolean running = true;

    public dispatc(BlockingQueue<clientreq> requestQueue, List<taxi> taxis){
        this.requestQueue = requestQueue;
        this.taxis = taxis;
    }

    public void shutdown(){
        running = false;
        Thread.currentThread().interrupt();
    }

    public void notifyTaxiFree(taxi taxi){
        System.out.printf("Dispatcher: taxi %d is now FREE%n", taxi.getId());
    }
    @Override
    public void run(){
        System.out.println("Dispatcher started");
        try {
            while (running){
                clientreq request = requestQueue.poll(REQUEST_POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (request == null){
                    continue;
                }

                taxi bestTaxi = null;
                int bestDistance = Integer.MAX_VALUE;
                taxiLock.lock();
                try{
                    for (taxi taxi : taxis){
                        if (!taxi.isFree()){
                            continue;
                        }
                        int distance = distanceTaxiToRequest(taxi, request);
                        if (distance < bestDistance){
                            bestDistance = distance;
                            bestTaxi = taxi;
                        }
                    }
                } finally{
                    taxiLock.unlock();
                }
                if (bestTaxi != null){
                    System.out.printf(
                            "Dispatcher: assign request %d to taxi %d (distance=%d)%n",
                            request.getId(),
                            bestTaxi.getId(),
                            bestDistance
                    );
                    bestTaxi.assignOrder(request);
                } else{
                    System.out.printf(
                            "Dispatcher: no free taxi for request %d, requeue%n",
                            request.getId()
                    );
                    requestQueue.put(request);
                    Thread.sleep(NO_TAXI_SLEEP_MS);
                }
            }
        } catch (InterruptedException e){
            // нормальное завершение
        }
        System.out.println("Dispatcher stopped");
    }

    private int distanceTaxiToRequest(taxi taxi, clientreq request) {
        int tx = taxi.getX();
        int ty = taxi.getY();
        int fx = request.getFromX();
        int fy = request.getFromY();
        return Math.abs(tx - fx) + Math.abs(ty - fy);
    }
}
