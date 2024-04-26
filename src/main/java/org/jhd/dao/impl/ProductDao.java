package org.jhd.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.jhd.dao.Dao;
import org.jhd.entity.Product;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProductDao implements Dao<Product> {
    private EntityManager entityManager;

    public ProductDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Product> get(long id) {
        return Optional.ofNullable(entityManager.find(Product.class, id));
    }

    @Override
    public List<Product> getAll() {
        return entityManager.createQuery("SELECT p from Product p", Product.class).getResultList();
    }

    @Override
    public void save(Product product) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            boolean success = false;
            transaction.begin();
            try {
                entityManager.persist(product); //adds the instance of entity to context - NOT AN INSERT QUERY//read
                success = true;
            } finally {
                if(success) {
                    transaction.commit(); //at this point decides whether to send INSERT query or not
                } else {
                    transaction.rollback();
                }
            }
        } catch(RuntimeException e) {
            transaction.rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void update(Product product, String[] params) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            boolean success = false;
            transaction.begin();
            try {
                product.setName(Objects.requireNonNull(params[0], "Name cannot be null"));
                entityManager.merge(product); //updates the instance of entity in context
                success = true;
            } finally {
                if(success) {
                    transaction.commit(); //at this point decides whether to send INSERT query or not
                } else {
                    transaction.rollback();
                }
            }
        } catch(RuntimeException e) {
            transaction.rollback();
            throw e;
        } finally {
            entityManager.close();
        }

    }

    @Override
    public void delete(Product product) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            boolean success = false;
            transaction.begin();
            try {
                entityManager.remove(product); //removes the instance of entity from context
                success = true;
            } finally {
                if(success) {
                    transaction.commit(); //at this point decides whether to send INSERT query or not
                } else {
                    transaction.rollback();
                }
            }
        } catch(RuntimeException e) {
            transaction.rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }
}
