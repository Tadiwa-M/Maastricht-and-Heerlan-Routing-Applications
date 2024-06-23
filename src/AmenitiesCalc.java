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
        PostAddress postAddress = AddressFinder.getAddress("6221AA");
        assert postAddress != null;
//        calculateAllScores(postAddress);
    }

    static double shopScores(PostAddress postAddress){
        List<Shop> shops = fetchShopsByCoords();
        double score = 0;
        double weightlessScore = 0;
        for(Shop shop : shops){
            double gaussScore = gaussianScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(shop.getLat(), shop.getLon())));
            weightlessScore += gaussScore;
            if(shop.getType().equals("mall")||shop.getType().equals("supermarket")){
                score += gaussScore*essentialShopWeight;
            } else{
                score += gaussScore*nonEssentialShopWeight;
            }
        }
        score = score / weightlessScore;
        return score;
    }

    static double amenityScores(PostAddress postAddress){
        List<Amenity> amenities = fetchAmenitiesByCords();
        double score = 0;
        double weightlessScore = 0;
        for(Amenity amenity : amenities){
            double gaussScore = gaussianScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(amenity.getLat(), amenity.getLon())));
            weightlessScore += gaussScore;
            if(amenity.getType().equals("bank")||amenity.getType().equals("college")||amenity.getType().equals("doctors")||amenity.getType().equals("hospital")
                    ||amenity.getType().equals("pharmacy")||amenity.getType().equals("school")||amenity.getType().equals("university")){
                score += gaussScore*essentialAmenityWeight;
            } else{
                score += gaussScore*nonEssentialAmenityWeight;
            }
        }
        score = score / weightlessScore;
        return score;
    }

    static double tourismScores(PostAddress postAddress){
        List<Tourism> landmarks = fetchAttractionsByCoords();
        double score = 0;
        double weightlessScore = 0;
        for(Tourism landmark : landmarks){
            double gaussScore = gaussianScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(landmark.getLat(), landmark.getLon())));
            weightlessScore += gaussScore;
            score += gaussScore*TourismWeight;
        }
        score = score / weightlessScore;
        return score;
    }

    public static double gaussianScore(double distance) {
        double mu = 0; // Mean distance (zero for maximum score at zero distance)
        return Math.exp(-Math.pow(distance - mu, 2) / (2 * Math.pow(RADIUS / 4, 2)));
    }

    public static void calculateScore(PostAddress postAddress){
        double score = fetchAddressScore(postAddress.getPostalCode());
        if (score == -1){
            score = tourismScores(postAddress) + amenityScores(postAddress) + shopScores(postAddress);
            System.out.println(score);
            insertAddressScore(postAddress.getPostalCode(), score);
        }
    }
    
    public static void calculateAllScores() {
        List<PostAddress> allAddresses = fetchAllAddresses();
        if (allAddresses != null && !allAddresses.isEmpty()) {
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            allAddresses.parallelStream().forEach(address -> {
                executorService.submit(() -> calculateScore(address));
            });
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                // Wait for all tasks to finish
            }
            System.out.println("All scores calculated.");
        }
    }
}
