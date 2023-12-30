package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;

    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() throws Exception{
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpotTest = new ParkingSpot(1,ParkingType.CAR,true);

        int nbPassageCar = ticketDAO.getNbTicket("ABCDEF");//aj

        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability

        int nbPassageCarAttendu = nbPassageCar + 1;
        nbPassageCar = ticketDAO.getNbTicket("ABCDEF");

        assertEquals(nbPassageCarAttendu,nbPassageCar);
        assertTrue(parkingSpotTest.isAvailable());
    }

    @Test
    public void testParkingLotExit() throws Exception{
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database

        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        double finalPrice = 0;
        boolean timeEndExistsInDatabase = false;

        if(ticket.getOutTime() != null){

            timeEndExistsInDatabase = true;
            long timeBegin = ticket.getInTime().getTime();
            long timeEnd = ticket.getOutTime().getTime();

            Long totalTime = timeEnd - timeBegin;

            if ( totalTime <= (30*60*1000) ){
                totalTime = 0L;
            }else{
                totalTime = totalTime - (30*60*1000);
            }

            float tauxDiscount = tauxDiscount = 0.95f;


            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    double carPrice = (tauxDiscount * totalTime * Fare.CAR_RATE_PER_HOUR);
                    finalPrice = carPrice;
                    break;
                }
                case BIKE: {
                    double bikePrice = (tauxDiscount * totalTime * Fare.BIKE_RATE_PER_HOUR);
                    finalPrice = bikePrice;
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }

        }


        assertEquals(finalPrice,ticket.getPrice());
        assertTrue(timeEndExistsInDatabase);
    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        Ticket ticket;
        ticket = new Ticket();

        testParkingLotExit();
        double ticketPriceBefore = ticket.getPrice();
        testParkingLotExit();

        assertEquals(ticketPriceBefore*(0.95), ticket.getPrice());
    }
}
