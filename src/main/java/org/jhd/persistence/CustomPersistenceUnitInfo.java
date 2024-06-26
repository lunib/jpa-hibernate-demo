package org.jhd.persistence;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

import javax.sql.DataSource;
import java.net.URL;
import java.util.List;
import java.util.Properties;
//We can bootstrap a JPA implementation without using the “persistence.xml” file, by just using plain Java. This
//class is a plain data container, which stores the configuration parameters bound to a specific persistence unit.
//It’s not suitable for use cases where it’s necessary to test in isolation the application components that use
//different persistence units class representing persistence.xml
public class CustomPersistenceUnitInfo implements PersistenceUnitInfo {
    private final String persistentUnitName;

    public CustomPersistenceUnitInfo(String persistentUnitName) {
        this.persistentUnitName = persistentUnitName;
    }

    @Override
    public String getPersistenceUnitName() {
        return persistentUnitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return "org.hibernate.jpa.HibernatePersistenceProvider";
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.RESOURCE_LOCAL;
    }

    @Override
    public DataSource getJtaDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:mysql://host.docker.internal:3307/jpahd");
        hikariDataSource.setUsername("root");
        hikariDataSource.setPassword("password");
        return hikariDataSource;
    }

    @Override
    public List<String> getManagedClassNames() {
        return List.of("org.jhd.entity.Product");
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return null;
    }

    @Override
    public List<String> getMappingFileNames() {
        return null;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return null;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return null;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return false;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return null;
    }

    @Override
    public ValidationMode getValidationMode() {
        return null;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void addTransformer(ClassTransformer classTransformer) {

    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return null;
    }
}
