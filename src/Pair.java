public class Pair implements Comparable<Pair> {
    double first;
    int second;

    public Pair(double first, int second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int compareTo(Pair o) {
        return Double.compare(first, o.first);
    }
}
