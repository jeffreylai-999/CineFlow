/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.WalkIn;

import control.MaintainCustomer;
import control.MaintainMovie;
import control.MaintainShowTime;
import domain.Customer;
import domain.Movie;
import domain.Showtime;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import ui.SeatPlan;

/**
 *
 * @author Jeffrey
 */
public final class AddWalkIn extends JPanel {

    //General
    private String strMovie, strDate, strTime;
    private int numOfAdultSeats = 0, numOfChildSeats = 0;
    private Movie movie = new Movie();
    private Customer customer = new Customer();
    private Showtime showtime = new Showtime();
    private ArrayList<Movie> moviesArrayList = new ArrayList<>();
    private ArrayList<Showtime> showArrayList = new ArrayList<>();
    private final MaintainCustomer progControlCustomer = new MaintainCustomer();
    private final MaintainShowTime progControlShow = new MaintainShowTime();
    private final MaintainMovie progControlMovie = new MaintainMovie();
    //Label
    private final JLabel jlID = new JLabel("Customer ID");
    private final JLabel jlMovie = new JLabel("Movie Name");
    private final JLabel jlDate = new JLabel("Movie Date");
    private final JLabel jlTime = new JLabel("Movie Time");
    private final JLabel jlAdult = new JLabel("Adult Seat(s)");
    private final JLabel jlChild = new JLabel("Child Seat(s)");
    private final JLabel jlErrorMessageID = new JLabel();
    private final JLabel jlErrorMessageMovie = new JLabel();
    private final JLabel jlErrorMessageDate = new JLabel();
    private final JLabel jlErrorMessageTime = new JLabel();
    private final JLabel jlErrorMessageAdultSeats = new JLabel();
    //Text Field
    private final JTextField jtfID = new JTextField(20);
    private final JTextField jtfAdult = new JTextField("0");
    private final JTextField jtfChild = new JTextField("0");
    //Button & ComboBox
    private final JButton jbtNext = new JButton("    Next    ");
    private final JButton jbtPulseAdult = new JButton("+");
    private final JButton jbtMinusAdult = new JButton("-");
    private final JButton jbtPulseChild = new JButton("+");
    private final JButton jbtMinusChild = new JButton("-");
    private final JComboBox jcbMovie = new JComboBox();
    private final JComboBox jcbDate = new JComboBox();
    private final JComboBox jcbTime = new JComboBox();
    //Date Time
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
    private final LocalDate today = new LocalDate();
    private final LocalTime localTime = new LocalTime();

    //Border style  
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Create Walk-In ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    //Main Panel
    JPanel main = new JPanel(new GridLayout(7, 1));

    public AddWalkIn() {
        //Set movie list
        setMovieList();

        //Button color
        jbtNext.setForeground(Color.WHITE);
        jbtMinusAdult.setForeground(Color.WHITE);
        jbtMinusChild.setForeground(Color.WHITE);
        jbtPulseAdult.setForeground(Color.WHITE);
        jbtPulseChild.setForeground(Color.WHITE);
        jbtNext.setBackground(new Color(74, 139, 245));
        jbtMinusAdult.setBackground(new Color(74, 139, 245));
        jbtMinusChild.setBackground(new Color(74, 139, 245));
        jbtPulseAdult.setBackground(new Color(74, 139, 245));
        jbtPulseChild.setBackground(new Color(74, 139, 245));

        //Button groups (Adult)
        JPanel buttonAdult = new JPanel(new GridLayout(1, 2, 7, 0));
        buttonAdult.add(jbtPulseAdult);
        buttonAdult.add(jbtMinusAdult);
        buttonAdult.setOpaque(false);
        jbtPulseAdult.addActionListener(new seatsPlaceListener());
        jbtMinusAdult.addActionListener(new seatsPlaceListener());

        //Button groups (Child)
        JPanel buttonChild_SCitizen = new JPanel(new GridLayout(1, 2, 7, 0));
        buttonChild_SCitizen.add(jbtPulseChild);
        buttonChild_SCitizen.add(jbtMinusChild);
        buttonChild_SCitizen.setOpaque(false);
        jbtPulseChild.addActionListener(new seatsPlaceListener());
        jbtMinusChild.addActionListener(new seatsPlaceListener());

        //Adult
        JPanel adult = new JPanel(new GridLayout(1, 2, 5, 0));
        adult.add(jtfAdult);
        adult.add(buttonAdult);
        adult.setOpaque(false);
        jtfAdult.setEditable(false);
        jtfAdult.setBackground(Color.WHITE);

        //Child
        JPanel child = new JPanel(new GridLayout(1, 2, 5, 0));
        child.add(jtfChild);
        child.add(buttonChild_SCitizen);
        child.setOpaque(false);
        jtfChild.setEditable(false);
        jtfChild.setBackground(Color.WHITE);

        //ID Panel
        JPanel idPanel = new JPanel(new GridLayout(2, 2));
        idPanel.add(jlID);
        idPanel.add(jtfID);
        idPanel.add(new JLabel());
        idPanel.add(jlErrorMessageID);
        idPanel.setOpaque(false);
        jtfID.setBackground(Color.WHITE);

        jlID.setFont(FontStyle);
        jlID.setForeground(Color.WHITE);
        jtfID.setBackground(Color.WHITE);

        //Movie panel
        JPanel moviePanel = new JPanel(new GridLayout(2, 2));
        moviePanel.add(jlMovie);
        moviePanel.add(jcbMovie);
        moviePanel.add(new JLabel());
        moviePanel.add(jlErrorMessageMovie);
        moviePanel.setOpaque(false);
        jlMovie.setFont(FontStyle);
        jlMovie.setForeground(Color.WHITE);
        jcbMovie.setBackground(Color.WHITE);
        jcbMovie.addActionListener(new retrieveMovieDateListener());

        //Date panel
        JPanel datePanel = new JPanel(new GridLayout(2, 2));
        datePanel.add(jlDate);
        datePanel.add(jcbDate);
        datePanel.add(new JLabel());
        datePanel.add(jlErrorMessageDate);
        datePanel.setOpaque(false);
        jlDate.setFont(FontStyle);
        jlDate.setForeground(Color.WHITE);
        jcbDate.setBackground(Color.WHITE);
        jcbDate.addActionListener(new setTimeListener());
        jcbDate.addItem("=============Select=============");

        //Time panel
        JPanel timePanel = new JPanel(new GridLayout(2, 2));
        timePanel.add(jlTime);
        timePanel.add(jcbTime);
        timePanel.add(new JLabel());
        timePanel.add(jlErrorMessageTime);
        timePanel.setOpaque(false);
        jlTime.setForeground(Color.WHITE);
        jlTime.setFont(FontStyle);
        jcbTime.setBackground(Color.WHITE);

        //Adult panel
        JPanel adultPanel = new JPanel(new GridLayout(2, 2));
        adultPanel.add(jlAdult);
        adultPanel.add(adult);
        adultPanel.add(new JLabel());
        adultPanel.add(jlErrorMessageAdultSeats);
        adultPanel.setOpaque(false);
        jlAdult.setForeground(Color.WHITE);
        jlAdult.setFont(FontStyle);

        //Child / Senior Citizen panel
        JPanel child_SCitizenPanel = new JPanel(new GridLayout(2, 2));
        child_SCitizenPanel.add(jlChild);
        child_SCitizenPanel.add(child);
        child_SCitizenPanel.add(new JLabel());
        child_SCitizenPanel.add(new JLabel());
        child_SCitizenPanel.setOpaque(false);
        jlChild.setForeground(Color.WHITE);
        jlChild.setFont(FontStyle);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel(""));
        buttons.add(jbtNext);
        buttons.setOpaque(false);
        jbtNext.addActionListener(new continuous());

        //Set general Layout
        setLayout(new FlowLayout());

        main.add(idPanel);
        main.add(moviePanel);
        main.add(datePanel);
        main.add(timePanel);
        main.add(adultPanel);
        main.add(child_SCitizenPanel);
        main.add(buttons);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
    }

    public void setMovieList() {
        moviesArrayList = progControlMovie.getAll();
        jcbMovie.addItem("=============Select=============");
        if (moviesArrayList != null) {
            moviesArrayList.forEach((mv) -> {
                LocalDate offline = dtf.parseLocalDate(mv.getOfflineDate());
                LocalDate releaseDate = dtf.parseLocalDate(mv.getReleaseDate());
                LocalDate showDate = today.plusDays(8);
                if ((today.isBefore(offline) || today.isEqual(offline)) && releaseDate.isBefore(showDate)) {
                    jcbMovie.addItem(mv.getMovieName());
                }
            });
        }
        else {
            jcbMovie.removeAllItems();
            jcbMovie.addItem("No Movie is available");
        }
    }

    private class retrieveMovieDateListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            movie = progControlMovie.selectRecordByName(jcbMovie.getSelectedItem().toString());
            jcbDate.removeAllItems();
            jcbDate.addItem("=============Select=============");
            jlErrorMessageMovie.setText(null);

            if (movie != null) {
                showArrayList = progControlShow.getShowTimes(movie.getMovieId());
                for (int i = 0; i < showArrayList.size(); i++) {
                    LocalDate showing = dtf.parseLocalDate(showArrayList.get(i).getShowDate());
                    LocalDate preShowingDate = today.plusDays(8);

                    //Show the movie date up to next week 
                    if (today.isBefore(showing) && showing.isBefore(preShowingDate) || today.isEqual(showing)) {
                        jcbDate.addItem(showing.toString("dd/MM/yyyy"));
                        i += 5;
                    }
                    else {
                        i += 5;
                    }
                }
            }
        }
    }

    private class setTimeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            jcbTime.removeAllItems();
            jcbTime.addItem("=============Select=============");
            if (jcbDate.getSelectedIndex() > 0) {
                LocalDate choosingDay = dtf.parseLocalDate((String) jcbDate.getSelectedItem());
                //Set the time of movies
                if (today.isEqual(choosingDay)) {
                    if (localTime.isBefore(new LocalTime(2, 0, 0))) {
                        jcbTime.addItem("0100");
                        jcbTime.addItem("1000");
                        jcbTime.addItem("1300");
                        jcbTime.addItem("1600");
                        jcbTime.addItem("1900");
                        jcbTime.addItem("2200");
                    }
                    else if (localTime.isBefore(new LocalTime(11, 0, 0))) {
                        jcbTime.addItem("1000");
                        jcbTime.addItem("1300");
                        jcbTime.addItem("1600");
                        jcbTime.addItem("1900");
                        jcbTime.addItem("2200");
                    }
                    else if (localTime.isBefore(new LocalTime(14, 0, 0))) {
                        jcbTime.addItem("1300");
                        jcbTime.addItem("1600");
                        jcbTime.addItem("1900");
                        jcbTime.addItem("2200");
                    }
                    else if (localTime.isBefore(new LocalTime(17, 0, 0))) {
                        jcbTime.addItem("1600");
                        jcbTime.addItem("1900");
                        jcbTime.addItem("2200");
                    }
                    else if (localTime.isBefore(new LocalTime(20, 0, 0))) {
                        jcbTime.addItem("1900");
                        jcbTime.addItem("2200");
                    }
                    else if (localTime.isBefore(new LocalTime(23, 0, 0))) {
                        jcbTime.addItem("2200");
                    }
                    else {
                        jcbTime.removeAllItems();
                        jcbTime.addItem("Not available");
                    }
                }
                else {
                    jcbTime.addItem("0100");
                    jcbTime.addItem("1000");
                    jcbTime.addItem("1300");
                    jcbTime.addItem("1600");
                    jcbTime.addItem("1900");
                    jcbTime.addItem("2200");
                }
            }
        }
    }

    private class continuous implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            //If ID not empty
            if (!jtfID.getText().isEmpty()) {
                //Get the customer record
                customer = progControlCustomer.selectRecord(jtfID.getText().toUpperCase());
                //If customer record is exist
                if (customer != null) {
                    //Make sure the other drop-down list is selected
                    if (validation()) {
                        strMovie = movie.getMovieId();
                        strDate = jcbDate.getSelectedItem().toString();
                        strTime = jcbTime.getSelectedItem().toString();
                        showtime = progControlShow.selectRecord(strMovie, strDate, strTime);
                        SeatPlan seatPlan = new SeatPlan(customer, showtime,
                                "walkin",
                                Integer.parseInt(jtfAdult.getText()),
                                Integer.parseInt(jtfChild.getText()));

                        removeAll();
                        add(seatPlan);
                        seatPlan.setOpaque(false);
                        revalidate();
                        repaint();
                    }
                } //If customer record is not exist
                else {
                    //Clear the ID field and prompt out error message
                    jtfID.setText(null);
                    jtfID.requestFocus();
                    jlErrorMessageID.setText("Can not found the ID");
                    jlErrorMessageID.setForeground(new Color(255, 114, 114));
                    jlErrorMessageMovie.setText(null);
                    jlErrorMessageDate.setText(null);
                    jlErrorMessageTime.setText(null);
                }
            } //If ID is blank, direct check other drop-down list is selected
            else if (validation()) {
                strMovie = movie.getMovieId();
                strDate = jcbDate.getSelectedItem().toString();
                strTime = jcbTime.getSelectedItem().toString();
                customer = null;
                showtime = progControlShow.selectRecord(strMovie, strDate, strTime);
                SeatPlan seatPlan = new SeatPlan(customer, showtime,
                        "walkin",
                        Integer.parseInt(jtfAdult.getText()),
                        Integer.parseInt(jtfChild.getText()));

                removeAll();
                add(seatPlan);
                seatPlan.setOpaque(false);
                revalidate();
                repaint();
            }
        }
    }

    private boolean validation() {
        /*
        - Make sure all drop-down list have been selected
        - If any drop-down list is not selected, error message will prompt out
        - And return false, otherwise return true
         */
        jlErrorMessageID.setText(null);
        jlErrorMessageMovie.setText(null);
        jlErrorMessageDate.setText(null);
        jlErrorMessageTime.setText(null);
        jlErrorMessageAdultSeats.setText(null);

        if (jcbMovie.getSelectedIndex() <= 0) {
            jlErrorMessageMovie.setText("Please select the movie");
            jlErrorMessageMovie.setForeground(new Color(255, 114, 114));
            return false;
        }
        else if (jcbDate.getSelectedIndex() <= 0) {
            jlErrorMessageDate.setText("Please select the date");
            jlErrorMessageDate.setForeground(new Color(255, 114, 114));
            return false;
        }
        else if (jcbTime.getSelectedIndex() <= 0) {
            jlErrorMessageTime.setText("Please select the time");
            jlErrorMessageTime.setForeground(new Color(255, 114, 114));
            return false;
        }
        else if (jtfAdult.getText().equals("0") && jtfChild.getText().equals("0")) {
            jlErrorMessageAdultSeats.setText("Please select the number(s) of seats");
            jlErrorMessageAdultSeats.setForeground(new Color(255, 114, 114));
            return false;
        }
        else {
            return true;
        }
    }

    private class seatsPlaceListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == jbtPulseAdult) {
                numOfAdultSeats += 1;
                jtfAdult.setText(String.valueOf(numOfAdultSeats));
            }
            if (event.getSource() == jbtMinusAdult) {
                numOfAdultSeats -= 1;

                if (numOfAdultSeats < 0) {
                    numOfAdultSeats = 0;
                    jtfAdult.setText(String.valueOf(numOfAdultSeats));
                }
                else {
                    jtfAdult.setText(String.valueOf(numOfAdultSeats));
                }
            }

            if (event.getSource() == jbtPulseChild) {
                numOfChildSeats += 1;
                jtfChild.setText(String.valueOf(numOfChildSeats));
            }
            if (event.getSource() == jbtMinusChild) {
                numOfChildSeats -= 1;

                if (numOfChildSeats < 0) {
                    numOfChildSeats = 0;
                    jtfChild.setText(String.valueOf(numOfChildSeats));
                }
                else {
                    jtfChild.setText(String.valueOf(numOfChildSeats));
                }
            }
        }
    }
}
