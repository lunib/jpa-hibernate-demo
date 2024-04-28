package org.jhd.dao;

import org.jhd.dto.ProductDto;

import java.util.List;
import java.util.Optional;

public interface Dao<T, V> {
    Optional<T> get(long id);

    List<T> getAll();

    void save(T t);

    T updateWithMergeDetached(T t, V v);

    T updateWithGetPersistent(T t, V v);

    void delete(T t);

    void deleteEMPerClass(T t);

    void saveEMPerClass(T t);
}
