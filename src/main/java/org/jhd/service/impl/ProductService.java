package org.jhd.service.impl;

import jakarta.persistence.EntityManagerFactory;
import org.jhd.dao.Dao;
import org.jhd.dao.impl.ProductDao;
import org.jhd.dto.ProductDto;
import org.jhd.entity.Product;
import org.jhd.service.Service;

import java.util.List;
import java.util.Optional;

public class ProductService implements Service<Product> {
    private Dao<Product> productDao;

    public ProductService(EntityManagerFactory entityManagerFactory) {
        this.productDao = new ProductDao(entityManagerFactory);
    }

    @Override
    public Optional<Product> get(Long id) {
        return productDao.get(id);
    }

    @Override
    public List<Product> getAll() {
        return productDao.getAll();
    }

    @Override
    public void save(Product product) {
        productDao.save(product);
    }

    @Override
    public Product updateWithMergeDetached(Product product, ProductDto productDto) {
        return productDao.updateWithMergeDetached(product, productDto);
    }

    @Override
    public Product updateWithGetPersistent(Product product, ProductDto productDto) {
        return productDao.updateWithGetPersistent(product, productDto);
    }

    @Override
    public void delete(Product product) {
        productDao.delete(product);
    }
}
