import com.azure.core.annotation.Post;
import dbTables.Amenity;
import dbTables.PostAddress;
import dbTables.Shop;
import dbTables.Tourism;

import static dbTables.Amenity.fetchAmenitiesByCords;
import static dbTables.Shop.fetchShopsByCoords;
import static dbTables.Tourism.fetchAttractionsByCoords;
import static dbTables.dbManager.*;

import java.util.List;

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
        calculateScore(postAddress);
    }

    static double shopScores(PostAddress postAddress){
        List<Shop> shops = fetchShopsByCoords(postAddress.getLat(), postAddress.getLon(), RADIUS);
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
        List<Amenity> amenities = fetchAmenitiesByCords(postAddress.getLat(), postAddress.getLon(), RADIUS);
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
        List<Tourism> landmarks = fetchAttractionsByCoords(postAddress.getLat(), postAddress.getLon(), RADIUS);
        double score = 0;
        double weightlessScore = 0;
        for(Tourism landmark : landmarks){
            double gaussScore = gaussianScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress("0000AA", landmark.getLat(), landmark.getLon())));
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

}
