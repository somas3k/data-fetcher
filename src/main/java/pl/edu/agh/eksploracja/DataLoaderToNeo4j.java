package pl.edu.agh.eksploracja;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.eksploracja.domain.Location;
import pl.edu.agh.eksploracja.domain.LocationType;
import pl.edu.agh.eksploracja.domain.Scooter;
import pl.edu.agh.eksploracja.domain.ScooterToLocation;
import pl.edu.agh.eksploracja.services.LocationService;
import pl.edu.agh.eksploracja.services.LocationTypeService;
import pl.edu.agh.eksploracja.services.ScooterService;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static pl.edu.agh.eksploracja.DistanceCalculator.distance;
import static pl.edu.agh.eksploracja.domain.Location.DEFAULT_LOCATION;

public class DataLoaderToNeo4j {
    private Set<Location> locations;
    private Set<LocationType> locationTypes = new HashSet<>();
    private Map<Scooter, ScooterToLocation> buffer = new HashMap<>();
    private Map<Integer, Scooter> carIdToSavedScooter = new HashMap<>();

    private LocationService locationService = new LocationService();
    private LocationTypeService locationTypeService = new LocationTypeService();
    private ScooterService scooterService = new ScooterService();

    private static DataLoaderToNeo4j loader = new DataLoaderToNeo4j();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH:mm:ss", Locale.forLanguageTag("pl"));


    public static void main(String[] args) {
        loader.loadLocations();

        loader.loadScootersIfSaved();

        File folder = new File("./hive/merged");
        long start = System.currentTimeMillis();
        List<File> files = Arrays.asList(Objects.requireNonNull(folder.listFiles()));
        files.sort(Comparator.comparing(file -> LocalDateTime.parse(getFirstDateFromFileName(file), FORMATTER)));
        ObjectMapper mapper = new ObjectMapper();
        for (File f : files) {
            System.out.println("Parsing file " + f.getName() + "...");
            try (InputStream s = new BufferedInputStream(new FileInputStream(f))) {
                mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                Map<String, List<Scooter>> values = mapper.readValue(s, new TypeReference<Map<String, List<Scooter>>>() {
                });
                values.forEach(loader::load);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Saving buffer...");
        loader.saveBufferToDb();
        System.out.println("Ending...");
        long timeTaken = System.currentTimeMillis() - start;
        System.out.println("Time to load data: " + (double) timeTaken / 60000.0 + " minutes." );
    }

    private void loadScootersIfSaved() {
        Collection<Scooter> scooters = scooterService.findAll();
        if (!scooters.isEmpty()) {
            scooters.forEach(scooter -> carIdToSavedScooter.put(scooter.getCarId(), scooter));
        }
    }

    private static String getFirstDateFromFileName(File file1) {
        return file1.getName().split(" ")[0];
    }

    private void load(String timestamp, List<Scooter> scooters) {
        Map<Scooter, Variables> scooterToVars = new HashMap<>();
        scooters.forEach(scooter -> scooterToVars.put(scooter, new Variables(scooter)));
        handleScootersInUse(scooters);
        for (Scooter s : scooters) {
            if (carIdToSavedScooter.containsKey(s.getCarId())) {
                s = carIdToSavedScooter.get(s.getCarId());
            }
            Variables variables = scooterToVars.get(s);
            Location nearest = getNearestLocation(variables.latLon).orElse(Pair.of(0.0, DEFAULT_LOCATION)).getRight();
            if (!buffer.containsKey(s)) {
                ScooterToLocation relation = ScooterToLocation.builder()
                        .fromTimestamp(timestamp)
                        .toTimestamp(timestamp)
                        .location(nearest)
                        .scooter(s)
                        .fuelLevel(variables.fuelLevel)
                        .exactLat(variables.latLon.getLeft())
                        .exactLon(variables.latLon.getRight())
                        .build();
                buffer.put(s, relation);
            } else {
                ScooterToLocation scooterToLocation = buffer.get(s);
                if (scooterToLocation.getLocation().equals(nearest)) {
                    scooterToLocation.setToTimestamp(timestamp);
                } else {
                    handleLocationChangeBetweenNextTimestamps(s, timestamp, nearest, variables);
                }
            }
        }
    }

    private static class Variables {
        Pair<Double, Double> latLon;
        Integer fuelLevel;
        Integer reservationState;

        Variables(Scooter s) {
            this.latLon = Pair.of(s.getLat(), s.getLon());
            this.fuelLevel = s.getFuelLevel();
            this.reservationState = s.getReservationState();
        }
    }

    private void saveBufferToDb() {
        buffer.forEach((scooter, scooterToLocation) -> {
            setDates(scooterToLocation);
            scooter.getScooterToLocations().add(scooterToLocation);
            scooterService.createOrUpdate(scooter);
        });
    }

    private void handleLocationChangeBetweenNextTimestamps(Scooter s, String timestamp, Location nearest, Variables v) {
        ScooterToLocation sl = buffer.get(s);
        setDates(sl);
        buffer.remove(s);
        s.getScooterToLocations().add(sl);
        s = scooterService.createOrUpdate(s);
        carIdToSavedScooter.put(s.getCarId(), s);
        ScooterToLocation relation = ScooterToLocation.builder()
                .fromTimestamp(timestamp)
                .toTimestamp(timestamp)
                .location(nearest)
                .scooter(s)
                .exactLat(v.latLon.getLeft())
                .exactLon(v.latLon.getRight())
                .fuelLevel(v.fuelLevel)
                .build();
        buffer.put(s, relation);
    }

    private void handleScootersInUse(List<Scooter> scooters) {
        Set<Scooter> toSave = buffer.keySet().stream().filter(scooter -> !scooters.contains(scooter)).collect(Collectors.toSet());
        for (Scooter s : toSave) {
            ScooterToLocation sl = buffer.get(s);
            buffer.remove(s);
            setDates(sl);
            s.getScooterToLocations().add(sl);
            s = scooterService.createOrUpdate(s);
            carIdToSavedScooter.put(s.getCarId(), s);
        }
    }

    private void setDates(ScooterToLocation sl) {
        sl.setFrom(new Date(Long.parseLong(sl.getFromTimestamp())));
        sl.setTo(new Date(Long.parseLong(sl.getToTimestamp())));
    }

    private Optional<Pair<Double, Location>> getNearestLocation(Pair<Double, Double> scooterCoords) {
        return locations.stream().map(loc -> Pair.of(distance(scooterCoords.getLeft(), scooterCoords.getRight(), loc), loc)).min(Comparator.comparingDouble(Pair::getLeft));
    }

    private void loadLocations() {
        Collection<Location> locations = locationService.findAll();
        if (locations.isEmpty()) {
            File f = new File("poi.txt");

            try (InputStream s = new BufferedInputStream(new FileInputStream(f))) {
                ObjectMapper mapper = new ObjectMapper();
                this.locations = mapper.readValue(s, new TypeReference<Set<Location>>() {
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
            putLocationsToDatabase();
        } else {
            this.locations = new HashSet<>(locations);
        }
    }

    public static class LocationTypeSetDeserializer extends JsonDeserializer<Set<LocationType>> {
        @Override
        public Set<LocationType> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            ObjectCodec oc = jsonParser.getCodec();
            JsonNode node = oc.readTree(jsonParser);
            if (node.isArray()) {
                List<String> types = new ObjectMapper().convertValue(node, new TypeReference<List<String>>() {
                });
                return types.stream().map(DataLoaderToNeo4j::checkAndSaveIfNeeded).collect(Collectors.toSet());
            }
            return new HashSet<>();
        }
    }

    private static LocationType checkAndSaveIfNeeded(String type) {
        for (LocationType t : loader.locationTypes) {
            if (t.getName().equals(type)) {
                return t;
            }
        }
        LocationType locationType = loader.locationTypeService.createOrUpdate(new LocationType(type));
        loader.locationTypes.add(locationType);
        return locationType;
    }

    private void putLocationsToDatabase() {
        locations = locations.stream().map(location ->
                locationService.createOrUpdate(location)).collect(Collectors.toSet());
    }


}
