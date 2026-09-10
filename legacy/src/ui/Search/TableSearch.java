package ui.Search;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class TableSearch extends JPanel {

    private static final String DATABASE_URL = "jdbc:derby://localhost:1527/cinemas";
    private static final String USERNAME = "APP_USER";
    private static final String PASSWORD = "changeme";
    private String QUERY;
    private ResultSetTableModel tableModel;
    private TableRowSorter<TableModel> sorter;
    private JTable resultTable;

    public TableSearch(String table) {
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new BorderLayout());
        switch (table) {
            case "customer":
                QUERY = "SELECT CUS_ID AS ID, cus_name as Name, cus_gender as Gender, cus_ic as IC, cus_email as Email FROM customer";
                break;
            case "staff":
                QUERY = "SELECT staff_ID AS ID, staff_name as Name, staff_gender as Gender, staff_ic as IC, staff_phone as Contact_Number, staff_status as Status FROM staff";
                break;
            case "movie":
                QUERY = "SELECT movie_id as ID, movie_name as Movie, movie_price as Price, running_time as Duration, release_date as Release_Date, offline_date as Offline_Date FROM movie";
                break;
            case "hall":
                QUERY = "SELECT hall_id as ID, hall_location as Location, hall_status as Status FROM hall";
                break;
            case "reservation":
                QUERY = "SELECT payment_id as ID, show_id as Show_ID, cus_id as Customer_ID, reservation_id as Reservation_ID, adult as Adult_Seat, child as Childe_Seats, total_price as Price, payment_date as Payment_Date FROM payment WHERE cus_type = 'reservation'";
                break;
            case "walkin":
                QUERY = "SELECT payment_id as ID, show_id as Show_ID, cus_id as Customer_ID, adult as Adult_Seat, child as Childe_Seats, total_price as Price, payment_date as Payment_Date FROM payment WHERE cus_type = 'walkin'";
                break;
        }

        try {

            tableModel = new ResultSetTableModel(DATABASE_URL, USERNAME, PASSWORD, QUERY);
            resultTable = new JTable(tableModel);
            main.add(new JScrollPane(resultTable), BorderLayout.CENTER);
            sorter = new TableRowSorter<>(tableModel);
            resultTable.setRowSorter(sorter);
            add(main);
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            tableModel.disconnectFromDatabase();
            System.exit(1);
        }
    }

    private class WindowCloseListener extends WindowAdapter {

        @Override
        public void windowClosed(WindowEvent event) {
            tableModel.disconnectFromDatabase();
            System.exit(0);
        }
    }
}
