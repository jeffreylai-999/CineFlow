package database;

import domain.Hall;
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
public class HallDA {

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";
    private final String tableName = "hall";
    private final String sqlQueryStr = "SELECT * FROM " + tableName;
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    public HallDA() {
        createConnection();
    }

    public void addRecord(Hall hall) {
        String insertStr = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, hall.getHallId());
            stmt.setString(2, hall.getHallLocation());
            stmt.setString(3, hall.getHallStatus());
            stmt.setString(4, hall.getHallPeriod());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Hall getRecord(String id) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE HALL_ID = ?";
        Hall hall = null;
        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                hall = new Hall(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return hall;
    }

    public void updateRecord(Hall hall) {
        String insertStr = "UPDATE " + tableName + " SET HALL_LOCATION = ?, HALL_STATUS = ?, HALL_PERIOD = ? WHERE HALL_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, hall.getHallLocation());
            stmt.setString(2, hall.getHallStatus());
            stmt.setString(3, hall.getHallPeriod());
            stmt.setString(4, hall.getHallId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteRecord(String id) {
        String deleteStr = "DELETE FROM " + tableName + " WHERE HALL_ID = ?";
        try {
            stmt = conn.prepareStatement(deleteStr);
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Hall lastID() {
        Hall hall = null;
        try {
            String sqlStr = "SELECT * FROM " + tableName + " ORDER BY HALL_ID";
            stmt = conn.prepareStatement(sqlStr, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();
            rs.last();
            hall = new Hall(rs.getString(1));
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return hall;
    }

    public ArrayList<Hall> getAllMovie() {
        ArrayList<Hall> hallList = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(sqlQueryStr);
            rs = stmt.executeQuery();

            while (rs.next()) {
                hallList.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return hallList;
    }

    public Hall getRecordID() {

        Hall hall = null;
        try {
            hall = new Hall(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
        }

        catch (SQLException ex) {
            return hall;
        }
        return hall;
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
