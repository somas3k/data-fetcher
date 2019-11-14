package pl.edu.agh.eksploracja.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.neo4j.ogm.annotation.*;
import pl.edu.agh.eksploracja.DataLoaderToNeo4j;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NodeEntity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location implements Entity {
    public transient static final Location DEFAULT_LOCATION = new Location();
    @Id @GeneratedValue
    Long id;

    @Index(unique = true)
    @JsonProperty(value = "place_id")
    String placeId;

    Double lat;
    Double lng;

    String name;
    String address;
    Float rating;
    String website;
    @JsonProperty(value = "user_ratings_total")
    Integer userRatingsTotal;

    @Relationship(type = "TYPE_OF")
    @JsonDeserialize(using = DataLoaderToNeo4j.LocationTypeSetDeserializer.class)
    Set<LocationType> types = new HashSet<>();

    @Override
    public Long getEntityId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return placeId.equals(location.placeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeId);
    }
}
