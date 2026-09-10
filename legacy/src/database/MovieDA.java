package database;

import domain.Hall;
import domain.Movie;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Jeffrey
 */
public class MovieDA {

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";
    private final String tableName = "movie";
    private final String sqlQueryStr = "SELECT * FROM " + tableName;
    private final HallDA hallDA = new HallDA();
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    public MovieDA() {
        createConnection();
    }

    public void addRecord(Movie movie) {
        String insertStr = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, movie.getMovieId());
            stmt.setString(2, movie.getMovieName());
            stmt.setString(3, movie.getMoviePrice());
            stmt.setString(4, movie.getRunningTime());
            stmt.setString(5, movie.getMovieGenre());
            stmt.setString(6, movie.getReleaseDate());
            stmt.setString(7, movie.getOfflineDate());
            stmt.setString(8, movie.getHallId().getHallId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Movie getRecord(String id) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE MOVIE_ID = ?";
        Movie movie = null;
        Hall hall = null;
        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                hall = hallDA.getRecord(rs.getString("HALL_ID"));
                movie = new Movie(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), hall);
            }

        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return movie;
    }

    public Movie getRecordByName(String name) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE MOVIE_NAME = ?";
        Movie movie = null;
        Hall hall = null;
        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, name);
            rs = stmt.executeQuery();

            if (rs.next()) {
                hall = hallDA.getRecord(rs.getString("HALL_ID"));
                movie = new Movie(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), hall);
            }

        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return movie;
    }

    public void updateRecord(Movie movie) {
        String insertStr = "UPDATE " + tableName + " SET MOVIE_NAME = ?, MOVIE_PRICE = ?, RUNNING_TIME = ?, MOVIE_GENRE = ?, RELEASE_DATE = ?, OFFLINE_DATE = ?, HALL_ID = ? WHERE MOVIE_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, movie.getMovieName());
            stmt.setString(2, movie.getMoviePrice());
            stmt.setString(3, movie.getRunningTime());
            stmt.setString(4, movie.getMovieGenre());
            stmt.setString(5, movie.getReleaseDate());
            stmt.setString(6, movie.getOfflineDate());
            stmt.setString(7, movie.getHallId().getHallId());
            stmt.setString(8, movie.getMovieId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteRecord(String id) {
        String deleteStr = "DELETE FROM " + tableName + " WHERE MOVIE_ID = ?";
        try {
            stmt = conn.prepareStatement(deleteStr);
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Movie lastID() {
        Movie movie = null;
        try {
            String sqlStr = "SELECT * FROM " + tableName + " ORDER BY MOVIE_ID";
            stmt = conn.prepareStatement(sqlStr, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();
            rs.last();
            movie = new Movie(rs.getString(1));
        }
        catch (SQLException ex) {
            return movie;
        }
        return movie;
    }

    public ArrayList<Movie> getAllMovie() {
        ArrayList<Movie> movie = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(sqlQueryStr);
            rs = stmt.executeQuery();

            while (rs.next()) {
                movie.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return movie;
    }

    public Movie getRecordID() {

        Movie movie = null;
        Hall hall = null;
        try {
            hall = hallDA.getRecord(rs.getString("HALL_ID"));
            movie = new Movie(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), hall);
        }

        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return movie;
    }

    private void createConnection() {
        try {
            conn = DriverManager.getConnection(host, user, password);
            System.out.println("***TRACE: Connection established.");
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void shutDown() {
        if (conn != null) {
            try {
                conn.close();
            }
            catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
