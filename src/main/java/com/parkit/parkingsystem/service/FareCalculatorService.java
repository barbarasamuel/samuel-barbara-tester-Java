package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FareCalculatorService {
    public void calculateFare(Ticket ticket){
        calculateFare(ticket,false);
    }
    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double tauxDiscount;
        double duree;
        double duration = outHour - inHour;

        duree = duration/3600000;
        //duree = TimeUnit.MILLISECONDS.toHours(duration);
        if ( duree < 0.5 ){
            duree = 0.0;
        }

        if(discount){
            tauxDiscount = 0.95;
        }else{
            tauxDiscount = 1.0;
        }

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(tauxDiscount * duree * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(tauxDiscount * duree * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}