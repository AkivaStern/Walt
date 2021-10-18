package com.walt.model;

public class DriverDistanceImp implements DriverDistance {

    private Driver driver;
    private Long totalDistance;

    public DriverDistanceImp(Driver driver, Long totalDistance) {
        this.driver = driver;
        this.totalDistance = totalDistance;
    }

    @Override
    public Driver getDriver() {
        return driver;
    }

    @Override
    public Long getTotalDistance() {
        return totalDistance;
    }
}
