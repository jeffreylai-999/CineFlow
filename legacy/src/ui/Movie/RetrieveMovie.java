package ui.Movie;

import control.MaintainMovie;
import domain.Movie;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Jeffrey
 */
public class RetrieveMovie extends JPanel {

    //General
    private Movie movie = new Movie();
    private final MaintainMovie progControl = new MaintainMovie();
    private ArrayList<Movie> moviesArrayList = new ArrayList<>();
    //Label
    private final JLabel jlID = new JLabel("Movie ID");
    private final JLabel jlName = new JLabel("Movie Name");
    private final JLabel jlPrice = new JLabel("Price");
    private final JLabel jlRunTime = new JLabel("Running Time (Minutes)");
    private final JLabel jlGenre = new JLabel("Genre");
    private final JLabel jlRelease = new JLabel("Release Date");
    private final JLabel jlOffline = new JLabel("Offline Date");
    private final JLabel jlHall = new JLabel("Hall ID");
    //Text Field
    private final JTextField jtfName = new JTextField();
    private final JTextField jtfPrice = new JTextField();
    private final JTextField jtfRunTime = new JTextField();
    private final JTextField jtfGenre = new JTextField();
    private final JTextField jtfRelease = new JTextField();
    private final JTextField jtfOffline = new JTextField();
    private final JTextField jtfHallID = new JTextField();
    //Button & ComboBox
    private final JButton jbtConfirm = new JButton("    Confirm    ");
    private final JComboBox movieID = new JComboBox();

    //Border style  
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Retrieve Movie ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public RetrieveMovie() {

        //Set Movie ID
        MovieList();
        movieID.addActionListener(new RetrieveListener());

        //Button color
        jbtConfirm.setForeground(Color.WHITE);
        jbtConfirm.setBackground(new Color(74, 139, 245));

        //ID Panel
        JPanel id = new JPanel(new GridLayout(1, 2));
        id.add(jlID);
        id.add(movieID);
        id.setOpaque(false);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);
        movieID.setBackground(Color.WHITE);

        //Name Panel
        JPanel name = new JPanel(new GridLayout(1, 2));
        name.add(jlName);
        name.add(jtfName);
        name.setOpaque(false);
        jlName.setForeground(Color.WHITE);
        jlName.setFont(FontStyle);
        jtfName.setEditable(false);
        jtfName.setBackground(Color.WHITE);

        //Price Panel
        JPanel price = new JPanel(new GridLayout(1, 2));
        price.add(jlPrice);
        price.add(jtfPrice);
        price.setOpaque(false);
        jlPrice.setForeground(Color.WHITE);
        jlPrice.setFont(FontStyle);
        jtfPrice.setEditable(false);
        jtfPrice.setBackground(Color.WHITE);

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

        //Hall ID Panel
        JPanel hall = new JPanel(new GridLayout(1, 2));
        hall.add(jlHall);
        hall.add(jtfHallID);
        hall.setOpaque(false);
        jtfHallID.setEditable(false);
        jtfHallID.setBackground(Color.WHITE);
        jlHall.setForeground(Color.WHITE);
        jlHall.setFont(FontStyle);

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
        main.add(hall);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
    }

    private void MovieList() {
        movieID.addItem("=================Selcet=================");
        moviesArrayList = progControl.getAll();
        for (Movie mv : moviesArrayList) {
            movieID.addItem(mv.getMovieId());
        }
    }

    private class RetrieveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            //If user no select any option
            if (movieID.getSelectedIndex() == 0) {
                //Set the text field to null value
                jtfName.setText(null);
                jtfPrice.setText(null);
                jtfRunTime.setText(null);
                jtfGenre.setText(null);
                jtfRelease.setText(null);
                jtfOffline.setText(null);
                jtfHallID.setText(null);
            }
            else {
                //Select the data from database
                movie = progControl.selectRecord(movieID.getSelectedItem().toString());

                //Show the record of Movie
                jtfName.setText(movie.getMovieName());
                jtfPrice.setText(movie.getMoviePrice());
                jtfRunTime.setText(movie.getRunningTime());
                jtfGenre.setText(movie.getMovieGenre());
                jtfRelease.setText(movie.getReleaseDate());
                jtfOffline.setText(movie.getOfflineDate());
                jtfHallID.setText(movie.getHallId().getHallId());
            }
        }
    }

}
