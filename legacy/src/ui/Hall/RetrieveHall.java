package ui.Hall;

import control.MaintainHall;
import domain.Hall;
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
public class RetrieveHall extends JPanel {

    //General
    private Hall hall = new Hall();
    private ArrayList<Hall> hallsArrayList = new ArrayList<>();
    private final MaintainHall progControl = new MaintainHall();
    //Label
    private final JLabel jlID = new JLabel("Hall ID");
    private final JLabel jlLocation = new JLabel("Location");
    private final JLabel jlStatus = new JLabel("Status");
    private final JLabel jlPeriod = new JLabel("Period");
    //Text Field
    private final JTextField jtfLocation = new JTextField();
    private final JTextField jtfStatus = new JTextField();
    private final JTextField jtfPeriod = new JTextField();

    //ComboBox  
    private final JComboBox jcbHall = new JComboBox();

    //Border style
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Retrieve Hall ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 16);

    public RetrieveHall() {
        //Set the Hall ID
        ListHallID();
        jcbHall.addActionListener(new RetrieveListener());

        //Hall
        JPanel id = new JPanel(new GridLayout(1, 2));
        id.add(jlID);
        id.add(jcbHall);
        id.setOpaque(false);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);
        jcbHall.setBackground(Color.WHITE);

        //Location
        JPanel location = new JPanel(new GridLayout(1, 2));
        location.add(jlLocation);
        location.add(jtfLocation);
        location.setOpaque(false);
        jlLocation.setForeground(Color.WHITE);
        jlLocation.setFont(FontStyle);
        jtfLocation.setEditable(false);
        jtfLocation.setBackground(Color.WHITE);

        //Status
        JPanel status = new JPanel(new GridLayout(1, 2));
        status.add(jlStatus);
        status.add(jtfStatus);
        status.setOpaque(false);
        jlStatus.setForeground(Color.WHITE);
        jlStatus.setFont(FontStyle);
        jtfStatus.setEditable(false);
        jtfStatus.setBackground(Color.WHITE);

        //Period
        JPanel period = new JPanel(new GridLayout(1, 2));
        period.add(jlPeriod);
        period.add(jtfPeriod);
        period.setOpaque(false);
        jlPeriod.setForeground(Color.WHITE);
        jlPeriod.setFont(FontStyle);
        jtfPeriod.setBackground(Color.WHITE);
        jtfPeriod.setEditable(false);

        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(4, 1, 0, 20));
        main.add(id);
        main.add(location);
        main.add(status);
        main.add(period);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50),
                BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(30, 60, 30, 60))));

    }

    private void ListHallID() {
        jcbHall.addItem("===============Select===============");
        hallsArrayList = progControl.getAll();
        for (Hall h : hallsArrayList) {
            jcbHall.addItem(h.getHallId());
        }
    }

    private class RetrieveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            //Set the text field to null value
            if (jcbHall.getSelectedIndex() == 0) {
                jtfLocation.setText(null);
                jtfPeriod.setText(null);
                jtfStatus.setText(null);
            }

            else {
                //Retrieve record (Hall) from database
                hall = progControl.selectRecord(jcbHall.getSelectedItem().toString());

                //Set value
                jtfLocation.setText(hall.getHallLocation());
                jtfPeriod.setText(hall.getHallPeriod());
                jtfStatus.setText(hall.getHallStatus());
            }
        }
    }

}
