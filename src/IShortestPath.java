import dbTables.PostAddress;

import java.util.ArrayList;

public interface IShortestPath {
    void runAlgorithm(int src);
    ArrayList<PostAddress> findPath(int dest);
    double getDistance(int indexEnd);
}
