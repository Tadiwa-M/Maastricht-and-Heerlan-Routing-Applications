import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class utilities {
    public static void main(String[] args) {
        String path = "data\\distances.csv"; // Replace with your CSV file path
        String line = "";
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            
            // Skipping the first two lines
            br.readLine();
            br.readLine();
            
            // Reading from the third line
            while ((line = br.readLine()) != null) {
                // Assuming CSV file is comma-separated
                String[] values = line.split(","); 
                // Process your data here
                System.out.println(line); // Print each line read from the third line onwards
            }
            
            br.close(); // Close the BufferedReader
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
