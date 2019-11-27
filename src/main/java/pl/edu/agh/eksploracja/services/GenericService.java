package pl.edu.agh.eksploracja.services;

import org.neo4j.ogm.session.Session;
import pl.edu.agh.eksploracja.Neo4jSessionFactory;
import pl.edu.agh.eksploracja.domain.Entity;

import java.util.Collection;

abstract class GenericService<T extends Entity> implements Service<T> {

    private static final int DEPTH_LIST = 0;
    private static final int DEPTH_ENTITY = 1;
    protected Session session = Neo4jSessionFactory.getInstance().getNeo4jSession();

    @Override
    public Collection<T> findAll() {
        return session.loadAll(getEntityType(), DEPTH_LIST);
    }

    @Override
    public T find(Long id) {
        return session.load(getEntityType(), id, DEPTH_ENTITY);
    }

    @Override
    public void delete(Long id) {
        session.delete(session.load(getEntityType(), id));
    }

    @Override
    public T createOrUpdate(T entity) {
        session.save(entity, DEPTH_ENTITY);
        return find(entity.getEntityId());
    }

    abstract Class<T> getEntityType();
}
