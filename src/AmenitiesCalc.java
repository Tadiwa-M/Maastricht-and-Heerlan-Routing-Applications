import dbTables.Amenity;
import dbTables.PostAddress;
import dbTables.Shop;
import dbTables.Tourism;

import static dbTables.AmenityManager.*;
import static dbTables.dbManager.fetchAllAddresses;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AmenitiesCalc {
    final static double RADIUS = 3;
    final static double essentialShopWeight = .3;
    final static double essentialAmenityWeight = .4;
    final static double nonEssentialShopWeight = .1;
    final static double nonEssentialAmenityWeight = .1;
    final static double TourismWeight = .1;

    public static void main(String[] args) {
        //PostAddress postAddress = AddressFinder.getAddress("6221AA");
        //assert postAddress != null;
        //calculateScore(postAddress);

        long startTime = System.nanoTime();
        calculateAllScores();
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        System.out.println("Execution time: " + duration / 1_000_000 + " ms");
    }

    static double shopScores(PostAddress postAddress) {
        List<Shop> shops = fetchShopsByCoords();
        return shopScores(postAddress, shops);
    }

    static double shopScores(PostAddress postAddress, List<Shop> shops) {
        double score = 0;
        double weightlessScore = 0;
        for (Shop shop : shops) {
            if(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(shop.getLat(), shop.getLon()))>RADIUS)continue;
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

    static double amenityScores(PostAddress postAddress, List<Amenity> amenities) {
        double score = 0;
        double weightlessScore = 0;
        for (Amenity amenity : amenities) {
            if(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(amenity.getLat(), amenity.getLon()))>RADIUS)continue;
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

    static double tourismScores(PostAddress postAddress) {
        List<Tourism> landmarks = fetchAttractionsByCoords();
        return tourismScores(postAddress, landmarks);
    }

    static double tourismScores(PostAddress postAddress, List<Tourism> landmarks) {
        double score = 0;
        double weightlessScore = 0;
        for (Tourism landmark : landmarks) {
            if(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(landmark.getLat(), landmark.getLon()))>RADIUS)continue;
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

    public static void calculateScore(PostAddress postAddress) {
        double score = fetchAddressScore(postAddress.getPostalCode());
        if (score == -1) {
            score = tourismScores(postAddress) + amenityScores(postAddress) + shopScores(postAddress);
//            System.out.println(score);
            insertAddressScore(postAddress.getPostalCode(), score);
        }
    }

    public static void calculateScore(PostAddress postAddress, List<Shop> shops, List<Amenity> amenities, List<Tourism> landmarks) {
        double score = fetchAddressScore(postAddress.getPostalCode());
        if (score == -1) {
            ExecutorService executor = Executors.newCachedThreadPool();

            CompletableFuture<Double> shopScoreFuture = CompletableFuture.supplyAsync(() -> shopScores(postAddress, shops), executor);
            CompletableFuture<Double> amenityScoreFuture = CompletableFuture.supplyAsync(() -> amenityScores(postAddress, amenities), executor);
            CompletableFuture<Double> tourismScoreFuture = CompletableFuture.supplyAsync(() -> tourismScores(postAddress, landmarks), executor);

            CompletableFuture<Double> totalScoreFuture = shopScoreFuture
                    .thenCombine(amenityScoreFuture, Double::sum)
                    .thenCombine(tourismScoreFuture, Double::sum);

            totalScoreFuture.thenAccept(totalScore -> {
//                System.out.println(totalScore);
//                insertAddressScore(postAddress.getPostalCode(), totalScore);
            }).join();

            executor.shutdown();
        }
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

        for(PostAddress address : allAddresses){
            calculateScore(address, shops, amenities, landmarks);
        };
        System.out.println("All scores calculated.");

    }
}
