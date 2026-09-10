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
public class UpdateHall extends JPanel {

    //General
    private Hall hall = new Hall();
    private ArrayList<Hall> hallsArrayList = new ArrayList<>();
    private final MaintainHall progControl = new MaintainHall();
    //Label
    private final JLabel jlID = new JLabel("Hall ID");
    private final JLabel jlLocation = new JLabel("Location");

    //Text Field
    private final JTextField jtfLocation = new JTextField();

    //Button & ComboBox
    private final JButton jbtUpdate = new JButton("    Update    ");
    private final JComboBox jcbHall = new JComboBox();

    //Border style
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Update Hall ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 16);

    public UpdateHall() {
        //Set the Hall ID
        ListHallID();
        jcbHall.addActionListener(new RetrieveListener());

        //Button color
        jbtUpdate.setForeground(Color.WHITE);
        jbtUpdate.setBackground(new Color(74, 139, 245));

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
        jtfLocation.setBackground(Color.WHITE);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.add(new JLabel(""));
        buttons.add(jbtUpdate);
        buttons.setOpaque(false);
        jbtUpdate.addActionListener(new UpdateListener());

        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(3, 1, 0, 20));
        main.add(id);
        main.add(location);
        main.add(buttons);
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
            }

            else {
                //Retrieve record (Hall) from database
                hall = progControl.selectRecord(jcbHall.getSelectedItem().toString());

                //Set value
                jtfLocation.setText(hall.getHallLocation());
            }
        }
    }

    private class UpdateListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (jcbHall.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please select an ID", "Error", JOptionPane.ERROR_MESSAGE);
            }

            else {
                Hall h = new Hall(jcbHall.getSelectedItem().toString(), jtfLocation.getText(), hall.getHallStatus(), hall.getHallPeriod());
                progControl.updateRecord(h);
                JOptionPane.showMessageDialog(null, "Hall ID : " + jcbHall.getSelectedItem() + "\nUpdate record successful", "Update is successful", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

}
