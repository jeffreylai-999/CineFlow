package control;

import database.HallDA;
import domain.Hall;
import java.util.ArrayList;

/**
 *
 * @author Jeffrey
 */
public class MaintainHall {

    private final HallDA da;

    public MaintainHall() {
        da = new HallDA();
    }

    public Hall selectRecord(String id) {
        return da.getRecord(id);
    }

    public void addRecord(Hall hall) {
        da.addRecord(hall);
    }

    public void updateRecord(Hall hall) {
        da.updateRecord(hall);
    }

    public void deleteRecord(String id) {
        da.deleteRecord(id);
    }

    public ArrayList<Hall> getAll() {
        return da.getAllMovie();
    }

    public Hall lastID() {
        return da.lastID();
    }
}
