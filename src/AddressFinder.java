import cn.hutool.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.Map;

public class AddressFinder {
    private static final RateLimiter rateLimiter = new RateLimiter();

    public static PostAddress getAddress(String postalCode) {
        // if the postal code is not in data file, get it from server
        if (!DataManager.getDataManager().PostAddressesContain(postalCode)) {
            PostAddress address = getAddressFromServer(postalCode);
            if (address != null) {
                DataManager.getDataManager().postAddresses.put(postalCode, address);
            }
            return address;
        } else {
            return DataManager.getDataManager().postAddresses.get(postalCode);
        }
    }


    public static PostAddress getAddressFromServer(String postalCode) {
        String clientIp = "localhost";
        if (!rateLimiter.isRequestAllowed(clientIp)) {
            throw new RuntimeException("Rate limit exceeded");
        }

        String baseUrl = "https://computerscience.dacs.unimaas.nl";
        String url = baseUrl + "/get_coordinates";

        OkHttpClient client = new OkHttpClient();

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("postcode", postalCode);


        Request request = new Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(jsonBody.toString(), okhttp3.MediaType.parse("application/json")))
                .build();

        try {
            ResponseBody responseBody;
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Unexpected code " + response);
                    return null;
                }

                responseBody = response.body();
            }
            catch (Exception e) {
                System.out.println("Error while sending request to server: " + e.getMessage());
                return null;
            }

            String jsonString = responseBody.string();
            JSONObject jsonResponse = new JSONObject(jsonString);

            // Check for errors in the response
            if (jsonResponse.containsKey("error")) {
                System.err.println("Error from server: " + jsonResponse.getStr("error"));
                return null; // Or handle the error accordingly
            }

            if (!jsonResponse.containsKey("latitude") || !jsonResponse.containsKey("longitude")) {
                System.err.println("Invalid response: missing latitude or longitude");
                return null;
            }

            double latitude = jsonResponse.getDouble("latitude");
            double longitude = jsonResponse.getDouble("longitude");

            return new PostAddress(postalCode, latitude, longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        PostAddress address = AddressFinder.getAddress("6216EG");
        System.out.println(address);
    }
}

// RateLimiter class as defined earlier
class RateLimiter {
    public static final long FIVE_SECONDS = TimeUnit.SECONDS.toMillis(5);
    public static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
    public static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
    public static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);

    public static final int FIVE_SECONDS_LIMIT = 1;
    public static final int ONE_MINUTE_LIMIT = 5;
    public static final int ONE_HOUR_LIMIT = 40;
    public static final int ONE_DAY_LIMIT = 100;

    private final ConcurrentHashMap<String, UserRequestInfo> requestCounts = new ConcurrentHashMap<>();

    public boolean isRequestAllowed(String ip) {
        long now = Instant.now().toEpochMilli();

        requestCounts.compute(ip, (key, userInfo) -> {
            if (userInfo == null) {
                userInfo = new UserRequestInfo();
            }

            userInfo.cleanupOldRequests(now);

            if (userInfo.getCountWithin(FIVE_SECONDS) >= FIVE_SECONDS_LIMIT ||
                    userInfo.getCountWithin(ONE_MINUTE) >= ONE_MINUTE_LIMIT ||
                    userInfo.getCountWithin(ONE_HOUR) >= ONE_HOUR_LIMIT ||
                    userInfo.getCountWithin(ONE_DAY) >= ONE_DAY_LIMIT) {
                return userInfo;
            }

            userInfo.addRequest(now);

            return userInfo;
        });

        UserRequestInfo userInfo = requestCounts.get(ip);
        return userInfo != null && userInfo.getCountWithin(FIVE_SECONDS) <= FIVE_SECONDS_LIMIT &&
                userInfo.getCountWithin(ONE_MINUTE) <= ONE_MINUTE_LIMIT &&
                userInfo.getCountWithin(ONE_HOUR) <= ONE_HOUR_LIMIT &&
                userInfo.getCountWithin(ONE_DAY) <= ONE_DAY_LIMIT;
    }
}

class UserRequestInfo {
    private final ConcurrentHashMap<Long, Integer> requestTimestamps = new ConcurrentHashMap<>();

    void addRequest(long timestamp) {
        requestTimestamps.merge(timestamp, 1, Integer::sum);
    }

    void cleanupOldRequests(long currentTimestamp) {
        requestTimestamps.keySet().removeIf(timestamp ->
                timestamp < currentTimestamp - RateLimiter.ONE_DAY);
    }

    int getCountWithin(long timePeriod) {
        long currentTimestamp = Instant.now().toEpochMilli();
        return requestTimestamps.entrySet().stream()
                .filter(entry -> entry.getKey() >= currentTimestamp - timePeriod)
                .mapToInt(Map.Entry::getValue)
                .sum();
    }
}
