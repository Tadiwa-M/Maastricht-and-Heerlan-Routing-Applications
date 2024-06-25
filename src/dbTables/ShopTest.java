package dbTables;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ShopTest {

    @Test
    public void testConstructorAndGetters() {
        String name = "Test Shop";
        double lat = 50.850346;
        double lon = 5.688889;
        String type = "Grocery";

        Shop shop = new Shop(name, lat, lon, type);

        assertEquals(name, shop.getName());
        assertEquals(lat, shop.getLat());
        assertEquals(lon, shop.getLon());
        assertEquals(type, shop.getType());
    }

    @Test
    public void testSetName() {
        Shop shop = new Shop("Test Shop", 50.850346, 5.688889, "Grocery");
        String newName = "New Shop Name";

        shop.setName(newName);
        assertEquals(newName, shop.getName());
    }

    @Test
    public void testSetLat() {
        Shop shop = new Shop("Test Shop", 50.850346, 5.688889, "Grocery");
        double newLat = 50.851368;

        shop.setLat(newLat);
        assertEquals(newLat, shop.getLat());
    }

    @Test
    public void testSetLon() {
        Shop shop = new Shop("Test Shop", 50.850346, 5.688889, "Grocery");
        double newLon = 5.690973;

        shop.setLon(newLon);
        assertEquals(newLon, shop.getLon());
    }

    @Test
    public void testSetType() {
        Shop shop = new Shop("Test Shop", 50.850346, 5.688889, "Grocery");
        String newType = "Supermarket";

        shop.setType(newType);
        assertEquals(newType, shop.getType());
    }
}
