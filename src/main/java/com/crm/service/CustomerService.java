package com.crm.service;

import com.crm.model.Customer;
import com.crm.repository.CustomerRepository;
import com.crm.service.event.CustomerEvent;
import com.crm.service.event.EventBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    
    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) {
        boolean isNew = (customer.getId() == null);
        Customer savedCustomer = customerRepository.save(customer);
        
        CustomerEvent.EventType eventType = isNew ? CustomerEvent.EventType.CREATED : CustomerEvent.EventType.UPDATED;
        EventBroker.getInstance().publish(new CustomerEvent(eventType, savedCustomer));
        
        return savedCustomer;
    }

    public void deleteCustomer(Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customerRepository.deleteById(id);
            EventBroker.getInstance().publish(new CustomerEvent(CustomerEvent.EventType.DELETED, customer));
        }
    }

    public List<Customer> searchByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }
}
