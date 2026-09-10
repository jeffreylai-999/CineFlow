package ui.Hall;

import control.MaintainHall;
import domain.Hall;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Jeffrey
 */
public class AddHall extends JPanel {

    //General
    private Hall hall = new Hall();
    private final MaintainHall progControl = new MaintainHall();

    //Label
    private final JLabel jlID = new JLabel("Hall ID");
    private final JLabel jlLocation = new JLabel("Location");
    //Text Field
    private final JTextField jtfID = new JTextField(25);
    private final JTextField jtfLocation = new JTextField();
    //Button & ComboBox
    private final JButton jbtConfirm = new JButton("    Confirm    ");

    //Border style
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Create Hall ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 16);

    public AddHall() {
        //Set the Hall ID
        setHallID();

        //Button color
        jbtConfirm.setForeground(Color.WHITE);
        jbtConfirm.setBackground(new Color(74, 139, 245));

        //Hall
        JPanel id = new JPanel(new GridLayout(1, 2));
        id.add(jlID);
        id.add(jtfID);
        id.setOpaque(false);
        jtfID.setEditable(false);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);
        jtfID.setBackground(Color.WHITE);

        //Location
        JPanel location = new JPanel(new GridLayout(1, 2));
        location.add(jlLocation);
        location.add(jtfLocation);
        location.setOpaque(false);
        jlLocation.setForeground(Color.WHITE);
        jlLocation.setFont(FontStyle);
        jtfLocation.setBackground(Color.WHITE);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel(""));
        buttons.add(jbtConfirm);
        buttons.setOpaque(false);
        jbtConfirm.addActionListener(new AddListener());

        setLayout(new FlowLayout());

        JPanel main = new JPanel(new GridLayout(3, 1, 10, 10)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(600, 270);
            }
        };
        main.add(id);
        main.add(location);
        main.add(buttons);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50),
                BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(30, 60, 30, 60))));

    }

    private void setHallID() {
        Hall h = progControl.lastID();

        if (h != null) {
            int id = Integer.parseInt(h.getHallId().substring(4)) + 1;
            String newID = "HALL" + String.format("%06d", id);
            jtfID.setText(newID);
        }
        else {
            jtfID.setText("HALL000001");
        }
    }

    private class AddListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            String location = jtfLocation.getText();
            hall = new Hall(jtfID.getText(), location, "Available", "N/A");

            //Insert new record (Hall) to database
            progControl.addRecord(hall);

            //Pop up insert data successful message 
            JOptionPane.showMessageDialog(null, "Hall ID :" + jtfID.getText() + "\nInsert new record successful", "New Hall", JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
