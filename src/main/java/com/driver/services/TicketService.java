package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        int trainId = bookTicketEntryDto.getTrainId();
        Optional<Train> optionalTrain = trainRepository.findById(trainId);
        if(optionalTrain.isEmpty()) throw new Exception("Train does not exist");

        Train train = optionalTrain.get();

        List<Ticket> ticketList = train.getBookedTickets();
        int noOfSeatsReq = bookTicketEntryDto.getNoOfSeats();
        int noOfSeatsBooked = ticketList.size();
        int totalNoOfSeats = train.getNoOfSeats();
        int noOfSeatsAvailable = totalNoOfSeats - noOfSeatsBooked;

        if(noOfSeatsAvailable < noOfSeatsReq) throw  new Exception("Less tickets are available");

        String[] stations = train.getRoute().split(",");
//        Set<String> stationsSet = new HashSet<>(Arrays.asList(stations));

        Map<String, Integer> stationsMap = new HashMap<>();
        for(int i = 0; i < stations.length; i++){
            stationsMap.put(stations[i], i);
        }
        Station fromStationenum = bookTicketEntryDto.getFromStation();
        Station toStationenum = bookTicketEntryDto.getToStation();
        String fromStation = String.valueOf(bookTicketEntryDto.getFromStation());
        String toStation = String.valueOf(bookTicketEntryDto.getToStation());
        if(!stationsMap.containsKey(fromStation) || !stationsMap.containsKey(toStation))
            throw new Exception("Invalid stations");

        int totalFare = (stationsMap.get(toStation) - stationsMap.get(fromStation)) * 300 * noOfSeatsReq;

        List<Passenger> passengersList = new ArrayList<>();
        for(Integer passengerId : bookTicketEntryDto.getPassengerIds()){
           Optional<Passenger> optionalPassenger = passengerRepository.findById(passengerId);
           if(optionalPassenger.isEmpty()) throw new Exception("Passenger does not exist");

           passengersList.add(optionalPassenger.get());
        }

        Ticket ticket = new Ticket();
        ticket.setPassengersList(passengersList);
        ticket.setTrain(train);
        ticket.setFromStation(fromStationenum);
        ticket.setToStation(toStationenum);
        ticket.setTotalFare(totalFare);

        Ticket savedTicket = ticketRepository.save(ticket);
        Train train1 = savedTicket.getTrain();
        List<Ticket> bookedTickets = train1.getBookedTickets();
        bookedTickets.add(savedTicket);
        train1.setBookedTickets(bookedTickets);
        Train savedTrain = trainRepository.save(train1);

        for(Passenger passenger : savedTicket.getPassengersList()){
            List<Ticket> bookedTickets1 = passenger.getBookedTickets();
            bookedTickets1.add(savedTicket);
            passenger.setBookedTickets(bookedTickets1);
            Passenger savedPassenger = passengerRepository.save(passenger);
        }

        return savedTicket.getTicketId();
    }
}
