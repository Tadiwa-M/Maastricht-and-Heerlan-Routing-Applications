import java.util.HashMap;
import java.util.Set;

public class main {
    final static String path = "data/distances.csv";

    public static void main(String[] args) {
        HashMap<String, PostAdress> postAdresses = Utilities.initPostAdressMap(path);
        Set<String> keySet =postAdresses.keySet();

        //PostAdress.basicDistances()
    }


}
