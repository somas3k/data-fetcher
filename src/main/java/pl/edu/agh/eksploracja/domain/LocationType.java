package pl.edu.agh.eksploracja.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Objects;

@NodeEntity
@NoArgsConstructor
@Setter
@Getter
public class LocationType implements Entity {
    @Id @GeneratedValue
    Long id;

    @Index(unique = true)
    String name;

    public LocationType(String name) {
        this.name = name;
    }

    @Override
    public Long getEntityId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationType that = (LocationType) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
