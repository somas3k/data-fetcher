package pl.edu.agh.eksploracja.services;

import pl.edu.agh.eksploracja.domain.LocationType;

public class LocationTypeService extends GenericService<LocationType> {

    @Override
    Class<LocationType> getEntityType() {
        return LocationType.class;
    }
}
