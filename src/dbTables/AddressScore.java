package dbTables;

public class AddressScore {
    private final PostAddress address;
    private double score;
    private final double amenityScore;
    private final double shopScore;
    private final double tourismScore;
    private final double accessibilityScore;

    public AddressScore(PostAddress address, double score, double amenityScore, double shopScore, double tourismScore, double accessibilityScore) {
        this.address = address;
        this.score = score;
        this.amenityScore = amenityScore;
        this.shopScore = shopScore;
        this.tourismScore = tourismScore;
        this.accessibilityScore = accessibilityScore;
    }

    public PostAddress getAddress() {
        return address;
    }

    public double getScore() {
        return score;
    }

    public double getAmenityScore() {
        return amenityScore;
    }

    public double getShopScore() {
        return shopScore;
    }

    public double getTourismScore() {
        return tourismScore;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getAccessibilityScore() {
        return this.accessibilityScore;
    }

    @Override
    public String toString() {
        return "AddressScore{" +
                "address=" + address.getPostalCode() +
                ", score=" + score +
                ", amenityScore=" + amenityScore +
                ", shopScore=" + shopScore +
                ", tourismScore=" + tourismScore +
                ", accessibilityScore=" + accessibilityScore +
                '}';
    }
}
