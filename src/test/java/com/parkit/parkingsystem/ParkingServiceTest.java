package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {
    @InjectMocks
    private static ParkingService parkingService;
    //@Mock
    //Database prodMock;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private Ticket ticket;
    private ParkingSpot parkingSpot;

    @BeforeEach
    public void setUpPerTest() {
        try {


            this.parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            this.ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");

            //when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
        when(parkingSpotDAO.updateParking(this.parkingSpot)).thenReturn(true);

        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(this.parkingSpot);

    }

    @Test
    public void testProcessInComingVehicle() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.updateParking(this.parkingSpot)).thenReturn(true);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(parkingSpotDAO.updateParking(this.parkingSpot)).thenReturn(false);

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(this.parkingSpot);

    }



    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception{
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(this.ticket);
        when(ticketDAO.updateTicket(this.ticket)).thenReturn(false);

        parkingService.processExitingVehicle();

        verify(parkingSpotDAO,Mockito.times(0)).updateParking(this.parkingSpot);


    }

    @Test
    public void testGetNextParkingNumberIfAvailable() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        ParkingType parkingType = ParkingType.CAR;
        when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(1);
        this.parkingSpot = null;
        this.parkingSpot = new ParkingSpot(1, ParkingType.CAR,true);

        ParkingSpot parkingSpot2 = parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpotDAO).getNextAvailableSlot(parkingType);
        assertEquals(this.parkingSpot,parkingSpot2);

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
        when(inputReaderUtil.readSelection()).thenReturn(1);
        ParkingType parkingType = ParkingType.CAR;
        when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(-1);

        this.parkingSpot = null;
        this.parkingSpot = new ParkingSpot(0, ParkingType.CAR,true);

        Exception exception = assertThrows(Exception.class, () -> parkingService.getNextParkingNumberIfAvailable());
        assertEquals("Error fetching parking number from DB. Parking slots might be full",exception.getMessage());

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
        when(inputReaderUtil.readSelection()).thenReturn(3);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> parkingService.getNextParkingNumberIfAvailable());
        assertEquals("Entered input is invalid",exception.getMessage());
    }

    @Test
    public void testProcessIncomingVehicleGetNbTicket() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        this.parkingSpot = null;
        this.parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        when(parkingSpotDAO.updateParking(this.parkingSpot)).thenReturn(true);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);

        parkingService.processIncomingVehicle();

        verify(ticketDAO,Mockito.times(1)).getNbTicket("ABCDEF");

    }

    @Test
    public void testProcessExitingVehicleGetNbTicket() throws Exception {

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(this.ticket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
        when(ticketDAO.updateTicket(this.ticket)).thenReturn(true);

        parkingService.processExitingVehicle();

        verify(ticketDAO,Mockito.times(1)).getNbTicket("ABCDEF");

    }


    @Test
    public void testGetTicket() throws Exception {

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        this.parkingSpot = null;
        this.parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(this.ticket);
        when(ticketDAO.updateTicket(this.ticket)).thenReturn(true);

        parkingService.processExitingVehicle();

        verify(ticketDAO,Mockito.times(1)).getTicket("ABCDEF");

    }

}
