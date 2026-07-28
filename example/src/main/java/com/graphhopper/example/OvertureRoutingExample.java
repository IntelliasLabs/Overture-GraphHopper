package com.graphhopper.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.overture.OvertureReader;
import com.graphhopper.util.*;
import java.util.Locale;

public class OvertureRoutingExample {

    public static void main(String[] args) {
        String relDir = args.length == 1 ? args[0] : "";
        GraphHopper hopper = createGraphHopperInstance(
                "/Users/user/Work/Overture/GraphHopperUA/florence_center.parquet");
        routing(hopper);

        hopper.close();
    }

    static GraphHopper createGraphHopperInstance(String ghLoc) {
        GraphHopper hopper = new GraphHopper();

        hopper.setEncodedValuesString("car_access, car_average_speed, foot_access, foot_average_speed, "
                + "road_access, hike_rating, foot_priority, country, road_class, "
                + "foot_road_access, mtb_rating, bike_access, bike_average_speed, "
                + "bike_network, bike_priority, bike_road_access, surface, hazmat, track_type");

        hopper.setDataReaderInitializer(context -> new OvertureReader(context.getBaseGraph())
                .setEncodedValueLookup(context.getEncodingManager())
                .setFile(context.getSourceFile()));
        hopper.setDataFile(ghLoc);

        // specify where to store graphhopper files
        hopper.setGraphHopperLocation("target/overture-graph-cache");

        hopper.setProfiles(
                new Profile("car").setCustomModel(GHUtility.loadCustomModelFromJar("car.json")),
                new Profile("foot").setCustomModel(GHUtility.loadCustomModelFromJar("foot_overture.json")),
                new Profile("bike").setCustomModel(GHUtility.loadCustomModelFromJar("bike_overture.json")));

        hopper
                .getCHPreparationHandler()
                .setCHProfiles(new CHProfile("car"), new CHProfile("foot"), new CHProfile("bike"));

        hopper.importOrLoad();
        return hopper;
    }

    public static void routing(GraphHopper hopper) {
        routeWithProfile(hopper, "car", 51.509339, -0.143126, 51.506618, -0.131746);
        routeWithProfile(hopper, "foot", 51.509339, -0.143126, 51.506618, -0.131746);
        routeWithProfile(hopper, "bike", 51.509339, -0.143126, 51.506618, -0.131746);
    }

    private static void routeWithProfile(
            GraphHopper hopper,
            String profile,
            double startLat,
            double startLon,
            double endLat,
            double endLon) {
        System.out.println("\n=== Routing for " + profile.toUpperCase() + " ===");
        try {
            GHRequest req = new GHRequest(startLat, startLon, endLat, endLon)
                    .setProfile(profile)
                    .setLocale(Locale.US);
            GHResponse rsp = hopper.route(req);

            if (rsp.hasErrors()) {
                System.err.println("Errors for " + profile + ": " + rsp.getErrors().toString());
                return;
            }

            ResponsePath path = rsp.getBest();

            PointList pointList = path.getPoints();
            double distance = path.getDistance();
            long timeInMs = path.getTime();

            Translation tr = hopper.getTranslationMap().getWithFallBack(Locale.UK);
            InstructionList il = path.getInstructions();

            System.out.printf("Route was built. Distance: %.2fm, time: %ds%n", distance, timeInMs / 1000);

            for (Instruction instruction : il) {
                System.out.println("distance " + instruction.getDistance() + " for instruction: "
                        + instruction.getTurnDescription(tr));
            }

            System.out.println("GeoJSON route geometry: " + toGeoJsonLineString(pointList));
        } catch (Exception e) {
            System.err.println("Error routing with " + profile + ": " + e.getMessage());
        }
    }

    private static String toGeoJsonLineString(PointList points) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        for (int i = 0; i < points.size(); i++) {
            sb.append('[')
                    .append(points.getLon(i))
                    .append(',')
                    .append(points.getLat(i))
                    .append(']')
                    .append('\n');
        }
        return sb.toString();
    }
}
