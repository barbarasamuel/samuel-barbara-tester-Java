package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.Math.round;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Unitary tests made to verify the functionalities' FareCalculatorService class
 */
public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        Long duration = ticket.getOutTime().getTime()-ticket.getInTime().getTime();

        if ( duration <= (30*60*1000) ){
            duration = 0L;
        }else{
            duration = duration - (30*60*1000);
        }

        double testPrice = Fare.CAR_RATE_PER_HOUR*duration;

        assertEquals(ticket.getPrice(),testPrice);

    }

    @Test
    public void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        Long duration = ticket.getOutTime().getTime()-ticket.getInTime().getTime();

        if ( duration <= (30*60*1000) ){
            duration = 0L;
        }else{
            duration = duration - (30*60*1000);
        }

        double testPrice = Fare.BIKE_RATE_PER_HOUR*duration;

        assertEquals(ticket.getPrice(), testPrice);

    }

    @Test
    public void calculateFareUnkownType(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        Long duration = ticket.getOutTime().getTime()-ticket.getInTime().getTime();

        if ( duration <= (30*60*1000) ){
            duration = 0L;
        }else{
            duration = duration - (30*60*1000);
        }

        double testPrice = Fare.BIKE_RATE_PER_HOUR*duration;

        assertEquals( (Math.round(testPrice*100.0)/100.0),ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        Long duration = ticket.getOutTime().getTime()-ticket.getInTime().getTime();

        if ( duration <= (30*60*1000) ){
            duration = 0L;
        }else{
            duration = duration - (30*60*1000);
        }

        double testPrice = Fare.CAR_RATE_PER_HOUR*duration;

        assertEquals( (Math.round(testPrice*100.0)/100.0),ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        Long duration = ticket.getOutTime().getTime()-ticket.getInTime().getTime();

        if ( duration <= (30*60*1000) ){
            duration = 0L;
        }else{
            duration = duration - (30*60*1000);
        }

        double testPrice = Fare.CAR_RATE_PER_HOUR*duration;

        assertEquals( ticket.getPrice(),(Math.round(testPrice*100.0)/100.0) );
    }

    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  29 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        int freePrice = 0;
        assertEquals( (29 * freePrice) , ticket.getPrice());
    }

@Test
public void calculateFareBikeWithLessThan30minutesParkingTime() {
    Date inTime = new Date();
    inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
    Date outTime = new Date();
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

    ticket.setInTime(inTime);
    ticket.setOutTime(outTime);
    ticket.setParkingSpot(parkingSpot);
    fareCalculatorService.calculateFare(ticket);

    int freePrice = 0;
    assertEquals((29 * freePrice), ticket.getPrice());
    }


    @Test
    public void calculateFareCarWithDiscount(){
        boolean discount = true;
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket,discount);

        Long duration = ticket.getOutTime().getTime()-ticket.getInTime().getTime();

        if ( duration <= (30*60*1000) ){
            duration = 0L;
        }else{
            duration = duration - (30*60*1000);
        }

        double discountPrice = 0.95;
        double testPrice = Fare.CAR_RATE_PER_HOUR*duration*discountPrice;

        assertEquals( ticket.getPrice(),(Math.round(testPrice*100.0)/100.0) );

    }

    @Test
    public void calculateFareBikeWithDiscount(){
        boolean discount = true;
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket,discount);

        Long duration = ticket.getOutTime().getTime()-ticket.getInTime().getTime();

        if ( duration <= (30*60*1000) ){
            duration = 0L;
        }else{
            duration = duration - (30*60*1000);
        }

        double discountPrice = 0.95;
        double testPrice = Fare.BIKE_RATE_PER_HOUR*duration*discountPrice;

        assertEquals( ticket.getPrice(),(Math.round(testPrice*100.0)/100.0) );
    }

}