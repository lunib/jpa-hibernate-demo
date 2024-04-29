package org.jhd;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.jhd.dto.ProductDto;
import org.jhd.entity.Product;
import org.jhd.persistence.CustomPersistenceUnitInfo;
import org.jhd.service.Service;
import org.jhd.service.impl.ProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application {
    public static void main(String[] args) {
        //Use persistence.xml
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-hibernate-persistence-unit");

        //Use PersistenceUnitInfo class - create persistence unit detail programmatically
        String persistenceUnitName = "jpa-hibernate-persistence-unit";
        Map<String, String> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "create"); //in production value should be 'none'
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.highlight_sql", "true");
        props.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");
        EntityManagerFactory emf = new HibernatePersistenceProvider()
                .createContainerEntityManagerFactory(new CustomPersistenceUnitInfo(persistenceUnitName), props);

        try {
            //create service
            Service<Product, ProductDto> productService = new ProductService(emf);

            //create product
            Product product1 = new Product();
            product1.setName("Biscuit");
            product1.setPrice(8.37);
            productService.save(product1);

            Product product2 = new Product();
            product2.setName("Shoes");
            product2.setPrice(49.99);
            productService.save(product2);

            Product product3 = new Product();
            product3.setName("Kettle");
            product3.setPrice(35.0);
            productService.save(product3);

            List<Product> products = productService.getAll();
            products.forEach(System.out::println);

            ProductDto productDto1 = new ProductDto("cake", 10.50);
            productService.updateWithGetPersistent(product1, productDto1);

            ProductDto productDto2 = new ProductDto("boots", 85.99);
            productService.updateWithMergeDetached(product2, productDto2);

//            productService.delete(product3);
        } finally {
            emf.close();
        }
    }
}
