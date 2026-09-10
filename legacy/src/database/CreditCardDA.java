package database;

import domain.Creditcard;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author Jeffrey
 */
public class CreditCardDA {

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";
    private final String tableName = "creditcard";
    private final String sqlQueryStr = "SELECT * FROM " + tableName;
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    public CreditCardDA() {
        createConnection();
    }

    public void addRecord(Creditcard credit) {
        String insertStr = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, credit.getCardId());
            stmt.setLong(2, credit.getCardNo());
            stmt.setString(3, credit.getCardHoldName());
            stmt.setString(4, credit.getCardType());
            stmt.setString(5, credit.getCardBank());
            stmt.setString(6, credit.getCardExp());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Creditcard getRecord(String id) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE CARD_ID = ?";
        Creditcard creditcard = null;
        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                creditcard = new Creditcard(rs.getString(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return creditcard;
    }

    public void updateRecord(Creditcard credit) {
        String insertStr = "UPDATE " + tableName + " SET CARD_NO = ?, CARD_HOLD_NAME = ?, CARD_TYPE = ?, CARD_BANK = ?, CARD_EXP = ? WHERE CARD_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(6, credit.getCardId());
            stmt.setLong(1, credit.getCardNo());
            stmt.setString(2, credit.getCardHoldName());
            stmt.setString(3, credit.getCardType());
            stmt.setString(4, credit.getCardBank());
            stmt.setString(5, credit.getCardExp());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteRecord(String id) {
        String deleteStr = "DELETE FROM " + tableName + " WHERE CARD_ID = ?";
        try {
            stmt = conn.prepareStatement(deleteStr);
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String lastID() {
        String id = null;
        try {
            String sqlStr = "SELECT * FROM " + tableName;
            Statement s2 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = s2.executeQuery(sqlStr);
            rs.afterLast();

            while (rs.previous()) {
                id = rs.getString("CARD_ID");
                break;
            }
        }
        catch (SQLException ex) {
            return id;
        }
        return id;
    }

    public ArrayList<Creditcard> getAllCreditCard() {
        ArrayList<Creditcard> creditcards = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(sqlQueryStr);
            rs = stmt.executeQuery();

            while (rs.next()) {
                creditcards.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return creditcards;
    }

    public Creditcard getRecordID() {

        Creditcard credit = null;
        try {
            credit = new Creditcard(rs.getString(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
        }

        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return credit;
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
