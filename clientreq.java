import java.time.Instant;

public class clientreq{

    private final long id;
    private final int fromX;
    private final int fromY;
    private final int toX;
    private final int toY;
    private final Instant createdAt;
    public clientreq(long id, int fromX, int fromY, int toX, int toY, Instant createdAt){
        this.id = id;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.createdAt = createdAt;
    }

    public long getId(){
        return id;
    }
    public int getFromX(){
        return fromX;
    }

    public int getFromY(){
        return fromY;
    }

    public int getToX(){
        return toX;
    }
    public int getToY(){
        return toY;
    }

    public Instant getCreatedAt(){
        return createdAt;
    }
    @Override
    public String toString(){
        return "Request{" +
                "id=" + id +
                ", from=(" + fromX + "," + fromY + ")" +
                ", to=(" + toX + "," + toY + ")" +
                ", createdAt=" + createdAt +
                '}';
    }
}
