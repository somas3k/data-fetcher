package pl.edu.agh.eksploracja.services;

import org.neo4j.ogm.model.Result;
import pl.edu.agh.eksploracja.domain.ScooterToLocation;

import java.util.Collections;

public class ScooterToLocationService extends GenericService<ScooterToLocation> {

    @Override
    Class<ScooterToLocation> getEntityType() {
        return ScooterToLocation.class;
    }

    public Iterable<ScooterToLocation> getLocationTypesForScooterOrderedByDate(Integer carId) {
        return session.query(getEntityType(),
                "MATCH (n:Scooter {carId:$param1})-[t:STAYS_AT]->(l:Location) RETURN n,t,l order by t.from", Collections.singletonMap("param1", carId));
    }

    public boolean detach(ScooterToLocation s) {
        return session.detachRelationshipEntity(s.getId());
    }
}
