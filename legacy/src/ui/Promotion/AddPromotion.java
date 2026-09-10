package ui.Promotion;

import control.MaintainMovie;
import control.MaintainPromotion;
import domain.Movie;
import domain.Promotion;
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
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Jeffrey
 */
public class AddPromotion extends JPanel {

    //General
    private Promotion promotion = new Promotion();
    private Movie movie = new Movie();
    private ArrayList<Movie> moviesArrayList = new ArrayList<>();
    private ArrayList<Promotion> promotionsArrayList = new ArrayList<>();
    private final MaintainMovie progControlMovie = new MaintainMovie();
    private final MaintainPromotion progControlPromo = new MaintainPromotion();
    private final String[] monthArray = {"Month", "January", "February",
        "March", "April", "May", "June", "July", "August", "September",
        "October", "November", "December"};
    //Label
    private final JLabel jlID = new JLabel("Promotion ID");
    private final JLabel jlMovie = new JLabel("Movie ID");
    private final JLabel jlDiscount = new JLabel("Discount Percentage(%)");
    private final JLabel jlStart = new JLabel("Promotion start");
    private final JLabel jlEnd = new JLabel("Promotion end");
    private final JLabel jlErrorMessageMovie = new JLabel();
    private final JLabel jlErrorMessagePromoStart = new JLabel();
    private final JLabel jlErrorMessagePromoEnd = new JLabel();
    private final JLabel jlErrorMessageDiscount = new JLabel();
    //Text Field
    private final JTextField jtfID = new JTextField();
    private final JTextField jtfDiscount = new JTextField();
    //Button & ComboBox
    private final JButton jbtConfirm = new JButton("    Confirm    ");

    private final JComboBox jcbMovie = new JComboBox();
    private final JComboBox jcbPromo_Start_Day = new JComboBox();
    private final JComboBox jcbPromo_Start_Month = new JComboBox(monthArray);
    private final JComboBox jcbPromo_Start_Year = new JComboBox();
    private final JComboBox jcbPromo_End_Day = new JComboBox();
    private final JComboBox jcbPromo_End_Month = new JComboBox(monthArray);
    private final JComboBox jcbPromo_End_Year = new JComboBox();
    //Date Time
    private final LocalDate localDate = new LocalDate();
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
    private final int currentYear = localDate.getYear();

    //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED, 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Create Promotion ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    private AddPromotion() {
        setPromotionID();   //Set promotion ID
        MovieLists();       //Set movie ID

        //Set drop-down list of Promotion Start
        jcbPromo_Start_Day.addItem("Day");     //Set days
        for (int i = 1; i <= 31; i++) {
            jcbPromo_Start_Day.addItem(String.format("%02d", i));
        }

        jcbPromo_Start_Year.addItem("Year");  //Set Year until the current year
        for (int i = currentYear; i <= currentYear + 1; i++) {
            jcbPromo_Start_Year.addItem(i);
        }

        //Set drop-down list of Promotion End
        jcbPromo_End_Day.addItem("Day");     //Set days
        for (int i = 1; i <= 31; i++) {
            jcbPromo_End_Day.addItem(String.format("%02d", i));
        }

        jcbPromo_End_Year.addItem("Year");   //Set Year until the current year
        for (int i = currentYear; i <= currentYear + 1; i++) {
            jcbPromo_End_Year.addItem(i);
        }

        //Button color
        jbtConfirm.setForeground(Color.WHITE);
        jbtConfirm.setBackground(new Color(74, 139, 245));

        //Promotion Start drop-down list
        JPanel start = new JPanel(new GridLayout(1, 3, 10, 0));
        start.add(jcbPromo_Start_Day);
        start.add(jcbPromo_Start_Month);
        start.add(jcbPromo_Start_Year);
        start.setOpaque(false);
        jcbPromo_Start_Day.setBackground(Color.WHITE);
        jcbPromo_Start_Month.setBackground(Color.WHITE);
        jcbPromo_Start_Year.setBackground(Color.WHITE);

        //Promotion End drop-down list
        JPanel end = new JPanel(new GridLayout(1, 3, 10, 0));
        end.add(jcbPromo_End_Day);
        end.add(jcbPromo_End_Month);
        end.add(jcbPromo_End_Year);
        end.setOpaque(false);
        jcbPromo_End_Day.setBackground(Color.WHITE);
        jcbPromo_End_Month.setBackground(Color.WHITE);
        jcbPromo_End_Year.setBackground(Color.WHITE);

        //Promotion ID
        JPanel id = new JPanel(new GridLayout(2, 2));
        id.add(jlID);
        id.add(jtfID);
        id.add(new JLabel());
        id.add(new JLabel());
        id.setOpaque(false);
        jtfID.setEditable(false);
        jtfID.setBackground(Color.WHITE);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Movie
        JPanel movies = new JPanel(new GridLayout(2, 2));
        movies.add(jlMovie);
        movies.add(jcbMovie);
        movies.add(new JLabel());
        movies.add(jlErrorMessageMovie);
        movies.setOpaque(false);
        jcbMovie.setBackground(Color.WHITE);
        jlMovie.setForeground(Color.WHITE);
        jlMovie.setFont(FontStyle);

        //Discount
        JPanel discount = new JPanel(new GridLayout(2, 2));
        discount.add(jlDiscount);
        discount.add(jtfDiscount);
        discount.add(new JLabel());
        discount.add(jlErrorMessageDiscount);
        discount.setOpaque(false);
        jtfDiscount.setBackground(Color.WHITE);
        jlDiscount.setForeground(Color.WHITE);
        jlDiscount.setFont(FontStyle);

        //Promotion Start
        JPanel promoStart = new JPanel(new GridLayout(2, 2));
        promoStart.add(jlStart);
        promoStart.add(start);
        promoStart.add(new JLabel());
        promoStart.add(jlErrorMessagePromoStart);
        promoStart.setOpaque(false);
        jlStart.setForeground(Color.WHITE);
        jlStart.setFont(FontStyle);

        //Promtion End
        JPanel promoEnd = new JPanel(new GridLayout(2, 2));
        promoEnd.add(jlEnd);
        promoEnd.add(end);
        promoEnd.add(new JLabel());
        promoEnd.add(jlErrorMessagePromoEnd);
        promoEnd.setOpaque(false);
        jlEnd.setForeground(Color.WHITE);
        jlEnd.setFont(FontStyle);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel(""));
        buttons.add(jbtConfirm);
        buttons.setOpaque(false);
        jbtConfirm.addActionListener(new AddPromotionListener());

        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(6, 1));
        main.add(id);
        main.add(movies);
        main.add(promoStart);
        main.add(promoEnd);
        main.add(discount);
        main.add(buttons);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
    }

    private void setPromotionID() {
        //Set Promotion ID
        promotion = progControlPromo.lastID();

        if (promotion != null) {
            int id = Integer.parseInt(promotion.getPromotionId().substring(3)) + 1;
            String newID = "PRO" + String.format("%07d", id);
            jtfID.setText(newID);
        }
        else {
            jtfID.setText("PRO0000001");
        }
    }

    private void MovieLists() {
        //Set Movie ID
        moviesArrayList = progControlMovie.getAll();
        promotionsArrayList = progControlPromo.getAllPromotion();
        jcbMovie.addItem("================Select================");

        //If the movie list is not empty in database
        if (moviesArrayList != null) {
            for (Movie mv : moviesArrayList) {
                LocalDate releaseDate = dtf.parseLocalDate(mv.getReleaseDate());
                releaseDate = releaseDate.minusDays(7);
                //Set movie taht haven't showing to movies drop-down list
                if (localDate.isBefore(releaseDate)) {
                    jcbMovie.addItem(mv.getMovieName());
                }
            }
            if (jcbMovie.getItemCount() == 1) {
                jcbMovie.removeAllItems();
                jcbMovie.addItem("No movies is available");
            }
        }
        else {
            //Remove all items in Movie drop-down list
            jcbMovie.removeAllItems();
            jcbMovie.addItem("No movies is available");
        }

        //Remove the movie that already has promotion in drop-down list
        for (Promotion promo : promotionsArrayList) {
            jcbMovie.removeItem(promo.getMovieId().getMovieName());
        }

        //If all movie is not available
        if (jcbMovie.getItemCount() == 1) {
            jcbMovie.removeAllItems();
            jcbMovie.addItem("No movies is available");
        }
    }

    private class AddPromotionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            Object[] start = {jcbPromo_Start_Day.getSelectedItem().toString(),
                jcbPromo_Start_Month.getSelectedIndex(),
                jcbPromo_Start_Year.getSelectedItem().toString()};
            Object[] end = {jcbPromo_End_Day.getSelectedItem().toString(),
                jcbPromo_End_Month.getSelectedIndex(),
                jcbPromo_End_Year.getSelectedItem().toString()};

            if (validation(start, end)) {
                String id = jtfID.getText();
                String promoStart = start[0] + "/" + start[1] + "/" + start[2];
                String promoEnd = end[0] + "/" + end[1] + "/" + end[2];
                int discount = Integer.parseInt(jtfDiscount.getText());
                promotion = new Promotion(id, promoStart, promoEnd, discount, movie);

                progControlPromo.addRecord(promotion);

                //Pop up insert data successful message 
                JOptionPane.showMessageDialog(null, "New promotion added successfully", "Movie Promotion", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private boolean validation(Object[] start, Object[] end) {
        //Clear the error meassage
        Border original = jtfID.getBorder();
        jtfDiscount.setBorder(original);
        jlErrorMessageDiscount.setText(null);
        jlErrorMessageMovie.setText(null);
        jlErrorMessagePromoEnd.setText(null);
        jlErrorMessagePromoStart.setText(null);

        //Check movie is selected
        if (jcbMovie.getSelectedItem().equals("No movies is available")) {
            jlErrorMessageMovie.setText("No movies is available");
            jlErrorMessageMovie.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check movie is selected
        else if (jcbMovie.getSelectedIndex() == 0) {
            jlErrorMessageMovie.setText("Please select the movie");
            jlErrorMessageMovie.setForeground(new Color(255, 114, 114));
            return false;
        }

        /*
        Check the date is valid
        Check promotion start date must after the movie release date
        Check promotion end date must before the movie offline date
         */
        try {
            //Promotion start date
            String strMovie = jcbMovie.getSelectedItem().toString();
            movie = progControlMovie.selectRecordByName(strMovie);
            LocalDate releaseDate = dtf.parseLocalDate(movie.getReleaseDate());
            LocalDate promoStart = dtf.parseLocalDate(start[0] + "/" + start[1] + "/" + start[2]);

            if (releaseDate.isAfter(promoStart)) {
                jlErrorMessagePromoStart.setText("Must after the movie release date");
                jlErrorMessagePromoStart.setForeground(new Color(255, 114, 114));
                return false;
            }

        }
        catch (Exception e) {
            jlErrorMessagePromoStart.setText("Invalid date format");
            jlErrorMessagePromoStart.setForeground((new Color(255, 114, 114)));
            return false;
        }
        try {
            //Promotion end date
            LocalDate offlineDate = dtf.parseLocalDate(movie.getOfflineDate());
            LocalDate promoEnd = dtf.parseLocalDate(end[0] + "/" + end[1] + "/" + end[2]);
            LocalDate promoStart = dtf.parseLocalDate(start[0] + "/" + start[1] + "/" + start[2]);

            if (offlineDate.isBefore(promoEnd) || promoEnd.isBefore(promoStart)) {
                jlErrorMessagePromoEnd.setText("Must before the movie offline date");
                jlErrorMessagePromoEnd.setForeground(new Color(255, 114, 114));
                return false;
            }
        }
        catch (Exception e) {
            jlErrorMessagePromoEnd.setText("Invalid date format");
            jlErrorMessagePromoEnd.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check the promotion discount (must inetger)
        try {
            if (jtfDiscount.getText().isEmpty()) {
                jlErrorMessageDiscount.setText("Please enter the discount pecentage");
                jlErrorMessageDiscount.setForeground(new Color(255, 114, 114));
                return false;
            }

            int discount = Integer.parseInt(jtfDiscount.getText());
            if (discount > 50) {
                jlErrorMessageDiscount.setText("Discount value is too large");
                jlErrorMessageDiscount.setForeground(new Color(255, 114, 114));
                return false;
            }
        }
        catch (Exception e) {
            jtfDiscount.setText(null);
            jtfDiscount.requestFocus();
            jlErrorMessageDiscount.setText("Invalid format. Only integer is acceptable");
            jlErrorMessageDiscount.setForeground(new Color(255, 114, 114));
            return false;
        }
        return true;
    }

}
