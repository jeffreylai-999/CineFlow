package database;

import domain.Creditcard;
import domain.Customer;
import domain.Payment;
import domain.Showtime;
import domain.Staff;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Jeffrey
 */
public class PaymentDA {

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";
    private final String tableName = "payment";
    private final String sqlQueryStr = "SELECT * FROM " + tableName;
    private final StaffDA staffDA = new StaffDA();
    private final CustomerDA customerDA = new CustomerDA();
    private final ShowTimeDA showTimeDA = new ShowTimeDA();
    private final CreditCardDA creditCardDA = new CreditCardDA();
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    public PaymentDA() {
        createConnection();
    }

    public void addPayment(Payment payment) {
        String insertStr = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, payment.getPaymentId());
            stmt.setString(2, payment.getStaffId().getStaffId());
            stmt.setString(3, payment.getShowId().getShowId());
            stmt.setString(4, payment.getCusType());
            stmt.setInt(5, payment.getAdult());
            stmt.setInt(6, payment.getChild());
            stmt.setString(7, payment.getSeatNo());
            stmt.setBigDecimal(8, payment.getTotalPrice());
            stmt.setString(9, payment.getReservationId());
            stmt.setString(10, payment.getCusId().getCusId());
            stmt.setString(11, payment.getPaymentType());
            stmt.setString(12, payment.getCardId().getCardId());
            stmt.setString(13, payment.getPaymentDate());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addPaymentWithout(Payment payment) {
        String insertStr = "INSERT INTO PAYMENT (PAYMENT_ID, STAFF_ID, SHOW_ID, CUS_TYPE, ADULT, CHILD, SEAT_NO, TOTAL_PRICE, RESERVATION_ID) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, payment.getPaymentId());
            stmt.setString(2, payment.getStaffId().getStaffId());
            stmt.setString(3, payment.getShowId().getShowId());
            stmt.setString(4, payment.getCusType());
            stmt.setInt(5, payment.getAdult());
            stmt.setInt(6, payment.getChild());
            stmt.setString(7, payment.getSeatNo());
            stmt.setBigDecimal(8, payment.getTotalPrice());
            stmt.setString(9, payment.getReservationId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateCustomer(Payment payment) {
        String insertStr = "UPDATE " + tableName + " SET CUS_ID = ? WHERE PAYMENT_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(2, payment.getPaymentId());
            stmt.setString(1, payment.getCusId().getCusId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateCard(Payment payment) {
        String insertStr = "UPDATE " + tableName + " SET CARD_ID = ? WHERE PAYMENT_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(2, payment.getPaymentId());
            stmt.setString(1, payment.getCardId().getCardId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updatePaymentType(Payment payment) {
        String insertStr = "UPDATE " + tableName + " SET PAYMENT_TYPE = ? WHERE PAYMENT_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(2, payment.getPaymentId());
            stmt.setString(1, payment.getPaymentType());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updatePaymentDate(Payment payment) {
        String insertStr = "UPDATE " + tableName + " SET PAYMENT_DATE = ? WHERE PAYMENT_ID = ?";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(2, payment.getPaymentId());
            stmt.setString(1, payment.getPaymentDate());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteRecord(String id) {
        String deleteStr = "DELETE FROM " + tableName + " WHERE PAYMENT_ID = ?";
        try {
            stmt = conn.prepareStatement(deleteStr);
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Payment getRecord(String id) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE PAYMENT_ID = ?";
        Payment payment = null;
        Customer customer = null;
        Showtime showtime = null;
        Staff staff = null;
        Creditcard creditcard = null;
        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                staff = staffDA.getRecord(rs.getString("staff_id"));
                customer = customerDA.getRecord(rs.getString("CUS_ID"));
                showtime = showTimeDA.getRecord(rs.getString("SHOW_ID"));
                creditcard = creditCardDA.getRecord(rs.getString("CARD_ID"));

                payment = new Payment(rs.getString(1), staff, showtime,
                        rs.getString(4), rs.getInt(5), rs.getInt(6),
                        rs.getString(7), rs.getBigDecimal(8), rs.getString(9),
                        customer, rs.getString(11), creditcard,
                        rs.getString(13));
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return payment;
    }

    public ArrayList<Payment> getReservationPayments() {
        ArrayList<Payment> payment = new ArrayList<>();
        String queryStr = "SELECT * FROM " + tableName + " WHERE CUS_TYPE = 'reservation'";
        try {
            stmt = conn.prepareStatement(queryStr);
            rs = stmt.executeQuery();

            while (rs.next()) {
                payment.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return payment;
    }

    public String paymentLastID() {
        String id = null;
        try {
            String sqlStr = "SELECT * FROM " + tableName + " ORDER BY PAYMENT_ID";
            Statement s2 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = s2.executeQuery(sqlStr);
            rs.afterLast();

            while (rs.previous()) {
                id = rs.getString("PAYMENT_ID");
                break;
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return id;
    }

    public String reservationLastID() {
        String id = null;
        try {
            String sqlStr = "SELECT * FROM " + tableName + " where cus_type = 'reservation' order by reservation_id";
            Statement s2 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = s2.executeQuery(sqlStr);
            rs.afterLast();
            while (rs.previous()) {
                id = rs.getString("reservation_id");
                break;
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return id;
    }

    public ArrayList<Payment> getAll() {
        ArrayList<Payment> payment = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(sqlQueryStr);
            rs = stmt.executeQuery();

            while (rs.next()) {
                payment.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return payment;
    }

    public ArrayList<Payment> getSeats(String type, String show) {
        ArrayList<Payment> payment = new ArrayList<>();
        String sqlStr = "SELECT * FROM " + tableName + " WHERE CUS_TYPE = ? AND SHOW_ID = ?";
        try {
            stmt = conn.prepareStatement(sqlStr);
            stmt.setString(1, type);
            stmt.setString(2, show);
            rs = stmt.executeQuery();

            while (rs.next()) {
                payment.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return payment;
    }

    public Payment getRecordID() {
        Payment payment = null;
        Customer customer = null;
        Showtime showtime = null;
        Staff staff = null;
        Creditcard creditcard = null;

        try {
            staff = staffDA.getRecord(rs.getString("staff_id"));
            customer = customerDA.getRecord(rs.getString("CUS_ID"));
            showtime = showTimeDA.getRecord(rs.getString("SHOW_ID"));
            creditcard = creditCardDA.getRecord(rs.getString("CARD_ID"));
            payment = new Payment(rs.getString(1), staff, showtime,
                    rs.getString(4), rs.getInt(5), rs.getInt(6),
                    rs.getString(7), rs.getBigDecimal(8), rs.getString(9),
                    customer, rs.getString(11), creditcard,
                    rs.getString(13));
        }

        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return payment;
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
