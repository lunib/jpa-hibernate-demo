package org.jhd.service;

import org.jhd.dto.ProductDto;
import org.jhd.entity.Product;

import java.util.List;
import java.util.Optional;

public interface Service<T> {
    Optional<T> get(Long id);

    List<T> getAll();

    void save(T t);

    T updateWithMergeDetached(T t, ProductDto productDto);

    T updateWithGetPersistent(T t, ProductDto productDto);

    void delete(T t);
}
