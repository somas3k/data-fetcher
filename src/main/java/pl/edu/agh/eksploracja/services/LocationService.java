package pl.edu.agh.eksploracja.services;

import pl.edu.agh.eksploracja.domain.Location;

public class LocationService extends GenericService<Location> {
    @Override
    Class<Location> getEntityType() {
        return Location.class;
    }
}
