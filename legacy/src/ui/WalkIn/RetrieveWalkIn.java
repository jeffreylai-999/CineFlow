/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.WalkIn;

import control.MaintainPayment;
import domain.Payment;
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
public class RetrieveWalkIn extends JPanel {

    //General
    private Payment payment = new Payment();
    private ArrayList<Payment> paymentsArrayList = new ArrayList<>();
    private final MaintainPayment progControl = new MaintainPayment();
    //Label
    private final JLabel jlID = new JLabel("Payment ID");
    private final JLabel jlCustomer = new JLabel("Customer");
    private final JLabel jlMovie = new JLabel("Movie Name");
    private final JLabel jlDate = new JLabel("Movie Date");
    private final JLabel jlTime = new JLabel("Movie Time");
    private final JLabel jlAdult = new JLabel("Adult Seat(s)");
    private final JLabel jlChild = new JLabel("Child Seat(s)");
    private final JLabel jlSeatsCode = new JLabel("Seat(s) Number");
    private final JLabel jlPaymentType = new JLabel("Payment Type");
    private final JLabel jlCreditCard = new JLabel("Credit Card Number");
    private final JLabel jlPaymentDate = new JLabel("Payment Date");
    //Text Field
    private final JTextField jtfCustomer = new JTextField();
    private final JTextField jtfMovie = new JTextField();
    private final JTextField jtfDate = new JTextField();
    private final JTextField jtfTime = new JTextField();
    private final JTextField jtfAdult = new JTextField();
    private final JTextField jtfChild = new JTextField();
    private final JTextField jtfSeatsCode = new JTextField();
    private final JTextField jtfPaymentType = new JTextField();
    private final JTextField jtfCreditCard = new JTextField();
    private final JTextField jtfPaymentDate = new JTextField();
    //Button & ComboBox
    private final JComboBox jcbID = new JComboBox();

    //Border style  
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Retrieve Walk-In ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public RetrieveWalkIn() {
        ReservationList();

        //ID Panel
        JPanel idPanel = new JPanel(new GridLayout(1, 2));
        idPanel.add(jlID);
        idPanel.add(jcbID);
        idPanel.setOpaque(false);
        jlID.setFont(FontStyle);
        jlID.setForeground(Color.WHITE);
        jcbID.setBackground(Color.WHITE);
        jcbID.addActionListener(new RetrieveReservationListener());

        //Customer Panel
        JPanel cusPanel = new JPanel(new GridLayout(1, 2));
        cusPanel.add(jlCustomer);
        cusPanel.add(jtfCustomer);
        cusPanel.setOpaque(false);
        jlCustomer.setFont(FontStyle);
        jlCustomer.setForeground(Color.WHITE);
        jtfCustomer.setBackground(Color.WHITE);
        jtfCustomer.setEditable(false);

        //Movie panel
        JPanel moviePanel = new JPanel(new GridLayout(1, 2));
        moviePanel.add(jlMovie);
        moviePanel.add(jtfMovie);
        moviePanel.setOpaque(false);
        jlMovie.setFont(FontStyle);
        jlMovie.setForeground(Color.WHITE);
        jtfMovie.setEditable(false);
        jtfMovie.setBackground(Color.WHITE);

        //Date panel
        JPanel datePanel = new JPanel(new GridLayout(1, 2));
        datePanel.add(jlDate);
        datePanel.add(jtfDate);
        datePanel.setOpaque(false);
        jlDate.setFont(FontStyle);
        jlDate.setForeground(Color.WHITE);
        jtfDate.setEditable(false);
        jtfDate.setBackground(Color.WHITE);

        //Time panel
        JPanel timePanel = new JPanel(new GridLayout(1, 2));
        timePanel.add(jlTime);
        timePanel.add(jtfTime);
        timePanel.setOpaque(false);
        jlTime.setFont(FontStyle);
        jlTime.setForeground(Color.WHITE);
        jtfTime.setEditable(false);
        jtfTime.setBackground(Color.WHITE);

        //Adult panel
        JPanel adultPanel = new JPanel(new GridLayout(1, 2));
        adultPanel.add(jlAdult);
        adultPanel.add(jtfAdult);
        adultPanel.setOpaque(false);
        adultPanel.setOpaque(false);
        jlAdult.setFont(FontStyle);
        jlAdult.setForeground(Color.WHITE);
        jtfAdult.setEditable(false);
        jtfAdult.setBackground(Color.WHITE);

        //Child / Senior Citizen panel
        JPanel childPanel = new JPanel(new GridLayout(1, 2));
        childPanel.add(jlChild);
        childPanel.add(jtfChild);
        childPanel.setOpaque(false);
        childPanel.setOpaque(false);
        jlChild.setFont(FontStyle);
        jlChild.setForeground(Color.WHITE);
        jtfChild.setEditable(false);
        jtfChild.setBackground(Color.WHITE);

        //Seats code panel
        JPanel seatsPanel = new JPanel(new GridLayout(1, 2));
        seatsPanel.add(jlSeatsCode);
        seatsPanel.add(jtfSeatsCode);
        seatsPanel.setOpaque(false);
        jlSeatsCode.setFont(FontStyle);
        jlSeatsCode.setForeground(Color.WHITE);
        jtfSeatsCode.setEditable(false);
        jtfSeatsCode.setBackground(Color.WHITE);

        //Payment Type panel
        JPanel typePanel = new JPanel(new GridLayout(1, 2));
        typePanel.add(jlPaymentType);
        typePanel.add(jtfPaymentType);
        typePanel.setOpaque(false);
        jlPaymentType.setFont(FontStyle);
        jlPaymentType.setForeground(Color.WHITE);
        jtfPaymentType.setEditable(false);
        jtfPaymentType.setBackground(Color.WHITE);

        //Credit Card number panel
        JPanel cardPanel = new JPanel(new GridLayout(1, 2));
        cardPanel.add(jlCreditCard);
        cardPanel.add(jtfCreditCard);
        cardPanel.setOpaque(false);
        jlCreditCard.setFont(FontStyle);
        jlCreditCard.setForeground(Color.WHITE);
        jtfCreditCard.setEditable(false);
        jtfCreditCard.setBackground(Color.WHITE);

        //Payment Date panel
        JPanel paymentDate = new JPanel(new GridLayout(1, 2));
        paymentDate.add(jlPaymentDate);
        paymentDate.add(jtfPaymentDate);
        paymentDate.setOpaque(false);
        jlPaymentDate.setFont(FontStyle);
        jlPaymentDate.setForeground(Color.WHITE);
        jtfPaymentDate.setEditable(false);
        jtfPaymentDate.setBackground(Color.WHITE);

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(11, 1, 0, 15));
        main.add(idPanel);
        main.add(cusPanel);
        main.add(moviePanel);
        main.add(datePanel);
        main.add(timePanel);
        main.add(adultPanel);
        main.add(childPanel);
        main.add(seatsPanel);
        main.add(typePanel);
        main.add(cardPanel);
        main.add(paymentDate);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 60, 25, 60)));
    }

    private void ReservationList() {
        //Get all records from datatbase
        paymentsArrayList = progControl.getAll();
        jcbID.removeAllItems();
        jcbID.addItem("===============Select===============");

        if (paymentsArrayList != null) {
            for (Payment p : paymentsArrayList) {
                if (p.getCusType().equals("walkin")) {
                    jcbID.addItem(p.getPaymentId());
                }
            }
            if (jcbID.getItemCount() == 1) {
                jcbID.removeAllItems();
                jcbID.addItem("No Walk-In records");
            }
        }
        else {
            jcbID.removeAllItems();
            jcbID.addItem("No Walk-In records");
        }
    }

    private class RetrieveReservationListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (jcbID.getSelectedIndex() > 0) {
                payment = progControl.selectRecord(jcbID.getSelectedItem().toString());
                String strServer = payment.getSeatNo().substring(1, payment.getSeatNo().length() - 1);
                String[] a = strServer.split(", ");
                StringBuilder seatCode = new StringBuilder();
                for (String a1 : a) {
                    String strRow = String.valueOf(a1.charAt(0)) + a1.charAt(1);
                    String strCol = String.valueOf(a1.charAt(2)) + a1.charAt(3);
                    int row = Integer.parseInt(strRow);
                    int col = Integer.parseInt(strCol);

                    String seat = Character.toString((char) (row + 65)) + String.format("%02d", col + 1) + ", ";
                    seatCode.append(seat);
                }
                String code = seatCode.toString().substring(0, seatCode.length() - 2);
                //Set value to text filed
                jtfMovie.setText(payment.getShowId().getMovieId().getMovieName());
                jtfDate.setText(payment.getShowId().getShowDate());
                jtfTime.setText(payment.getShowId().getShowTime());
                jtfAdult.setText(String.valueOf(payment.getAdult()));
                jtfChild.setText(String.valueOf(payment.getChild()));
                jtfSeatsCode.setText(code);
                jtfPaymentType.setText(payment.getPaymentType());
                jtfPaymentDate.setText(payment.getPaymentDate());

                try {
                    jtfCreditCard.setText(String.valueOf(payment.getCardId().getCardNo()));
                }
                catch (Exception e) {
                    jtfCreditCard.setText("-");
                }

                try {
                    jtfCustomer.setText(payment.getCusId().getCusName());
                }
                catch (Exception e) {
                    jtfCustomer.setText("-");
                }
            }
            else {
                jtfCustomer.setText(null);
                jtfAdult.setText(null);
                jtfChild.setText(null);
                jtfDate.setText(null);
                jtfMovie.setText(null);
                jtfSeatsCode.setText(null);
                jtfTime.setText(null);
                jtfPaymentType.setText(null);
                jtfCreditCard.setText(null);
                jtfPaymentDate.setText(null);
            }
        }
    }

}
