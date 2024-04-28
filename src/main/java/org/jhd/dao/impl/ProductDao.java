package org.jhd.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;
import org.jhd.dao.Dao;
import org.jhd.dto.ProductDto;
import org.jhd.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

//https://www.baeldung.com/java-dao-pattern
//In some scenarios we don't always need to use the DAO pattern’s functionality with JPA as the pattern becomes just another layer of
//abstraction and complexity on top of the one provided by JPA’s entity manager.
//But sometimes we just want to expose to our application only a few domain-specific methods of the entity manager’s API. The DAO pattern
//has its place in such cases.

//@Transactional only works when we call the method throw proxy
//@Transactional
public class ProductDao implements Dao<Product> {
    private EntityManagerFactory entityManagerFactory;

    //an EntityManager contains a persistence context, that will track everything it reads from/writes to db.
    //EntityManager per transaction - to avoid bloated memory, we should use a new one per transaction
    //or clear it at some point
    //EntityManger per class - if we read the same object through two different EntityManager we will get
    //different objects back

    //EntityManager per class
    private EntityManager entityManager;

    public ProductDao(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Override
    public Optional<Product> get(long id) {
        //EntityManager per transaction
        EntityManager em = entityManagerFactory.createEntityManager();
        return Optional.ofNullable(em.find(Product.class, id));
    }

    @Override
    public List<Product> getAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        return em.createQuery("SELECT p FROM Product p", Product.class)
                .getResultList();
    }

    //https://stackoverflow.com/questions/1069992/jpa-entitymanager-why-use-persist-over-merge
    @Override
    public void save(Product product) {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(product); //adds the instance of entity to context - NOT AN INSERT QUERY//read
            transaction.commit(); //at this point decides whether to send INSERT query or not
        } catch(RuntimeException e) {
            transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    //https://blog.akquinet.de/2020/10/05/jpa-pitfalls-13-entitymanager-merge-result/#:~:text=The%20current%20persistence%20context%20already,still%20in%20the%20detached%20state.
    //updates the instance of entity in context - involves SELECT query before UPDATE
    //merge on a detached object loads the persistent object with the id of the passed detached object
    //- if no record is found in db, inserts the passed object making it persistent
    //- if an object is found, transfers all the fields from that detached object into the persistent object
    @Override
    public Product updateWithMergeDetached(Product detachedProduct, ProductDto productDto) {
        //EntityManager per transaction
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            //invoking em.merge(user) before setters or after setters doesn't matter. The persistent entity
            //will be updated when method invocation finishes - both #1 and #2 produce same result
            //#1
            // detachedProduct.setName(productDto.name());
            // detachedProduct.setPrice(productDto.price());
            //returns the persistent object
            Product persistentProduct = em.merge(detachedProduct);
            //#2
            persistentProduct.setName(productDto.name());
            persistentProduct.setPrice(productDto.price());

            //flush - changes are reflected in database after encountering flush but the transaction
            //is still active so the changes are still in transaction and can be rollback
            //entityManager.flush();

            //commit - changes are made into the database & transaction ends there
            transaction.commit();
            return persistentProduct;
        } catch(RuntimeException e) {
            transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    //We can fetch and modify our objects in the same transaction (i.e. with the same entityManager)
    //like below but it means a lot more database access and this strategy must generally be combined
    //with a second-level cache for performance reason. In this case we won't have to call merge
    @Override
    public Product updateWithGetPersistent(Product detachedProduct, ProductDto productDto) {
        //EntityManager per transaction
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        //first get the persistent entity with the same id as the detached one
        Product persistentProduct = em.find(Product.class, detachedProduct.getId());

        //update the persistent entity
        try {
            transaction.begin();
            persistentProduct.setName(productDto.name());
            persistentProduct.setPrice(productDto.price());
            transaction.commit();
            return persistentProduct;
        } catch(RuntimeException e) {
            transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Product detachedProduct) {
        //EntityManager per transaction
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        //first get the persistent entity product with same id as the detached product
        //passed in the argument
        Product persistentProduct = em.find(Product.class, detachedProduct.getId());

        //delete the product
        try {
            transaction.begin();
            em.remove(persistentProduct); //removes the instance of entity from context
            transaction.commit();
        } catch(RuntimeException e) {
            transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
//    @Transactional(value = Transactional.TxType.SUPPORTS)
    public void deleteEMPerClass(Product product) {
        //EntityManager per class
        EntityTransaction transaction = entityManager.getTransaction();
        System.out.println("entityManager " + entityManager);
        try {
            transaction.begin();
            entityManager.remove(product);
            transaction.commit();
        } catch(RuntimeException e) {
            transaction.rollback();
            throw e;
        }
    }
    @Override
    public void saveEMPerClass(Product product) {
        //EntityManager per class
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(product);
            transaction.commit();
        } catch(RuntimeException e) {
            transaction.rollback();
            throw e;
        }
    }
//    @Override
//    public void delete(Product product) {
//        Product deletedProduct = executeInsideTransaction(entityManager -> {
//            entityManager.remove(product);
//            return null;
//        });
//    }

    private <R> R executeInsideTransaction(Function<EntityManager, R> transactionFunction) {
        //EntityManager per transaction
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
//        R returnValue;
        try {
            transaction.begin();
            R returnValue = transactionFunction.apply(em);
            transaction.commit();
            return returnValue;
        } catch(RuntimeException e) {
            transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

//    @Override
//    public void delete(Product product) {
//        executeInsideTransaction(entityManager -> entityManager.remove(product));
//    }
//    private void executeInsideTransaction(Consumer<EntityManager> action) {
//        //EntityManager per class
//        EntityTransaction tx = entityManager.getTransaction();
//        try {
//            tx.begin();
//            action.accept(entityManager);
//            tx.commit();
//        }
//        catch (RuntimeException e) {
//            tx.rollback();
//            throw e;
//        }
//    }

}
