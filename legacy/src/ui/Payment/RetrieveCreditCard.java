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
public class RetrieveCreditCard extends JPanel {

    //General
    private Creditcard card = new Creditcard();
    private ArrayList<Creditcard> creditcards = new ArrayList<>();
    private final MaintainCreditCard progControlCard = new MaintainCreditCard();
    //Label
    private final JLabel jlID = new JLabel("Credit Card ID");
    private final JLabel jlCardNo = new JLabel("Credit Card Number");
    private final JLabel jlCardName = new JLabel("Credit Card Hold Name");
    private final JLabel jlCardType = new JLabel("Type of Credit Card");
    private final JLabel jlCardBank = new JLabel("Credit Card Bank");
    private final JLabel jlCardExp = new JLabel("Credit Card Expired Date");
    //Text Field
    private final JTextField jtfCardType = new JTextField();
    private final JTextField jtfCardNo = new JTextField();
    private final JTextField jtfCardName = new JTextField();
    private final JTextField jtfCardBank = new JTextField();
    private final JTextField jtfCardExp = new JTextField();
    //ComboBox
    private final JComboBox jcbCardID = new JComboBox();

    //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED, 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Retrieve Credit Card ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public RetrieveCreditCard() {
        getCreditCardID();
        //ID 
        JPanel idPanel = new JPanel(new GridLayout(1, 2));
        idPanel.add(jlID);
        idPanel.add(jcbCardID);
        idPanel.setOpaque(false);
        jcbCardID.setBackground(Color.WHITE);
        jcbCardID.addActionListener(new RetrieveCreditCardListener());
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Card No
        JPanel cardNoPanel = new JPanel(new GridLayout(1, 2));
        cardNoPanel.add(jlCardNo);
        cardNoPanel.add(jtfCardNo);
        cardNoPanel.setOpaque(false);
        jtfCardNo.setBackground(Color.WHITE);
        jtfCardNo.setEditable(false);
        jlCardNo.setForeground(Color.WHITE);
        jlCardNo.setFont(FontStyle);

        //Card Name
        JPanel cardNamePanel = new JPanel(new GridLayout(1, 2));
        cardNamePanel.add(jlCardName);
        cardNamePanel.add(jtfCardName);
        cardNamePanel.setOpaque(false);
        jtfCardName.setBackground(Color.WHITE);
        jtfCardName.setEditable(false);
        jlCardName.setForeground(Color.WHITE);
        jlCardName.setFont(FontStyle);

        //Card Type
        JPanel cardTypePanel = new JPanel(new GridLayout(1, 2));
        cardTypePanel.add(jlCardType);
        cardTypePanel.add(jtfCardType);
        cardTypePanel.setOpaque(false);
        jtfCardType.setBackground(Color.WHITE);
        jtfCardType.setEditable(false);
        jlCardType.setForeground(Color.WHITE);
        jlCardType.setFont(FontStyle);

        //Card Bank
        JPanel cardBankPanel = new JPanel(new GridLayout(1, 2));
        cardBankPanel.add(jlCardBank);
        cardBankPanel.add(jtfCardBank);
        cardBankPanel.setOpaque(false);
        jtfCardBank.setBackground(Color.WHITE);
        jtfCardBank.setEditable(false);
        jlCardBank.setForeground(Color.WHITE);
        jlCardBank.setFont(FontStyle);

        //Card Expired Date
        JPanel cardExpPanel = new JPanel(new GridLayout(1, 2));
        cardExpPanel.add(jlCardExp);
        cardExpPanel.add(jtfCardExp);
        cardExpPanel.setOpaque(false);
        jtfCardExp.setBackground(Color.WHITE);
        jtfCardExp.setEditable(false);
        jlCardExp.setForeground(Color.WHITE);
        jlCardExp.setFont(FontStyle);

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(6, 1, 0, 20));
        main.add(idPanel);
        main.add(cardNoPanel);
        main.add(cardNamePanel);
        main.add(cardTypePanel);
        main.add(cardBankPanel);
        main.add(cardExpPanel);
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
                jtfCardType.setText(card.getCardType());
            }
            else {
                jtfCardBank.setText(null);
                jtfCardExp.setText(null);
                jtfCardName.setText(null);
                jtfCardNo.setText(null);
                jtfCardType.setText(null);
            }
        }
    }
}
