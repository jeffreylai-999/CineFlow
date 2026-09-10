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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Jeffrey
 */
public class RetrievePromotion extends JPanel {

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
    private final JComboBox jcbPromotion = new JComboBox();

    //Border style   
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Retrieve Promotion ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public RetrievePromotion() {
        promotionLists();   //Set promotion ID 

        //Promotion ID
        JPanel id = new JPanel(new GridLayout(1, 2));
        id.add(jlID);
        id.add(jcbPromotion);
        id.setOpaque(false);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);
        jcbPromotion.setBackground(Color.WHITE);
        jcbPromotion.addActionListener(new RetrievePromotionListener());

        //Movie
        JPanel movies = new JPanel(new GridLayout(1, 2));
        movies.add(jlMovie);
        movies.add(jtfMovie);
        movies.setOpaque(false);
        jlMovie.setForeground(Color.WHITE);
        jlMovie.setFont(FontStyle);
        jtfMovie.setBackground(Color.WHITE);
        jtfMovie.setEditable(false);

        //Discount
        JPanel discount = new JPanel(new GridLayout(1, 2));
        discount.add(jlDiscount);
        discount.add(jtfDiscount);
        discount.setOpaque(false);
        jlDiscount.setForeground(Color.WHITE);
        jlDiscount.setFont(FontStyle);
        jtfDiscount.setBackground(Color.WHITE);
        jtfDiscount.setEditable(false);

        //Promotion Start
        JPanel promoStart = new JPanel(new GridLayout(1, 2));
        promoStart.add(jlStart);
        promoStart.add(jtfStart);
        promoStart.setOpaque(false);
        jlStart.setForeground(Color.WHITE);
        jlStart.setFont(FontStyle);
        jtfStart.setBackground(Color.WHITE);
        jtfStart.setEditable(false);

        //Promtion End
        JPanel promoEnd = new JPanel(new GridLayout(1, 2));
        promoEnd.add(jlEnd);
        promoEnd.add(jtfEnd);
        promoEnd.setOpaque(false);
        jlEnd.setForeground(Color.WHITE);
        jlEnd.setFont(FontStyle);
        jtfEnd.setBackground(Color.WHITE);
        jtfEnd.setEditable(false);

        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(5, 1, 0, 20));
        main.add(id);
        main.add(movies);
        main.add(promoStart);
        main.add(promoEnd);
        main.add(discount);

        add(main);
        main.setOpaque(false);
        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
    }

    private void promotionLists() {
        //Set Promotion ID
        promotionsArrayList = progControlPromo.getAllPromotion();

        jcbPromotion.addItem("=================Select=================");
        for (Promotion p : promotionsArrayList) {
            jcbPromotion.addItem(p.getPromotionId());
        }
        if (jcbPromotion.getItemCount() == 1) {
            jcbPromotion.removeAllItems();
            jcbPromotion.addItem("No promotion is available");
        }
    }

    private class RetrievePromotionListener implements ActionListener {

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

}
