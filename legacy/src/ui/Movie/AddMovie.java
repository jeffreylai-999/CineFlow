package ui.Movie;

import control.MaintainHall;
import control.MaintainMovie;
import domain.Hall;
import domain.Movie;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import ui.Showtime.AddShowTime;
import ui.Validation;

/**
 *
 * @author Jeffrey
 */
public class AddMovie extends JPanel {

    //General
    private Movie movie = new Movie();
    private Hall hall = new Hall();
    private ArrayList<Hall> hallArrayList = new ArrayList<>();
    private final MaintainMovie progControlMovie = new MaintainMovie();
    private final MaintainHall progControlHall = new MaintainHall();
    private final Validation v = new Validation();

    private final String[] monthArray = {"Month", "January", "February",
        "March", "April", "May", "June", "July", "August", "September",
        "October", "November", "December"};
    private final String[] genreArray = {"=================Select=================",
        "Action", "Science Fiction",
        "Horror", "Drama", "Romance", "Animation", "Comedy"};
    //Label
    private final JLabel jlID = new JLabel("Movie ID");
    private final JLabel jlName = new JLabel("Movie Name");
    private final JLabel jlPrice = new JLabel("Price");
    private final JLabel jlRunTime = new JLabel("Running Time (Minutes)");
    private final JLabel jlGenre = new JLabel("Genre");
    private final JLabel jlRelease = new JLabel("Release Date");
    private final JLabel jlOffline = new JLabel("Offline Date");
    private final JLabel jlHall = new JLabel("Hall ID");
    private final JLabel jlErrorMessageName = new JLabel();
    private final JLabel jlErrorMessagePrice = new JLabel();
    private final JLabel jlErrorMessageRuntime = new JLabel();
    private final JLabel jlErrorMessageGenre = new JLabel();
    private final JLabel jlErrorMessageRelease = new JLabel();
    private final JLabel jlErrorMessageOffline = new JLabel();
    private final JLabel jlErrorMessageHall = new JLabel();
    //Text Field
    private final JTextField jtfID = new JTextField();
    private final JTextField jtfName = new JTextField();
    private final JTextField jtfPrice = new JTextField();
    private final JTextField jtfRunTime = new JTextField();
    //Button & ComboBox
    private final JButton jbtConfirm = new JButton("    Confirm    ");

    private final JComboBox jcbGenre = new JComboBox(genreArray);
    private final JComboBox jcbReleaseDay = new JComboBox();
    private final JComboBox jcbReleaseMon = new JComboBox(monthArray);
    private final JComboBox jcbReleaseYear = new JComboBox();
    private final JComboBox jcbOffDay = new JComboBox();
    private final JComboBox jcbOffMon = new JComboBox(monthArray);
    private final JComboBox jcbOffYear = new JComboBox();
    private final JComboBox jcbHall = new JComboBox();
    //Date Time
    private final LocalDate localDate = new LocalDate();
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
    private final int currentYear = localDate.getYear();

    //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED, 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Create Movie ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public AddMovie() {
        //Set ID
        setMovieID_HallID();

        //Set drop-down list of release date and offline date
        jcbReleaseDay.addItem("Day");        //Set days
        for (int i = 1; i <= 31; i++) {
            jcbReleaseDay.addItem(String.format("%02d", i));
        }

        jcbReleaseYear.addItem("Year");      //Set Year until the current year
        for (int i = currentYear; i <= currentYear + 1; i++) {
            jcbReleaseYear.addItem(i);
        }

        jcbOffDay.addItem("Day");        //Set days
        for (int i = 1; i <= 31; i++) {
            jcbOffDay.addItem(String.format("%02d", i));
        }

        jcbOffYear.addItem("Year");      //Set Year until the current year
        for (int i = currentYear; i <= currentYear + 1; i++) {
            jcbOffYear.addItem(i);
        }

        //Button color
        jbtConfirm.setForeground(Color.WHITE);
        jbtConfirm.setBackground(new Color(74, 139, 245));

        //Release date
        JPanel releaseDate = new JPanel(new GridLayout(1, 3, 10, 0));
        releaseDate.add(jcbReleaseDay);
        releaseDate.add(jcbReleaseMon);
        releaseDate.add(jcbReleaseYear);
        releaseDate.setOpaque(false);
        jcbReleaseDay.setBackground(Color.WHITE);
        jcbReleaseMon.setBackground(Color.WHITE);
        jcbReleaseYear.setBackground(Color.WHITE);

        //Offline date
        JPanel offlineDate = new JPanel(new GridLayout(1, 3, 10, 0));
        offlineDate.add(jcbOffDay);
        offlineDate.add(jcbOffMon);
        offlineDate.add(jcbOffYear);
        offlineDate.setOpaque(false);
        jcbOffDay.setBackground(Color.WHITE);
        jcbOffMon.setBackground(Color.WHITE);
        jcbOffYear.setBackground(Color.WHITE);

        //ID Panel
        JPanel id = new JPanel(new GridLayout(2, 2));
        id.add(jlID);
        id.add(jtfID);
        id.add(new JLabel());
        id.add(new JLabel());
        id.setOpaque(false);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);
        jtfID.setEditable(false);
        jtfID.setBackground(Color.WHITE);

        //Name Panel
        JPanel name = new JPanel(new GridLayout(2, 2));
        name.add(jlName);
        name.add(jtfName);
        name.add(new JLabel());
        name.add(jlErrorMessageName);
        name.setOpaque(false);
        jlName.setForeground(Color.WHITE);
        jlName.setFont(FontStyle);

        //Price Panel
        JPanel price = new JPanel(new GridLayout(2, 2));
        price.add(jlPrice);
        price.add(jtfPrice);
        price.add(new JLabel());
        price.add(jlErrorMessagePrice);
        price.setOpaque(false);
        jlPrice.setForeground(Color.WHITE);
        jlPrice.setFont(FontStyle);

        //Running Time Panel
        JPanel runnigTime = new JPanel(new GridLayout(2, 2));
        runnigTime.add(jlRunTime);
        runnigTime.add(jtfRunTime);
        runnigTime.add(new JLabel());
        runnigTime.add(jlErrorMessageRuntime);
        runnigTime.setOpaque(false);
        jlRunTime.setForeground(Color.WHITE);
        jlRunTime.setFont(FontStyle);

        //Genre Panel
        JPanel genre = new JPanel(new GridLayout(2, 2));
        genre.add(jlGenre);
        genre.add(jcbGenre);
        genre.add(new JLabel());
        genre.add(jlErrorMessageGenre);
        genre.setOpaque(false);
        jlGenre.setForeground(Color.WHITE);
        jlGenre.setFont(FontStyle);
        jcbGenre.setBackground(Color.WHITE);

        //Release Date Panel
        JPanel release = new JPanel(new GridLayout(2, 2));
        release.add(jlRelease);
        release.add(releaseDate);
        release.add(new JLabel());
        release.add(jlErrorMessageRelease);
        release.setOpaque(false);
        jlRelease.setForeground(Color.WHITE);
        jlRelease.setFont(FontStyle);

        //Offline Date Panel
        JPanel offline = new JPanel(new GridLayout(2, 2));
        offline.add(jlOffline);
        offline.add(offlineDate);
        offline.add(new JLabel());
        offline.add(jlErrorMessageOffline);
        offline.setOpaque(false);
        jlOffline.setForeground(Color.WHITE);
        jlOffline.setFont(FontStyle);

        //Hall Panel
        JPanel halls = new JPanel(new GridLayout(2, 2));
        halls.add(jlHall);
        halls.add(jcbHall);
        halls.add(new JLabel());
        halls.add(jlErrorMessageHall);
        halls.setOpaque(false);
        jlHall.setForeground(Color.WHITE);
        jlHall.setFont(FontStyle);
        jcbHall.setBackground(Color.WHITE);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel(""));
        buttons.add(jbtConfirm);
        buttons.setOpaque(false);
        jbtConfirm.addActionListener(new AddMovieListener());

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(9, 1, 0, 3));

        main.add(id);
        main.add(name);
        main.add(price);
        main.add(runnigTime);
        main.add(genre);
        main.add(release);
        main.add(offline);
        main.add(halls);
        main.add(buttons);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
    }

    private void setMovieID_HallID() {
        Movie mv = progControlMovie.lastID();

        if (mv != null) {
            int id = Integer.parseInt(mv.getMovieId().substring(3)) + 1;
            String newID = "MOV" + String.format("%07d", id);
            jtfID.setText(newID);
        }
        else {
            jtfID.setText("MOV0000001");
        }

        //Check the Hall is available
        setHallToAvailable();

        //Set the drop-down list of Hall ID
        hallArrayList = progControlHall.getAll();

        //Set the Hall ID list if it's available for use 
        jcbHall.addItem("=================Select=================");
        for (Hall h : hallArrayList) {
            if (h.getHallStatus().equals("Available")) {
                jcbHall.addItem(h.getHallId());
            }
        }
    }

    private void setHallToAvailable() {
        //Get all record of hall from database 
        hallArrayList = progControlHall.getAll();

        for (Hall h : hallArrayList) {
            //if the hall period have a date
            if (!h.getHallPeriod().equals("N/A")) {
                //Set the date
                LocalDate endDate = dtf.parseLocalDate(h.getHallPeriod());
                LocalDate today = new LocalDate();

                //Compare the date between today and the last day of using
                if (endDate.isBefore(today)) {
                    hall = new Hall(h.getHallId(), h.getHallLocation(), "Available", "N/A");
                    //Update the lastest status of hall
                    progControlHall.updateRecord(hall);
                }
            }
        }
    }

    private class AddMovieListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            //Clear the border color while
            Border original = jtfID.getBorder();
            jtfName.setBorder(original);
            jtfPrice.setBorder(original);
            jtfRunTime.setBorder(original);
            jlErrorMessageGenre.setText(null);
            jlErrorMessageHall.setText(null);
            jlErrorMessageName.setText(null);
            jlErrorMessageOffline.setText(null);
            jlErrorMessagePrice.setText(null);
            jlErrorMessageRelease.setText(null);
            jlErrorMessageRuntime.setText(null);

            //Get the variable
            int relYear = jcbReleaseYear.getSelectedIndex();
            int relMon = jcbReleaseMon.getSelectedIndex();
            int relDay = jcbReleaseDay.getSelectedIndex();
            int offYear = jcbOffYear.getSelectedIndex();
            int offMon = jcbOffMon.getSelectedIndex();
            int offDay = jcbOffDay.getSelectedIndex();

            if (validation(relYear, relMon, relDay, offYear, offMon, offDay)) {
                //Get the value
                String id = jtfID.getText();
                String name = jtfName.getText();
                String strPrice = jtfPrice.getText();
                double price = Double.parseDouble(strPrice);
                String p = String.format("%.2f", price);
                String runTime = jtfRunTime.getText();
                String genre = jcbGenre.getSelectedItem().toString();
                String releaseDate = String.format("%02d", relDay) + "/" + String.format("%02d", relMon) + "/" + jcbReleaseYear.getSelectedItem();
                String offlineDate = String.format("%02d", offDay) + "/" + String.format("%02d", offMon) + "/" + jcbOffYear.getSelectedItem();
                String strHallID = jcbHall.getSelectedItem().toString();

                //Get the value of hall from database
                hall = progControlHall.selectRecord(strHallID);
                hall = new Hall(strHallID, hall.getHallLocation(), "Not Available", offlineDate);

                movie = new Movie(id, name, p, runTime, genre, releaseDate, offlineDate, hall);

                //Add new movie record and update hall status to database
                progControlMovie.addRecord(movie);
                progControlHall.updateRecord(hall);

                //Add show time record
                AddShowTime addShowTime = new AddShowTime(movie);

                //Pop up insert data successful message 
                JOptionPane.showMessageDialog(null, "Movie ID : " + id + "\nInsert new record successful", "New Movie", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private boolean validation(int relYear, int relMon, int relDay, int offYear, int offMon, int offDay) {
        String strName = jtfName.getText();
        String strPrice = jtfPrice.getText();
        String strRunTime = jtfRunTime.getText();
        String strReleaseDate = String.format("%02d", relDay) + "/" + String.format("%02d", relMon) + "/" + jcbReleaseYear.getSelectedItem();
        String strOfflineDate = String.format("%02d", offDay) + "/" + String.format("%02d", offMon) + "/" + jcbOffYear.getSelectedItem();

        //Check movie name
        if (strName.isEmpty()) {
            jtfName.setBorder(border);
            jtfName.setText(null);
            jtfName.requestFocus();
            jlErrorMessageName.setText("Movie name can not be empty");
            jlErrorMessageName.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check the format of price
        try {
            double price = Double.parseDouble(strPrice);
            //Price can not more than RM50
            if (price > 50 && price < 5) {
                jtfPrice.setBorder(border);
                jtfPrice.setText(null);
                jtfPrice.requestFocus();
                jlErrorMessagePrice.setText("Price number is too high");
                jlErrorMessagePrice.setForeground(new Color(255, 114, 114));
                return false;
            }
        }
        catch (NumberFormatException e) {
            jtfPrice.setBorder(border);
            jtfPrice.setText(null);
            jtfPrice.requestFocus();
            jlErrorMessagePrice.setText("Invalid Format or empty");
            jlErrorMessagePrice.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check the format of running time (minutes) 
        try {
            int r = Integer.parseInt(strRunTime);
            //Running time can not more 220 minutes
            if (r > 220) {
                jtfRunTime.setBorder(border);
                jtfRunTime.setText(null);
                jtfRunTime.requestFocus();
                jlErrorMessageRuntime.setText("Number is too high");
                jlErrorMessageRuntime.setForeground(new Color(255, 114, 114));
                return false;
            }
        }
        catch (NumberFormatException e) {
            jtfRunTime.setBorder(border);
            jtfRunTime.setText(null);
            jtfRunTime.requestFocus();
            jlErrorMessageRuntime.setText("Invalid format or empty");
            jlErrorMessageRuntime.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check genre
        if (jcbGenre.getSelectedIndex() == 0) {
            jlErrorMessageGenre.setText("Please select the genre");
            jlErrorMessageGenre.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check the release date format
        else if (relDay == 0 || relMon == 0 || relYear == 0) {
            jlErrorMessageRelease.setText("Please select the release date");
            jlErrorMessageRelease.setForeground(new Color(255, 114, 114));
            return false;
        }

        else if (!v.isReleaseDateValid(strReleaseDate) || !v.isValidDate(strReleaseDate)) {
            jlErrorMessageRelease.setText("Invalid date format");
            jlErrorMessageRelease.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check the offline date
        else if (offDay == 0 || offMon == 0 || offYear == 0) {
            jlErrorMessageOffline.setText("Please select the offline date");
            jlErrorMessageOffline.setForeground(new Color(255, 114, 114));
            return false;
        }
        else if (!v.isOfflineDateValid(strReleaseDate, strOfflineDate) || !v.isValidDate(strOfflineDate)) {
            jlErrorMessageOffline.setText("Invalid date format");
            jlErrorMessageOffline.setForeground(new Color(255, 114, 114));
            return false;
        }
        else if (jcbHall.getSelectedIndex() == 0) {
            jlErrorMessageHall.setText("Please select a hall");
            jlErrorMessageHall.setForeground(new Color(255, 114, 114));
            return false;
        }
        return true;
    }

}
