import java.util.HashMap;

public class main {
    final static String path = "data/distances.csv";
    public static void main(String[] args) {
        HashMap<String, PostAdress> postAdress =utilities.initPostAdressMap(path);
        PostAdress start = postAdress.get("6229ZA");
        PostAdress end = postAdress.get("6229ZB");

        System.out.println(PostAdress.basicDistances(start, end));
    }
}
