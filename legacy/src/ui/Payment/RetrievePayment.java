/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.Payment;

import control.MaintainPromotion;
import control.MaintainPayment;
import domain.*;
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
public class RetrievePayment extends JPanel {

    //Ganeral
    private Promotion promotion = new Promotion();
    private Payment payment = new Payment();
    private ArrayList<Promotion> promotionArrayList = new ArrayList<>();
    private ArrayList<Payment> paymentsArrayList = new ArrayList<>();
    private final MaintainPayment progControlPayment = new MaintainPayment();
    private final MaintainPromotion progControlPromotion = new MaintainPromotion();
    //Label
    private final JLabel jlID = new JLabel("Payment ID");
    private final JLabel jlCustomer = new JLabel("Customer Name");
    private final JLabel jlStaffID = new JLabel("Staff ID");
    private final JLabel jlMovie = new JLabel("Movie Name");
    private final JLabel jlAdult = new JLabel("Number of Adult Seat(s)");
    private final JLabel jlChild = new JLabel("Number of Child Seat(s)");
    private final JLabel jlPromotion = new JLabel("Promotion Discount(%)");
    private final JLabel jlSeatsNumber = new JLabel("Seats Number");
    private final JLabel jlPrice = new JLabel("Total Price");
    private final JLabel jlPaymentDate = new JLabel("Payment Date");
    //Text Field
    private final JTextField jtfCustomer = new JTextField();
    private final JTextField jtfStaffID = new JTextField();
    private final JTextField jtfMovie = new JTextField();
    private final JTextField jtfPromotion = new JTextField();
    private final JTextField jtfAdult = new JTextField();
    private final JTextField jtfChild = new JTextField();
    private final JTextField jtfSeatsNumber = new JTextField();
    private final JTextField jtfPrice = new JTextField();
    private final JTextField jtfPaymentDate = new JTextField();
    //Button & ComboBox
    private final JComboBox jcbPaymentID = new JComboBox();

    //Border style
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Retrieve Payment ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public RetrievePayment() {
        MainLayout();
    }

    private void MainLayout() {
        PaymentList();

        //ID Panel
        JPanel idPanel = new JPanel(new GridLayout(1, 2));
        idPanel.add(jlID);
        idPanel.add(jcbPaymentID);
        idPanel.setOpaque(false);
        jcbPaymentID.setBackground(Color.WHITE);
        jcbPaymentID.addActionListener(new RetrievePaymentListener());
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Customer ID Panel
        JPanel cusPanel = new JPanel(new GridLayout(1, 2));
        cusPanel.add(jlCustomer);
        cusPanel.add(jtfCustomer);
        cusPanel.setOpaque(false);
        jtfCustomer.setBackground(Color.WHITE);
        jtfCustomer.setEditable(false);
        jlCustomer.setForeground(Color.WHITE);
        jlCustomer.setFont(FontStyle);

        //Staff ID Panel
        JPanel staffPanel = new JPanel(new GridLayout(1, 2));
        staffPanel.add(jlStaffID);
        staffPanel.add(jtfStaffID);
        staffPanel.setOpaque(false);
        jtfStaffID.setBackground(Color.WHITE);
        jtfStaffID.setEditable(false);
        jlStaffID.setForeground(Color.WHITE);
        jlStaffID.setFont(FontStyle);

        //Movie Panel
        JPanel moviePanel = new JPanel(new GridLayout(1, 2));
        moviePanel.add(jlMovie);
        moviePanel.add(jtfMovie);
        moviePanel.setOpaque(false);
        jtfMovie.setBackground(Color.WHITE);
        jtfMovie.setEditable(false);
        jlMovie.setForeground(Color.WHITE);
        jlMovie.setFont(FontStyle);

        //Number of Adult Panel
        JPanel adult = new JPanel(new GridLayout(1, 2));
        adult.add(jlAdult);
        adult.add(jtfAdult);
        adult.setOpaque(false);
        jtfAdult.setBackground(Color.WHITE);
        jtfAdult.setEditable(false);
        jlAdult.setForeground(Color.WHITE);
        jlAdult.setFont(FontStyle);

        //Number of Child Panel
        JPanel child = new JPanel(new GridLayout(1, 2));
        child.add(jlChild);
        child.add(jtfChild);
        child.setOpaque(false);
        jtfChild.setBackground(Color.WHITE);
        jtfChild.setEditable(false);
        jlChild.setForeground(Color.WHITE);
        jlChild.setFont(FontStyle);

        //Seat Number Panel
        JPanel seatNumberPanel = new JPanel(new GridLayout(1, 2));
        seatNumberPanel.add(jlSeatsNumber);
        seatNumberPanel.add(jtfSeatsNumber);
        seatNumberPanel.setOpaque(false);
        jtfSeatsNumber.setBackground(Color.WHITE);
        jtfSeatsNumber.setEditable(false);
        jlSeatsNumber.setForeground(Color.WHITE);
        jlSeatsNumber.setFont(FontStyle);

        //Promotion Panel
        JPanel promotionPanel = new JPanel(new GridLayout(1, 2));
        promotionPanel.add(jlPromotion);
        promotionPanel.add(jtfPromotion);
        promotionPanel.setOpaque(false);
        jtfPromotion.setBackground(Color.WHITE);
        jtfPromotion.setEditable(false);
        jlPromotion.setForeground(Color.WHITE);
        jlPromotion.setFont(FontStyle);

        //Payment Date Panel
        JPanel paymentDatePanel = new JPanel(new GridLayout(1, 2));
        paymentDatePanel.add(jlPaymentDate);
        paymentDatePanel.add(jtfPaymentDate);
        paymentDatePanel.setOpaque(false);
        jtfPaymentDate.setBackground(Color.WHITE);
        jtfPaymentDate.setEditable(false);
        jlPaymentDate.setForeground(Color.WHITE);
        jlPaymentDate.setFont(FontStyle);

        //Price Panel
        JPanel pricePanel = new JPanel(new GridLayout(1, 2));
        pricePanel.add(jlPrice);
        pricePanel.add(jtfPrice);
        pricePanel.setOpaque(false);
        jtfPrice.setBackground(Color.WHITE);
        jtfPrice.setEditable(false);
        jlPrice.setForeground(Color.WHITE);
        jlPrice.setFont(FontStyle);

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(11, 1, 0, 16));
        main.add(idPanel);
        main.add(cusPanel);
        main.add(staffPanel);
        main.add(moviePanel);
        main.add(adult);
        main.add(child);
        main.add(seatNumberPanel);
        main.add(promotionPanel);
        main.add(pricePanel);
        main.add(paymentDatePanel);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
    }

    private void PaymentList() {
        paymentsArrayList = progControlPayment.getAll();
        jcbPaymentID.addItem("==================Select==================");
        if (paymentsArrayList != null) {
            for (Payment p : paymentsArrayList) {
                if (p.getPaymentDate() != null) {
                    jcbPaymentID.addItem(p.getPaymentId());
                }
            }
            if (jcbPaymentID.getItemCount() == 1) {
                jcbPaymentID.removeAllItems();
                jcbPaymentID.addItem("No payment record");
            }
        }
        else {
            jcbPaymentID.removeAllItems();
            jcbPaymentID.addItem("No payment record");
        }
    }

    private class RetrievePaymentListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (jcbPaymentID.getSelectedIndex() != 0) {
                String id = jcbPaymentID.getSelectedItem().toString();
                payment = progControlPayment.selectRecord(id);

                //Set Customer
                if (payment.getCusId() != null) {
                    jtfCustomer.setText(payment.getCusId().getCusName());
                }
                else {
                    jtfCustomer.setText("-");
                }

                jtfStaffID.setText(payment.getStaffId().getStaffId());
                jtfAdult.setText(String.valueOf(payment.getAdult()));
                jtfChild.setText(String.valueOf(payment.getChild()));
                jtfPaymentDate.setText(payment.getPaymentDate());
                jtfPrice.setText(String.valueOf(payment.getTotalPrice()));
                jtfMovie.setText(payment.getShowId().getMovieId().getMovieName());

                //Set Promotion
                promotionArrayList = progControlPromotion.getAllPromotion();
                if (promotionArrayList.size() > 0) {
                    for (Promotion p : promotionArrayList) {
                        String movieID = payment.getShowId().getMovieId().getMovieId();
                        if (p.getMovieId().getMovieId().equals(movieID)) {
                            jtfPromotion.setText(String.valueOf(p.getPromotionDiscount()));
                            break;
                        }
                        else {
                            jtfPromotion.setText("-");
                        }
                    }
                }
                else {
                    jtfPromotion.setText("-");
                }

                //Set seats number
                String seatsNumber = payment.getSeatNo().substring(1, payment.getSeatNo().length() - 1);
                String[] a = seatsNumber.split(", ");
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
                jtfSeatsNumber.setText(code);

            }
            else {
                jtfAdult.setText(null);
                jtfChild.setText(null);
                jtfCustomer.setText(null);
                jtfMovie.setText(null);
                jtfPaymentDate.setText(null);
                jtfPrice.setText(null);
                jtfPromotion.setText(null);
                jtfStaffID.setText(null);
                jtfSeatsNumber.setText(null);
            }
        }
    }
}
