package pl.edu.agh.eksploracja.services;

import pl.edu.agh.eksploracja.domain.Entity;

interface Service<T extends Entity> {

    Iterable<T> findAll();

    T find(Long id);

    void delete(Long id);

    T createOrUpdate(T object);

}
