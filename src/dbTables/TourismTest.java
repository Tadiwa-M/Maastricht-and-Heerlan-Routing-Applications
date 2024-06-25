package dbTables;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TourismTest {

    @Test
    public void testConstructorAndGetters() {
        String name = "Test Tourism";
        double lat = 50.850346;
        double lon = 5.688889;
        String type = "Museum";

        Tourism tourism = new Tourism(name, lat, lon, type);

        assertEquals(name, tourism.getName());
        assertEquals(lat, tourism.getLat());
        assertEquals(lon, tourism.getLon());
        assertEquals(type, tourism.getType());
    }

    @Test
    public void testSetName() {
        Tourism tourism = new Tourism("Test Tourism", 50.850346, 5.688889, "Museum");
        String newName = "New Tourism Name";

        tourism.setName(newName);
        assertEquals(newName, tourism.getName());
    }

    @Test
    public void testSetLat() {
        Tourism tourism = new Tourism("Test Tourism", 50.850346, 5.688889, "Museum");
        double newLat = 50.851368;

        tourism.setLat(newLat);
        assertEquals(newLat, tourism.getLat());
    }

    @Test
    public void testSetLon() {
        Tourism tourism = new Tourism("Test Tourism", 50.850346, 5.688889, "Museum");
        double newLon = 5.690973;

        tourism.setLon(newLon);
        assertEquals(newLon, tourism.getLon());
    }

    @Test
    public void testSetType() {
        Tourism tourism = new Tourism("Test Tourism", 50.850346, 5.688889, "Museum");
        String newType = "Park";

        tourism.setType(newType);
        assertEquals(newType, tourism.getType());
    }
}