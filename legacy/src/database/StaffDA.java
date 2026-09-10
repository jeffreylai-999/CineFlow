package database;

import domain.Staff;
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
public class StaffDA {

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";
    private final String tableName = "staff";
    private final String sqlQueryStr = "SELECT * FROM " + tableName;
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    public StaffDA() {
        createConnection();
    }

    public void addRecord(Staff staff) {
        String insertStr = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, staff.getStaffId());
            stmt.setString(2, staff.getStaffName());
            stmt.setString(3, staff.getStaffPosition());
            stmt.setString(4, staff.getStaffPassword());
            stmt.setString(5, staff.getStaffGender());
            stmt.setString(6, staff.getStaffBirthday());
            stmt.setString(7, staff.getStaffIc());
            stmt.setString(8, staff.getStaffPhone());
            stmt.setString(9, staff.getStaffAddress());
            stmt.setString(10, staff.getStaffEmail());
            stmt.setString(11, staff.getStaffStatus());
            stmt.setInt(12, staff.getStaffQuestion());
            stmt.setString(13, staff.getStaffAnswer());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Staff getRecord(String id) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE STAFF_ID = ?";
        Staff staff = null;
        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                staff = new Staff(rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4), rs.getString(5),
                        rs.getString(6), rs.getString(7), rs.getString(8),
                        rs.getString(9), rs.getString(10), rs.getString(11),
                        rs.getInt(12), rs.getString(13));
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return staff;
    }

    public void updateRecord(Staff staff) {
        String insertStr = "UPDATE " + tableName + " SET STAFF_NAME = ?, STAFF_POSITION = ?, STAFF_PASSWORD = ?, STAFF_GENDER = ?, STAFF_BIRTHDAY = ?, STAFF_IC = ?, STAFF_PHONE = ?, STAFF_ADDRESS = ?, STAFF_EMAIL = ?, STAFF_STATUS = ?, STAFF_QUESTION = ?, STAFF_ANSWER = ? WHERE STAFF_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, staff.getStaffName());
            stmt.setString(2, staff.getStaffPosition());
            stmt.setString(3, staff.getStaffPassword());
            stmt.setString(4, staff.getStaffGender());
            stmt.setString(5, staff.getStaffBirthday());
            stmt.setString(6, staff.getStaffIc());
            stmt.setString(7, staff.getStaffPhone());
            stmt.setString(8, staff.getStaffAddress());
            stmt.setString(9, staff.getStaffEmail());
            stmt.setString(10, staff.getStaffStatus());
            stmt.setInt(11, staff.getStaffQuestion());
            stmt.setString(12, staff.getStaffAnswer());
            stmt.setString(13, staff.getStaffId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteRecord(String id) {
        String deleteStr = "DELETE FROM " + tableName + " WHERE STAFF_ID = ?";
        try {
            stmt = conn.prepareStatement(deleteStr);
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Staff lastID() {
        Staff staff = null;
        try {
            String sqlStr = "SELECT * FROM " + tableName + " ORDER BY STAFF_ID";
            stmt = conn.prepareStatement(sqlStr, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();
            rs.last();
            staff = new Staff(rs.getString(1));
        }
        catch (SQLException ex) {
            return staff;
        }
        return staff;
    }

    public ArrayList<Staff> getAllStaff() {
        ArrayList<Staff> staffList = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(sqlQueryStr);
            rs = stmt.executeQuery();

            while (rs.next()) {
                staffList.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return staffList;
    }

    public Staff getRecordID() {
        Staff staff = null;
        try {
            staff = new Staff(rs.getString(1), rs.getString(2),
                    rs.getString(3), rs.getString(4), rs.getString(5),
                    rs.getString(6), rs.getString(7), rs.getString(8),
                    rs.getString(9), rs.getString(10), rs.getString(11),
                    rs.getInt(12), rs.getString(13));
        }

        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return staff;
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
