package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {
    Customer findByName(String name);
    Customer findByNameAndCityAndAddress(String name, City city, String add);
}
