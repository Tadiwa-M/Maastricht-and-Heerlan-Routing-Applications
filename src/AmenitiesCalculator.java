import dbTables.Amenity;
import dbTables.PostAddress;
import dbTables.Shop;
import dbTables.Tourism;

import static dbTables.AmenityManager.*;
import static dbTables.dbManager.fetchAllAddresses;

import java.util.List;

public class AmenitiesCalculator {
    final static double RADIUS = 3;
    final static double essentialShopWeight = .3;
    final static double essentialAmenityWeight = .4;
    final static double nonEssentialShopWeight = .1;
    final static double nonEssentialAmenityWeight = .1;
    final static double TourismWeight = .1;

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        calculateAllScores();
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        System.out.println("Execution time: " + duration / 1_000_000 + " ms");
    }

    public static double shopScores(PostAddress postAddress) {
        List<Shop> shops = fetchShopsByCoords();
        return shopScores(postAddress, shops);
    }

    private static double shopScores(PostAddress postAddress, List<Shop> shops) {
        double score = 0;
        double weightlessScore = 0;

        for (Shop shop : shops) {
            double gaussScore = gaussianScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(shop.getLat(), shop.getLon())));
            weightlessScore += gaussScore;
            if (shop.getType().equals("mall") || shop.getType().equals("supermarket")) {
                score += gaussScore * essentialShopWeight;
            } else {
                score += gaussScore * nonEssentialShopWeight;
            }
        }
        score = score / weightlessScore;
        return score;
    }

    static double amenityScores(PostAddress postAddress) {
        List<Amenity> amenities = fetchAmenitiesByCords();
        return amenityScores(postAddress, amenities);
    }

    private static double amenityScores(PostAddress postAddress, List<Amenity> amenities) {
        double score = 0;
        double weightlessScore = 0;

        for (Amenity amenity : amenities) {
            double gaussScore = gaussianScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(amenity.getLat(), amenity.getLon())));
            weightlessScore += gaussScore;
            if (amenity.getType().equals("bank") || amenity.getType().equals("college") || amenity.getType().equals("doctors") || amenity.getType().equals("hospital")
                    || amenity.getType().equals("pharmacy") || amenity.getType().equals("school") || amenity.getType().equals("university")) {
                score += gaussScore * essentialAmenityWeight;
            } else {
                score += gaussScore * nonEssentialAmenityWeight;
            }
        }
        score = score / weightlessScore;
        return score;
    }

    public static double tourismScores(PostAddress postAddress) {
        List<Tourism> landmarks = fetchAttractionsByCoords();
        return tourismScores(postAddress, landmarks);
    }

    private static double tourismScores(PostAddress postAddress, List<Tourism> landmarks) {
        double score = 0;
        double weightlessScore = 0;
        for (Tourism landmark : landmarks) {
            double gaussScore = gaussianScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(landmark.getLat(), landmark.getLon())));
            weightlessScore += gaussScore;
            score += gaussScore * TourismWeight;
        }
        score = score / weightlessScore;
        return score;
    }

    public static double gaussianScore(double distance) {
        double mu = 0; // Mean distance (zero for maximum score at zero distance)
        return Math.exp(-Math.pow(distance - mu, 2) / (2 * Math.pow(RADIUS / 4, 2)));
    }

    public static void calculateAllScores() {
        long startTime = System.nanoTime();

        List<PostAddress> allAddresses = fetchAllAddresses();
        List<Shop> shops = fetchShopsByCoords();
        List<Amenity> amenities = fetchAmenitiesByCords();
        List<Tourism> landmarks = fetchAttractionsByCoords();

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println("Fetch time: " + duration / 1_000_000 + " ms");

        for(PostAddress address : allAddresses) {
            double shopScore = shopScores(address, shops);
            double amenityScore = amenityScores(address, amenities);
            double tourismScore = tourismScores(address, landmarks);

            double totalScore = shopScore + amenityScore + tourismScore;
            System.out.println("Total score for " + address.getPostalCode() + ": " + totalScore);
        }
        System.out.println("All scores calculated.");

    }
}
