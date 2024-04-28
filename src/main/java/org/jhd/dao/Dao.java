package org.jhd.dao;

import org.jhd.dto.ProductDto;
import org.jhd.entity.Product;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {
    Optional<T> get(long id);

    List<T> getAll();

    void save(T t);

    T updateWithMergeDetached(T t, ProductDto productDto);

    T updateWithGetPersistent(T t, ProductDto productDto);

    void delete(T t);

    void deleteEMPerClass(T t);

    void saveEMPerClass(T t);
}
