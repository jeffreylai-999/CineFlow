package control;

import database.TicketDA;
import domain.Ticket;
import java.util.ArrayList;

/**
 *
 * @author Jeffrey
 */
public class MaintainTicket {

    private final TicketDA da;

    public MaintainTicket() {
        da = new TicketDA();
    }

    public Ticket selectRecord(String id) {
        return da.getRecord(id);
    }

    public void addRecord(Ticket ticket) {
        da.addRecord(ticket);
    }

    public ArrayList<Ticket> getAll() {
        return da.getAll();
    }

    public Ticket lastID() {
        return da.lastID();
    }
}
