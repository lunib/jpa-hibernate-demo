package org.jhd.service;

import org.jhd.dto.ProductDto;
import org.jhd.entity.Product;

import java.util.List;
import java.util.Optional;

public interface Service<T> {
    Optional<T> get(Long id);

    List<T> getAll();

    void save(T t);

    Product updateWithMergeDetached(Product product, ProductDto productDto);

    Product updateWithGetPersistent(Product product, ProductDto productDto);

    void delete(Product product);
}