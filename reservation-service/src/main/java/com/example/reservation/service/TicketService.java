package com.example.reservation.service;

import com.example.reservation.model.Ticket;
import org.bson.types.ObjectId;

import java.util.List;

public interface TicketService {

    void bookNewTicket(Ticket ticket);
    List<Ticket> getTickets(String user);
    void cancelTicket(ObjectId ticketId);

}
