package com.walt;


import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class WaltServiceImpl implements WaltService {

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    CustomerRepository customerRepository;

    /**
     * Create a new order after assigning an available Driver.
     * @param customer for the order
     * @param restaurant for the order
     * @param deliveryTime for the order
     * @return the new Delivery
     * @throws BadOrderException if order cannot be placed
     */
    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) throws BadOrderException {

        //also assumes order cannot be placed for a time in the past (allows 5 second delay)
        checkParameters(customer, restaurant, deliveryTime);

        synchronized(this) {
            Driver driver = driverRepository.findAllDriversByCity(customer.getCity())
                    .stream()
                    .map(d -> new Pair<>(d, deliveryRepository.findAllDeliverysByDriverOrderByIdDesc(d)))
                    .filter(deliveries -> isAvailable(deliveries.second, deliveryTime))
                    .min(Comparator.comparingInt(deliveries -> deliveries.second.size()))
                    .orElse(new Pair<>(null, null)).first;

            if (driver == null)
                throw new BadOrderException("No available driver in " + restaurant.getCity().getName()
                        + ", for delivery time: " + deliveryTime);


            return new Delivery(driver, restaurant, customer, deliveryTime);
        }
    }

    /**
     * Calculate and return a list of DriverDistance in descending order according to totalDistance of deliveries by that Driver
     * for ALL deliveries, where each DriverDistance contains the Driver and the total distance of deliveries they made.
     * @return the calculated list of DriverDistance
     */
    @Override
    public List<DriverDistance> getDriverRankReport() {
        return calculateDriverReport(driverRepository.findAll());
    }

    /**
     * Calculate and return a list of DriverDistance in descending order according to totalDistance of deliveries by that Driver
     * for deliveries made in a specific city,
     * where each DriverDistance contains the Driver and the total distance of deliveries they made.
     * @param city to calculate total distance of deliveries.
     * @return the calculated list of DriverDistance
     */
    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return calculateDriverReport(driverRepository.findAllDriversByCity(city));
    }

    /**
     * Finds or creates a new customer in customerRepository based on received params.
     * @param name of customer
     * @param city of customer
     * @param address of customer
     * @return Customer -- the existing or newly created customer.
     */
    public synchronized Customer findOrCreateCustomer(String name, City city, String address) {

        Customer customer = customerRepository.findByNameAndCityAndAddress(name, city, address);

        if(customer == null) {
            customer = (new Customer(name, city, address));
            customerRepository.save(customer);
        }
        return customer;
    }

    /**
     * Calculates a total driving distance for Drivers in a given list of Drivers.
     * @param driverList to give report for
     * @return List<DriverDistance>, a sorted by desc. list of the drivers distance
     */
    private List<DriverDistance> calculateDriverReport(List<Driver> driverList) {

        return driverList.stream()
                .map(driver-> new DriverDistanceImp(driver, ((Double) deliveryRepository.findAllDeliverysByDriver(driver)
                        .stream().mapToDouble(Delivery::getDistance).sum()).longValue()))
                .sorted(Comparator.comparingLong(DriverDistanceImp::getTotalDistance).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Checks if a new delivery time collides with an existing delivery in a list of deliveries.
     * @param deliveries the list of existing deliveries
     * @param deliveryTime the new delivery time
     * @return true if no collisions, else false
     */
    private boolean isAvailable(List<Delivery> deliveries, Date deliveryTime) {
        if( deliveries.size() == 0 ) return true;
        for(Delivery del : deliveries) {
            if(Math.abs(TimeUnit.MILLISECONDS.toMinutes(deliveryTime.getTime() - del.getDeliveryTime().getTime())) < 59) {
                return false;
            }
        }
        return true;
    }


    /**
     * Checks if parameters of a new order are within the allowed for a new order.
     * @param customer of the new order
     * @param restaurant of the new order
     * @param deliveryTime of the new order
     * @throws BadOrderException if parameters are not ok
     */
    private void checkParameters(Customer customer, Restaurant restaurant, Date deliveryTime) throws BadOrderException {
        if(TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - deliveryTime.getTime()) > 5)
            throw new BadOrderException("Cannot place order in the past.");

        if(!customer.getCity().getName().equals(restaurant.getCity().getName())) {
            throw new BadOrderException("Customer must be from the same city as restaurant!");
        }
    }
}
