package pl.edu.agh.eksploracja.services;

import pl.edu.agh.eksploracja.domain.Scooter;

public class ScooterService extends GenericService<Scooter> {
    @Override
    Class<Scooter> getEntityType() {
        return Scooter.class;
    }
}
