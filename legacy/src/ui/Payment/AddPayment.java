/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.Payment;

import control.MaintainCreditCard;
import control.MaintainPayment;
import control.MaintainPromotion;
import control.MaintainStaff;
import domain.Creditcard;
import domain.Customer;
import domain.Payment;
import domain.Promotion;
import domain.Showtime;
import domain.Staff;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
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
import ui.AddTicket;

/**
 *
 * @author Jeffrey
 */
public class AddPayment extends JPanel {

    //Ganeral
    private Creditcard card = null;
    private Showtime showtime = new Showtime();
    private Payment payment = new Payment();
    private Staff staff = new Staff();
    private Customer customer = new Customer();
    private ArrayList<Promotion> promotionArrayList = new ArrayList<>();
    private ArrayList<Creditcard> creditcards = new ArrayList<>();
    private String type, seats, reservationID;
    private String[] paymentType = {"Cash", "Credit Card"};
    private int discount = 0, adult = 0, child = 0;
    private double entertainmentTax = 0, gstTax = 0, sum = 0, doubleSum = 0;
    private final MaintainPayment progControlPayment = new MaintainPayment();
    private final MaintainPromotion progControlPromotion = new MaintainPromotion();
    private final MaintainStaff progControStaff = new MaintainStaff();
    private final MaintainCreditCard progControlCard = new MaintainCreditCard();
    private final CalculatePrice cp = new CalculatePrice();
    //Label
    private final JLabel jlID = new JLabel("Payment ID");
    private final JLabel jlCustomer = new JLabel("Customer Name");
    private final JLabel jlStaffID = new JLabel("Staff ID");
    private final JLabel jlReservation = new JLabel("Reservation ID");
    private final JLabel jlPromotionID = new JLabel("Promotion ID");
    private final JLabel jlPromotion = new JLabel("Promotion Discount(%)");
    private final JLabel jlGST = new JLabel("GST (6%)");
    private final JLabel jlEntertainment = new JLabel("Entertainment Tax");
    private final JLabel jlPrice = new JLabel("Price");
    private final JLabel jlTotolPrice = new JLabel("Total Price");
    private final JLabel jlPaymentType = new JLabel("Payment Type");
    private final JLabel jlCreditCard = new JLabel("Credit Card");
    //Text Field
    private final JTextField jtfID = new JTextField();
    private final JTextField jtfCustomer = new JTextField();
    private final JTextField jtfStaffID = new JTextField();
    private final JTextField jtfReservation = new JTextField();
    private final JTextField jtfPromotion = new JTextField();
    private final JTextField jtfPromotionID = new JTextField();
    private final JTextField jtfGST = new JTextField();
    private final JTextField jtfEntertainment = new JTextField();
    private final JTextField jtfPrice = new JTextField();
    private final JTextField jtfTotalPrice = new JTextField();
    private final JTextField jtfCreditCard = new JTextField();
    //Button & ComboBox
    private final JButton jbtConfirm = new JButton("    Confirm    ");
    private final JButton jbtBooking = new JButton("    Booking    ");
    private final JComboBox jcbPayment = new JComboBox(paymentType);
    //Date
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
    private final LocalDate localDate = new LocalDate();

    //Border style
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Payment ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public AddPayment(Showtime showtime, String type, int adult, int child,
            String seats, String reservationID, Customer customer) {
        MainLayout(type);
        this.showtime = showtime;
        this.type = type;
        this.adult = adult;
        this.child = child;
        this.seats = seats;
        this.reservationID = reservationID;
        this.customer = customer;

        promotionArrayList = progControlPromotion.getAllPromotion();

        //Set Customer name
        try {
            jtfCustomer.setText(customer.getCusName());
        }
        catch (Exception e) {
            jtfCustomer.setText("-");
        }

        if (type.equals("walkin")) {
            jtfReservation.setText("-");
        }
        else {
            jtfReservation.setText(reservationID);
        }

        //Set Staff ID
        staff = progControStaff.selectRecord("STF0000001");
        jtfStaffID.setText("STF0000001");

        //Set Promotion Discount
        if (promotionArrayList.size() > 0) {
            for (Promotion p : promotionArrayList) {
                String strMovie = showtime.getMovieId().getMovieName();
                LocalDate promoStart = dtf.parseLocalDate(p.getPromotionStart());
                LocalDate promoEnd = dtf.parseLocalDate(p.getPromotionEnd());
                if (strMovie.equals(p.getMovieId().getMovieId())) {
                    if ((promoStart.isBefore(localDate) || promoStart.isEqual(localDate))
                            && (localDate.isBefore(promoEnd) || localDate.isEqual(promoEnd))) {
                        discount = p.getPromotionDiscount();
                        jtfPromotion.setText(String.valueOf(discount));
                        jtfPromotionID.setText(p.getPromotionId());
                    }
                    else {
                        jtfPromotion.setText("-");
                        jtfPromotionID.setText("-");
                    }
                }
                else {
                    jtfPromotion.setText("-");
                    jtfPromotionID.setText("-");
                }
            }
        }
        else {
            jtfPromotion.setText("-");
            jtfPromotionID.setText("-");
        }

        //Set price
        sum = cp.calculateSubPrice(showtime, adult, child, discount);
        jtfPrice.setText("RM " + String.format("%.2f", sum));

        //Set Enetertainment
        entertainmentTax = cp.calculateEntertainmentTax(sum);
        jtfEntertainment.setText("RM " + String.format("%.2f", entertainmentTax));

        //Set GST
        gstTax = cp.calculateGST(sum);
        jtfGST.setText("RM " + String.format("%.2f", gstTax));

        //Set total price
        doubleSum = cp.calculateTotalPrice(sum, gstTax, entertainmentTax);
        jtfTotalPrice.setText("RM " + String.format("%.2f", doubleSum));
    }

    private void MainLayout(String type) {
        setPaymentID();

        //Button color
        jbtConfirm.setForeground(Color.WHITE);
        jbtConfirm.setBackground(new Color(74, 139, 245));
        jbtBooking.setForeground(Color.WHITE);
        jbtBooking.setBackground(new Color(74, 139, 245));

        /* ============ leftmainFeild left hand side for JLabel ============ */
        JPanel leftmainLabel = new JPanel(new GridLayout(6, 1, 0, 16));
        leftmainLabel.setOpaque(false);

        //ID Label
        leftmainLabel.add(jlID);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Reservation ID Label       
        leftmainLabel.add(jlReservation);
        jlReservation.setForeground(Color.WHITE);
        jlReservation.setFont(FontStyle);

        //Customer ID Label       
        leftmainLabel.add(jlCustomer);
        jlCustomer.setForeground(Color.WHITE);
        jlCustomer.setFont(FontStyle);

        //Staff ID Label        
        leftmainLabel.add(jlStaffID);
        jlStaffID.setForeground(Color.WHITE);
        jlStaffID.setFont(FontStyle);

        //Promotion ID Label       
        leftmainLabel.add(jlPromotionID);
        jlPromotionID.setForeground(Color.WHITE);
        jlPromotionID.setFont(FontStyle);

        //Promotion Label
        leftmainLabel.add(jlPromotion);
        jlPromotion.setForeground(Color.WHITE);
        jlPromotion.setFont(FontStyle);

        /* ============ leftmainField right hand side for text field ============ */
        JPanel leftmainField = new JPanel(new GridLayout(6, 1, 0, 16));
        leftmainField.setOpaque(false);

        //ID Field
        leftmainField.add(jtfID);
        jtfID.setBackground(Color.WHITE);
        jtfID.setEditable(false);

        //Reservation ID Field
        leftmainField.add(jtfReservation);
        jtfReservation.setBackground(Color.WHITE);
        jtfReservation.setEditable(false);

        //Customer ID Field
        leftmainField.add(jtfCustomer);
        jtfCustomer.setBackground(Color.WHITE);
        jtfCustomer.setEditable(false);

        //Staff ID Field  
        leftmainField.add(jtfStaffID);
        jtfStaffID.setBackground(Color.WHITE);
        jtfStaffID.setEditable(false);

        //Promotion ID Field
        leftmainField.add(jtfPromotionID);
        jtfPromotionID.setBackground(Color.WHITE);
        jtfPromotionID.setEditable(false);

        //Promotion Field
        leftmainField.add(jtfPromotion);
        jtfPromotion.setBackground(Color.WHITE);
        jtfPromotion.setEditable(false);

        /* ============ rightmainLabel right hand side for JLabel ============ */
        JPanel rightmainLabel = new JPanel(new GridLayout(6, 1, 0, 16));
        rightmainLabel.setOpaque(false);

        //GST Label
        rightmainLabel.add(jlGST);
        jlGST.setForeground(Color.WHITE);
        jlGST.setFont(FontStyle);

        //Entertainment Label       
        rightmainLabel.add(jlEntertainment);
        jlEntertainment.setForeground(Color.WHITE);
        jlEntertainment.setFont(FontStyle);

        //Price Label       
        rightmainLabel.add(jlPrice);
        jlPrice.setForeground(Color.WHITE);
        jlPrice.setFont(FontStyle);

        //Total Price Label
        rightmainLabel.add(jlTotolPrice);
        jlTotolPrice.setForeground(Color.WHITE);
        jlTotolPrice.setFont(FontStyle);

        if (type.equals("walkin")) {
            //Payment Type Label
            rightmainLabel.add(jlPaymentType);
            jlPaymentType.setForeground(Color.WHITE);
            jlPaymentType.setFont(FontStyle);

            //Credit card Label
            rightmainLabel.add(jlCreditCard);
            jlCreditCard.setForeground(Color.WHITE);
            jlCreditCard.setFont(FontStyle);
        }

        /* ============ rightmainField right hand side for TextField or combo box ============ */
        JPanel rightmainField = new JPanel(new GridLayout(6, 1, 0, 16));
        rightmainField.setOpaque(false);

        //GST Field
        rightmainField.add(jtfGST);
        jtfGST.setBackground(Color.WHITE);
        jtfGST.setEditable(false);

        //Entertainment Field
        rightmainField.add(jtfEntertainment);
        jtfEntertainment.setBackground(Color.WHITE);
        jtfEntertainment.setEditable(false);

        //Price Field     
        rightmainField.add(jtfPrice);
        jtfPrice.setBackground(Color.WHITE);
        jtfPrice.setEditable(false);

        //Total Price Field
        rightmainField.add(jtfTotalPrice);
        jtfTotalPrice.setBackground(Color.WHITE);
        jtfTotalPrice.setEditable(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (type.equals("walkin")) {
            //Payment Type Combo Box
            rightmainField.add(jcbPayment);
            jcbPayment.setBackground(Color.WHITE);
            jcbPayment.addActionListener(new PaymentTypeListener());

            //Credit card Field
            rightmainField.add(jtfCreditCard);
            jtfCreditCard.setEditable(false);
            jtfCreditCard.setBackground(Color.WHITE);

            //Button & Function
            buttons.add(jbtConfirm);
            buttons.setOpaque(false);
            jbtConfirm.addActionListener(new AddPaymentListener());
        }

        else {
            buttons.add(jbtBooking);
            buttons.setOpaque(false);
            jbtBooking.addActionListener(new AddPaymentListener());
        }

        //Set general Layout
        setLayout(new FlowLayout());

        //Panel which consists leftmainLabel,leftmainField,rightmainLabel and rightmainField
        JPanel leftmain = new JPanel(new BorderLayout(30, 30)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(370, 260);
            }
        };
        leftmain.setOpaque(false);

        JPanel rightmain = new JPanel(new BorderLayout(30, 30)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(370, 260);
            }
        };
        rightmain.setOpaque(false);

        leftmain.add(leftmainLabel, BorderLayout.WEST);
        leftmain.add(leftmainField, BorderLayout.CENTER);
        rightmain.add(rightmainLabel, BorderLayout.WEST);
        rightmain.add(rightmainField, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.add(buttons, BorderLayout.SOUTH);
        main.add(leftmain, BorderLayout.WEST);
        main.add(rightmain, BorderLayout.CENTER);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
    }

    private void setPaymentID() {
        String paymentID = progControlPayment.paymentLastID();
        if (paymentID != null) {
            int id = Integer.parseInt(paymentID.substring(1)) + 1;
            String newID = "P" + String.format("%09d", id);
            jtfID.setText(newID);
        }
        else {
            jtfID.setText("P000000001");
        }
    }

    private class PaymentTypeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (type.equals("walkin")) {
                if (jcbPayment.getSelectedIndex() == 1) {
                    jtfCreditCard.setEditable(true);
                }
                else {
                    jtfCreditCard.setEditable(false);
                    jtfCreditCard.setText(null);
                }
            }
            else {
                jtfCreditCard.setEditable(false);
                jtfCreditCard.setText("-");
            }
        }
    }

    private void addRerocd() {
        BigDecimal bg = BigDecimal.valueOf(doubleSum);
        String id = jtfID.getText();
        String strType = jcbPayment.getSelectedItem().toString();
        String strCard = jtfCreditCard.getText();
        String date = localDate.toString("dd/MM/yyyy");

        payment = new Payment(id, staff, showtime, type, adult, child,
                seats, bg, null, customer, strType, card, date);

        if (type.equals("walkin")) {
            //Payment by Credit Card
            if (jcbPayment.getSelectedIndex() == 1) {
                if (card == null) {
                    AddCreditCard add = new AddCreditCard(payment, strCard, discount);

                    removeAll();
                    add(add);
                    add.setOpaque(false);
                    revalidate();
                    repaint();
                }
                else {
                    progControlPayment.addPaymentWithout(payment);
                    progControlPayment.updatePaymentDate(payment);
                    progControlPayment.updatePaymentType(payment);
                    progControlPayment.updateCard(payment);

                    if (!jtfCustomer.getText().equals("-")) {
                        progControlPayment.updateCustomer(payment);
                    }
                    AddTicket add = new AddTicket(seats, adult, child, discount, showtime, payment);
                }
            }
            //Payment by Cash
            else {
                progControlPayment.addPaymentWithout(payment);
                progControlPayment.updatePaymentDate(payment);
                progControlPayment.updatePaymentType(payment);

                if (!jtfCustomer.getText().equals("-")) {
                    progControlPayment.updateCustomer(payment);
                }
                AddTicket add = new AddTicket(seats, adult, child, discount, showtime, payment);
            }
        }
        //Reservation
        else {
            payment.setReservationId(reservationID);
            progControlPayment.addPaymentWithout(payment);
            progControlPayment.updateCustomer(payment);
        }
    }

    private boolean validation() {
        String strCard = jtfCreditCard.getText();
        try {
            long cardNo = Long.parseLong(strCard);
            if (strCard.length() < 12 || strCard.length() > 18) {
                JOptionPane.showMessageDialog(null, "Invalid of credit card number", "Error", 0);
                return false;
            }
        }
        catch (NumberFormatException | HeadlessException e) {
            JOptionPane.showMessageDialog(null, "Acceptable for integer only in creadit card", "Error", 0);
            return false;
        }
        return true;
    }

    private class AddPaymentListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            creditcards = progControlCard.getAll();
            if (jcbPayment.getSelectedIndex() == 1) {
                if (validation()) {
                    for (Creditcard c : creditcards) {
                        long cardNo = Long.parseLong(jtfCreditCard.getText());
                        if (c.getCardNo() == cardNo) {
                            card = c;
                            break;
                        }
                    }
                    addRerocd();
                }
            }
            else {
                addRerocd();
            }
        }
    }

}
