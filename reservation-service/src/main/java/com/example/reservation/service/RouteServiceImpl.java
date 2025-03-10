package com.example.reservation.service;

import com.example.reservation.exception.ResourceNotFoundException;
import com.example.reservation.model.*;
import com.example.reservation.repository.BusRepository;
import com.example.reservation.repository.ReservedSeatsRepository;
import com.example.reservation.repository.RouteRepository;
import com.example.reservation.rest.payloads.BusPayload;
import com.example.reservation.rest.payloads.RoutePayload;
import com.example.reservation.rest.payloads.TripPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RouteServiceImpl implements RouteService{

    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private BusRepository busRepository;

    @Autowired
    private ReservedSeatsRepository reservedSeatsRepository;

//    @Autowired
//    private MongoTemplate mongoTemplate;

    @Override
    public Route addNewRoute(Route route) {
       return routeRepository.insert(route);
    }

    @Override
    public Trip addNewTrip(String routeId, Trip trip) {
        Optional<Route> optionalRoute =routeRepository.findById(routeId);
        Route route=optionalRoute.orElseThrow(()->new ResourceNotFoundException(routeId));
        route.getTripList().add(trip);
        Route updatedRoute= routeRepository.save(route);
        return  trip;
    }

    @Override
    public void addBusToRouteTrip(String routeId, String tripId, String busNumber) {

        Optional<Route> optionalRoute =routeRepository.findById(routeId);
        Route route=optionalRoute.orElseThrow(()->new ResourceNotFoundException(routeId));

        System.out.println(route.getTripList().size());

        Optional<Trip> optionalTrip=route.getTripList().stream().filter(t->t.getId().equals(tripId)).findFirst();
        Trip trip= optionalTrip.orElseThrow();

        Optional<Bus> optionalBus=busRepository.findById(busNumber);
        Bus bus=optionalBus.orElseThrow();

        bus.setRoute(route);
        trip.setBus(bus);

        busRepository.save(bus);
        routeRepository.save(route);

    }

    @Override
    public RoutePayload getRoute(LocalDate date, String source, String destination) {
        Route dbRoute= routeRepository.find(source,destination);

        RoutePayload routePayload=new RoutePayload();
        routePayload.setId(dbRoute.getId());
        routePayload.setSource(dbRoute.getSource());
        routePayload.setDestination(dbRoute.getDestination());
        routePayload.setDistance(dbRoute.getDistance());

        List<Trip> trips=dbRoute.getTripList();

        List<TripPayload> tripPayloads=new ArrayList<>();
        for(Trip trip:trips){

            Bus bus=trip.getBus();
            BusType busType=bus.getType();
            SeatType seatType=bus.getSeatType();

            double tripPrice=7*dbRoute.getDistance();

            if(busType.equals(BusType.NON_AC) && seatType.equals(SeatType.SLEEPER)){
                tripPrice+=100;
            }
            if(busType.equals(BusType.AC) && seatType.equals(SeatType.CHAIR)){
                tripPrice+=200;
            }
            if(busType.equals(BusType.AC) && seatType.equals(SeatType.SLEEPER)){
                tripPrice+=300;
            }

            TripPayload tripPayload=new TripPayload();
            tripPayload.setId(trip.getId());
            tripPayload.setDepTime(trip.getDepTime());
            tripPayload.setArrivalTime(trip.getArrivalTime());
            tripPayload.setPrice(tripPrice);

            BusPayload busPayload=new BusPayload();
            busPayload.setNumber(trip.getBus().getNumber());
            busPayload.setName(trip.getBus().getName());
            busPayload.setType(trip.getBus().getType());
            busPayload.setSeats(trip.getBus().getSeats());
            busPayload.setSeatType(trip.getBus().getSeatType());
            busPayload.setContact(trip.getBus().getContact());
            tripPayload.setBus(busPayload);


            ReservedSeats reservedSeats=reservedSeatsRepository.findReservedSeats(date,trip.getBus().getNumber());
            tripPayload.setReservedSeats(reservedSeats.getReservedSeats());

            tripPayloads.add(tripPayload);

        }

        routePayload.setTripList(tripPayloads);

        return routePayload;
    }
}
