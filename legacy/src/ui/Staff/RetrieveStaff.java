package ui.Staff;

import domain.Staff;
import control.MaintainStaff;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.ArrayList;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Jeffrey
 */
public class RetrieveStaff extends JPanel {

    //General
    private final MaintainStaff progControl = new MaintainStaff();
    private Staff staff = new Staff();
    private ArrayList<Staff> staffArrayList = new ArrayList<>();
    //Label
    private final JLabel jlID = new JLabel("Staff ID");
    private final JLabel jlPosition = new JLabel("Position");
    private final JLabel jlName = new JLabel("Name");
    private final JLabel jlGender = new JLabel("Gender");
    private final JLabel jlPhone = new JLabel("Phone");
    private final JLabel jlBday = new JLabel("Birthday");
    private final JLabel jlAddress = new JLabel("Home Address");
    private final JLabel jlEmail = new JLabel("E-mail Address");
    private final JLabel jlStatus = new JLabel("Status");
    private final JLabel jlIC = new JLabel("IC Number");
    //Text Field
    private final JTextField jtfPosition = new JTextField();
    private final JTextField jtfName = new JTextField();
    private final JTextField jtfGender = new JTextField();
    private final JTextField jtfBday = new JTextField();
    private final JTextField jtfIC = new JTextField();
    private final JTextField jtfPhone = new JTextField();
    private final JTextField jtfAddress = new JTextField();
    private final JTextField jtfEmail = new JTextField();
    private final JTextField jtfStatus = new JTextField();
    //Button & ComboBox
    private final JComboBox jcbStaff = new JComboBox();

    //Border style    
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Retrieve Staff ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 15);

    public RetrieveStaff() {
        //Set the ID
        staffList();

        //ID Panel
        JPanel id = new JPanel(new GridLayout(1, 2));
        id.add(jlID);
        id.add(jcbStaff);
        id.setOpaque(false);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);
        jcbStaff.setBackground(Color.WHITE);
        jcbStaff.addActionListener(new RetrieveListener());

        //Position
        JPanel position = new JPanel(new GridLayout(1, 2));
        position.add(jlPosition);
        position.add(jtfPosition);
        position.setOpaque(false);
        jlPosition.setForeground(Color.WHITE);
        jlPosition.setFont(FontStyle);
        jtfPosition.setBackground(Color.WHITE);
        jtfPosition.setEditable(false);

        //Name Panel
        JPanel name = new JPanel(new GridLayout(1, 2));
        name.add(jlName);
        name.add(jtfName);
        name.setOpaque(false);
        jlName.setForeground(Color.WHITE);
        jlName.setFont(FontStyle);
        jtfName.setBackground(Color.WHITE);
        jtfName.setEditable(false);

        //Gender Panel
        JPanel gender = new JPanel(new GridLayout(1, 2));
        gender.add(jlGender);
        gender.add(jtfGender);
        gender.setOpaque(false);
        jlGender.setForeground(Color.WHITE);
        jlGender.setFont(FontStyle);
        jtfGender.setBackground(Color.WHITE);
        jtfGender.setEditable(false);

        //Birthday Panel
        JPanel birthday = new JPanel(new GridLayout(1, 2));
        birthday.add(jlBday);
        birthday.add(jtfBday);
        birthday.setOpaque(false);
        jtfBday.setEditable(false);
        jlBday.setForeground(Color.WHITE);
        jlBday.setFont(FontStyle);
        jtfBday.setBackground(Color.WHITE);

        //IC Panel
        JPanel IC = new JPanel(new GridLayout(1, 2));
        IC.add(jlIC);
        IC.add(jtfIC);
        IC.setOpaque(false);
        jlIC.setForeground(Color.WHITE);
        jlIC.setFont(FontStyle);
        jtfIC.setBackground(Color.WHITE);
        jtfIC.setEditable(false);

        //Phone Panel
        JPanel phone = new JPanel(new GridLayout(1, 2));
        phone.add(jlPhone);
        phone.add(jtfPhone);
        phone.setOpaque(false);
        jlPhone.setForeground(Color.WHITE);
        jlPhone.setFont(FontStyle);
        jtfPhone.setBackground(Color.WHITE);
        jtfPhone.setEditable(false);

        //Address Panel
        JPanel address = new JPanel(new GridLayout(1, 2));
        address.add(jlAddress);
        address.add(jtfAddress);
        address.setOpaque(false);
        jlAddress.setForeground(Color.WHITE);
        jlAddress.setFont(FontStyle);
        jtfAddress.setBackground(Color.WHITE);
        jtfAddress.setEditable(false);

        //E-mail Panel
        JPanel email = new JPanel(new GridLayout(1, 2));
        email.add(jlEmail);
        email.add(jtfEmail);
        email.setOpaque(false);
        jlEmail.setForeground(Color.WHITE);
        jlEmail.setFont(FontStyle);
        jtfEmail.setBackground(Color.WHITE);
        jtfEmail.setEditable(false);

        //Status
        JPanel status = new JPanel(new GridLayout(1, 2));
        status.add(jlStatus);
        status.add(jtfStatus);
        status.setOpaque(false);
        jlStatus.setForeground(Color.WHITE);
        jlStatus.setFont(FontStyle);
        jtfStatus.setBackground(Color.WHITE);
        jtfStatus.setEditable(false);

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(10, 1, 0, 20));
        main.add(id);
        main.add(position);
        main.add(name);
        main.add(gender);
        main.add(birthday);
        main.add(IC);
        main.add(phone);
        main.add(address);
        main.add(email);
        main.add(status);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 15, 50)));
    }

    private void staffList() {
        jcbStaff.addItem("===============Select===============");
        staffArrayList = progControl.getAll();
        if (staffArrayList != null) {
            for (Staff stf : staffArrayList) {
                jcbStaff.addItem(stf.getStaffId());
            }
        }
        else {
            jcbStaff.removeAllItems();
            jcbStaff.addItem("No Staff");
        }
    }

    private class RetrieveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (jcbStaff.getSelectedIndex() != 0) {
                staff = progControl.selectRecord(jcbStaff.getSelectedItem().toString());

                //Show the record of member
                jtfPosition.setText(staff.getStaffPosition());
                jtfName.setText(staff.getStaffName());
                jtfGender.setText(staff.getStaffGender());
                jtfBday.setText(staff.getStaffBirthday());
                jtfIC.setText(staff.getStaffIc());
                jtfPhone.setText(staff.getStaffPhone());
                jtfAddress.setText(staff.getStaffAddress());
                jtfEmail.setText(staff.getStaffEmail());
                jtfStatus.setText(staff.getStaffStatus());
            }
            else {
                //Clear text field
                jtfName.setText(null);
                jtfGender.setText(null);
                jtfBday.setText(null);
                jtfIC.setText(null);
                jtfPhone.setText(null);
                jtfAddress.setText(null);
                jtfEmail.setText(null);
                jtfPosition.setText(null);
                jtfStatus.setText(null);
            }
        }
    }

}
