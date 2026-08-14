package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.DataReaderInitializer;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Integration tests for loading graphs from different file formats and verifying their equivalence.
 */
@Testcontainers
public class MultiFormatIntegrationTests {

    // Minio AWS container
    private static final String ACCESS_KEY = "admin";
    private static final String SECRET_KEY = "password";
    private static final String minioBucket = "test-bucket";
    private static final String GH_LOCATION = "target/graphhopper-test-gh";
    @Container
    private static MinIOContainer minio = new MinIOContainer("minio/minio")
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY);
    private static S3Client minioClient;

    @BeforeAll
    public static void startEnvironment() {
        minioClient = S3Client.builder()
                .endpointOverride(URI.create(minio.getS3URL()))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .build();

        minioClient.createBucket(CreateBucketRequest.builder().bucket(minioBucket).build());
    }

    private static Stream<Arguments> readerConfigurationProviderForTests() {
        return Stream.of(
                Arguments.of(
                        Paths.get(
                                        "src/test/resources/",
                                        "com/graphhopper/reader/overture/parser/correctGeoJson_CenterOfLviv.geojson")
                                .toFile(),
                        Paths.get(
                                        "src/test/resources/",
                                        "com/graphhopper/reader/overture/parquet/correctGeoJson_CenterOfLviv.parquet")
                                .toFile(),
                        (DataReaderInitializer) (baseGraph, osmParsers, config) -> new OvertureReader(baseGraph)
                                .setS3Client(minioClient)
                                .setS3Source(minioBucket, "correctGeoJson_CenterOfLviv.parquet")),
                Arguments.of(
                        Paths.get(
                                        "src/test/resources/",
                                        "com/graphhopper/reader/overture/parser/centerOfBerlin.geojson")
                                .toFile(),
                        Paths.get(
                                        "src/test/resources/",
                                        "com/graphhopper/reader/overture/parquet/centerOfBerlin.parquet")
                                .toFile(),
                        (DataReaderInitializer) (baseGraph, osmParsers, config) -> new OvertureReader(baseGraph)
                                .setS3Client(minioClient)
                                .setS3Source(minioBucket, "centerOfBerlin.parquet")),
                Arguments.of(
                        Paths.get(
                                        "src/test/resources/",
                                        "com/graphhopper/reader/overture/parser/correctGeoJson_CenterOfKyiv.geojson")
                                .toFile(),
                        Paths.get(
                                        "src/test/resources/",
                                        "com/graphhopper/reader/overture/parquet/correctGeoJson_CenterOfKyiv.parquet")
                                .toFile(),
                        (DataReaderInitializer) (baseGraph, osmParsers, config) -> new OvertureReader(baseGraph)
                                .setS3Client(minioClient)
                                .setS3Source(minioBucket, "correctGeoJson_CenterOfKyiv.parquet")));
    }

    @AfterAll
    public static void stopEnvironment() {
        minioClient.deleteBucket(DeleteBucketRequest.builder().bucket(minioBucket).build());
        minio.stop();
    }

    private static BaseGraph createGraph(File file, DataReaderInitializer initializer) {
        GraphHopper hopper = new GraphHopper();
        hopper.setFileBacked(false);
        hopper.setEncodedValuesString("car_access, car_average_speed, foot_access, foot_average_speed, "
                + "road_access, hike_rating, foot_priority, country, road_class, "
                + "foot_road_access, mtb_rating, bike_access, bike_average_speed, "
                + "bike_network, bike_priority, bike_road_access, surface, hazmat, track_type, "
                + "road_environment, ferry_speed, max_speed");

        if (file != null) {
            hopper.setDataFile(file.getAbsolutePath());
            hopper.setGraphHopperLocation(GH_LOCATION + "-" + file.getName());
        } else hopper.setGraphHopperLocation(GH_LOCATION);

        hopper.setDataReaderInitializer((baseGraph, osmParsers, config) -> ((OvertureReader)
                initializer.initializeDataReader(baseGraph, osmParsers, config))
                .setEncodedValueLookup(hopper.getEncodingManager()));

        hopper.setProfiles(
                new Profile("car").setCustomModel(GHUtility.loadCustomModelFromJar("car_overture.json")));

        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));

        hopper.importOrLoad();

        return hopper.getBaseGraph();
    }

    @ParameterizedTest
    @MethodSource("readerConfigurationProviderForTests")
    @DisplayName("Test equivalence of loaded data from different file formats.")
    public void testEquivalentsOfLoadingDifferentFormats(
            File geoJsonFile, File parquetFile, DataReaderInitializer graphDataInitializer) {
        putFileInMimoContainer(parquetFile);
        BaseGraph geoJsonGraph = createGraph(geoJsonFile, createOvertureFileDataInitializer());
        BaseGraph parquetGraph = createGraph(parquetFile, createOvertureFileDataInitializer());
        BaseGraph awsMinioGraph = createGraph(null, graphDataInitializer);

        assertTrue(assertEqualsGraphs(geoJsonGraph, parquetGraph));
        assertTrue(assertEqualsGraphs(geoJsonGraph, awsMinioGraph));

        cleanEnvironment(parquetFile.getName());
    }

    private DataReaderInitializer createOvertureFileDataInitializer() {
        return (baseGraph, osmParsers, config) -> new OvertureReader(baseGraph);
    }

    private void putFileInMimoContainer(File file) {
        minioClient.putObject(
                PutObjectRequest.builder().bucket(minioBucket).key(file.getName()).build(),
                RequestBody.fromFile(file));
    }

    private void cleanEnvironment(String fileName) {
        minioClient.deleteObject(
                DeleteObjectRequest.builder().bucket(minioBucket).key(fileName).build());
    }

    private boolean assertEqualsGraphs(BaseGraph g1, BaseGraph g2) {
        if (g1.getEdges() != g2.getEdges()) return false;
        if (g1.getNodes() != g2.getNodes()) return false;

        if (!assertEqualsBoundsGraphs(g1, g2)) return false;

        Map<String, List<PointList>> edgesMap = new HashMap<>();
        AllEdgesIterator it1 = g1.getAllEdges();
        while (it1.next()) {
            String key = edgeKey(it1);
            edgesMap
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(it1.fetchWayGeometry(FetchMode.ALL));
        }

        AllEdgesIterator it2 = g2.getAllEdges();
        while (it2.next()) {
            String key = edgeKey(it2);
            List<PointList> list = edgesMap.get(key);

            if (list == null || list.isEmpty()) return false;

            PointList p2 = it2.fetchWayGeometry(FetchMode.ALL);
            Iterator<PointList> iter = list.iterator();
            boolean isMatched = false;
            while (iter.hasNext()) {
                PointList p = iter.next();
                if (pointListsEqualIgnoreDirection(p, p2)) {
                    isMatched = true;
                    iter.remove();
                    if (list.isEmpty()) edgesMap.remove(key);
                    break;
                }
            }

            if (!isMatched) return false;
        }

        return edgesMap.isEmpty();
    }

    private String edgeKey(AllEdgesIterator it) {
        int a = it.getBaseNode();
        int b = it.getAdjNode();
        int min = Math.min(a, b);
        int max = Math.max(a, b);

        long dist = Math.round(it.getDistance() * 1000);

        return min + "_" + max + "_" + dist;
    }

    private boolean assertEqualsBoundsGraphs(BaseGraph g1, BaseGraph g2) {
        BBox b1 = g1.getBounds();
        BBox b2 = g2.getBounds();

        return doubleEquals(b1.minLon, b2.minLon)
                && doubleEquals(b1.maxLon, b2.maxLon)
                && doubleEquals(b1.minLat, b2.minLat)
                && doubleEquals(b1.maxLat, b2.maxLat)
                && doubleEquals(b1.maxEle, b2.maxEle)
                && doubleEquals(b1.minEle, b2.minEle);
    }

    private boolean doubleEquals(double a, double b) {
        return Double.compare(a, b) == 0;
    }

    private boolean pointListsEqualIgnoreDirection(PointList p1, PointList p2) {
        if (p1.size() != p2.size()) return false;

        if (p1.equals(p2)) return true;

        for (int i = 0; i < p1.size(); i++) {
            int revIndex = p2.size() - 1 - i;

            if (!doubleEquals(p1.getLat(i), p2.getLat(revIndex))) return false;
            if (!doubleEquals(p1.getLon(i), p2.getLon(revIndex))) return false;

            if (p1.is3D() && p2.is3D()) {
                if (!doubleEquals(p1.getEle(i), p2.getEle(revIndex))) return false;
            }
        }

        return true;
    }

}