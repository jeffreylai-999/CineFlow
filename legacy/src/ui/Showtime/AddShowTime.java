package ui.Showtime;

import control.MaintainShowTime;
import domain.Movie;
import domain.Showtime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Jeffrey
 */
public class AddShowTime {

    private final Showtime[] show = new Showtime[6];
    private final String[] time = {"0100", "1000", "1300", "1600", "1900", "2200"};
    private final MaintainShowTime progControlShowTime = new MaintainShowTime();
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");

    public AddShowTime(Movie movie) {
        LocalDate releaseDate = dtf.parseLocalDate(movie.getReleaseDate());
        LocalDate offlineDate = dtf.parseLocalDate(movie.getOfflineDate());

        while (releaseDate.isBefore(offlineDate) || releaseDate.isEqual(offlineDate)) {
            for (int i = 0; i < 6; i++) {
                show[i] = new Showtime(setShowID(), releaseDate.toString("dd/MM/yyyy"), time[i], movie);
                progControlShowTime.addRecord(show[i]);
            }
            releaseDate = releaseDate.plusDays(1);
        }
    }

    private String setShowID() {
        Showtime lastID = progControlShowTime.lastID();
        //System.err.println(lastID);
        if (lastID != null) {
            int id = Integer.parseInt(lastID.getShowId().substring(1)) + 1;
            String newID = "S" + String.format("%09d", id);
            return newID;
        }
        else {
            return "S000000001";
        }
    }
}
