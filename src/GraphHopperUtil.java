import java.util.ArrayList;
import java.util.List;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.VehicleEncodedValuesFactory;
import com.graphhopper.util.shapes.GHPoint;

public class GraphHopperUtil {
    final String osmFile = "data/locationInfo/Maastricht.osm.pbf";
    final String graphFolder = "graphFolder";

    GraphHopper graphHopper = new GraphHopper();
    GraphHopperUtil() {
        List<Profile> profileList = new ArrayList<>();
        profileList.add(new Profile("foot").setVehicle("foot"));
        profileList.add(new Profile("bike").setVehicle("bike"));
        profileList.add(new Profile("car").setVehicle("car"));

        graphHopper.setOSMFile(osmFile);
        graphHopper.setProfiles(profileList);
        graphHopper.setGraphHopperLocation(graphFolder);

        graphHopper.importOrLoad();
    }

    public void calculateRoute(String sourcePostalCode, String destinationPostalCode, String vehicle) {
        PostAddress source = AddressFinder.getAddress(sourcePostalCode);
        PostAddress destination = AddressFinder.getAddress(destinationPostalCode);

        GHPoint sourcePoint = new GHPoint(source.getLat(), source.getLon());
        GHPoint destinationPoint = new GHPoint(destination.getLat(), destination.getLon());

        GHRequest request = new GHRequest(sourcePoint, destinationPoint);
        request.setProfile(vehicle);
        GHResponse response = graphHopper.route(request);

        // Convert time from milliseconds to minutes and seconds
        long timeInMillis = response.getBest().getTime();
        long minutes = (timeInMillis / 1000) / 60;
        int seconds = (int)((timeInMillis / 1000) % 60);

        System.out.println(minutes + " minutes " + seconds + " seconds " + response.getBest().getDistance() + " meters");
    }

    public static void main(String[] args) {
        GraphHopperUtil graphHopperUtil = new GraphHopperUtil();
        graphHopperUtil.calculateRoute("6229EN", "6229HD", "foot");
        graphHopperUtil.calculateRoute("6229EN", "6229HD", "bike");
        graphHopperUtil.calculateRoute("6229EN", "6229HD", "car");
    }
}
