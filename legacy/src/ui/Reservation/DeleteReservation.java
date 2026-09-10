/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.Reservation;

import control.MaintainPayment;
import domain.Payment;
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
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Jeffrey
 */
public class DeleteReservation extends JPanel
{
    //General
    private Payment payment = new Payment();
    private ArrayList<Payment> paymentsArrayList = new ArrayList<>();
    private final MaintainPayment progControl = new MaintainPayment();
     //Label
    private final JLabel jlID = new JLabel("Customer ID");
    private final JLabel jlMovie = new JLabel("Movie");
    private final JLabel jlDate = new JLabel("Booking Date");
    private final JLabel jlTime = new JLabel("Booking Time");
    private final JLabel jlAdult = new JLabel("Adult");
    private final JLabel jlChild=new JLabel("Child");
    private final JLabel jlSeatsCode = new JLabel("Seat(s) Code");
    //Text Field
    private final JTextField jtfMovie = new JTextField(25);
    private final JTextField jtfDate = new JTextField();
    private final JTextField jtfTime = new JTextField();
    private final JTextField jtfAdult = new JTextField();
    private final JTextField jtfChild = new JTextField();
    private final JTextField jtfSeatsCode = new JTextField();
    //Button & ComboBox
    private final JButton jbtDelete = new JButton(" Cancel Reservation ");
    private final JComboBox jcbID = new JComboBox();
    //Date Time
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
    private final LocalDate localDate = new LocalDate();
    private final LocalTime localTime = new LocalTime();
    
    //Border style  
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240,240,225),3);
    
    //Border Title
    TitledBorder title = new TitledBorder(borderMain," Reservation Cancellation ",TitledBorder.LEFT,
    TitledBorder.DEFAULT_POSITION, new Font ( "Arial", Font.BOLD, 20 ), new Color(255,255,153));
    
    //JLabel Style
    Font FontStyle = new Font("SansSerif",Font.BOLD, 14); 
    
    public DeleteReservation()
    {
        ReservationList();
        
        //Button color
        jbtDelete.setForeground(Color.WHITE);
        jbtDelete.setBackground(new Color(74,139,245));
        
        //ID Panel
        JPanel idPanel = new JPanel(new GridLayout(1,2));
        idPanel.add(jlID);
        idPanel.add(jcbID);
        idPanel.setOpaque(false);
        jcbID.setBackground(Color.WHITE);
        jcbID.addActionListener(new RetrieveReservationListener());
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);   
        
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
        seatsPanel.add(jtfSeatsCode);
        seatsPanel.setOpaque(false);
        jtfSeatsCode.setEditable(false);
        jtfSeatsCode.setBackground(Color.WHITE);
        jlSeatsCode.setForeground(Color.WHITE);
        jlSeatsCode.setFont(FontStyle);   
        
        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel());
        buttons.add(jbtDelete);
        buttons.setOpaque(false);
        jbtDelete.addActionListener(new DeleteReservationListener());
        
        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(8,1,0,20));
        main.add(idPanel);
        main.add(moviePanel);
        main.add(datePanel);
        main.add(timePanel);
        main.add(adultPanel);
        main.add(childPanel);
        main.add(seatsPanel);
        main.add(buttons);
        main.setOpaque(false);
        add(main);
        
        main.setBorder (BorderFactory.createCompoundBorder ( title,BorderFactory.createEmptyBorder ( 20, 50, 25, 50 )) );
    }
    
    private void ReservationList()
    {
        //Get all records from datatbase
        paymentsArrayList = progControl.getAll();
        jcbID.removeAllItems();
        jcbID.addItem("===============Select===============");
        
        if (paymentsArrayList != null){
            for (Payment pay : paymentsArrayList)
            {
                if (pay.getPaymentDate() == null)
                    jcbID.addItem(pay.getPaymentId());
            }
            if (jcbID.getItemCount() == 1){
                jcbID.removeAllItems();
                jcbID.addItem("No reservation records");
            }
        }
        else{
            jcbID.removeAllItems();
            jcbID.addItem("No reservation records");
        }
    }
    
    private class RetrieveReservationListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            if (jcbID.getSelectedIndex() > 0){
                payment = progControl.selectRecord(jcbID.getSelectedItem().toString());
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
                //Set value to text filed
                jtfMovie.setText(payment.getShowId().getMovieId().getMovieName());
                jtfDate.setText(payment.getShowId().getShowDate());
                jtfTime.setText(payment.getShowId().getShowTime());
                jtfAdult.setText(String.valueOf(payment.getAdult()));
                jtfChild.setText(String.valueOf(payment.getChild()));
                jtfSeatsCode.setText(code);
            }
            else{
                jtfAdult.setText(null);
                jtfChild.setText(null);
                jtfDate.setText(null);
                jtfMovie.setText(null);
                jtfSeatsCode.setText(null);
                jtfTime.setText(null);
            }        
        }
    }
    
    private class DeleteReservationListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            /*
            If user didn't select any option
            Pop up invalid action message
            */
            if (jcbID.getSelectedIndex() == 0)              
                JOptionPane.showMessageDialog(null, "Select an reservation ID", "Info",JOptionPane.INFORMATION_MESSAGE);
            else{
                int option = JOptionPane.showConfirmDialog(null, "Confirm cancel the Reservation Ticket ? " + payment.getReservationId(), "Confirmation", JOptionPane.YES_NO_OPTION);
                
                //Delete movie record from database
                if (option == JOptionPane.YES_OPTION){
                    progControl.deleteRecord(jcbID.getSelectedItem().toString());

                    //Pop up delete data successful message
                    JOptionPane.showMessageDialog(null, "Reservation ID :" + payment.getReservationId() + "\nCancel reservation successfully", "Cancel Reservation",JOptionPane.INFORMATION_MESSAGE);
                    ReservationList();
                }
            }
        }
    }
}
