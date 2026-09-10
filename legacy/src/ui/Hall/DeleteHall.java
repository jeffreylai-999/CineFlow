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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Jeffrey
 */
public class DeleteHall extends JPanel {

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
    private final JButton jbtRemove = new JButton("    Remove    ");
    private final JComboBox hallID = new JComboBox();

    //Border style
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Delete Hall ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 16);

    public DeleteHall() {
        //Set the Hall ID
        ListHallID();

        //Button Color
        jbtRemove.setForeground(Color.WHITE);
        jbtRemove.setBackground(new Color(74, 139, 245));

        //Hall
        JPanel id = new JPanel(new GridLayout(1, 2));
        id.add(jlID);
        id.add(hallID);
        id.setOpaque(false);
        hallID.setBackground(Color.WHITE);
        hallID.addActionListener(new RetrieveListener());
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Location
        JPanel location = new JPanel(new GridLayout(1, 2));
        location.add(jlLocation);
        location.add(jtfLocation);
        location.setOpaque(false);
        jtfLocation.setEditable(false);
        jtfLocation.setBackground(Color.WHITE);
        jlLocation.setForeground(Color.WHITE);
        jlLocation.setFont(FontStyle);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel(""));
        buttons.add(jbtRemove);
        buttons.setOpaque(false);
        jbtRemove.addActionListener(new DeleteListener());

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
        hallID.addItem("==============Select==============");
        hallsArrayList = progControl.getAll();
        for (Hall h : hallsArrayList) {
            if (!h.getHallStatus().equals("De-active")) {
                hallID.addItem(h.getHallId());
            }
        }
    }

    private class RetrieveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (hallID.getSelectedIndex() == 0) {
                jtfLocation.setText(null);   //Set the text field to null value
            }
            else {
                //Retrieve record (Hall) from database
                hall = progControl.selectRecord(hallID.getSelectedItem().toString());

                //Set value
                jtfLocation.setText(hall.getHallLocation());
            }
        }
    }

    private class DeleteListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (hallID.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please select an ID", "Error", JOptionPane.ERROR_MESSAGE);
            }

            else {
                int option = JOptionPane.showConfirmDialog(null, "Confirm detele the Hall : " + hallID.getSelectedItem(), "Confirmation", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    JPanel pass = new JPanel(new GridLayout(2, 1));
                    pass.add(new JLabel("Password"));
                    JPasswordField password = new JPasswordField(10);
                    pass.add(password);
                    String[] options = {"OK", "Cancel"};
                    int a = JOptionPane.showOptionDialog(null, pass, "Administrator", JOptionPane.NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

                    if (a == 0) {
                        if (password.getText().equals("confirm-delete")) {
                            //Delete Hall record from database
                            hall = new Hall(hall.getHallId(), hall.getHallLocation(), "De-active", "N/A");
                            progControl.updateRecord(hall);

                            //Pop up message
                            JOptionPane.showMessageDialog(null, "Hall ID : " + hallID.getSelectedItem() + "\nDelete record successful", "Delete is successful", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else {
                            JOptionPane.showMessageDialog(null, "Access Denied!", "Administrator", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }

}
