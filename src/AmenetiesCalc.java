import dbTables.Amenity;
import dbTables.PostAddress;
import dbTables.Shop;
import dbTables.Tourism;

import static dbTables.PostAddress.basicDistances;
import static dbTables.dbManager.*;

import java.util.List;

public class AmenetiesCalc {
    final double RADIUS = 4;
    final double essentialShopWeight = .3;
    final double essentialAmenityWeight = .4;
    final double nonEssentialShopWeight = .1;
    final double nonEssentialAmenityWeight = .1;
    final double TourismWeight = .1;

    private double shopScores(PostAddress postAddress){
        List<Shop> shops = fetchShopsByCoords(postAddress.getLat(), postAddress.getLon(), RADIUS);
        double score = 0;
        double weigtlessScore = 0;
        for(Shop shop : shops){
            double gaussScore = gaussianScore(basicDistances(postAddress, new PostAddress("0000AA", shop.getLat(), shop.getLon())));
            weigtlessScore += gaussScore;
            if(shop.getType().equals("mall")||shop.getType().equals("supermarket")){
                score += gaussScore*essentialShopWeight;
            } else{
                score += gaussScore*nonEssentialShopWeight;
            }
        }
        score = score / weigtlessScore;
        return score;
    }

    private double amenityScores(PostAddress postAddress){
        List<Amenity> amenities = fetchAmenitiesByCoords(postAddress.getLat(), postAddress.getLon(), RADIUS);
        double score = 0;
        double weigtlessScore = 0;
        for(Amenity amenity : amenities){
            double gaussScore = gaussianScore(basicDistances(postAddress, new PostAddress("0000AA", amenity.getLat(), amenity.getLon())));
            weigtlessScore += gaussScore;
            if(amenity.getType().equals("bank")||amenity.getType().equals("college")||amenity.getType().equals("doctors")||amenity.getType().equals("hospital")
                    ||amenity.getType().equals("pharmacy")||amenity.getType().equals("school")||amenity.getType().equals("university")){
                score += gaussScore*essentialAmenityWeight;
            } else{
                score += gaussScore*nonEssentialAmenityWeight;
            }
        }
        score = score / weigtlessScore;
        return score;
    }

    private double tourismScores(PostAddress postAddress){
        List<Tourism> landmarks = fetchAttractionsByCoords(postAddress.getLat(), postAddress.getLon(), RADIUS);
        double score = 0;
        double weigtlessScore = 0;
        for(Tourism landmark : landmarks){
            double gaussScore = gaussianScore(basicDistances(postAddress, new PostAddress("0000AA", landmark.getLat(), landmark.getLon())));
            weigtlessScore += gaussScore;
            score += gaussScore*TourismWeight;
        }
        score = score / weigtlessScore;
        return score;
    }

    public double gaussianScore(double distance) {
        double mu = 0; // Mean distance (zero for maximum score at zero distance)
        return Math.exp(-Math.pow(distance - mu, 2) / (2 * Math.pow(RADIUS/2, 2)));
    }

    public double calculateScore(PostAddress postAddress){
        return tourismScores(postAddress) + amenityScores(postAddress) + shopScores(postAddress);
    }

}
