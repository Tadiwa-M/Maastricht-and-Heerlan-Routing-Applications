import cn.hutool.json.JSONArray;
import okhttp3.*;
import cn.hutool.json.JSONObject;
import java.io.IOException;

public class GraphHopper {
    private final static String apiKey = "bd0ff3ab-f676-49fc-8e3d-e05018b7f33e";


    public static void main(String[] args) {
        String postalCode1 = "6216EG";
        String postalCode2 = "6229EN";

        int vehicleType = 0;

        String vehicle;

        if (vehicleType == 0) {
            vehicle = "foot";
        } else if (vehicleType == 1) {
            vehicle = "bike";
        } else {
            vehicle = "car";
        }

        DataManager dataManager = DataManager.getDataManager();

        PostAddress startAddress = dataManager.postAddresses.get(postalCode1);
        PostAddress endAddress = dataManager.postAddresses.get(postalCode2);

        calculateFromServer(startAddress, endAddress, vehicle);
    }

    public static void calculateFromServer(PostAddress startAddress, PostAddress endAddress, String vehicle) {
        String start = startAddress.getLat() + "," + endAddress.getLon(); // Start coordinates
        String end = endAddress.getLat() + "," + endAddress.getLon(); // End coordinates

        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://graphhopper.com/api/1/route").newBuilder();
        urlBuilder.addQueryParameter("point", start);
        urlBuilder.addQueryParameter("point", end);
        urlBuilder.addQueryParameter("vehicle", vehicle);
        urlBuilder.addQueryParameter("key", apiKey);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                String responseData = response.body().string();
                // Parse the JSON response to extract time and distance
                // Example: responseData contains paths.distance (in meters) and paths.time (in milliseconds)
                // You can convert them to appropriate units (e.g., kilometers and minutes)

                //distance and time
                JSONObject jsonResponse = new JSONObject(responseData);

                double distance = 0;
                double time = 0;

                JSONArray pathsArray = jsonResponse.getJSONArray("paths");


                for (int i = 0; i < pathsArray.size(); i++) {
                    JSONObject pathObject = pathsArray.getJSONObject(i);

                    // Get the "instructions" array from the pathObject
                    JSONArray instructionsArray = pathObject.getJSONArray("instructions");

                    // Iterate through each instruction in the "instructions" array
                    for (int j = 0; j < instructionsArray.size(); j++) {
                        JSONObject instructionObject = instructionsArray.getJSONObject(j);

                        // Extract distance and time from the instructionObject and add to total
                        distance += instructionObject.getDouble("distance");
                        time += instructionObject.getLong("time");
                    }
                }

                distance /= 1000;
                time /= 60000;

                System.out.println("Distance: " + distance + " kilometers");
                System.out.println("Time: " + time + " minutes");
            }
        });
    }
}
