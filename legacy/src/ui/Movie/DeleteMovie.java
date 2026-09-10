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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Jeffrey
 */
public class DeleteMovie extends JPanel {

    //General
    private Movie movie = new Movie();
    private Hall hall = new Hall();
    private final MaintainMovie progControlMovie = new MaintainMovie();
    private final MaintainHall progControlHall = new MaintainHall();
    private ArrayList<Movie> moviesArrayList = new ArrayList<>();
    //Label
    private final JLabel jlID = new JLabel("Movie ID");
    private final JLabel jlName = new JLabel("Movie Name");
    private final JLabel jlPrice = new JLabel("Price");
    private final JLabel jlRunTime = new JLabel("Running Time (Minutes)");
    private final JLabel jlGenre = new JLabel("Genre");
    private final JLabel jlRelease = new JLabel("Release Date");
    private final JLabel jlOffline = new JLabel("Offline Date");
    //Text Field
    private final JTextField jtfName = new JTextField(25);
    private final JTextField jtfPrice = new JTextField();
    private final JTextField jtfRunTime = new JTextField();
    private final JTextField jtfGenre = new JTextField();
    private final JTextField jtfRelease = new JTextField();
    private final JTextField jtfOffline = new JTextField();
    //Button & ComboBox
    private final JButton jbtDelete = new JButton("    Delete    ");
    private final JButton jbtAdmin = new JButton("    Emergency    ");
    private final JComboBox movieID = new JComboBox();
    //Date Time
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
    private final LocalDate today = new LocalDate();

    //Border style  
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Delete Movie ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public DeleteMovie() {

        //Set Movie ID
        MovieList();

        //Button color
        jbtDelete.setForeground(Color.WHITE);
        jbtAdmin.setForeground(Color.WHITE);
        jbtDelete.setBackground(new Color(74, 139, 245));
        jbtAdmin.setBackground(new Color(74, 139, 245));

        //ID Panel
        JPanel id = new JPanel(new GridLayout(1, 2));
        id.add(jlID);
        id.add(movieID);
        id.setOpaque(false);
        movieID.setBackground(Color.WHITE);
        movieID.addActionListener(new RetrieveListener());
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Name Panel
        JPanel name = new JPanel(new GridLayout(1, 2));
        name.add(jlName);
        name.add(jtfName);
        name.setOpaque(false);
        jtfName.setEditable(false);
        jtfName.setBackground(Color.WHITE);
        jlName.setForeground(Color.WHITE);
        jlName.setFont(FontStyle);

        //Price Panel
        JPanel price = new JPanel(new GridLayout(1, 2));
        price.add(jlPrice);
        price.add(jtfPrice);
        price.setOpaque(false);
        jtfPrice.setEditable(false);
        jtfPrice.setBackground(Color.WHITE);
        jlPrice.setForeground(Color.WHITE);
        jlPrice.setFont(FontStyle);

        //Running Time Panel
        JPanel runnigTime = new JPanel(new GridLayout(1, 2));
        runnigTime.add(jlRunTime);
        runnigTime.add(jtfRunTime);
        runnigTime.setOpaque(false);
        jtfRunTime.setEditable(false);
        jtfRunTime.setBackground(Color.WHITE);
        jlRunTime.setForeground(Color.WHITE);
        jlRunTime.setFont(FontStyle);

        //Genre Panel
        JPanel genre = new JPanel(new GridLayout(1, 2));
        genre.add(jlGenre);
        genre.add(jtfGenre);
        genre.setOpaque(false);
        jtfGenre.setEditable(false);
        jtfGenre.setBackground(Color.WHITE);
        jlGenre.setForeground(Color.WHITE);
        jlGenre.setFont(FontStyle);

        //Release Date Panel
        JPanel release = new JPanel(new GridLayout(1, 2));
        release.add(jlRelease);
        release.add(jtfRelease);
        release.setOpaque(false);
        jtfRelease.setEditable(false);
        jtfRelease.setBackground(Color.WHITE);
        jlRelease.setForeground(Color.WHITE);
        jlRelease.setFont(FontStyle);

        //Offline Date Panel
        JPanel offline = new JPanel(new GridLayout(1, 2));
        offline.add(jlOffline);
        offline.add(jtfOffline);
        offline.setOpaque(false);
        jtfOffline.setEditable(false);
        jtfOffline.setBackground(Color.WHITE);
        jlOffline.setForeground(Color.WHITE);
        jlOffline.setFont(FontStyle);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(jbtAdmin);
        buttons.add(jbtDelete);
        buttons.setOpaque(false);
        jbtDelete.addActionListener(new DeleteListener());
        jbtAdmin.addActionListener(new emergencyListener());

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(8, 1, 0, 20));

        main.add(id);
        main.add(name);
        main.add(price);
        main.add(runnigTime);
        main.add(genre);
        main.add(release);
        main.add(offline);
        main.add(buttons);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
    }

    private void MovieList() {
        movieID.addItem("===============Select===============");
        moviesArrayList = progControlMovie.getAll();
        if (moviesArrayList.size() > 0) {
            for (Movie mv : moviesArrayList) {
                LocalDate releaseDate = dtf.parseLocalDate(mv.getReleaseDate());
                LocalDate offlineDate = dtf.parseLocalDate(mv.getOfflineDate());

                //Only can delete movies that already offline or haven't release
                if (today.plusDays(7).isBefore(releaseDate) || today.isAfter(offlineDate)) {
                    movieID.addItem(mv.getMovieId());
                }
            }
            if (movieID.getItemCount() == 1) {
                movieID.removeAllItems();
                movieID.addItem("No movie is available");
            }
        }
        else {
            movieID.removeAllItems();
            movieID.addItem("No movie is available");
        }
    }

    private class emergencyListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            movieID.removeAllItems();
            movieID.addItem("===============Select===============");

            if (moviesArrayList.size() > 0) {
                for (Movie mv : moviesArrayList) {
                    movieID.addItem(mv.getMovieId());
                }
            }
        }
    }

    private class RetrieveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            //If user no select any option
            if (movieID.getSelectedIndex() > 0) {
                //Select the data from database
                movie = progControlMovie.selectRecord(movieID.getSelectedItem().toString());

                //Show the record of Movie
                jtfName.setText(movie.getMovieName());
                jtfPrice.setText(movie.getMoviePrice());
                jtfRunTime.setText(movie.getRunningTime());
                jtfGenre.setText(movie.getMovieGenre());
                jtfRelease.setText(movie.getReleaseDate());
                jtfOffline.setText(movie.getOfflineDate());
            }
            else {
                //Set the text field to null value
                jtfName.setText(null);
                jtfPrice.setText(null);
                jtfRunTime.setText(null);
                jtfGenre.setText(null);
                jtfRelease.setText(null);
                jtfOffline.setText(null);
            }
        }
    }

    private class DeleteListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            //If user didn't select any option
            if (movieID.getSelectedItem().equals("No movie is available")) {
                JOptionPane.showMessageDialog(null, "No movie is availble", "Information", JOptionPane.INFORMATION_MESSAGE);
            }

            else if (movieID.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Select an Movie ID", "Information", JOptionPane.INFORMATION_MESSAGE);
            }

            else {
                int option = JOptionPane.showConfirmDialog(null, "Confirm detele the Movie : " + movieID.getSelectedItem(), "Confirmation", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    JPanel pass = new JPanel(new GridLayout(2, 1));
                    pass.add(new JLabel("Password"));
                    JPasswordField password = new JPasswordField(10);
                    pass.add(password);
                    String[] options = {"OK", "Cancel"};
                    int a = JOptionPane.showOptionDialog(null, pass, "Administrator", JOptionPane.NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

                    //Delete movie record from database
                    if (a == 0) {
                        if (password.getText().equals("confirm-delete")) {
                            progControlMovie.deleteRecord(movie.getMovieId());

                            //Set the hall status to "Available"
                            hall = new Hall(movie.getHallId().getHallId(), movie.getHallId().getHallLocation(), "Available", "N/A");
                            progControlHall.updateRecord(hall);

                            //Pop up delete data successful message
                            JOptionPane.showMessageDialog(null, "Movie ID : " + movie.getMovieId() + "\nDelete record successful", "Delete Movie", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else {
                            JOptionPane.showMessageDialog(null, "Access Denied!", "Administrator", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }
}
