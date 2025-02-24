# multitenant-service

Multi-Tenancy:
Implements multi tenants by adding different schemas for tenants
Separates the data on schema basis

**How to Set-Up?**
1. Install Java 17
2. Install Docker Desktop

1. run **docker compose up -d** so the DB will be Created. I used PSQL
2. mvn clean install -DskipTests=true
3. mvn spring-boot:run

The service will be up and running, if you want to test the tenant based data you can also run TenantTest which is located in src/main/test.
After the tenant is run call http://localhost:8080/public/customer with Header **X-TenantID: amazon ** Data from amazon schema will be returned.

**How to add new tenant?**
1. At application.yaml spring:application:tenants just add the tenant you want and the liquibase scripts will be used to execute on new schema.

 
