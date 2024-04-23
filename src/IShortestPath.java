import java.util.ArrayList;

public interface IShortestPath {
    public void runAlgorithm(int src);
    public ArrayList<PostAddress> findPath(int dest);
}
