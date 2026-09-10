package database;

import domain.Showtime;
import domain.Movie;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author Jeffrey
 */
public class ShowTimeDA {

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";
    private final String tableName = "showtime";
    private final String sqlQueryStr = "SELECT * FROM " + tableName;
    private final MovieDA movieDA = new MovieDA();
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    public ShowTimeDA() {
        createConnection();
    }

    public void addRecord(Showtime show) {
        String insertStr = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, show.getShowId());
            stmt.setString(2, show.getShowDate());
            stmt.setString(3, show.getShowTime());
            stmt.setString(4, show.getMovieId().getMovieId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Showtime getRecord(String id) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE SHOW_ID = ?";
        Showtime show = null;
        Movie movie = null;

        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                movie = movieDA.getRecord(rs.getString("MOVIE_ID"));
                show = new Showtime(rs.getString(1), rs.getString(2), rs.getString(3), movie);
            }

        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return show;
    }

    public Showtime getRecordSearchByMovieTime(String movieID, String date, String time) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE MOVIE_ID = ? AND SHOW_DATE = ? AND SHOW_TIME = ?";
        Showtime show = null;
        Movie movie = null;

        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, movieID);
            stmt.setString(2, date);
            stmt.setString(3, time);
            rs = stmt.executeQuery();

            if (rs.next()) {
                movie = movieDA.getRecord(rs.getString("MOVIE_ID"));
                show = new Showtime(rs.getString(1), rs.getString(2), rs.getString(3), movie);
            }

        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return show;
    }

    public void updateRecord(Showtime show) {
        String insertStr = "UPDATE " + tableName + " SET SHOW_DATE = ?, SHOW_TIME = ?, MOVIE_ID = ? WHERE SHOW_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, show.getShowDate());
            stmt.setString(2, show.getShowTime());
            stmt.setString(3, show.getMovieId().getMovieId());
            stmt.setString(4, show.getShowId());
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

    public Showtime lastID() {
        Showtime show = null;
        String sqlStr = "SELECT * FROM " + tableName + " ORDER BY SHOW_ID";
        try {
            stmt = conn.prepareStatement(sqlStr);
            stmt = conn.prepareStatement(sqlStr, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();
            rs.last();
            show = new Showtime(rs.getString(1));
        }
        catch (SQLException ex) {
            return show;
        }
        return show;
    }

    public ArrayList<Showtime> getShowTimes(String id) {
        ArrayList<Showtime> show = new ArrayList<>();
        String sqlStr = "SELECT * FROM " + tableName + " WHERE MOVIE_ID = ?";

        try {
            stmt = conn.prepareStatement(sqlStr);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            while (rs.next()) {
                show.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return show;
    }

    public ArrayList<Showtime> getAllShowTimes() {
        ArrayList<Showtime> show = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(sqlQueryStr);
            rs = stmt.executeQuery();

            while (rs.next()) {
                show.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return show;
    }

    public Showtime getRecordID() {

        Showtime show = null;
        Movie movie = null;
        try {
            movie = movieDA.getRecord(rs.getString("MOVIE_ID"));
            show = new Showtime(rs.getString(1), rs.getString(2), rs.getString(3), movie);
        }

        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return show;
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
