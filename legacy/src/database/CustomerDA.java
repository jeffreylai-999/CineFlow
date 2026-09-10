package database;

import domain.Customer;
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
public class CustomerDA {

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";
    private final String tableName = "customer";
    private final String sqlQueryStr = "SELECT * FROM " + tableName;
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    public CustomerDA() {
        createConnection();
    }

    public void addRecord(Customer cus) {
        String insertStr = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, cus.getCusId());
            stmt.setString(2, cus.getCusName());
            stmt.setString(3, cus.getCusGender());
            stmt.setString(4, cus.getCusBirthday());
            stmt.setString(5, cus.getCusIc());
            stmt.setString(6, cus.getCusPhone());
            stmt.setString(7, cus.getCusAddress());
            stmt.setString(8, cus.getCusEmail());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Customer getRecord(String id) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE CUS_ID = ?";
        Customer cus = null;
        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                cus = new Customer(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8));
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return cus;
    }

    public void updateRecord(Customer cus) {
        String insertStr = "UPDATE " + tableName + " SET CUS_NAME = ?, CUS_GENDER = ?, CUS_BIRTHDAY = ?, CUS_IC = ?, CUS_PHONE = ?, CUS_ADDRESS = ?, CUS_EMAIL = ? WHERE CUS_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, cus.getCusName());
            stmt.setString(2, cus.getCusGender());
            stmt.setString(3, cus.getCusBirthday());
            stmt.setString(4, cus.getCusIc());
            stmt.setString(5, cus.getCusPhone());
            stmt.setString(6, cus.getCusAddress());
            stmt.setString(7, cus.getCusEmail());
            stmt.setString(8, cus.getCusId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteRecord(String id) {
        String deleteStr = "DELETE FROM " + tableName + " WHERE CUS_ID = ?";
        try {
            stmt = conn.prepareStatement(deleteStr);
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Customer lastID() {
        Customer cus = null;
        try {
            String sqlStr = "SELECT * FROM " + tableName + " ORDER BY CUS_ID";
            stmt = conn.prepareStatement(sqlStr, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();
            rs.last();
            cus = new Customer(rs.getString(1));
        }
        catch (SQLException ex) {
            return cus;
        }
        return cus;
    }

    public ArrayList<Customer> getAllMember() {
        ArrayList<Customer> cus = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(sqlQueryStr);
            rs = stmt.executeQuery();

            while (rs.next()) {
                cus.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return cus;
    }

    public Customer getRecordID() {

        Customer cus = null;
        try {
            cus = new Customer(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8));
        }

        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return cus;
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
