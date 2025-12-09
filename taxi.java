import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class taxi implements Runnable{

    private static final long BASE_TO_CLIENT_DELAY_MS = 200L;
    private static final long DISTANCE_TO_CLIENT_COEFF_MS = 20L;
    private static final long BASE_RIDE_DELAY_MS =300L;
    private static final long DISTANCE_RIDE_COEFF_MS = 30L;

    private final int id;
    private final dispatc dispatcher;

    private final BlockingQueue<clientreq> personalQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean busy = new AtomicBoolean(false);

    private volatile boolean running = true;

    // координаты такси
    private int x;
    private int y;

    public taxi(int id, int startX, int startY, dispatc dispatcher){
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.dispatcher = dispatcher;
    }

    public int getId(){
        return id;
    }

    public synchronized int getX() {
        return x;
    }

    public synchronized int getY(){
        return y;
    }

    private synchronized void setPosition(int newX,int newY){
        this.x = newX;
        this.y= newY;
    }

    public boolean isFree(){
        return !busy.get() && running;
    }

    //Диспетчер назначает заказ этому такси.
    public void assignOrder(clientreq request) throws InterruptedException{
        busy.set(true);
        personalQueue.put(request);
    }
    public void shutdown(){
        running = false;
        Thread.currentThread().interrupt();
    }
    @Override
    public void run(){
        System.out.printf("Taxi %d started at (%d,%d)%n", id, x, y);
        try {
            while (running) {
                clientreq request = personalQueue.take();
                processRequest(request);
            }
        } catch (InterruptedException e){
            // нормальное завершение потока
        }
        System.out.printf("Taxi %d stopped%n", id);
    }

    private void processRequest(clientreq request) throws InterruptedException{
        System.out.printf("Taxi %d: got %s%n", id, request);

        // 1. Едем к клиенту
        int distanceToClient = distanceTo(request.getFromX(), request.getFromY());
        System.out.printf("Taxi %d: driving to client, distance=%d%n", id, distanceToClient);
        Thread.sleep(BASE_TO_CLIENT_DELAY_MS + distanceToClient * DISTANCE_TO_CLIENT_COEFF_MS);
        setPosition(request.getFromX(), request.getFromY());

        // 2. Везём клиента
        int rideDistance = distanceBetween(
                request.getFromX(), request.getFromY(),
                request.getToX(), request.getToY()
        );
        System.out.printf("Taxi %d: driving client, distance=%d%n", id, rideDistance);
        Thread.sleep(BASE_RIDE_DELAY_MS + rideDistance * DISTANCE_RIDE_COEFF_MS);
        setPosition(request.getToX(), request.getToY());
        System.out.printf(
                "Taxi %d: finished request %d, now at (%d,%d)%n",
                id,
                request.getId(),
                getX(),
                getY()
        );

        busy.set(false);
        dispatcher.notifyTaxiFree(this);
    }

    private int distanceTo(int targetX, int targetY) {
        return distanceBetween(getX(), getY(), targetX, targetY);
    }

    private int distanceBetween(int x1, int y1, int x2, int y2) {
        // манхэттенская метрика
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}
