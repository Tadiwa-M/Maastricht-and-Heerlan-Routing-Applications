import dbTables.*;

import static dbTables.AmenityManager.*;
import static dbTables.dbManager.fetchAllAddresses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AmenitiesCalculator {
    final static double essentialShopWeight = 0.5;
    final static double specialtyShopWeight = 0.3;
    final static double miscShopWeight = 0.2;
    final static double essentialAmenityWeight = 0.5;
    final static double communityAmenityWeight = 0.3;
    final static double recAmenityWeight = 0.2;
    final static double highTourismWeight = 0.4;
    final static double moderateTourismWeight = 0.3;
    final static double lowTourismWeight = 0.3;

    // Define weights for each category
    final static double shopCategoryWeight = 0.3;
    final static double amenityCategoryWeight = 0.5;
    final static double tourismCategoryWeight = 0.2;

    // Decay constants for different categories
    final static double essentialShopDecay = 0.3;
    final static double specialtyShopDecay = 0.5;
    final static double miscShopDecay = 0.7;
    final static double essentialAmenityDecay = 0.3;
    final static double communityAmenityDecay = 0.5;
    final static double recAmenityDecay = 0.7;
    final static double highTourismDecay = 0.3;
    final static double moderateTourismDecay = 0.5;
    final static double lowTourismDecay = 0.7;

    // Weights for different modes of transport
    final static double busWeight = 0.4;
    final static double walkWeight = 0.2;
    final static double bikeWeight = 0.2;
    final static double carWeight = 0.2;

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
            double decayConstant = getShopDecayConstant(shop.type());
            double decayScore = exponentialDecayScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(shop.lat(), shop.lon())), decayConstant);
            weightlessScore += decayScore;
            if (isEssentialShop(shop.type())) {
                score += decayScore * essentialShopWeight;
            } else if (isSpecialtyShop(shop.type())) {
                score += decayScore * specialtyShopWeight;
            } else {
                score += decayScore * miscShopWeight;
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
            double decayConstant = getAmenityDecayConstant(amenity.type());
            double decayScore = exponentialDecayScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(amenity.lat(), amenity.lon())), decayConstant);
            weightlessScore += decayScore;
            if (isEssentialAmenity(amenity.type())) {
                score += decayScore * essentialAmenityWeight;
            } else if (isCommunityAmenity(amenity.type())) {
                score += decayScore * communityAmenityWeight;
            } else {
                score += decayScore * recAmenityWeight;
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
            double decayConstant = getTourismDecayConstant(attraction.getType());
            double decayScore = exponentialDecayScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(attraction.getLat(), attraction.getLon())), decayConstant);
            weightlessScore += decayScore;
            if (isHighTourism(attraction.getType())) {
                score += decayScore * highTourismWeight;
            } else if (isModerateTourism(attraction.getType())) {
                score += decayScore * moderateTourismWeight;
            } else {
                score += decayScore * lowTourismWeight;
            }
        }
        score = score / weightlessScore;
        return score;
    }

    public static double calculateAccessibilityScore(double busTime, double walkTime, double bikeTime, double carTime) {
        return 1 / (1 + busTime * busWeight + walkTime * walkWeight + bikeTime * bikeWeight + carTime * carWeight);
    }


    public static double exponentialDecayScore(double distance, double decayConstant) {
        return Math.exp(-decayConstant * distance / 1000); // Convert distance to kilometers
    }

    private static double getShopDecayConstant(String type) {
        if (isEssentialShop(type)) {
            return essentialShopDecay;
        } else if (isSpecialtyShop(type)) {
            return specialtyShopDecay;
        } else {
            return miscShopDecay;
        }
    }

    private static double getAmenityDecayConstant(String type) {
        if (isEssentialAmenity(type)) {
            return essentialAmenityDecay;
        } else if (isCommunityAmenity(type)) {
            return communityAmenityDecay;
        } else {
            return recAmenityDecay;
        }
    }

    private static double getTourismDecayConstant(String type) {
        if (isHighTourism(type)) {
            return highTourismDecay;
        } else if (isModerateTourism(type)) {
            return moderateTourismDecay;
        } else {
            return lowTourismDecay;
        }
    }

    private static boolean isEssentialShop(String type) {
        return type.equals("supermarket") || type.equals("mall") || type.equals("pharmacy") || type.equals("medical_supply") || type.equals("convenience") || type.equals("bakery") || type.equals("butcher") || type.equals("greengrocer");
    }

    private static boolean isSpecialtyShop(String type) {
        return type.equals("clothing") || type.equals("shoes") || type.equals("books") || type.equals("electronics") || type.equals("sports") || type.equals("bicycle") || type.equals("toys") || type.equals("music") || type.equals("art") || type.equals("furniture") || type.equals("jewelry");
    }

    private static boolean isEssentialAmenity(String type) {
        return type.equals("hospital") || type.equals("clinic") || type.equals("fire_station") || type.equals("police") || type.equals("school") || type.equals("university") || type.equals("childcare") || type.equals("nursing_home") || type.equals("doctors");
    }

    private static boolean isCommunityAmenity(String type) {
        return type.equals("place_of_worship") || type.equals("community_centre") || type.equals("library") || type.equals("arts_centre") || type.equals("theatre") || type.equals("social_facility") || type.equals("townhall");
    }

    private static boolean isHighTourism(String type) {
        return type.equals("museum") || type.equals("gallery") || type.equals("attraction") || type.equals("artwork") || type.equals("zoo");
    }

    private static boolean isModerateTourism(String type) {
        return type.equals("hotel") || type.equals("hostel") || type.equals("guest_house") || type.equals("caravan_site") || type.equals("viewpoint") || type.equals("apartment");
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

//            double accessibilityScore = calculateAccessibilityScore(travelTimes.getBusTime(), travelTimes.getWalkTime(), travelTimes.getBikeTime(), travelTimes.getCarTime());
            double accessibilityScore = 0; // Placeholder value

            double totalScore = (shopScore * shopCategoryWeight) + (amenityScore * amenityCategoryWeight) + (tourismScore * tourismCategoryWeight) + (accessibilityScore * 0.5);

            scores.add(new AddressScore(address, totalScore, amenityScore, shopScore, tourismScore, accessibilityScore));
        }

        normalizeScores(scores);
        sortScores(scores);

        System.out.println("Most desirable addresses:");
        AddressScore score = scores.get(0);
        System.out.println("Address: " + score.getAddress().getPostalCode() + ", " + score.getAddress() + ", " + score.getScore() + ", " + score.getAmenityScore() + ", " + score.getShopScore() + ", " + score.getTourismScore() + ", " + score.getAccessibilityScore());

        System.out.println("Least desirable addresses:");
        score = scores.get(scores.size() - 1);
        System.out.println("Address: " + score.getAddress().getPostalCode() + ", " + score.getAddress() + ", " + score.getScore() + ", " + score.getAmenityScore() + ", " + score.getShopScore() + ", " + score.getTourismScore() + ", " + score.getAccessibilityScore());

        AddressScore addressScore = getAddressScore("6211LC", scores);
        if (addressScore != null) {
            System.out.println(addressScore);
        }
    }

    private static void sortScores(List<AddressScore> scores) {
        scores.sort((score1, score2) -> Double.compare(score2.getScore(), score1.getScore()));
    }

    public static AddressScore getAddressScore(String postalCode, List<AddressScore> scores) {
        for (AddressScore score : scores) {
            if (score.getAddress().getPostalCode().equals(postalCode)) {
                return score;
            }
        }
        return null;
    }

    private static void normalizeScores(List<AddressScore> scores) {
        if (scores.isEmpty()) {
            System.out.println("No scores to normalize");
            return;
        }

        double maxScore = scores.stream().mapToDouble(AddressScore::getScore).max().orElse(0);
        double minScore = scores.stream().mapToDouble(AddressScore::getScore).min().orElse(0);

        if (maxScore == minScore) {
            for (AddressScore score : scores) {
                score.setScore(0);
            }
        } else {
            for (AddressScore score : scores) {
                double normalizedScore = 100 * (score.getScore() - minScore) / (maxScore - minScore);
                score.setScore(normalizedScore);
            }
        }
    }
}