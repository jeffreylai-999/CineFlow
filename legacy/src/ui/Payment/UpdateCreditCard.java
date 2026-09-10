/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.Payment;

import control.MaintainCreditCard;
import domain.Creditcard;
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
public class UpdateCreditCard extends JPanel {

    //General
    private Creditcard card = new Creditcard();
    private ArrayList<Creditcard> creditcards = new ArrayList<>();
    private final String[] type = {"===============Select===============",
        "MASTER", "VISA", "American Express"};
    private final MaintainCreditCard progControlCard = new MaintainCreditCard();
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
    private final JTextField jtfCardNo = new JTextField();
    private final JTextField jtfCardName = new JTextField();
    private final JTextField jtfCardBank = new JTextField();
    private final JTextField jtfCardExp = new JTextField();
    //ComboBox
    private final JComboBox jcbCardID = new JComboBox();
    private final JComboBox jcbCardType = new JComboBox(type);
    private final JButton jbtContinuous = new JButton("    Save Change   ");

    //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED, 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Update Credit Card ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public UpdateCreditCard() {
        getCreditCardID();

        //Buttons Color
        jbtContinuous.setForeground(Color.WHITE);
        jbtContinuous.setBackground(new Color(74, 139, 245));

        //ID 
        JPanel idPanel = new JPanel(new GridLayout(2, 2));
        idPanel.add(jlID);
        idPanel.add(jcbCardID);
        idPanel.add(new JLabel());
        idPanel.add(new JLabel());
        idPanel.setOpaque(false);
        jcbCardID.setBackground(Color.WHITE);
        jcbCardID.addActionListener(new RetrieveCreditCardListener());
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Card No
        JPanel cardNoPanel = new JPanel(new GridLayout(2, 2));
        cardNoPanel.add(jlCardNo);
        cardNoPanel.add(jtfCardNo);
        cardNoPanel.add(new JLabel());
        cardNoPanel.add(jlErrorMessageCardNo);
        cardNoPanel.setOpaque(false);
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
        jbtContinuous.addActionListener(new UpdateCreditCardListener());

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

    private void getCreditCardID() {
        creditcards = progControlCard.getAll();
        jcbCardID.removeAllItems();
        jcbCardID.addItem("===============Select===============");

        if (creditcards != null) {
            for (Creditcard c : creditcards) {
                jcbCardID.addItem(c.getCardId());
            }
        }
        else {
            jcbCardID.removeAllItems();
            jcbCardID.addItem("===============Select===============");
        }

    }

    private class RetrieveCreditCardListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (jcbCardID.getSelectedIndex() != 0) {
                String id = jcbCardID.getSelectedItem().toString();
                card = progControlCard.selectRecord(id);

                jtfCardBank.setText(card.getCardBank());
                jtfCardExp.setText(card.getCardExp());
                jtfCardName.setText(card.getCardHoldName());
                jtfCardNo.setText(String.valueOf(card.getCardNo()));

                if (card.getCardType().equals("VISA")) {
                    jcbCardType.setSelectedIndex(2);
                }
                else if (card.getCardType().equals("MASTER")) {
                    jcbCardType.setSelectedIndex(1);
                }
                else {
                    jcbCardType.setSelectedIndex(3);
                }
            }
            else {
                jtfCardBank.setText(null);
                jtfCardExp.setText(null);
                jtfCardName.setText(null);
                jtfCardNo.setText(null);
                jcbCardType.setSelectedIndex(0);
            }
        }
    }

    private class UpdateCreditCardListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {

        }
    }
}
