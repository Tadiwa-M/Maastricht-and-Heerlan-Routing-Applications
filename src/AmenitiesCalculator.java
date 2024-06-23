import dbTables.*;

import static dbTables.AmenityManager.*;
import static dbTables.dbManager.fetchAllAddresses;

import java.util.ArrayList;
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
        List<Tourism> attractions = fetchAttractionsByCoords();
        return tourismScores(postAddress, attractions);
    }

    private static double tourismScores(PostAddress postAddress, List<Tourism> attractions) {
        double score = 0;
        double weightlessScore = 0;
        for (Tourism attraction : attractions) {
            double gaussScore = gaussianScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(attraction.getLat(), attraction.getLon())));
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
        if (allAddresses == null || allAddresses.isEmpty()) {
            System.out.println("No addresses found");
            return;
        }

        List<Shop> shops = fetchShopsByCoords();
        if (shops == null || shops.isEmpty()) {
            System.out.println("No shops found");
            return;
        }

        List<Amenity> amenities = fetchAmenitiesByCords();
        if (amenities == null || amenities.isEmpty()) {
            System.out.println("No amenities found");
            return;
        }

        List<Tourism> attractions = fetchAttractionsByCoords();
        if (attractions == null || attractions.isEmpty()) {
            System.out.println("No attractions found");
            return;
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println("Fetch time: " + duration / 1_000_000 + " ms");

        List<AddressScore> scores = new ArrayList<>();

        for (PostAddress address : allAddresses) {
            double shopScore = shopScores(address, shops);
            double amenityScore = amenityScores(address, amenities);
            double tourismScore = tourismScores(address, attractions);

            double totalScore = shopScore + amenityScore + tourismScore;

            scores.add(new AddressScore(address, totalScore, amenityScore, shopScore, tourismScore));
        }

        normalizeScores(scores);
    }

    private static void normalizeScores(List<AddressScore> scores) {
        // Check if the list is empty
        if (scores.isEmpty()) {
            System.out.println("No scores to normalize");
            return;
        }

        double maxScore = scores.stream().mapToDouble(AddressScore::getScore).max().orElse(0);
        double minScore = scores.stream().mapToDouble(AddressScore::getScore).min().orElse(0);

        // Prevent potential issues if maxScore is equal to minScore
        if (maxScore == minScore) {
            for (AddressScore score : scores) {
                score.setScore(0);  // Assign a normalized score of 0 if all scores are the same
            }
        } else {
            for (AddressScore score : scores) {
                // Normalize the score using an exponential function
                double normalizedScore = 100 * (Math.exp(score.getScore() - minScore) - 1) / (Math.exp(maxScore - minScore) - 1);
                score.setScore(normalizedScore);
            }
        }

        for (AddressScore score : scores) {
            System.out.println(score);
        }
    }
}
