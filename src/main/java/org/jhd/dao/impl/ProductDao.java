package org.jhd.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
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
public class ProductDao implements Dao<Product, ProductDto> {
    private final EntityManagerFactory emf;

    //an EntityManager contains a persistence context, that will track everything it reads from/writes to db.
    //EntityManager per transaction - to avoid bloated memory, we should use a new one per transaction
    //or clear it at some point
    //EntityManger per class - if we read the same object through two different EntityManager we will get
    //different objects back

    //EntityManager per class - not needed for rest of the methods
    private final EntityManager emPerClass;

    public ProductDao(EntityManagerFactory entityManagerFactory) {
        this.emf = entityManagerFactory;
        this.emPerClass = entityManagerFactory.createEntityManager();
    }

    @Override
    public Optional<Product> get(long id) {
        EntityManager em = emf.createEntityManager();
        return Optional.ofNullable(em.find(Product.class, id));
    }

    @Override
    public List<Product> getAll() {
        EntityManager em = emf.createEntityManager();
        return em.createQuery("SELECT p FROM Product p", Product.class)
                .getResultList();
    }

    @Override
    public void save(Product product) {
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction tx = em.getTransaction();
//        try {
//            tx.begin();
//            em.persist(product); //adds the instance of entity to context - NOT AN INSERT QUERY//read
//            tx.commit(); //at this point decides whether to send INSERT query or not
//        } catch(RuntimeException e) {
//            tx.rollback();
//            throw e;
//        } finally {
//            em.close();
//        }
        executeInsideTransaction(em -> em.persist(product));
    }

    //https://stackoverflow.com/questions/1069992/jpa-entitymanager-why-use-persist-over-merge

    //https://blog.akquinet.de/2020/10/05/jpa-pitfalls-13-entitymanager-merge-result/#:~:text=The%20current%20persistence%20context%20already,still%20in%20the%20detached%20state.
    //updates the instance of entity in context - involves SELECT query before UPDATE
    //merge on a detached object loads the persistent object with the id of the passed detached object
    //- if no record is found in db, inserts the passed object making it persistent
    //- if an object is found, transfers all the fields from that detached object into the persistent object
//    @Override
//    public Product updateWithMergeDetached(Product detachedProduct, ProductDto productDto) {
//        //EntityManager per transaction
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction tx = em.getTransaction();
//        try {
//            tx.begin();
//            //invoking em.merge(user) before setters or after setters doesn't matter. The persistent entity
//            //will be updated when method invocation finishes - both #1 and #2 produce same result
//            //#1
//            // detachedProduct.setName(productDto.name());
//            // detachedProduct.setPrice(productDto.price());
//            //returns the persistent object
//            Product persistentProduct = em.merge(detachedProduct);
//            //#2
//            persistentProduct.setName(productDto.name());
//            persistentProduct.setPrice(productDto.price());
//
//            //flush - changes are reflected in database after encountering flush but the transaction
//            //is still active so the changes are still in transaction and can be rollback
//            //entityManager.flush();
//
//            //commit - changes are made into the database & transaction ends there
//            tx.commit();
//            return persistentProduct;
//        } catch(RuntimeException e) {
//            tx.rollback();
//            throw e;
//        } finally {
//            em.close();
//        }
//    }
    @Override
    public Product updateWithMergeDetached(Product detachedProduct, ProductDto productDto) {
        return executeInsideTransactionWithFn(em -> {
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

            return persistentProduct;
        });
    }

    //We can fetch and modify our objects in the same transaction (i.e. with the same entityManager)
    //like below but it means a lot more database access and this strategy must generally be combined
    //with a second-level cache for performance reason. In this case we won't have to call merge
    @Override
    public Product updateWithGetPersistent(Product detachedProduct, ProductDto productDto) {
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction tx = em.getTransaction();
//        //update the persistent entity
//        try {
//            tx.begin();
//            persistentProduct.setName(productDto.name());
//            persistentProduct.setPrice(productDto.price());
//            tx.commit();
//            return persistentProduct;
//        } catch(RuntimeException e) {
//            tx.rollback();
//            throw e;
//        } finally {
//            em.close();
//        }
        return executeInsideTransactionWithFn(em -> {
            //first get the persistent entity with the same id as the detached one
            Product persistentProduct = em.find(Product.class, detachedProduct.getId());
            persistentProduct.setName(productDto.name());
            persistentProduct.setPrice(productDto.price());
            return persistentProduct;
        });
    }

//    @Override
//    public void delete(Product detachedProduct) {
//        //EntityManager per transaction
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction tx = em.getTransaction();
//
//        //first get the persistent entity product with same id as the detached product
//        //passed in the argument
//        Product persistentProduct = em.find(Product.class, detachedProduct.getId());
//
//        //delete the product
//        try {
//            tx.begin();
//            em.remove(persistentProduct); //removes the instance of entity from context
//            tx.commit();
//        } catch(RuntimeException e) {
//            tx.rollback();
//            throw e;
//        } finally {
//            em.close();
//        }
//    }

    @Override
    public void delete(Product detachedProduct) {
        executeInsideTransaction(entityManager -> {
            //first get the persistent entity product with same id as the detached product
            //passed in the argument and then remove it

            //https://stackoverflow.com/questions/1607532/when-to-use-entitymanager-find-vs-entitymanager-getreference-with-jpa

            //issues a SELECT query to get the actual object from db - not necessary in this scenario
            //Product persistentProduct = entityManager.find(Product.class, detachedProduct.getId());

            //does not issue a SELECT query to get the actual object from db, only gets a proxy object
            //with the PK initialised. This is enough in this scenario as we won't be accessing the fields
            //of the object (which is issue a SELECT query to load the properties of the object), we are
            //just removing it so just the proxy object containing the PK reference is enough
            Product persistentProduct = entityManager.getReference(Product.class, detachedProduct.getId());

            entityManager.remove(persistentProduct);
        });
    }

    //EntityManager per transaction
    //centralise boilerplate code
    private void executeInsideTransaction(Consumer<EntityManager> operation) {
        //context is empty
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        //try-with-resource block - no need to have finally block to close em
        try (em) {
            tx.begin();
            operation.accept(em);
            //flush - changes are reflected in database after encountering flush but the transaction
            //is still active so the changes are still in transaction and can be rollback
            //entityManager.flush();
            //commit - changes are made into the database & transaction ends there
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }

    //EntityManager per transaction
    private <R> R executeInsideTransactionWithFn(Function<EntityManager, R> operation) {
        //context is empty
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try (em) {
            tx.begin();
            R returnValue = operation.apply(em);
            tx.commit();
            return returnValue;
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }



    //EntityManager per class
    private void executeInsideTransactionEMPerClass(Consumer<EntityManager> action) {
        //context already exists
        EntityTransaction tx = emPerClass.getTransaction();
        try {
            tx.begin();
            action.accept(emPerClass);
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }    @Override
//    @Transactional(value = Transactional.TxType.SUPPORTS)
    public void deleteEMPerClass(Product product) {
        executeInsideTransactionEMPerClass(em -> em.remove(product));
    }
    //EntityManager per class
    @Override
    public void saveEMPerClass(Product product) {
        executeInsideTransactionEMPerClass(em -> em.persist(product));
    }
}
