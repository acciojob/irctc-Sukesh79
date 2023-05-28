package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        List<Station> stationList = trainEntryDto.getStationRoute();
        String route = "";

        for(Station station : stationList){
            route += station.toString() + ",";
        }

        Train train = new Train();
        train.setBookedTickets(new ArrayList<>());
        train.setRoute(route);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        Train savedTrain = trainRepository.save(train);
        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto) throws Exception {

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        int trainId = seatAvailabilityEntryDto.getTrainId();
        Station FromStation = seatAvailabilityEntryDto.getFromStation();
        Station ToStation = seatAvailabilityEntryDto.getToStation();

        Optional<Train> optionalTrain = trainRepository.findById(trainId);
        if(optionalTrain.isEmpty()) throw new Exception("Train does not exist");

        Train train = optionalTrain.get();

        String route = train.getRoute();
        String[] stations = route.split(",");

        Set<String> stationsSet = new HashSet<>();
        boolean flag = false;
        for(String station : stations){
            if(Objects.equals(station, seatAvailabilityEntryDto.getFromStation().toString())){
                flag = true;
            }
            if(Objects.equals(station, seatAvailabilityEntryDto.getToStation().toString())){
                stationsSet.add(station);
                break;
            }
            if(flag) stationsSet.add(station);
        }


        List<Ticket> tickets = train.getBookedTickets();
        int noOfSeatsBooked = 0;

        for(Ticket ticket : tickets){
            if(stationsSet.contains(ticket.getFromStation().toString()) || stationsSet.contains(ticket.getToStation().toString())){
                noOfSeatsBooked += ticket.getPassengersList().size();
            }
        }

        return train.getNoOfSeats()-noOfSeatsBooked;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Optional<Train> optionalTrain = trainRepository.findById(trainId);
        if(optionalTrain.isEmpty()) throw new Exception("Train does not exist");

        Train train = optionalTrain.get();
        String route = train.getRoute();

        String[] stations = route.split(",");
        Set<String> stationsSet = new HashSet<>(Arrays.asList(stations));
        if(!stationsSet.contains(station.toString())) throw new Exception("Train is not passing from this station");

        List<Ticket> tickets = train.getBookedTickets();
        int ans=0;
        for(Ticket ticket : tickets){
            if(ticket.getFromStation().toString().equals(station.toString()))
                ans += ticket.getPassengersList().size();
        }

        return ans;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId) throws Exception {

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Optional<Train> optionalTrain = trainRepository.findById(trainId);
        if(optionalTrain.isEmpty()) throw new Exception("Train does not exist");

        Train train = optionalTrain.get();
        int maxAge = Integer.MIN_VALUE;

        for(Ticket ticket : train.getBookedTickets()){
            for(Passenger passenger: ticket.getPassengersList()){
                maxAge = Math.max(maxAge, passenger.getAge());
            }
        }

        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> list = new ArrayList<>();

        for(Train train : trainRepository.findAll()){
            String[] route = train.getRoute().split(",");
            LocalTime departureTime = train.getDepartureTime();

            int AddHours = 0;
            for(String stationString : route){
                if(Objects.equals(stationString, station.toString())){
                    LocalTime stationTime = departureTime.plusHours(AddHours);
                    if(!startTime.isAfter(stationTime) && !endTime.isBefore(stationTime)){
                        list.add(train.getTrainId());
                    }
                    break;
                }
                AddHours++;
            }

        }
        return list;
    }

}
