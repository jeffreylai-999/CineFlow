package control;

import database.ShowTimeDA;
import domain.Showtime;
import java.util.ArrayList;

/**
 *
 * @author Jeffrey
 */
public class MaintainShowTime {

    private final ShowTimeDA da;

    public MaintainShowTime() {
        da = new ShowTimeDA();
    }

    public Showtime selectRecord(String id) {
        return da.getRecord(id);
    }

    public Showtime selectRecord(String movieID, String date, String time) {
        return da.getRecordSearchByMovieTime(movieID, date, time);
    }

    public void addRecord(Showtime show) {
        da.addRecord(show);
    }

    public void updateRecord(Showtime show) {
        da.updateRecord(show);
    }

    public void deleteRecord(String id) {
        da.deleteRecord(id);
    }

    public ArrayList<Showtime> getAll() {
        return da.getAllShowTimes();
    }

    public ArrayList<Showtime> getShowTimes(String id) {
        return da.getShowTimes(id);
    }

    public Showtime lastID() {
        return da.lastID();
    }
}
