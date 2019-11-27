package pl.edu.agh.eksploracja;

import pl.edu.agh.eksploracja.domain.Scooter;
import pl.edu.agh.eksploracja.domain.ScooterToLocation;
import pl.edu.agh.eksploracja.services.ScooterService;
import pl.edu.agh.eksploracja.services.ScooterToLocationService;

import java.util.Iterator;

public class MergeRelationsUtil {
    private ScooterToLocationService scooterToLocationService = new ScooterToLocationService();
    private ScooterService scooterService = new ScooterService();

    public void merge() {
        Iterable<Scooter> scooters = scooterService.findAll();

        for (Scooter s : scooters) {
            System.out.println("Looking for broken relations in: " + s.getCarId());
            merge(scooterToLocationService.getLocationTypesForScooterOrderedByDate(s.getCarId()));
        }
        System.out.println("Done");

    }

    private void merge(Iterable<ScooterToLocation> locationTypesForScooterOrderedByDate) {
        Iterator<ScooterToLocation> iterator = locationTypesForScooterOrderedByDate.iterator();
        if (iterator.hasNext()) {
            ScooterToLocation prev = iterator.next();
            while (iterator.hasNext()) {
                ScooterToLocation actual = iterator.next();
                if (isInConditionToDetach(prev, actual)) {
                    if (prev.getTo().before(actual.getTo())) {
                        prev.setTo(actual.getTo());
                    }
                    prev.setFuelLevel(actual.getFuelLevel());
                    scooterToLocationService.detach(actual);
                    scooterToLocationService.delete(actual.getId());
                } else {
                    prev = actual;
                }
            }
        }
    }

    private boolean isInConditionToDetach(ScooterToLocation prev, ScooterToLocation actual) {
        if (prev.getLocation().equals(actual.getLocation())) {
            return actual.getFrom().getTime() - prev.getTo().getTime() < 1500 ||
                    (actual.getFrom().getTime() - prev.getTo().getTime() < 2500 * 60 && actual.getFuelLevel().equals(prev.getFuelLevel())
                            && actual.getExactLat().equals(prev.getExactLat()) && actual.getExactLon().equals(prev.getExactLon()));
        }
        return false;
    }

    public static void main(String[] args) {
        MergeRelationsUtil util = new MergeRelationsUtil();
        util.merge();
    }
}
