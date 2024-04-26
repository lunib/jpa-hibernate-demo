package org.jhd;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.jhd.entity.Product;
import org.jhd.persistence.CustomPersistenceUnitInfo;

import java.util.HashMap;

public class Application {
    public static void main(String[] args) {
        //Use persistence.xml
        //EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-hibernate-persistence-unit");
        //Use PersistenceUnitInfo - create persistence unit detail programmatically
        EntityManagerFactory emf = new HibernatePersistenceProvider()
                .createContainerEntityManagerFactory(new CustomPersistenceUnitInfo(), new HashMap());

        EntityManager em = emf.createEntityManager(); //represents the context
        try {
            EntityTransaction transaction = em.getTransaction();
            boolean success = false;
            transaction.begin();
            try {
//                Product product = new Product();
//                product.setName("Biscuit");
//                em.persist(product); //adds the instance of entity to context - NOT AN INSERT QUERY//read

                Product product1 = em.find(Product.class, 1l);
                System.out.println("before update" + product1);
                product1.setName("Cake");
                success = true;
            } finally {
                if(success) {
                    transaction.commit(); //at this point decides whether to send INSERT query or not
                } else {
                    transaction.rollback();
                }
            }
        } finally {
            em.close();
        }
    }
}
