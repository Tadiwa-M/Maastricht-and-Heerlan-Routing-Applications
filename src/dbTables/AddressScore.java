package dbTables;

public class AddressScore {
    private PostAddress address;
    private double score;
    private double amenityScore;
    private double shopScore;
    private double tourismScore;

    public AddressScore(PostAddress address, double score, double amenityScore, double shopScore, double tourismScore) {
        this.address = address;
        this.score = score;
        this.amenityScore = amenityScore;
        this.shopScore = shopScore;
        this.tourismScore = tourismScore;
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

    public void setAmenityScore(double amenityScore) {
        this.amenityScore = amenityScore;
    }

    public void setShopScore(double shopScore) {
        this.shopScore = shopScore;
    }

    public void setTourismScore(double tourismScore) {
        this.tourismScore = tourismScore;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "AddressScore{" +
                "address=" + address.getPostalCode() +
                ", score=" + score +
                ", amenityScore=" + amenityScore +
                ", shopScore=" + shopScore +
                ", tourismScore=" + tourismScore +
                '}';
    }
}
