import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class GetCoordinates{
    private Map<String, double[]> postalCodeToCoordsMap = new HashMap<>();

    public GetCoordinates(String filePath) {
        loadPostalCodeData(filePath);
    }

    private void loadPostalCodeData(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String postalCode = values[0].trim();
                    double latitude = Double.parseDouble(values[1].trim());
                    double longitude = Double.parseDouble(values[2].trim());
                    postalCodeToCoordsMap.put(postalCode, new double[]{latitude, longitude});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double[] getCoordinates(String postalCode) {
        double[] coords = postalCodeToCoordsMap.get(postalCode);
        if (coords == null) {
            // Attempt to fetch from the API
            coords = fetchCoordinatesFromApi(postalCode);
            if (coords != null) {
                postalCodeToCoordsMap.put(postalCode, coords); // Optionally cache the result
            }
        }
        return coords;
    }

     private double[] fetchCoordinatesFromApi(String postalCode) {
        String requestURL = "https://computerscience.dacs.unimaas.nl/get_coordinates?postcode=" + URLEncoder.encode(postalCode, StandardCharsets.UTF_8);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(requestURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            
            // For successful response, parse the latitude and longitude
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return parseCoordinatesFromResponse(response.toString());
            } else if (responseCode == 429) {
                System.out.println("Rate limit exceeded. Try again later.");
            } else {
                // General error handling, possibly parsing error messages
                System.out.println("API Error: " + response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }
    private double[] parseCoordinatesFromResponse(String jsonResponse) {
        double latitude = 0.0;
        double longitude = 0.0;
    
        try {
            // Find the index of "latitude" and read the value
            String latitudeKey = "\"latitude\":";
            int latIndex = jsonResponse.indexOf(latitudeKey);
            if (latIndex != -1) {
                int startLat = jsonResponse.indexOf(":", latIndex) + 1;
                int endLat = jsonResponse.indexOf(",", startLat);
                String latString = jsonResponse.substring(startLat, endLat).trim();
                latitude = Double.parseDouble(latString);
            }
    
            // Find the index of longitude and read the value
            String longitudeKey = "\"longitude\":";
            int lonIndex = jsonResponse.indexOf(longitudeKey);
            if (lonIndex != -1) {
                int startLon = jsonResponse.indexOf(":", lonIndex) + 1;
                int endLon = jsonResponse.indexOf("}", startLon);
                String lonString = jsonResponse.substring(startLon, endLon).trim();
                longitude = Double.parseDouble(lonString);
            }
            
            return new double[]{latitude, longitude};
    
        } catch (Exception e) {
            System.out.println("Error parsing coordinates: " + e.getMessage());
        }
    
        // Return null if parsing fails
        return null;
    }
}