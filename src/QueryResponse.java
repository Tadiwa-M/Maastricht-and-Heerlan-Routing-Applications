import java.util.ArrayList;

public class QueryResponse {
    private ArrayList<PostAddress> path = new ArrayList<>();
    private final double distance;
    private final long time;

    public QueryResponse(ArrayList<PostAddress> path, double distance, long time) {
        this.path = path;
        this.distance = distance;
        this.time = time;
    }

    public double getDistance() {
        return distance;
    }

    public long getTime() {
        return time;
    }

    public ArrayList<PostAddress> getPath() {
        return path;
    }
}
