package pl.edu.agh.eksploracja.services;

import pl.edu.agh.eksploracja.domain.LocationType;

import java.util.Collections;
import java.util.List;

public class LocationTypeService extends GenericService<LocationType> {

    @Override
    Class<LocationType> getEntityType() {
        return LocationType.class;
    }


}
