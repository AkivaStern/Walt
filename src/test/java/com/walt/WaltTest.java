package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData() {

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);
    }

    private void createRestaurant(City jerusalem, City tlv) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }


    private void createDelivery(String custName, String custCityName, String custAddress, String restCityName, String restAddress, Date deliveryTime) {

        try {
            Customer customer = waltService.findOrCreateCustomer(custName, cityRepository.findByName(custCityName), custAddress);
            Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurantRepository.findByCityAndAddress(cityRepository.findByName(restCityName), restAddress), deliveryTime);
            deliveryRepository.save(delivery);
        } catch (BadOrderException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testBasics(){

        Date initialTime = new Date();
        createDelivery("Akiva" , "Jerusalem", "koresh", "Jerusalem", "All meat restaurant", initialTime);
        createDelivery("Yishai" , "Jerusalem", "ibn ezra", "Jerusalem", "All meat restaurant", initialTime);
        createDelivery("Beethoven" , "Tel-Aviv", "Ludwig van Beethoven", "Tel-Aviv", "Only vegan", initialTime);
        createDelivery("Akiva" , "Jerusalem", "koresh", "Jerusalem", "All meat restaurant", initialTime);
        //these two will will not be added:
        createDelivery("Joseph" , "Tel-Aviv", "allenby", "Jerusalem", "All meat restaurant", initialTime);
        createDelivery("Eitan" , "Jerusalem", "yanai", "Jerusalem", "All meat restaurant", initialTime);

        assertEquals(((List<Customer>) customerRepository.findAll()).size(), 9);
        assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 4);

        createDelivery("Eitan" , "Jerusalem", "yanai", "Jerusalem", "All meat restaurant", new Date(initialTime.getTime() + ( 1 * 60 * 60 * 1000)));

        assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 5);
        assertEquals(deliveryRepository.findTopByOrderByIdDesc().getDriver().getName(), "Robert");

        createDelivery("Mozart" , "Jerusalem", "Wolfgang Amadeus Mozart", "Jerusalem", "All meat restaurant", new Date((long) (initialTime.getTime() + ( 2.5 * 60 * 60 * 1000))));

        assertEquals(deliveryRepository.findTopByOrderByIdDesc().getDriver().getName(), "David");

        createDelivery("Eitan" , "Jerusalem", "yanai", "Jerusalem", "All meat restaurant", new Date(initialTime.getTime() + ( 1 * 60 * 60 * 1000)));
        createDelivery("Eitan" , "Jerusalem", "yanai", "Jerusalem", "All meat restaurant", new Date(initialTime.getTime() + ( 1 * 60 * 60 * 1000)));

        assertEquals(deliveryRepository.findTopByOrderByIdDesc().getDriver().getName(), "David");

        createDelivery("Beethoven" , "Tel-Aviv", "Ludwig van Beethoven", "Tel-Aviv", "Only vegan", initialTime);
        createDelivery("Beethoven" , "Tel-Aviv", "Ludwig van Beethoven", "Tel-Aviv", "Only vegan", initialTime);
        //this one won't be added:
        createDelivery("Beethoven" , "Tel-Aviv", "Ludwig van Beethoven", "Tel-Aviv", "Only vegan", initialTime);
        createDelivery("Jonathan" , "Tel-Aviv", "rothschield", "Tel-Aviv", "Only vegan", new Date(initialTime.getTime() + ( 1 * 60 * 60 * 1000)));

        assertEquals(((List<Customer>) customerRepository.findAll()).size(), 10);
        assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 11);

        createDelivery("Beethoven" , "Tel-Aviv", "Ludwig van Beethoven", "Tel-Aviv", "Only vegan", new Date(initialTime.getTime()  + ( 2 * 60 * 60 * 1000)));
        createDelivery("Beethoven" , "Tel-Aviv", "Ludwig van Beethoven", "Tel-Aviv", "Only vegan", new Date(initialTime.getTime()  + ( 2 * 60 * 60 * 1000)));
        createDelivery("Beethoven" , "Tel-Aviv", "Ludwig van Beethoven", "Tel-Aviv", "Only vegan", new Date(initialTime.getTime()  + ( 1 * 60 * 60 * 1000)));
        createDelivery("Beethoven" , "Tel-Aviv", "Ludwig van Beethoven", "Tel-Aviv", "Only vegan", new Date(initialTime.getTime()  + ( 5 * 60 * 60 * 1000)));
        createDelivery("Beethoven" , "Tel-Aviv", "Ludwig van Beethoven", "Tel-Aviv", "Only vegan", new Date(initialTime.getTime()  + ( 1 * 60 * 60 * 1000)));

        assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(), 16);
        assertEquals(deliveryRepository.findTopByOrderByIdDesc().getDriver().getName(), "Daniel");

        //Print driver reports:
        List<DriverDistance> driverReport = waltService.getDriverRankReport();
        System.out.println("Driver Report:\nName  -> Total Distance\n----------------------");
        driverReport.forEach(report -> System.out.println(report.getDriver().getName() + " -> " + report.getTotalDistance()));

        City city = cityRepository.findByName("Jerusalem");
        driverReport = waltService.getDriverRankReportByCity(city);
        System.out.println("Driver Report by City: " + city.getName() + ":\nName  -> Total Distance\n----------------------");
        driverReport.forEach(report -> System.out.println(report.getDriver().getName() + " -> " + report.getTotalDistance()));
    }
}
