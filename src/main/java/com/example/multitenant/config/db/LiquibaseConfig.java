package com.example.multitenant.config.db;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

@Component
public class LiquibaseConfig {
    private static final String CHANGELOG_MASTER_PATH = "db/changelog/changelog-master.xml";
    private final Environment environment;

    @Value("${spring.application.tenants}")
    String [] tenants;

    private final DatasourceConfig datasourceConfig;

    public LiquibaseConfig(Environment environment, DatasourceConfig datasourceConfig) {
        this.environment = environment;
        this.datasourceConfig = datasourceConfig;

    }

    @PostConstruct
    public void migrateAllTenants() {
        Arrays.stream(tenants).toList().forEach(this::ensureSchemaAndMigrate);
    }

    private void ensureSchemaAndMigrate(String tenantId) {
        try (Connection adminConnection = createDataSource(null).getConnection()) {
            createSchemaIfNotExists(adminConnection, tenantId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure schema for tenant: " + tenantId, e);
        }
        migrateForTenant(tenantId);
    }


    private void createSchemaIfNotExists(Connection connection, String schemaName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        }
    }

    private void migrateForTenant(String tenantId) {
        BasicDataSource dataSource = createDataSource(tenantId);

        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName(tenantId);

            Liquibase liquibase = new Liquibase(CHANGELOG_MASTER_PATH, new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
        } catch (Exception e) {
            throw new RuntimeException("Liquibase migration failed for tenant: " + tenantId, e);
        }
    }

    private BasicDataSource createDataSource(String schemaName) {
        BasicDataSource dataSource = new BasicDataSource();

        if (schemaName != null) {
            dataSource.setUrl(datasourceConfig.getUrlForSchema(schemaName));
        } else {
            dataSource.setUrl(datasourceConfig.getUrl());
        }

        dataSource.setDriverClassName(datasourceConfig.getDriverClassName());
        dataSource.setUsername(datasourceConfig.getUsername());
        dataSource.setPassword(datasourceConfig.getPassword());
        return dataSource;
    }

}

