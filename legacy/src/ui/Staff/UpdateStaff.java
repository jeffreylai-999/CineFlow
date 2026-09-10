package ui.Staff;

import domain.Staff;
import control.MaintainStaff;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import java.util.ArrayList;
import javax.swing.border.TitledBorder;
import org.joda.time.LocalDate;
import ui.Validation;

/**
 *
 * @author Jeffrey
 */
public class UpdateStaff extends JPanel {

    //General
    private ArrayList<Staff> staffArrayList = new ArrayList<>();
    private Staff staff = new Staff();
    private final Validation va = new Validation();
    private final MaintainStaff progControl = new MaintainStaff();
    private final String[] positionArray = {"=================Select=================", "Staff", "Administrator"};
    private final String[] genderArray = {"=================Select=================",
        "Male", "Female"};
    private final String[] monthArray = {"Month", "January", "February",
        "March", "April", "May", "June", "July", "August", "September",
        "October", "November", "December"};
    //Label
    private final JLabel jlPosition = new JLabel("Position");
    private final JLabel jlID = new JLabel("Member ID");
    private final JLabel jlName = new JLabel("Name");
    private final JLabel jlGender = new JLabel("Gender");
    private final JLabel jlPhone = new JLabel("Phone");
    private final JLabel jlBday = new JLabel("Birthday");
    private final JLabel jlAddress = new JLabel("Home Address");
    private final JLabel jlEmail = new JLabel("E-mail Address");
    private final JLabel jlIC = new JLabel("IC Number");
    private final JLabel jlPassword = new JLabel("Password");
    private final JLabel jlErrorMessagePosition = new JLabel();
    private final JLabel jlErrorMessageName = new JLabel();
    private final JLabel jlErrorMessageGender = new JLabel();
    private final JLabel jlErrorMessagePhone = new JLabel();
    private final JLabel jlErrorMessageAddress = new JLabel();
    private final JLabel jlErrorMessageEmail = new JLabel();
    private final JLabel jlErrorMessageBday = new JLabel();
    private final JLabel jlErrorMessageIC = new JLabel();
    //Text Field
    private final JTextField jtfName = new JTextField();
    private final JTextField jtfPhone = new JTextField();
    private final JTextField jtfAddress = new JTextField();
    private final JTextField jtfEmail = new JTextField();
    private final JTextField jtfIC = new JTextField();
    private final JPasswordField jtfPassword = new JPasswordField();
    //Button & ComboBox
    private final JButton jbtUpdate = new JButton("    Save change    ");
    private final JButton jbtReset = new JButton("    Reset password    ");
    private final JButton jbtResign = new JButton("    Resign    ");
    private final JComboBox jcbStaff = new JComboBox();
    private final JComboBox jcbGender = new JComboBox(genderArray);
    private final JComboBox bDay = new JComboBox();
    private final JComboBox bMon = new JComboBox(monthArray);
    private final JComboBox bYear = new JComboBox();
    private final JComboBox jcbPosition = new JComboBox(positionArray);
    //Date Time
    private final LocalDate localDate = new LocalDate();
    private final int currentYear = localDate.getYear();

    //Border style
    private final Border original = jtfEmail.getBorder();
    private final Border border = BorderFactory.createLineBorder(Color.RED, 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Update Staff ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 15);

    public UpdateStaff() {
        //List the Member ID
        staffLists();

        //Set drop-down list of Birthday
        bDay.addItem("Day");        //Set days
        for (int i = 1; i <= 31; i++) {
            bDay.addItem(String.format("%02d", i));
        }

        bYear.addItem("Year");      //Set Year until the current year
        for (int i = 1970; i <= currentYear; i++) {
            bYear.addItem(i);
        }

        //Button color
        jbtUpdate.setForeground(Color.WHITE);
        jbtReset.setForeground(Color.WHITE);
        jbtResign.setForeground(Color.WHITE);
        jbtUpdate.setBackground(new Color(74, 139, 245));
        jbtReset.setBackground(new Color(74, 139, 245));
        jbtResign.setBackground(new Color(74, 139, 245));

        //Birthday
        JPanel pDate = new JPanel(new GridLayout(1, 3, 10, 0));
        pDate.add(bDay);
        pDate.add(bMon);
        pDate.add(bYear);
        pDate.setOpaque(false);
        bDay.setBackground(Color.WHITE);
        bMon.setBackground(Color.WHITE);
        bYear.setBackground(Color.WHITE);

        /* ============ leftmainLabel left hand side for JLabel ============ */
        JPanel leftmainLabel = new JPanel(new GridLayout(10, 1, 0, 0));
        leftmainLabel.setOpaque(false);

        //Position Label
        leftmainLabel.add(jlPosition);
        leftmainLabel.add(new JLabel(""));
        leftmainLabel.setOpaque(false);
        jlPosition.setForeground(Color.WHITE);
        jlPosition.setFont(FontStyle);

        //ID Label
        leftmainLabel.add(jlID);
        leftmainLabel.add(new JLabel(""));
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //Name Label
        leftmainLabel.add(jlName);
        leftmainLabel.add(new JLabel(""));
        jlName.setForeground(Color.WHITE);
        jlName.setFont(FontStyle);

        //Password Label
        leftmainLabel.add(jlPassword);
        leftmainLabel.add(new JLabel(""));
        jlPassword.setForeground(Color.WHITE);
        jlPassword.setFont(FontStyle);

        //Gender
        leftmainLabel.add(jlGender);
        leftmainLabel.add(new JLabel(""));
        jlGender.setForeground(Color.WHITE);
        jlGender.setFont(FontStyle);

        /* ============ rightmainLabel left hand side for JLabel ============ */
        JPanel rightmainLabel = new JPanel(new GridLayout(10, 1, 0, 0));
        rightmainLabel.setOpaque(false);

        //Birthday Label
        rightmainLabel.add(jlBday);
        rightmainLabel.add(new JLabel());
        jlBday.setForeground(Color.WHITE);
        jlBday.setFont(FontStyle);

        //IC
        rightmainLabel.add(jlIC);
        rightmainLabel.add(new JLabel());
        jlIC.setForeground(Color.WHITE);
        jlIC.setFont(FontStyle);

        //Phone Number Label
        rightmainLabel.add(jlPhone);
        rightmainLabel.add(new JLabel());
        jlPhone.setForeground(Color.WHITE);
        jlPhone.setFont(FontStyle);

        //Address Label
        rightmainLabel.add(jlAddress);
        rightmainLabel.add(new JLabel());
        jlAddress.setForeground(Color.WHITE);
        jlAddress.setFont(FontStyle);

        //Email Label
        rightmainLabel.add(jlEmail);
        rightmainLabel.add(new JLabel());
        jlEmail.setForeground(Color.WHITE);
        jlEmail.setFont(FontStyle);

        /* ============ leftmainField left hand side for TexField and combo box ============ */
        JPanel leftmainField = new JPanel(new GridLayout(10, 1, 0, 0));
        leftmainField.setOpaque(false);

        //Position combo box   
        leftmainField.add(jcbPosition);
        leftmainField.add(jlErrorMessagePosition);
        jcbPosition.setBackground(Color.WHITE);

        //Staff combo box  
        leftmainField.add(jcbStaff);
        leftmainField.add(new JLabel());
        leftmainField.setOpaque(false);
        jcbStaff.setBackground(Color.WHITE);
        jcbStaff.addActionListener(new CheckListener());

        //Name TextField
        leftmainField.add(jtfName);
        leftmainField.add(jlErrorMessageName);
        jtfName.setBackground(Color.WHITE);

        //Password TextField
        leftmainField.add(jtfPassword);
        leftmainField.add(new JLabel());
        jtfPassword.setBackground(Color.WHITE);
        jtfPassword.setEditable(false);

        //Gender combo box
        leftmainField.add(jcbGender);
        leftmainField.add(jlErrorMessageGender);
        jcbGender.setBackground(Color.WHITE);

        /* ============ rightmainField left hand side for TexField and combo box ============ */
        JPanel rightmainField = new JPanel(new GridLayout(10, 1, 0, 0));
        rightmainField.setOpaque(false);

        //Birthday combo box    
        rightmainField.add(pDate);
        rightmainField.add(jlErrorMessageBday);

        //IC TextField       
        rightmainField.add(jtfIC);
        rightmainField.add(jlErrorMessageIC);
        jtfIC.setBackground(Color.WHITE);

        //Phone Number TextField
        rightmainField.add(jtfPhone);
        rightmainField.add(jlErrorMessagePhone);
        jtfPhone.setBackground(Color.WHITE);

        //Address TextField                
        rightmainField.add(jtfAddress);
        rightmainField.add(jlErrorMessageAddress);
        jtfAddress.setBackground(Color.WHITE);

        //E-mail TextField             
        rightmainField.add(jtfEmail);
        rightmainField.add(jlErrorMessageEmail);
        jtfEmail.setBackground(Color.WHITE);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(jbtReset);
        buttons.add(jbtResign);
        buttons.add(jbtUpdate);
        buttons.setOpaque(false);
        jbtUpdate.addActionListener(new UpdateListener());
        jbtReset.addActionListener(new ResetPasswordListener());
        jbtResign.addActionListener(new ResignListener());

        //Panel which consists leftmainLabel,leftmainField,rightmainLabel and rightmainField
        JPanel leftmain = new JPanel(new BorderLayout(30, 30));
        leftmain.setOpaque(false);

        JPanel rightmain = new JPanel(new BorderLayout(30, 30));
        rightmain.setOpaque(false);

        leftmain.add(leftmainLabel, BorderLayout.WEST);
        leftmain.add(leftmainField, BorderLayout.CENTER);
        rightmain.add(rightmainLabel, BorderLayout.WEST);
        rightmain.add(rightmainField, BorderLayout.CENTER);

        //main panel
        JPanel main3 = new JPanel(new BorderLayout(60, 20));
        main3.add(buttons, BorderLayout.SOUTH);
        main3.add(leftmain, BorderLayout.WEST);
        main3.add(rightmain, BorderLayout.CENTER);
        main3.setOpaque(false);
        add(main3);
        main3.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(30, 80, 5, 80)));
    }

    private void staffLists() {
        jcbStaff.addItem("=================Select=================");
        staffArrayList = progControl.getAll();
        if (staffArrayList != null) {
            for (Staff stf : staffArrayList) {
                if (stf.getStaffStatus().equals("Active")) {
                    jcbStaff.addItem(stf.getStaffId());
                }
            }
        }
        else {
            jcbStaff.removeAllItems();
            jcbStaff.addItem("No Staff");
        }
    }

    private void clearErrorMessage() {
        jtfName.setBorder(original);
        jtfPassword.setBorder(original);
        jtfPhone.setBorder(original);
        jtfEmail.setBorder(original);
        jtfAddress.setBorder(original);
        jtfIC.setBorder(original);
        jlErrorMessageName.setText(null);
        jlErrorMessageAddress.setText(null);
        jlErrorMessageBday.setText(null);
        jlErrorMessageEmail.setText(null);
        jlErrorMessageIC.setText(null);
        jlErrorMessageGender.setText(null);
        jlErrorMessagePhone.setText(null);
    }

    private class CheckListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (jcbStaff.getSelectedIndex() != 0) {
                //Clear the border color
                clearErrorMessage();

                staff = progControl.selectRecord(jcbStaff.getSelectedItem().toString());

                //Show the record of member
                jcbPosition.setSelectedItem(staff.getStaffPosition());
                jtfName.setText(staff.getStaffName());
                jtfPassword.setText(staff.getStaffPassword());
                jtfIC.setText(staff.getStaffIc());
                jtfPhone.setText(staff.getStaffPhone());
                jtfAddress.setText(staff.getStaffAddress());
                jtfEmail.setText(staff.getStaffEmail());
                jcbGender.setSelectedItem(staff.getStaffGender());

                //Get the member Birthday and show it
                String[] birth = staff.getStaffBirthday().split("/");
                bDay.setSelectedIndex(Integer.parseInt(birth[0]));
                bMon.setSelectedIndex(Integer.parseInt(birth[1]));
                bYear.setSelectedIndex(Integer.parseInt(birth[2]) - 1969);
            }

            else {
                //Clear text field
                clearErrorMessage();
                jtfName.setText(null);
                jtfIC.setText(null);
                jtfPhone.setText(null);
                jtfAddress.setText(null);
                jtfEmail.setText(null);
                jtfPassword.setText(null);
                jcbGender.setSelectedIndex(0);
                bDay.setSelectedIndex(0);
                bMon.setSelectedIndex(0);
                bYear.setSelectedIndex(0);
                jcbPosition.setSelectedIndex(0);
            }
        }
    }

    private class ResetPasswordListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (jcbStaff.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please select a ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else {
                jtfPassword.setText(jcbStaff.getSelectedItem().toString());
            }
        }
    }

    private class UpdateListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            //Clear the border color      
            clearErrorMessage();

            //Get the value
            int year = bYear.getSelectedIndex();
            int month = bMon.getSelectedIndex();
            int day = bDay.getSelectedIndex();
            String bday = bDay.getSelectedItem() + "/" + String.format("%02d", bMon.getSelectedIndex()) + "/" + bYear.getSelectedItem();

            if (jcbStaff.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please select an ID", "Error", JOptionPane.ERROR_MESSAGE);
            }

            //Check the Validation
            else if (validation(year, month, day, bday)) {
                //Get the variable
                String name = jtfName.getText();
                String position = jcbPosition.getSelectedItem().toString();
                String pass = jtfPassword.getText();
                String ic = jtfIC.getText();
                String phone = jtfPhone.getText();
                String email = jtfEmail.getText();
                String address = jtfAddress.getText();
                String gender = jcbGender.getSelectedItem().toString();
                String ans = staff.getStaffAnswer();
                int quest = staff.getStaffQuestion();

                //Update staff details
                Staff updateStaff = new Staff(staff.getStaffId(), name, position, pass, gender, bday, ic, phone, address, email, "Active", quest, ans);
                progControl.updateRecord(updateStaff);
                JOptionPane.showMessageDialog(null, "Staff ID : " + jcbStaff.getSelectedItem() + "\nUpdate record successful", "Update is successful", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class ResignListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (jcbStaff.getSelectedIndex() != 0) {
                String id = staff.getStaffId();
                String name = staff.getStaffName();
                String ic = staff.getStaffIc();
                String phone = staff.getStaffPhone();
                String email = staff.getStaffEmail();
                String address = staff.getStaffAddress();
                String securityAns = staff.getStaffAnswer();
                String gender = staff.getStaffGender();
                String position = staff.getStaffPosition();
                String password = staff.getStaffPassword();
                String birthday = staff.getStaffBirthday();
                int ques = staff.getStaffQuestion();

                staff = new Staff(id, name, position, password, gender, birthday, ic, phone, address, email, "De-active", ques, securityAns);
                int option = JOptionPane.showConfirmDialog(null, "Confirm staff ID of " + staff.getStaffId() + " is resign", "Confirmation", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    progControl.updateRecord(staff);
                    JOptionPane.showMessageDialog(null, "Staff ID : " + jcbStaff.getSelectedItem() + "\nUpdate record successful", "Update is successful", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            else {
                JOptionPane.showMessageDialog(null, "Please select a staff", "Update is successful", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private boolean validation(int year, int month, int day, String bday) {
        String strName = jtfName.getText();
        String strPhone = jtfPhone.getText();
        String strEmail = jtfEmail.getText();
        String strAddress = jtfAddress.getText();

        if (jcbPosition.getSelectedIndex() == 0) {
            jlErrorMessagePosition.setText("Please select the position");
            jlErrorMessagePosition.setForeground(Color.red);
            return false;
        }

        //Check the name is empty 
        if (strName.isEmpty() || !va.isValidName(strName)) {
            jtfName.setBorder(border);
            jtfName.setText(null);
            jtfName.requestFocus();
            jlErrorMessageName.setText("Name is empty or not valid");
            jlErrorMessageName.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check gender is selected
        else if (jcbGender.getSelectedIndex() == 0) {
            jlErrorMessageGender.setText("Please select the gender");
            jlErrorMessageGender.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check Birthday is selected
        else if (day == 0 || month == 0 || year == 0) {
            jlErrorMessageBday.setText("Please select the birthday");
            jlErrorMessageBday.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check is valid date
        else if (!va.isValidDate(bday)) {
            jlErrorMessageBday.setText("Wrong format of date");
            jlErrorMessageBday.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check IC is empty & valid
        else if (!va.isValidIC(year, month, day, jtfIC.getText())) {
            String str = String.format("%02d", (year + 1969) % 100) + String.format("%02d", month) + bDay.getSelectedItem() + "-";
            jtfIC.setBorder(border);
            jtfIC.setText(str);
            jtfIC.requestFocus();
            jlErrorMessageIC.setText("E.g :" + str + "01-2345");
            jlErrorMessageIC.setForeground(new Color(255, 114, 114));
            return false;
        }
        //Check phone is empty & is phone number is valid
        else if (strPhone.isEmpty() || !va.isValidPhoneNumber(strPhone)) {
            jtfPhone.setBorder(border);
            jtfPhone.setText(null);
            jtfPhone.requestFocus();
            jlErrorMessagePhone.setText("E.g : 012-3456789");
            jlErrorMessagePhone.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check address is empty
        else if (strAddress.isEmpty()) {
            jtfAddress.setBorder(border);
            jtfAddress.setText(null);
            jtfAddress.requestFocus();
            jlErrorMessageAddress.setText("Address can not be empty");
            jlErrorMessageAddress.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check e-mail is empty
        else if (strEmail.isEmpty() || !va.isValidEmailAddress(strEmail)) {
            jtfEmail.setBorder(border);
            jtfEmail.setText(null);
            jtfEmail.requestFocus();
            jlErrorMessageEmail.setText("E.g : username@hostname.com");
            jlErrorMessageEmail.setForeground(new Color(255, 114, 114));
            return false;
        }
        return true;
    }

}
