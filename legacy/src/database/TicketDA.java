package database;

import domain.Payment;
import domain.Ticket;
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
public class TicketDA {

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";
    private final String tableName = "ticket";
    private final String sqlQueryStr = "SELECT * FROM " + tableName;
    private final PaymentDA da = new PaymentDA();
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    public TicketDA() {
        createConnection();
    }

    public void addRecord(Ticket ticket) {
        String insertStr = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?, ?, ?, ?)";
        try {
            stmt = conn.prepareStatement(insertStr);
            stmt.setString(1, ticket.getTicketId());
            stmt.setString(2, ticket.getSeatNo());
            stmt.setString(3, ticket.getTicketType());
            stmt.setString(4, ticket.getTicketPrice());
            stmt.setString(5, ticket.getGoodsServiceTax());
            stmt.setString(6, ticket.getEntertainmentTax());
            stmt.setString(7, ticket.getPaymentId().getPaymentId());
            stmt.executeUpdate();
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Ticket getRecord(String id) {
        String queryStr = "SELECT * FROM " + tableName + " WHERE TICKET_ID = ?";
        Ticket ticket = null;
        Payment payment = null;

        try {
            stmt = conn.prepareStatement(queryStr);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                payment = da.getRecord(rs.getString("PAYMENT_ID"));
                ticket = new Ticket(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), payment);
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return ticket;
    }

    public Ticket lastID() {
        Ticket ticket = null;
        try {
            String sqlStr = "SELECT * FROM " + tableName + " ORDER BY TICKET_ID";
            stmt = conn.prepareStatement(sqlStr, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();
            rs.last();
            ticket = new Ticket(rs.getString(1));
        }
        catch (SQLException ex) {
            return ticket;
        }
        return ticket;
    }

    public ArrayList<Ticket> getAll() {
        ArrayList<Ticket> ticket = new ArrayList<>();

        try {
            stmt = conn.prepareStatement(sqlQueryStr);
            rs = stmt.executeQuery();

            while (rs.next()) {
                ticket.add(getRecordID());
            }
        }
        catch (SQLException ex) {
            ex.getMessage();
        }
        return ticket;
    }

    public Ticket getRecordID() {
        Payment payment = null;
        Ticket ticket = null;
        try {
            payment = da.getRecord(rs.getString("PAYMENT_ID"));
            ticket = new Ticket(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), payment);
        }

        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        return ticket;
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
