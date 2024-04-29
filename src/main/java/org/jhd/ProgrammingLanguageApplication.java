package org.jhd;

import jakarta.persistence.*;
import org.jhd.entity.ProgrammingLanguage;
import org.jhd.service.JpaService;

import java.util.Arrays;
import java.util.List;

//https://www.youtube.com/watch?v=UVo2SRR-ZRM - MariaDB
public class ProgrammingLanguageApplication {
    private static final JpaService jpaService = JpaService.getInstance();
    public static void main(String[] args) {
        try {
            //*****************************************************************************************
            //we can encapsulate all the boilerplate code below in jpaService.runInTransaction() method
//            EntityManagerFactory emf = jpaService.getEntityManagerFactory();
//            EntityManager em = emf.createEntityManager(); //represents the context
//            EntityTransaction transaction = em.getTransaction();
//            boolean success = false;
//            transaction.begin();
//            try {
//                em.persist(new ProgrammingLanguage("Java", 10));
//                em.persist(new ProgrammingLanguage("JavaScript", 10));
//                em.persist(new ProgrammingLanguage("Python", 10));
//                success = true;
//            } finally {
//                if(success) {
//                    transaction.commit(); //at this point decides whether to send INSERT query or not
//                } else {
//                    transaction.rollback();
//                }
//            }
            //*****************************************************************************************

//            jpaService.runInTransaction(entityManager -> {
//                Product product = new Product();
//                product.setName("Biscuit");
//                entityManager.persist(product);
//                return null;
//            });
            createProgrammingLanguages();
            printProgrammingLanguages();
        } finally {
            jpaService.shutdown();
        }
    }

    private static void createProgrammingLanguages() {
        jpaService.runInTransaction(entityManager -> {
            Arrays.stream("Java,JavaScript,C++,C#,Python,Go,Rust,PHP".split(","))
                    .map(name -> new ProgrammingLanguage(name, (int) (Math.random() * 10)))
                    //.forEach(pl -> entityManager.persist(pl));
                    .forEach(entityManager::persist);
            return null;
        });
    }

    private static void printProgrammingLanguages() {
        List<ProgrammingLanguage> programmingLanguages = jpaService.runInTransaction(entityManager -> {
           Query query =  entityManager.createQuery("""
                   SELECT pl FROM ProgrammingLanguage pl
                   WHERE pl.rating > 5
                   """,
                   ProgrammingLanguage.class);
           return query.getResultList();
        });
        programmingLanguages.stream()
                .map(pl -> pl.getName() + " : " + pl.getRating())
                .forEach(System.out::println);
    }
}