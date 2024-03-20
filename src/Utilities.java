import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Utilities {
    public static void main(String[] args) {
        String path = "data/distances.csv"; // Replace with your CSV file path
        HashMap<String, PostAddress> postAdress =initPostAdressMap(path);
        PostAddress adress = postAdress.get("6229ZE");
        System.out.println(adress.getLat());
    }

    public static HashMap<String, PostAddress> initPostAdressMap(String filePath){
        String line = "";
        HashMap<String, PostAddress> postAdresses = new HashMap<String, PostAddress>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            
            // Skipping the first two lines
            br.readLine();
            br.readLine();
            
            // Reading from the third line
            while ((line = br.readLine()) != null) {
                // Assuming CSV file is comma-separated
                String[] values = line.split(","); 
                postAdresses.put(values[0], new PostAddress(values[0], Double.parseDouble(values[1]), Double.parseDouble(values[2])));
            }
            
            br.close(); // Close the BufferedReader
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postAdresses;
    }
}
