import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class dbManager {
    private static String USERNAME = "";
    private static String PORT = "";
    private static String HOST = "";
    private static String DATABASE_NAME = "";
    private static String PASSWORD = "";
    private static String DATABASE_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME + "?autoReconnect=true&useSSL=true&requireSSL=true";

    public static void main(String[] args) {
        loadLoginInfo();

        Connection conn = getSqlConnection();

        try {//exmaple query
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM trips"); 
            ResultSet resultSet = stmt.executeQuery();
                System.out.println(resultSet.getStrings("service_id")+ "   " + resultSet.getStrings("date_info")+ "   "+ resultSet.getStrings("exception_type"));//the methods actually return arrays, but it works as an example
            resultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void loadLoginInfo() {//the order of the variables in the file is unimportant 
        try (BufferedReader reader = new BufferedReader(new FileReader("dbCredentials.txt"))) {//TODO:for now the file is in the same folder, later it should be moved to a different location
            String line;
            while ((line = reader.readLine()) != null) {
                switch (line.trim()) {
                    case "###":
                        System.out.println(line); //echo any comments from the credentials file to the console
                        break;
                    case "host=":
                        HOST = line.split("=")[1];
                        System.out.println(HOST);
                        break;
                    case "port=":
                        PORT = line.split("=")[1];
                        System.out.println(PORT);
                        break;
                    case "username=":
                        USERNAME = line.split("=")[1];
                        System.out.println(USERNAME);
                        break;
                    case "databaseName=":
                        DATABASE_NAME = line.split("=")[1];
                        System.out.println(DATABASE_NAME);
                        break;
                    case "password=":
                        PASSWORD = line.split("=")[1];
                        System.out.println(PASSWORD);
                        break;
                    default:
                        System.out.println("Unrecognized line in dbCredentials.txt");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading dbCredentials.txt file: " + e.getMessage());
        }
    }

    private static Connection getSqlConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); //XXX:this might return an error, i still have no idea how to work with maven
                                                        //XXX:If you are the one to install the dependency grab the multiplatform version
            return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            return null;
        }
    }
}
