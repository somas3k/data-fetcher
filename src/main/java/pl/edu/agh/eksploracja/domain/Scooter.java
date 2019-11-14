package pl.edu.agh.eksploracja.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NodeEntity
@NoArgsConstructor
@Setter
@Getter
public class Scooter implements Entity {
    @Id @GeneratedValue
    Long id;
    @Index(unique = true)
    Integer carId;
    String title;
    @Transient
    Double lat;
    @Transient
    Double lon;
    @JsonProperty(value = "licencePlate")
    String name;
    @Transient
    Integer fuelLevel;
    Integer vehicleStateId;
    Integer vehicleTypeId;
    String pricingTime;
    String pricingParking;
    @Transient
    Integer reservationState;
    @Transient
    String address;
    @Transient
    String zipCode;
    @Transient
    String city;
    @Transient
    Integer locationId;

    @Relationship(type = ScooterToLocation.TYPE)
    Set<ScooterToLocation> scooterToLocations = new HashSet<>();

    @Override
    public Long getEntityId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scooter scooter = (Scooter) o;
        return carId.equals(scooter.carId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(carId);
    }
}
