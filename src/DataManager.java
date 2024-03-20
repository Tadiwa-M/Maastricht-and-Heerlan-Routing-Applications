import java.util.HashMap;

public final class DataManager {
    private static DataManager dataManager;
    private static final String filePath = "data/distances.csv";
    HashMap<String, PostAddress> postAddresses;

    public DataManager() {
        postAddresses = Utilities.initPostAddressMap(filePath);
    }

    public static DataManager getDataManager() {
        if (dataManager == null)
            dataManager = new DataManager();
        return dataManager;
    }

    boolean PostAddressesContain(String postalCode) {
        return postAddresses.containsKey(postalCode);
    }
}
