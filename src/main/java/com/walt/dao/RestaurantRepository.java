package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Restaurant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface RestaurantRepository extends CrudRepository<Restaurant, Long> {
    Restaurant findByName(String name);
    Restaurant findByNameAndCityAndAddress(String s, City c, String add);
    Restaurant findByCityAndAddress(City jerusalem, String all_meat_restaurant);
}
