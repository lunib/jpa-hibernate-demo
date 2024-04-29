package org.jhd.dao.impl;

import jakarta.persistence.EntityManagerFactory;
import junit.framework.TestCase;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.jhd.dao.Dao;
import org.jhd.dto.ProductDto;
import org.jhd.entity.Product;
import org.jhd.persistence.CustomPersistenceUnitInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class ProductDaoTest extends TestCase {
    private static EntityManagerFactory emf;
    private Dao<Product, ProductDto> productDao;

    @Before
    public void init() {
        //Use persistence.xml
        String persistenceUnitName = "jpa-hibernate-persistence-unit";
        Map<String, String> props = new HashMap<>();
        props.put("hibernate.bhm2ddl.auto", "create"); //in production value should be 'none'
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.highlight_sql", "true");
        props.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");

        //Use persistence.xml
        //emf = Persistence.createEntityManagerFactory(persistenceUnitName);

        //Use PersistenceUnitInfo class - create persistence unit detail programmatically
        emf = new HibernatePersistenceProvider()
                .createContainerEntityManagerFactory(new CustomPersistenceUnitInfo(persistenceUnitName), props);
        productDao = new ProductDao(emf);
    }

    @After
    public void teardown() {
        if(emf != null) {
            emf.close();
        }
    }

    @Test
    public void testGet() {
        //save the object
        Product product = new Product();
        product.setName("Biscuit");
        product.setPrice(8.37);
        productDao.save(product);

        //get the object
        Product productFromDb = productDao.get(product.getId()).orElse(null);

        assertNotNull(productFromDb);
        assertNotNull(productFromDb.getId());
        assertEquals(productFromDb.getName(), product.getName());
    }

    @Test
    public void testGetAll() {
        //save products
        Product product1 = new Product();
        product1.setName("Biscuit");
        product1.setPrice(8.37);
        productDao.save(product1);

        Product product2 = new Product();
        product2.setName("Shoes");
        product2.setPrice(49.99);
        productDao.save(product2);

        Product product3 = new Product();
        product3.setName("Kettle");
        product3.setPrice(35.0);
        productDao.save(product3);

        //get all products
        List<Product> products = productDao.getAll();
        products.forEach(System.out::println);
        assertEquals(products.size(), 3);
    }

    @Test
    public void testSave() {
        //create product
        Product product = new Product();
        product.setName("Shampoo");
        product.setPrice(3.37);
        assertNull(product.getId());
        //save product
        productDao.save(product);
        assertNotNull(product.getId());
    }

    @Test
    public void testUpdateWithMergeDetached() {
        //create product
        Product product = new Product();
        product.setName("Chocolate");
        product.setPrice(4.50);
        //save product
        productDao.save(product);
        assertNotNull(product.getId());
        System.out.println("product " + product);

        ProductDto productDto = new ProductDto("Biscuit", 6.0);
        //update the detached entity 'product' using merge
        Product updatedProduct = productDao.updateWithMergeDetached(product, productDto);
        System.out.println("updatedProduct " + updatedProduct);

        assertNotNull(updatedProduct);
        assertEquals(updatedProduct.getId(), product.getId());
        assertEquals(updatedProduct.getName(), "Biscuit");
        assertEquals(updatedProduct.getPrice(), 6.0);
    }

    @Test
    public void testUpdateWithGetPersistent() {
        //create product
        Product product = new Product();
        product.setName("Sandwich");
        product.setPrice(5.37);
        //save product
        productDao.save(product);
        assertNotNull(product.getId());
        System.out.println("product " + product);

        ProductDto productDto = new ProductDto("Burger", 10.0);

        //update the detached entity 'product' using entityManager.find
        Product updatedProduct = productDao.updateWithGetPersistent(product, productDto);
        System.out.println("updatedProduct " + updatedProduct);

        assertNotNull(updatedProduct);
        assertEquals(updatedProduct.getId(), product.getId());
        assertEquals(updatedProduct.getName(), "Burger");
        assertEquals(updatedProduct.getPrice(), 10.0);
    }

    @Test
    public void testDelete() {
        //create product
        Product product = new Product();
        product.setName("Milk");
        product.setPrice(2.37);
        //save product
        productDao.save(product);

        //as save and delete use separate EntityManger, the 'product' entity is in
        //detached state at this point so inside delete() method we need to first
        //persist that entity by getting it from db and then remove it
        productDao.delete(product);

        Product deletedProduct =productDao.get(product.getId()).orElse(null);
        assertNull(deletedProduct);
    }

    @Test
//    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void testDeleteEMPerClass() {
        //create product
        Product product = new Product();
        product.setName("Milk");
        product.setPrice(2.37);
        //save product
        productDao.saveEMPerClass(product);

        //saveEMPerClass and deleteEMPerClass both use the same EntityManager instance hence
        //the 'product' entity passed to entityManager.remove(product) is not detached so
        //we can simply remove it without getting it from db
        productDao.deleteEMPerClass(product);

        Product deletedProduct =productDao.get(product.getId()).orElse(null);
        assertNull(deletedProduct);
    }
}