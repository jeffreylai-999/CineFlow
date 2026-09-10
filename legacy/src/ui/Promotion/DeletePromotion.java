package ui.Promotion;

import control.MaintainPromotion;
import domain.Promotion;
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

/**
 *
 * @author Jeffrey
 */
public class DeletePromotion extends JPanel {

    //General
    private Promotion promotion = new Promotion();
    private ArrayList<Promotion> promotionsArrayList = new ArrayList<>();
    private final MaintainPromotion progControlPromo = new MaintainPromotion();
    //Label
    private final JLabel jlID = new JLabel("Promotion ID");
    private final JLabel jlMovie = new JLabel("Movie ID");
    private final JLabel jlDiscount = new JLabel("Discount Percentage(%)");
    private final JLabel jlStart = new JLabel("Promotion start");
    private final JLabel jlEnd = new JLabel("Promotion end");
    //Text Field
    private final JTextField jtfDiscount = new JTextField(25);
    private final JTextField jtfMovie = new JTextField();
    private final JTextField jtfStart = new JTextField();
    private final JTextField jtfEnd = new JTextField();
    //Button & ComboBox
    private final JButton jbtDelete = new JButton("    Delete    ");
    private final JComboBox jcbPromotion = new JComboBox();
    //Date Time
    private final LocalDate localDate = new LocalDate();
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");

    //Border style   
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Delete Promotion ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public DeletePromotion() {
        promotionLists();   //Set promotion ID

        //Button color
        jbtDelete.setForeground(Color.WHITE);
        jbtDelete.setBackground(new Color(74, 139, 245));

        //Promotion ID
        JPanel id = new JPanel(new GridLayout(1, 2));
        id.add(jlID);
        id.add(jcbPromotion);
        id.setOpaque(false);
        jcbPromotion.setBackground(Color.WHITE);
        jcbPromotion.addActionListener(new RetrievePromotionListener());
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Movie
        JPanel movies = new JPanel(new GridLayout(1, 2));
        movies.add(jlMovie);
        movies.add(jtfMovie);
        movies.setOpaque(false);
        jtfMovie.setBackground(Color.WHITE);
        jtfMovie.setEditable(false);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Discount
        JPanel discount = new JPanel(new GridLayout(1, 2));
        discount.add(jlDiscount);
        discount.add(jtfDiscount);
        discount.setOpaque(false);
        jtfDiscount.setBackground(Color.WHITE);
        jtfDiscount.setEditable(false);
        jlDiscount.setForeground(Color.WHITE);
        jlDiscount.setFont(FontStyle);

        //Promotion Start
        JPanel promoStart = new JPanel(new GridLayout(1, 2));
        promoStart.add(jlStart);
        promoStart.add(jtfStart);
        promoStart.setOpaque(false);
        jtfStart.setBackground(Color.WHITE);
        jtfStart.setEditable(false);
        jlStart.setForeground(Color.WHITE);
        jlStart.setFont(FontStyle);

        //Promtion End
        JPanel promoEnd = new JPanel(new GridLayout(1, 2));
        promoEnd.add(jlEnd);
        promoEnd.add(jtfEnd);
        promoEnd.setOpaque(false);
        jtfEnd.setBackground(Color.WHITE);
        jtfEnd.setEditable(false);
        jlEnd.setForeground(Color.WHITE);
        jlEnd.setFont(FontStyle);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel(""));
        buttons.add(jbtDelete);
        buttons.setOpaque(false);
        jbtDelete.addActionListener(new DeletePromotionListener());

        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(6, 1, 0, 20));
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

    public void promotionLists() {
        //Set Promotion ID
        promotionsArrayList = progControlPromo.getAllPromotion();

        if (promotionsArrayList != null) {
            jcbPromotion.addItem("=================Select=================");
            for (Promotion p : promotionsArrayList) {
                LocalDate showDate = dtf.parseLocalDate(p.getMovieId().getReleaseDate()).minusDays(7);
                if (localDate.isBefore(showDate)) {
                    jcbPromotion.addItem(p.getPromotionId());
                }
            }
            if (jcbPromotion.getItemCount() == 1) {
                jcbPromotion.removeAllItems();
                jcbPromotion.addItem("No promotion is available");
            }
        }
        else {
            jcbPromotion.addItem("No promotion is available");
        }
    }

    public class RetrievePromotionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (jcbPromotion.getSelectedIndex() != 0) {
                String id = jcbPromotion.getSelectedItem().toString();
                promotion = progControlPromo.selectRecord(id);

                //Set value to text field
                jtfDiscount.setText(String.valueOf(promotion.getPromotionDiscount()));
                jtfEnd.setText(promotion.getPromotionEnd());
                jtfMovie.setText(promotion.getMovieId().getMovieName());
                jtfStart.setText(promotion.getPromotionStart());
            }
            else {
                jtfDiscount.setText(null);
                jtfEnd.setText(null);
                jtfMovie.setText(null);
                jtfStart.setText(null);
            }
        }
    }

    public class DeletePromotionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            int option = JOptionPane.showConfirmDialog(null, "Confirm delete promotion ID of " + promotion.getPromotionId(), "Confirmation", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                progControlPromo.deleteRecord(promotion.getPromotionId());
                JOptionPane.showConfirmDialog(null, "Promotion ID " + promotion.getPromotionId() + "\nDelete sucessfully", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                promotionLists();
            }
        }
    }
}
