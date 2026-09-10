/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.Payment;

import control.MaintainCreditCard;
import control.MaintainPayment;
import domain.Creditcard;
import domain.Payment;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.joda.time.LocalDate;
import ui.AddTicket;
import ui.Validation;

/**
 *
 * @author Jeffrey
 */
public class AddCreditCard extends JPanel {

    //General
    private Creditcard card = new Creditcard();
    private Payment payment = new Payment();
    private final String[] type = {"===============Select===============",
        "MASTER", "VISA", "AMERICAN EXPRESS"};
    private int discount = 0;
    private final Validation valid = new Validation();
    private final MaintainCreditCard progControlCard = new MaintainCreditCard();
    private final MaintainPayment progControlPayment = new MaintainPayment();
    //Label
    private final JLabel jlID = new JLabel("Credit Card ID");
    private final JLabel jlCardNo = new JLabel("Credit Card Number");
    private final JLabel jlCardName = new JLabel("Credit Card Hold Name");
    private final JLabel jlCardType = new JLabel("Type of Credit Card");
    private final JLabel jlCardBank = new JLabel("Credit Card Bank");
    private final JLabel jlCardExp = new JLabel("Credit Card Expired Date");
    private final JLabel jlErrorMessageCardNo = new JLabel();
    private final JLabel jlErrorMessageCardName = new JLabel();
    private final JLabel jlErrorMessageCardType = new JLabel();
    private final JLabel jlErrorMessageCardBank = new JLabel();
    private final JLabel jlErrorMessageExpired = new JLabel();
    //Text Field
    private final JTextField jtfID = new JTextField();
    private final JTextField jtfCardNo = new JTextField();
    private final JTextField jtfCardName = new JTextField();
    private final JTextField jtfCardBank = new JTextField();
    private final JTextField jtfCardExp = new JTextField();
    //ComboBox & Buttons
    private final JComboBox jcbCardType = new JComboBox(type);
    private final JButton jbtContinuous = new JButton("    Add Credit Card    ");
    //Date
    private final LocalDate today = new LocalDate();

    //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED, 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Create Credit Card ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public AddCreditCard(Payment payment, String no, int discount) {
        setCardID();
        this.payment = payment;
        this.discount = discount;

        //Buttons Color
        jbtContinuous.setForeground(Color.WHITE);
        jbtContinuous.setBackground(new Color(74, 139, 245));

        //ID 
        JPanel idPanel = new JPanel(new GridLayout(2, 2));
        idPanel.add(jlID);
        idPanel.add(jtfID);
        idPanel.add(new JLabel());
        idPanel.add(new JLabel());
        idPanel.setOpaque(false);
        jtfID.setEditable(false);
        jtfID.setBackground(Color.WHITE);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Card No
        JPanel cardNoPanel = new JPanel(new GridLayout(2, 2));
        cardNoPanel.add(jlCardNo);
        cardNoPanel.add(jtfCardNo);
        cardNoPanel.add(new JLabel());
        cardNoPanel.add(jlErrorMessageCardNo);
        cardNoPanel.setOpaque(false);
        jtfCardNo.setText(no);
        jlCardNo.setForeground(Color.WHITE);
        jlCardNo.setFont(FontStyle);

        //Card Name
        JPanel cardNamePanel = new JPanel(new GridLayout(2, 2));
        cardNamePanel.add(jlCardName);
        cardNamePanel.add(jtfCardName);
        cardNamePanel.add(new JLabel());
        cardNamePanel.add(jlErrorMessageCardName);
        cardNamePanel.setOpaque(false);
        jlCardName.setForeground(Color.WHITE);
        jlCardName.setFont(FontStyle);

        //Card Type
        JPanel cardTypePanel = new JPanel(new GridLayout(2, 2));
        cardTypePanel.add(jlCardType);
        cardTypePanel.add(jcbCardType);
        cardTypePanel.add(new JLabel());
        cardTypePanel.add(jlErrorMessageCardType);
        cardTypePanel.setOpaque(false);
        jcbCardType.setBackground(Color.WHITE);
        jlCardType.setForeground(Color.WHITE);
        jlCardType.setFont(FontStyle);

        //Card Bank
        JPanel cardBankPanel = new JPanel(new GridLayout(2, 2));
        cardBankPanel.add(jlCardBank);
        cardBankPanel.add(jtfCardBank);
        cardBankPanel.add(new JLabel());
        cardBankPanel.add(jlErrorMessageCardBank);
        cardBankPanel.setOpaque(false);
        jlCardBank.setForeground(Color.WHITE);
        jlCardBank.setFont(FontStyle);

        //Card Expired Date
        JPanel cardExpPanel = new JPanel(new GridLayout(2, 2));
        cardExpPanel.add(jlCardExp);
        cardExpPanel.add(jtfCardExp);
        cardExpPanel.add(new JLabel());
        cardExpPanel.add(jlErrorMessageExpired);
        cardExpPanel.setOpaque(false);
        jlCardExp.setForeground(Color.WHITE);
        jlCardExp.setFont(FontStyle);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel());
        buttons.add(jbtContinuous);
        buttons.setOpaque(false);
        jbtContinuous.addActionListener(new AddCreditCardListener());

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(7, 1));
        main.add(idPanel);
        main.add(cardNoPanel);
        main.add(cardNamePanel);
        main.add(cardTypePanel);
        main.add(cardBankPanel);
        main.add(cardExpPanel);
        main.add(buttons);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));

    }

    private void setCardID() {
        String creditcardID = progControlCard.lastID();
        String cardID;
        if (creditcardID != null) {
            int newID = Integer.parseInt(creditcardID.substring(4)) + 1;
            cardID = "CARD" + String.format("%06d", newID);
            jtfID.setText(cardID);
        }
        else {
            cardID = "CARD000001";
            jtfID.setText(cardID);
        }
    }

    private class AddCreditCardListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (validation()) {
                card = new Creditcard(jtfID.getText(),
                        Long.parseLong(jtfCardNo.getText()),
                        jtfCardName.getText(),
                        jcbCardType.getSelectedItem().toString(),
                        jtfCardBank.getText(), jtfCardExp.getText());
                payment.setCardId(card);
                progControlCard.addRecord(card);

                if (payment.getCusType().equals("walkin")) {
                    progControlPayment.addPaymentWithout(payment);
                    progControlPayment.updatePaymentType(payment);
                    progControlPayment.updatePaymentDate(payment);
                    progControlPayment.updateCard(payment);
                    if (payment.getCusId() != null) {
                        progControlPayment.updateCustomer(payment);
                    }
                    AddTicket add = new AddTicket(payment.getSeatNo(), payment.getAdult(), payment.getChild(), discount, payment.getShowId(), payment);
                    // JOptionPane.showMessageDialog(null, "sUCCESSFUL", "Error", 0);
                }
                else {
                    payment.setPaymentDate(today.toString("dd/MM/yyyy"));
                    payment.setPaymentType("Credit Card");
                    progControlPayment.updateCard(payment);
                    progControlPayment.updatePaymentDate(payment);
                    progControlPayment.updatePaymentType(payment);
                    AddTicket add = new AddTicket(payment.getSeatNo(), payment.getAdult(), payment.getChild(), discount, payment.getShowId(), payment);
                    // JOptionPane.showMessageDialog(null, "sUCCESSFUL", "Error", 0);
                }
            }
        }
    }

    private boolean validation() {
        //Clear the error message
        jlErrorMessageCardBank.setText(null);
        jlErrorMessageCardName.setText(null);
        jlErrorMessageCardNo.setText(null);
        jlErrorMessageCardType.setText(null);
        jlErrorMessageExpired.setText(null);

        try {
            long n = Long.parseLong(jtfCardNo.getText());
        }
        catch (Exception e) {
            jlErrorMessageCardNo.setText("Invalid card number");
            jlErrorMessageCardNo.setForeground(Color.red);
            jtfCardNo.requestFocus();
            return false;
        }

        //Check card number is less than 12 number
        if (jtfCardNo.getText().length() < 12) {
            jlErrorMessageCardNo.setText("Invalid card number");
            jlErrorMessageCardNo.setForeground(Color.red);
            jtfCardNo.requestFocus();
            return false;
        }

        //Check card name is empty
        else if (jtfCardName.getText().isEmpty()) {
            jlErrorMessageCardName.setText("Please enter the card holder name");
            jlErrorMessageCardName.setForeground(Color.red);
            jtfCardName.requestFocus();
            return false;
        }

        else if (!valid.isValidName(jtfCardName.getText())) {
            jlErrorMessageCardName.setText("Invalid name");
            jlErrorMessageCardName.setForeground(Color.red);
            jtfCardName.setText(null);
            jtfCardName.requestFocus();
            return false;
        }
        //Check type of card is selected
        else if (jcbCardType.getSelectedIndex() == 0) {
            jlErrorMessageCardType.setText("Please select the type of card");
            jlErrorMessageCardType.setForeground(Color.red);
            return false;
        }
        //Check bank of card
        else if (jtfCardBank.getText().isEmpty()) {
            jlErrorMessageCardBank.setText("Please enter the bank of card");
            jlErrorMessageCardBank.setForeground(Color.red);
            jtfCardBank.requestFocus();
            return false;
        }
        //Check the card expired
        else if (jtfCardExp.getText().isEmpty()) {
            jlErrorMessageExpired.setText("Please enter the card expired date");
            jlErrorMessageExpired.setForeground(Color.red);
            jtfCardExp.setText(null);
            jtfCardExp.requestFocus();
            return false;
        }
        else {
            try {
                if (jtfCardExp.getText().length() != 7) {
                    jlErrorMessageExpired.setText("E.g : 12/" + today.getYear());
                    jlErrorMessageExpired.setForeground(Color.red);
                    jtfCardExp.requestFocus();
                    return false;
                }
                else {
                    String date = jtfCardExp.getText();
                    int month = Integer.parseInt(date.substring(0, 2));
                    int year = Integer.parseInt(date.substring(3));

                    if (month > 12) {
                        jlErrorMessageExpired.setText("E.g : 12/" + today.getYear());
                        jlErrorMessageExpired.setForeground(Color.red);
                        jtfCardExp.requestFocus();
                        return false;
                    }
                    else if (year < today.getYear() || year > today.getYear() + 7) {
                        jlErrorMessageExpired.setText("E.g : 12/" + today.getYear());
                        jlErrorMessageExpired.setForeground(Color.red);
                        jtfCardExp.requestFocus();
                        return false;
                    }
                }
            }
            catch (Exception e) {
                jlErrorMessageExpired.setText("E.g : 12/" + today.getYear());
                jlErrorMessageExpired.setForeground(Color.red);
                jtfCardExp.requestFocus();
                return false;
            }
        }
        return true;
    }
}
