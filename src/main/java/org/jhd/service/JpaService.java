package org.jhd.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Function;

//make this class singleton
public class JpaService {
    private static JpaService instance;

    EntityManagerFactory emf;

    private JpaService() {
        emf = Persistence.createEntityManagerFactory("jpa-hibernate-persistence-unit");
    }

    public static synchronized JpaService getInstance() {
        return (instance == null) ? instance = new JpaService() : instance;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public void shutdown() {
        if (emf != null) {
            emf.close();
        }
    }

    public <T> T runInTransaction(Function<EntityManager, T> function) {
        EntityManager em = emf.createEntityManager(); //represents the context
        EntityTransaction transaction = em.getTransaction();
        boolean success = false;
        transaction.begin();
        try {
            T returnValue = function.apply(em);
            success = true;
            return returnValue;
        } finally {
            if(success) {
                transaction.commit();
            } else {
                transaction.rollback();
            }
        }
    }
}
