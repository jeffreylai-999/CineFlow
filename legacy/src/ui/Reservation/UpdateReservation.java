/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.Reservation;

import control.MaintainCreditCard;
import control.MaintainPayment;
import control.MaintainPromotion;
import domain.Creditcard;
import domain.Payment;
import domain.Promotion;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
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
import ui.Payment.AddCreditCard;

/**
 *
 * @author Jeffrey
 */
public class UpdateReservation extends JPanel
{
    //Ganeral
    private Creditcard card = null;
    private Payment payment = new Payment();
    private ArrayList<Payment> paymentArrayList = new ArrayList<>();
    private ArrayList<Creditcard> creditcards = new ArrayList<>();
    private ArrayList<Promotion> promotionArrayList = new ArrayList<>();
    private int discount = 0;
    private final String[] paymentType = {"Cash", "Credit Card"};
    private final MaintainPayment progControlPayment = new MaintainPayment();
    private final MaintainCreditCard progControlCard = new MaintainCreditCard();
    private final MaintainPromotion progControlPromotion = new MaintainPromotion();
    //Label
    private final JLabel jlID = new JLabel("Payment ID");
    private final JLabel jlReservation = new JLabel("Reservation ID");
    private final JLabel jlCustomer = new JLabel("Customer");
    private final JLabel jlMovie = new JLabel("Movie Name");
    private final JLabel jlDate = new JLabel("Booking Date");
    private final JLabel jlTime = new JLabel("Booking Time");
    private final JLabel jlAdult = new JLabel("Adult Seat(s)");
    private final JLabel jlChild = new JLabel("Child Seat(s)");
    private final JLabel jlSeatsCode = new JLabel("Seat(s) Number");
    private final JLabel jlPrice = new JLabel("Total Price");
    private final JLabel jlPaymentType = new JLabel("Payment Type");
    private final JLabel jlCreditCard = new JLabel("Credit Card");
    //Text Field
    private final JTextField jtfReservation = new JTextField(23);
    private final JTextField jtfCustomer = new JTextField();
    private final JTextField jtfMovie = new JTextField();
    private final JTextField jtfDate = new JTextField();
    private final JTextField jtfTime = new JTextField();
    private final JTextField jtfAdult = new JTextField();
    private final JTextField jtfChild = new JTextField();
    private final JTextField jtfSeatsNo = new JTextField();
    private final JTextField jtfPrice = new JTextField();
    private final JTextField jtfCreditCard = new JTextField();
    //Button & ComboBox
    private final JButton jbtPayment = new JButton("    Payment    ");
    private final JComboBox jcbPayment = new JComboBox(paymentType);
    private final JComboBox jcbID = new JComboBox();
    //Date
    private final LocalDate localDate = new LocalDate();
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
    
     //Border style  
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240,240,225),3);
    
    //Border Title
    TitledBorder title = new TitledBorder(borderMain," Update Reservation ",TitledBorder.LEFT,
    TitledBorder.DEFAULT_POSITION, new Font ( "Arial", Font.BOLD, 20 ), new Color(255,255,153));
    
    //JLabel Style
    Font FontStyle = new Font("SansSerif",Font.BOLD, 14); 
    
    public UpdateReservation()
    {
        getPaymentID();
        
        //Button color
        jbtPayment.setForeground(Color.WHITE);
        jbtPayment.setBackground(new Color(74,139,245));
        
        //ID Panel
        JPanel idPanel = new JPanel(new GridLayout(1,2));
        idPanel.add(jlID);
        idPanel.add(jcbID);
        idPanel.setOpaque(false);
        jcbID.setBackground(Color.WHITE);
        jcbID.addActionListener(new RetrieveReservationListener());
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);   
        
        //ID Panel
        JPanel reservation = new JPanel(new GridLayout(1,2));
        reservation.add(jlReservation);
        reservation.add(jtfReservation);
        reservation.setOpaque(false);
        jtfReservation.setBackground(Color.WHITE);
        jtfReservation.setEditable(false);
        jlReservation.setForeground(Color.WHITE);
        jlReservation.setFont(FontStyle); 
        
        //Customer Panel
        JPanel cusPanel = new JPanel(new GridLayout(1,2));
        cusPanel.add(jlCustomer);
        cusPanel.add(jtfCustomer);
        cusPanel.setOpaque(false);
        jtfCustomer.setBackground(Color.WHITE);
        jtfCustomer.setEditable(false);
        jlCustomer.setForeground(Color.WHITE);
        jlCustomer.setFont(FontStyle); 
        
        //Movie panel
        JPanel moviePanel = new JPanel(new GridLayout(1,2));
        moviePanel.add(jlMovie);
        moviePanel.add(jtfMovie);
        moviePanel.setOpaque(false);
        jtfMovie.setEditable(false);
        jtfMovie.setBackground(Color.WHITE);
        jlMovie.setForeground(Color.WHITE);
        jlMovie.setFont(FontStyle); 
        
        //Date panel
        JPanel datePanel = new JPanel(new GridLayout(1,2));
        datePanel.add(jlDate);
        datePanel.add(jtfDate);
        datePanel.setOpaque(false);
        jtfDate.setEditable(false);
        jtfDate.setBackground(Color.WHITE);
        jlDate.setForeground(Color.WHITE);
        jlDate.setFont(FontStyle); 
                
        //Time panel
        JPanel timePanel = new JPanel(new GridLayout(1,2));
        timePanel.add(jlTime);
        timePanel.add(jtfTime);
        timePanel.setOpaque(false);
        jtfTime.setEditable(false);
        jtfTime.setBackground(Color.WHITE);
        jlTime.setForeground(Color.WHITE);
        jlTime.setFont(FontStyle); 
        
        //Adult panel
        JPanel adultPanel = new JPanel(new GridLayout(1,2));
        adultPanel.add(jlAdult);
        adultPanel.add(jtfAdult);
        adultPanel.setOpaque(false);
        adultPanel.setOpaque(false);
        jtfAdult.setEditable(false);
        jtfAdult.setBackground(Color.WHITE);
        jlAdult.setForeground(Color.WHITE);
        jlAdult.setFont(FontStyle); 
        
        //Child / Senior Citizen panel
        JPanel childPanel = new JPanel(new GridLayout(1,2));
        childPanel.add(jlChild);
        childPanel.add(jtfChild);
        childPanel.setOpaque(false);
        childPanel.setOpaque(false);
        jtfChild.setEditable(false);
        jtfChild.setBackground(Color.WHITE);
        jlChild.setForeground(Color.WHITE);
        jlChild.setFont(FontStyle); 
        
        //Seats code panel
        JPanel seatsPanel = new JPanel(new GridLayout(1,2));
        seatsPanel.add(jlSeatsCode);
        seatsPanel.add(jtfSeatsNo);
        seatsPanel.setOpaque(false);
        jtfSeatsNo.setEditable(false);
        jtfSeatsNo.setBackground(Color.WHITE);
        jlSeatsCode.setForeground(Color.WHITE);
        jlSeatsCode.setFont(FontStyle); 
        
        //Price panel
        JPanel pricePanel = new JPanel(new GridLayout(1,2));
        pricePanel.add(jlPrice);
        pricePanel.add(jtfPrice);
        pricePanel.setOpaque(false);
        jtfPrice.setEditable(false);
        jtfPrice.setBackground(Color.WHITE);
        jlPrice.setForeground(Color.WHITE);
        jlPrice.setFont(FontStyle); 
        
        //Payment Type Panel
        JPanel paymentPanel = new JPanel(new GridLayout(1,2));
        paymentPanel.add(jlPaymentType);
        paymentPanel.add(jcbPayment);
        paymentPanel.setOpaque(false);
        jcbPayment.setBackground(Color.WHITE);
        jcbPayment.addActionListener(new PaymentTypeListener());
        jlPaymentType.setForeground(Color.WHITE);
        jlPaymentType.setFont(FontStyle); 
        
        //Credit card Panel
        JPanel credit = new JPanel(new GridLayout(1,2));
        credit.add(jlCreditCard);
        credit.add(jtfCreditCard);
        credit.setOpaque(false);
        jtfCreditCard.setEditable(false);
        jtfCreditCard.setBackground(Color.WHITE);
        jlCreditCard.setForeground(Color.WHITE);
        jlCreditCard.setFont(FontStyle); 
        
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel(""));
        buttons.add(jbtPayment);
        buttons.setOpaque(false);
        jbtPayment.addActionListener(new PaymentListener());
        
        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(13,1,0,10));
        main.add(idPanel);
        main.add(reservation);
        main.add(cusPanel);
        main.add(moviePanel);
        main.add(datePanel);
        main.add(timePanel);
        main.add(adultPanel);
        main.add(childPanel);
        main.add(seatsPanel);
        main.add(pricePanel);
        main.add(paymentPanel);
        main.add(credit);
        main.add(buttons);
        main.setOpaque(false);
        add(main);
        
        main.setBorder (BorderFactory.createCompoundBorder ( title,BorderFactory.createEmptyBorder ( 20, 50, 25, 50 )) );
        
        /*setSize(700, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Payment");
        setVisible(true);
        */
    }
    
    private void getPaymentID()
    {
        paymentArrayList = progControlPayment.getReservationPayments();
        jcbID.addItem("===============Select===============");
        
        if (paymentArrayList.size() > 0)
        {
            for (Payment p : paymentArrayList)
            {
                if (p.getPaymentDate() == null)
                    jcbID.addItem(p.getPaymentId());
            }
            if (jcbID.getItemCount() == 1)
            {
                jcbID.removeAllItems();
                jcbID.addItem("No reservation is available");
            }
        }
        else{
            jcbID.removeAllItems();
            jcbID.addItem("No reservation is available");
        }
    }
    
    private class PaymentTypeListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            if (jcbID.getSelectedIndex() != 0)
            {
                if (jcbPayment.getSelectedIndex() == 1)
                {
                    jtfCreditCard.setEditable(true);
                    jtfCreditCard.requestFocus();
                }
                else{
                    jtfCreditCard.setEditable(false);
                    jtfCreditCard.setText(null);
                }
            }
        }
    }

    private class RetrieveReservationListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            if (jcbID.getSelectedIndex() != 0)
            {
                String id = jcbID.getSelectedItem().toString();
                payment = progControlPayment.selectRecord(id);

                jtfCustomer.setText(payment.getCusId().getCusName());
                jtfAdult.setText(String.valueOf(payment.getAdult()));
                jtfChild.setText(String.valueOf(payment.getChild()));
                jtfDate.setText(payment.getShowId().getShowDate());
                jtfTime.setText(payment.getShowId().getShowTime());
                jtfMovie.setText(payment.getShowId().getMovieId().getMovieName());
                jtfPrice.setText(String.valueOf(payment.getTotalPrice()));
                jtfReservation.setText(payment.getReservationId());

                String strServer = payment.getSeatNo().substring(1, payment.getSeatNo().length() - 1);
                String[] a = strServer.split(", ");
                StringBuilder seatCode = new StringBuilder();

                for (String a1 : a) 
                {
                    String strRow = String.valueOf(a1.charAt(0)) + a1.charAt(1);
                    String strCol = String.valueOf(a1.charAt(2)) + a1.charAt(3);
                    int row = Integer.parseInt(strRow);
                    int col = Integer.parseInt(strCol);

                    String seat = Character.toString((char)(row + 65)) + String.format("%02d", col + 1) + ", ";
                    seatCode.append(seat);
                }
                String code = seatCode.toString().substring(0, seatCode.length() - 2);
                jtfSeatsNo.setText(code);
                jcbPayment.setSelectedIndex(0);
            }
            else{
                jtfAdult.setText(null);
                jtfChild.setText(null);
                jtfCreditCard.setText(null);
                jtfCreditCard.setEditable(false);
                jtfCustomer.setText(null);
                jtfDate.setText(null);
                jtfMovie.setText(null);
                jtfPrice.setText(null);
                jtfReservation.setText(null);
                jtfSeatsNo.setText(null);
                jtfTime.setText(null);
                jcbPayment.setSelectedIndex(0);
            }
        }
    }
    
    private class PaymentListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            String date = localDate.toString("dd/MM/yyyy");
            promotionArrayList = progControlPromotion.getAllPromotion();
            if (promotionArrayList.size() > 0)
            {
                for (Promotion p : promotionArrayList)
                {
                    String strMovie = payment.getShowId().getMovieId().getMovieId();
                    LocalDate promoStart = dtf.parseLocalDate(p.getPromotionStart());
                    LocalDate promoEnd = dtf.parseLocalDate(p.getPromotionEnd());
                    if (strMovie.equals(p.getMovieId().getMovieId()))
                    {
                        if ((promoStart.isBefore(localDate) || promoStart.isEqual(localDate)) 
                                && (localDate.isBefore(promoEnd) || localDate.isEqual(promoEnd)))
                        {
                            discount = p.getPromotionDiscount();
                        }
                    }
                }
            }
            
            //Payment by Cash
            if (jcbPayment.getSelectedIndex() == 0)
            {
                payment.setPaymentType("Cash");
                payment.setPaymentDate(date);
                progControlPayment.updatePaymentDate(payment);
                progControlPayment.updatePaymentType(payment);
                AddTicket add = new AddTicket(payment.getSeatNo(), payment.getAdult(), payment.getChild(), discount, payment.getShowId(), payment);
            }
            //Payment by Credit Card
            else
            {
                creditcards = progControlCard.getAll();
                try 
                {
                    long cardNo = Long.parseLong(jtfCreditCard.getText());
                    
                    if (jtfCreditCard.getText().length() > 12)
                    {
                        if (creditcards.size() > 0)
                        {
                            for (Creditcard c : creditcards)
                            {
                                if (c.getCardNo() == cardNo)
                                {
                                    card = c;
                                    break;
                                }
                            }
                            
                            //Don't has card reord
                            if (card == null)
                            {
                                AddCreditCard add = new AddCreditCard(payment, jtfCreditCard.getText(), discount);
                            }
                            //Has card record
                            else
                            {
                                payment.setPaymentType("Credit Card");
                                payment.setPaymentDate(date);
                                payment.setCardId(card);
                                progControlPayment.updateCard(payment);
                                progControlPayment.updatePaymentDate(payment);
                                progControlPayment.updatePaymentType(payment);
                                AddTicket add = new AddTicket(payment.getSeatNo(), payment.getAdult(), payment.getChild(), discount, payment.getShowId(), payment);
                            }
                        }
                        //Empty credit card record from database
                        else
                        {
                            AddCreditCard add = new AddCreditCard(payment, jtfCreditCard.getText(), discount);
                        }
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "Invalid of creadit card number", "Error", 0);
                        jtfCreditCard.requestFocus();
                    }
                }
                catch (NumberFormatException | HeadlessException e) 
                {
                    JOptionPane.showMessageDialog(null, "Acceptable for integer only in creadit card", "Error", 0);
                    jtfCreditCard.setText(null);
                    jtfCreditCard.requestFocus();
                }
            }
        }
    }
    
   /* public static void main(String[] args)
    {
        UpdateReservation update = new UpdateReservation();
    }
    */
}
