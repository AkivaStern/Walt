package com.walt;

import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public  interface WaltService {


    Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) throws BadOrderException;

    List<DriverDistance> getDriverRankReport();

    List<DriverDistance> getDriverRankReportByCity(City city);

    Customer findOrCreateCustomer(String name, City city, String address) throws BadOrderException;
}

