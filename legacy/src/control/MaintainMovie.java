package control;

import database.MovieDA;
import domain.Movie;
import java.util.ArrayList;

/**
 *
 * @author Jeffrey
 */
public class MaintainMovie {

    private final MovieDA da;

    public MaintainMovie() {
        da = new MovieDA();
    }

    public Movie selectRecord(String id) {
        return da.getRecord(id);
    }

    public Movie selectRecordByName(String Name) {
        return da.getRecordByName(Name);
    }

    public void addRecord(Movie movie) {
        da.addRecord(movie);
    }

    public void updateRecord(Movie movie) {
        da.updateRecord(movie);
    }

    public void deleteRecord(String id) {
        da.deleteRecord(id);
    }

    public ArrayList<Movie> getAll() {
        return da.getAllMovie();
    }

    public Movie lastID() {
        return da.lastID();
    }
}
