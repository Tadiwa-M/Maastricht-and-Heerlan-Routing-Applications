package dbTables;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AmenityTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        double lat = 40.7128;
        double lon = -74.0060;
        String type = "Park";

        // Act
        Amenity amenity = new Amenity(lat, lon, type);

        // Assert
        assertEquals(lat, amenity.getLat());
        assertEquals(lon, amenity.getLon());
        assertEquals(type, amenity.getType());
    }

    @Test
    void testSetters() {
        // Arrange
        Amenity amenity = new Amenity(40.7128, -74.0060, "Park");

        // Act
        amenity.setLat(34.0522);
        amenity.setLon(-118.2437);
        amenity.setType("Museum");

        // Assert
        assertEquals(34.0522, amenity.getLat());
        assertEquals(-118.2437, amenity.getLon());
        assertEquals("Museum", amenity.getType());
    }
}