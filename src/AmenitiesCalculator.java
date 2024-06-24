import dbTables.*;

import static dbTables.AmenityManager.*;
import static dbTables.dbManager.fetchAllAddresses;

import java.util.ArrayList;
import java.util.List;

public class AmenitiesCalculator {
    // Shop Weight Constants
    final static double essentialShopWeight = 0.5;
    final static double specialtyShopWeight = 0.3;
    final static double miscShopWeight = 0.2;
    // Amenities Weight Constants
    final static double essentialAmenityWeight = 0.5;
    final static double communityAmenityWeight = 0.3;
    final static double recAmenityWeight = 0.2;
    // Tourism Weight Constants
    final static double highTourismWeight = 0.5;
    final static double moderateTourismWeight = 0.3;
    final static double lowTourismWeight = 0.2;

    // Define weights for each category
    final static double shopCategoryWeight = 0.3;
    final static double amenityCategoryWeight = 0.5;
    final static double tourismCategoryWeight = 0.2;
    final static double accessibilityCategoryWeight = 1;


    // Decay constants for different categories
    // The higher the decay constant, the faster the score decreases with distance
    // The decay constant is inversely proportional to the distance

    // Shop decay constants
    final static double essentialShopDecay = 0.3;
    final static double specialtyShopDecay = 0.5;
    final static double miscShopDecay = 0.7;
    // Amenity decay constants
    final static double essentialAmenityDecay = 0.3;
    final static double communityAmenityDecay = 0.5;
    final static double recAmenityDecay = 0.7;
    // Tourism decay constants
    final static double highTourismDecay = 0.3;
    final static double moderateTourismDecay = 0.5;
    final static double lowTourismDecay = 0.7;


    public static void main(String[] args) {
        List<AddressScore> scores = new ArrayList<>();
        calculateAllScores(scores);
        printBestAndWorst(scores);
    }

    private static double shopScores(PostAddress postAddress, List<Shop> shops) {
        double score = 0;

        for (Shop shop : shops) {
            double decayConstant = getShopDecayConstant(shop.type());
            double decayScore = exponentialDecayScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(shop.lat(), shop.lon())), decayConstant);
            if (isEssentialShop(shop.type())) {
                score += decayScore * essentialShopWeight;
            } else if (isSpecialtyShop(shop.type())) {
                score += decayScore * specialtyShopWeight;
            } else {
                score += decayScore * miscShopWeight;
            }
        }
        return score;
    }

    private static double amenityScores(PostAddress postAddress, List<Amenity> amenities) {
        double score = 0;

        for (Amenity amenity : amenities) {
            double decayConstant = getAmenityDecayConstant(amenity.type());
            double decayScore = exponentialDecayScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(amenity.lat(), amenity.lon())), decayConstant);
            if (isEssentialAmenity(amenity.type())) {
                score += decayScore * essentialAmenityWeight;
            } else if (isCommunityAmenity(amenity.type())) {
                score += decayScore * communityAmenityWeight;
            } else {
                score += decayScore * recAmenityWeight;
            }
        }
        return score;
    }

    private static double tourismScores(PostAddress postAddress, List<Tourism> attractions) {
        double score = 0;
        for (Tourism attraction : attractions) {
            double decayConstant = getTourismDecayConstant(attraction.getType());
            double decayScore = exponentialDecayScore(LineDistanceCalculator.basicDistances(postAddress, new PostAddress(attraction.getLat(), attraction.getLon())), decayConstant);
            if (isHighTourism(attraction.getType())) {
                score += decayScore * highTourismWeight;
            } else if (isModerateTourism(attraction.getType())) {
                score += decayScore * moderateTourismWeight;
            } else {
                score += decayScore * lowTourismWeight;
            }
        }
        return score;
    }

    public static double calculateAccessibilityScore(PostAddress postAddress, List<Stop> busStops) {
        double score = 0;

        for (Stop busStop : busStops) {
            double distance = LineDistanceCalculator.basicDistances(postAddress, new PostAddress(busStop.getStopLat(), busStop.getStopLon()));
            score += 1 / (1 + distance); // Using a decay function for proximity score
        }
        return score;
    }

    public static double exponentialDecayScore(double distance, double decayConstant) {
        return Math.exp(-decayConstant * distance); // Convert distance to kilometers
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

    public static void calculateAllScores(List<AddressScore> scores) {
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

        List<Stop> busStops = fetchBusStopsByCoords();
        if (busStops == null || busStops.isEmpty()) {
            System.out.println("No bus stops found");
            return;
        }

        for (PostAddress address : allAddresses) {
            double shopScore = shopScores(address, shops);
            double amenityScore = amenityScores(address, amenities);
            double tourismScore = tourismScores(address, attractions);
            double accessibilityScore = calculateAccessibilityScore(address, busStops);

            scores.add(new AddressScore(address, amenityScore, shopScore, tourismScore, accessibilityScore));
        }

        normalizeScores(scores);
        sortScores(scores);
    }

    public static void printBestAndWorst(List<AddressScore> scores) {
        System.out.println("Most desirable addresses:");
        System.out.println(scores.get(0));

        System.out.println("Least desirable addresses:");
        System.out.println(scores.get(scores.size() - 1));
    }

    private static void sortScores(List<AddressScore> scores) {
        scores.sort((score1, score2) -> Double.compare(score2.getScore(), score1.getScore()));
    }

    public static AddressScore getAddressScore(String postalCode, List<AddressScore> scores) {
        for (AddressScore score : scores) {
            if (score.getPostalCode().equals(postalCode)) {
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

        normalizeAmenityScores(scores);
        normalizeShopScores(scores);
        normalizeTourismScores(scores);
        normalizeAccessibilityScores(scores);

        setTotalScores(scores);

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

    private static void setTotalScores(List<AddressScore> scores) {
        for (AddressScore score : scores) {
            double totalScore = (score.getAmenityScore() * amenityCategoryWeight) + (score.getShopScore() * shopCategoryWeight) + (score.getTourismScore() * tourismCategoryWeight) + (score.getAccessibilityScore() * accessibilityCategoryWeight);
            score.setScore(totalScore);
        }
    }

    private static void normalizeAmenityScores(List<AddressScore> scores) {
        double maxAmenityScore = scores.stream().mapToDouble(AddressScore::getAmenityScore).max().orElse(0);
        double minAmenityScore = scores.stream().mapToDouble(AddressScore::getAmenityScore).min().orElse(0);

        if (maxAmenityScore == minAmenityScore) {
            for (AddressScore score : scores) {
                score.setAmenityScore(0);
            }
        } else {
            for (AddressScore score : scores) {
                double normalizedAmenityScore = (score.getAmenityScore() - minAmenityScore) / (maxAmenityScore - minAmenityScore);
                score.setAmenityScore(normalizedAmenityScore);
            }
        }
    }

    private static void normalizeShopScores(List<AddressScore> scores) {
        double maxShopScore = scores.stream().mapToDouble(AddressScore::getShopScore).max().orElse(0);
        double minShopScore = scores.stream().mapToDouble(AddressScore::getShopScore).min().orElse(0);

        if (maxShopScore == minShopScore) {
            for (AddressScore score : scores) {
                score.setShopScore(0);
            }
        } else {
            for (AddressScore score : scores) {
                double normalizedShopScore = (score.getShopScore() - minShopScore) / (maxShopScore - minShopScore);
                score.setShopScore(normalizedShopScore);
            }
        }
    }

    private static void normalizeTourismScores(List<AddressScore> scores) {
        double maxTourismScore = scores.stream().mapToDouble(AddressScore::getTourismScore).max().orElse(0);
        double minTourismScore = scores.stream().mapToDouble(AddressScore::getTourismScore).min().orElse(0);

        if (maxTourismScore == minTourismScore) {
            for (AddressScore score : scores) {
                score.setTourismScore(0);
            }
        } else {
            for (AddressScore score : scores) {
                double normalizedTourismScore = (score.getTourismScore() - minTourismScore) / (maxTourismScore - minTourismScore);
                score.setTourismScore(normalizedTourismScore);
            }
        }
    }

    private static void normalizeAccessibilityScores(List<AddressScore> scores) {
        double maxAccessibilityScore = scores.stream().mapToDouble(AddressScore::getAccessibilityScore).max().orElse(0);
        double minAccessibilityScore = scores.stream().mapToDouble(AddressScore::getAccessibilityScore).min().orElse(0);

        if (maxAccessibilityScore == minAccessibilityScore) {
            for (AddressScore score : scores) {
                score.setAccessibilityScore(0);
            }
        } else {
            for (AddressScore score : scores) {
                double normalizedAccessibilityScore = (score.getAccessibilityScore() - minAccessibilityScore) / (maxAccessibilityScore - minAccessibilityScore);
                score.setAccessibilityScore(normalizedAccessibilityScore);
            }
        }
    }
}