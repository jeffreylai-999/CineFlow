/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import control.MaintainPayment;
import domain.Customer;
import domain.Payment;
import domain.Showtime;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import ui.Payment.AddPayment;
import ui.Payment.CalculatePrice;

/**
 *
 * @author Jeffrey
 */
public class SeatPlan extends JPanel {

    //Genereal
    private final ArrayList<String> walkinList = new ArrayList<>();
    private final ArrayList<String> bookingList = new ArrayList<>();
    private final ArrayList<String> chooseSeats = new ArrayList<>();
    private final MaintainPayment progControlPayment = new MaintainPayment();
    private final CalculatePrice cp = new CalculatePrice();
    private ArrayList<Payment> walkinSeats = new ArrayList<>();
    private ArrayList<Payment> reservationSeats = new ArrayList<>();
    private String newReservationID, type = null;
    private String[] walk, booking;
    private int adult = 0, child = 0, totalSeats = 0;
    private Customer customer = new Customer();
    private Showtime showtime = new Showtime();

    //Buttons
    private final JButton available = new JButton("");
    private final JButton unavailable = new JButton("");
    private final JButton reservation = new JButton("");
    private final JButton selected = new JButton("");
    private final JButton[][] jbtSeats = new JButton[10][14];
    private final JButton jbtConfirm = new JButton("    Confirm    ");
    private final JButton jbtReset = new JButton("    Reset    ");
    private final JButton screen = new JButton("Big Screen");

    //Date
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
    private final LocalDate today = new LocalDate();
    private final LocalTime localTime = new LocalTime();

    //Border style   
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Choose Seats ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public SeatPlan(Customer cus, Showtime show, String type, int adultSeat, int childSeat) {
        //Button color
        jbtConfirm.setForeground(Color.WHITE);
        jbtReset.setForeground(Color.WHITE);
        jbtConfirm.setBackground(new Color(74, 139, 245));
        jbtReset.setBackground(new Color(74, 139, 245));

        //List of Buttons & Buttons Function       
        JPanel listButton = new JPanel(new FlowLayout(FlowLayout.LEADING, 13, 13));
        JLabel jlblavailable = new JLabel("available");
        JLabel jlblunavailable = new JLabel("unavailable");
        JLabel jlblreserved = new JLabel("reserved");
        JLabel jlblselected = new JLabel("selected");

        listButton.setOpaque(false);
        listButton.add(available);
        listButton.add(jlblavailable);
        listButton.add(unavailable);
        listButton.add(jlblunavailable);
        listButton.add(reservation);
        listButton.add(jlblreserved);
        listButton.add(selected);
        listButton.add(jlblselected);

        //Buttons Color
        available.setBackground(Color.WHITE);
        unavailable.setBackground(Color.GRAY);
        reservation.setBackground(new Color(255, 114, 114));
        selected.setBackground(Color.BLUE);
        available.setEnabled(false);
        unavailable.setEnabled(false);
        reservation.setEnabled(false);
        selected.setEnabled(false);

        //List of buttons
        jlblavailable.setForeground(Color.WHITE);
        jlblunavailable.setForeground(Color.WHITE);
        jlblreserved.setForeground(Color.WHITE);
        jlblselected.setForeground(Color.WHITE);

        //Reset and Confirm buttons
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.setOpaque(false);
        buttons.add(jbtReset);
        buttons.add(jbtConfirm);
        jbtConfirm.addActionListener(new confirmListener());
        jbtReset.addActionListener(new resetListener());

        //Reset and Confirm Buttons and List of Buttons Panel 
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(listButton);
        buttonPanel.add(buttons);

        //Screen
        screen.setEnabled(false);
        screen.setBackground(Color.BLACK);
        screen.setForeground(Color.WHITE);
        screen.setPreferredSize(new Dimension(240, 60));

        //Seats West
        JPanel seatsWest = new JPanel(new GridLayout(10, 2));
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 2; j++) {
                jbtSeats[i][j] = new JButton(Character.toString((char) (i + 65)) + String.format("%02d", j + 1));
                jbtSeats[i][j].setBackground(Color.WHITE);
                jbtSeats[i][j].addActionListener(new processSeatListener());
                seatsWest.add(jbtSeats[i][j]);
                //jbtSeats[i][j].setPreferredSize(new Dimension(60, 30));
            }
        }

        //Seats Centre
        JPanel seatsCentre = new JPanel(new GridLayout(10, 10));
        for (int i = 0; i < 10; i++) {
            for (int j = 2; j < 12; j++) {
                jbtSeats[i][j] = new JButton(Character.toString((char) (i + 65)) + String.format("%02d", j + 1));
                jbtSeats[i][j].setBackground(Color.WHITE);
                jbtSeats[i][j].addActionListener(new processSeatListener());
                seatsCentre.add(jbtSeats[i][j]);
                // jbtSeats[i][j].setPreferredSize(new Dimension(60, 30));
            }
        }

        //Seats East
        JPanel seatsEast = new JPanel(new GridLayout(10, 2));
        for (int i = 0; i < 10; i++) {
            for (int j = 12; j < 14; j++) {
                jbtSeats[i][j] = new JButton(Character.toString((char) (i + 65)) + String.valueOf(j + 1));
                jbtSeats[i][j].setBackground(Color.WHITE);
                jbtSeats[i][j].addActionListener(new processSeatListener());
                seatsEast.add(jbtSeats[i][j]);
                // jbtSeats[i][j].setPreferredSize(new Dimension(60, 30));
            }
        }

        //Set Layout
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.add(screen, BorderLayout.NORTH);
        main.add(seatsWest, BorderLayout.WEST);
        main.add(seatsCentre, BorderLayout.CENTER);
        main.add(seatsEast, BorderLayout.EAST);
        main.add(buttonPanel, BorderLayout.SOUTH);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));

        customer = cus;
        showtime = show;
        this.type = type;
        this.adult = adultSeat;
        this.child = childSeat;
        totalSeats = adultSeat + childSeat;
        setReservation();
        setSeats();

        /* setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Seats");
        setVisible(true);*/
    }

    private void setReservation() {
        String id = progControlPayment.reservationLastID();

        if (id != null) {
            int reservationID = Integer.parseInt(id.substring(1)) + 1;
            newReservationID = "R" + String.format("%09d", reservationID);
        }
        else {
            newReservationID = "R000000001";
        }
    }
    //Get the seats number that already booking or buy

    private void setSeats() {
        walkinSeats = progControlPayment.getSeats("walkin", showtime.getShowId());
        reservationSeats = progControlPayment.getSeats("reservation", showtime.getShowId());

        //Seats number that already buy
        if (walkinSeats != null) {
            for (Payment walkIn : walkinSeats) {
                String strServer = walkIn.getSeatNo().substring(1, walkIn.getSeatNo().length() - 1);
                walk = strServer.split(", ");

                for (String a1 : walk) {
                    String strRow = String.valueOf(a1.charAt(0)) + a1.charAt(1);
                    String strCol = String.valueOf(a1.charAt(2)) + a1.charAt(3);
                    int row = Integer.parseInt(strRow);
                    int col = Integer.parseInt(strCol);
                    jbtSeats[row][col].setEnabled(false);
                    jbtSeats[row][col].setBackground(Color.GRAY);
                    UIManager.put("jbtSeats.foreground", Color.blue);
                    walkinList.add(strRow + strCol);
                }
            }
        }

        //Seats numbe already booking
        if (reservationSeats != null) {
            for (Payment r : reservationSeats) {
                String strServer = r.getSeatNo().substring(1, r.getSeatNo().length() - 1);
                booking = strServer.split(", ");

                for (String a1 : booking) {
                    String strRow = String.valueOf(a1.charAt(0)) + a1.charAt(1);
                    String strCol = String.valueOf(a1.charAt(2)) + a1.charAt(3);
                    int row = Integer.parseInt(strRow);
                    int col = Integer.parseInt(strCol);
                    jbtSeats[row][col].setEnabled(false);
                    jbtSeats[row][col].setBackground(new Color(255, 114, 114));
                    UIManager.put("jbtSeats[row][col].foreground", Color.blue);
                    bookingList.add(strRow + strCol);

                }
            }
        }
    }

    //Recovery the seats selected action
    private class resetListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 14; j++) {
                    jbtSeats[i][j].setEnabled(true);
                    jbtSeats[i][j].setBackground(Color.white);
                }
            }
            if (walkinList.size() > 0) {
                for (String a1 : walkinList) {
                    String strRow = String.valueOf(a1.charAt(0)) + a1.charAt(1);
                    String strCol = String.valueOf(a1.charAt(2)) + a1.charAt(3);
                    int row = Integer.parseInt(strRow);
                    int col = Integer.parseInt(strCol);
                    jbtSeats[row][col].setEnabled(false);
                    jbtSeats[row][col].setBackground(Color.GRAY);
                    UIManager.put("jbtSeats.foreground", Color.blue);
                }
            }
            if (bookingList.size() > 0) {
                for (String b1 : bookingList) {
                    String strRow = String.valueOf(b1.charAt(0)) + b1.charAt(1);
                    String strCol = String.valueOf(b1.charAt(2)) + b1.charAt(3);
                    int row = Integer.parseInt(strRow);
                    int col = Integer.parseInt(strCol);
                    jbtSeats[row][col].setEnabled(false);
                    jbtSeats[row][col].setBackground(new Color(255, 114, 114));
                    UIManager.put("jbtSeats[row][col].foreground", Color.blue);
                }
            }
            chooseSeats.clear();
            totalSeats = adult + child;
        }
    }

    private class processSeatListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 14; j++) {
                    if (event.getSource() == jbtSeats[i][j] && totalSeats != 0) {

                        jbtSeats[i][j].setBackground(Color.BLUE);

                        jbtSeats[i][j].setEnabled(false);
                        chooseSeats.add(String.format("%02d", i) + String.format("%02d", j));
                        Collections.sort(chooseSeats);
                        totalSeats -= 1;
                    }
                }
            }
        }
    }

    private class confirmListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (totalSeats == 0) {
                AddPayment addPayment = new AddPayment(showtime, type, adult,
                        child, chooseSeats.toString(),
                        newReservationID, customer);

                removeAll();
                add(addPayment);
                addPayment.setOpaque(false);
                revalidate();
                repaint();
            }
            else {
                JOptionPane.showMessageDialog(null, "You have " + totalSeats + " seat(s) has not select", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
