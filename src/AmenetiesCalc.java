import dbTables.PostAddress;
import dbTables.Shop;
import dbTables.Amenity;
import dbTables.Tourism;

import java.util.List;

import static dbTables.dbManager.fetchShopsByCoords;
import static dbTables.dbManager.fetchAmenitiesByCoords;
import static dbTables.dbManager.fetchAttractionsByCoords;

public class AmenetiesCalc {
    double RADIUS = 1;

    private double shopScores(PostAddress postAddress){
        List<Shop> shops = fetchShopsByCoords(postAddress.getLat(), postAddress.getLon(), RADIUS);
        for(Shop shop : shops){
            if(shop.getType().equals("mall")||shop.getType().equals("supermarket")){
    
            }
        }
        return 0;
    }

    public double calculateScore(PostAddress postAddress){
        return 0;
    }

}
