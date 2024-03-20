import cn.hutool.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;

public class AddressFinder {
    public static PostAddress getAddress(String postalCode) {
        //if the postal code is not in data file, get it from server
        if (!DataManager.getDataManager().PostAddressesContain(postalCode)) {
            PostAddress address = getAddressFromServer(postalCode);
            DataManager.getDataManager().postAddresses.put(postalCode, address);
            return address;
        }
        else {
            return DataManager.getDataManager().postAddresses.get(postalCode);
        }


    }
    public static PostAddress getAddressFromServer(String postalCode) {
        String baseUrl = "https://computerscience.dacs.unimaas.nl";

        String url = baseUrl + "/get_coordinates";

        // Create a new okhttp client
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("postcode", postalCode);

        Request request = new Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(jsonBody.toString(), okhttp3.MediaType.parse("application/json")))
                .build();

        try {
            // Execute the request
            ResponseBody responseBody;
            try (Response response = client.newCall(request).execute()) {

                // Check if the request was successful (HTTP status code 200)
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Failed to execute request: " + response);
                }

                // Get the response body as a JSON string
                responseBody = response.body();
            }
            String jsonString = responseBody.string();

            // Parse the JSON string into a JSONObject
            JSONObject jsonResponse = new JSONObject(jsonString);

            // Get the latitude and longitude from the JSON response
            double latitude = jsonResponse.getDouble("latitude");
            double longitude = jsonResponse.getDouble("longitude");

            return new PostAddress(postalCode, latitude, longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        AddressFinder addressFinder = new AddressFinder();
        PostAddress address = addressFinder.getAddressFromServer("6216EG");
        System.out.println(address);
    }
}
