package pl.edu.agh.eksploracja.domain;

import lombok.*;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.typeconversion.DateString;

import java.util.Date;

@RelationshipEntity(type = ScooterToLocation.TYPE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ScooterToLocation {
    static final String TYPE = "STAYS_AT";
    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    Scooter scooter;

    @EndNode
    Location location;

    @DateString
    Date from;

    @DateString
    Date to;

    Integer fuelLevel;

    Double exactLat;
    Double exactLon;

    @Transient
    String fromTimestamp;

    @Transient
    String toTimestamp;
}
