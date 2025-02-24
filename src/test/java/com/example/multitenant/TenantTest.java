package com.example.multitenant;

import com.example.multitenant.config.TenantContext;
import com.example.multitenant.customer.entity.Customer;
import com.example.multitenant.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class})
class TenantTest {
    public static final String ALIBABA = "alibaba";
    public static final String AMAZON = "amazon";

    @Autowired
    CustomerService customerService;

    @Test
    void saveAndLoadPerson() {
        final String ALIBABA_USER_NAME = "alibaba_test_" + System.currentTimeMillis();
        final String AMAZON_USER_NAME = "amazon_test_" + System.currentTimeMillis();

        Customer alibabaCustomer = createCustomer(ALIBABA, ALIBABA_USER_NAME);
        Customer amazonCustomer = createCustomer(AMAZON, AMAZON_USER_NAME);

        TenantContext.setCurrentTenant(ALIBABA);
        assertThat(customerService.findById(alibabaCustomer.getId()).getName()).isEqualTo(alibabaCustomer.getName());

        TenantContext.setCurrentTenant(AMAZON);
        assertThat(customerService.findById(amazonCustomer.getId()).getName()).isEqualTo(amazonCustomer.getName());
    }

    private Customer createCustomer(String schema, String name) {
        TenantContext.setCurrentTenant(schema);

        Customer customer = new Customer();
        customer.setName(name);
        return customerService.save(customer);
    }
}