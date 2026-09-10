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
import javax.swing.border.TitledBorder;
import org.joda.time.LocalDate;
import ui.Validation;

/**
 *
 * @author Jeffrey
 */
public class AddStaff extends JPanel//*/JFrame
{

    //General
    private final MaintainStaff progControl = new MaintainStaff();

    private final String[] positionArray = {"Staff", "Administrator"};
    private final String[] genderArray = {"=================Select=================",
        "Male", "Female"};
    private final String[] monthArray = {"Month", "January", "February",
        "March", "April", "May", "June", "July", "August", "September",
        "October", "November", "December"};
    private final String[] questionArray = {"=================Select=================",
        "What is your first phone number?",
        "What is the last name of your first lover?",
        "What is your fovarites food?",
        "What is the middle name of your best friend?"};
    private static final Validation va = new Validation();
    //Label
    private final JLabel jlPosition = new JLabel("Position");
    private final JLabel jlID = new JLabel("Staff ID");
    private final JLabel jlName = new JLabel("Name");
    private final JLabel jlGender = new JLabel("Gender");
    private final JLabel jlPhone = new JLabel("Phone");
    private final JLabel jlBday = new JLabel("Birthday");
    private final JLabel jlAddress = new JLabel("Home Address");
    private final JLabel jlEmail = new JLabel("E-mail Address");
    private final JLabel jlIC = new JLabel("IC Number");
    private final JLabel jlSecurityQuestion = new JLabel("Security Question");
    private final JLabel jlSecurityAnswer = new JLabel("Answer");
    private final JLabel jlErrorMessageName = new JLabel();
    private final JLabel jlErrorMessageGender = new JLabel();
    private final JLabel jlErrorMessagePhone = new JLabel();
    private final JLabel jlErrorMessageAddress = new JLabel();
    private final JLabel jlErrorMessageEmail = new JLabel();
    private final JLabel jlErrorMessageBday = new JLabel();
    private final JLabel jlErrorMessageIC = new JLabel();
    private final JLabel jlErrorMessageSecurityQues = new JLabel();
    private final JLabel jlErrorMessageSecurityAns = new JLabel();
    //Text Field
    private final JTextField jtfID = new JTextField();
    private final JTextField jtfName = new JTextField();
    private final JTextField jtfPhone = new JTextField();
    private final JTextField jtfAddress = new JTextField();
    private final JTextField jtfEmail = new JTextField();
    private final JTextField jtfIC = new JTextField();
    private final JTextField jtfSecurityAns = new JTextField();
    //Buttons & ComboBox
    private final JButton jbtConfirm = new JButton("    Confirm    ");
    private final JComboBox jcbPosition = new JComboBox(positionArray);
    private final JComboBox jcbGender = new JComboBox(genderArray);
    private final JComboBox bDay = new JComboBox();
    private final JComboBox bMon = new JComboBox(monthArray);
    private final JComboBox bYear = new JComboBox();
    private final JComboBox jcbSecurity = new JComboBox(questionArray);
    //Date Time
    private final LocalDate localDate = new LocalDate();
    private final int currentYear = localDate.getYear();

    //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED, 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Create Staff ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 15);

    public AddStaff() {
        //Set ID
        setStaffID();

        //Set drop-down list of Birthday
        bDay.addItem("Day");        //Set days
        for (int i = 1; i <= 31; i++) {
            bDay.addItem(String.format("%02d", i));
        }

        bYear.addItem("Year");      //Set Year until the current year
        for (int i = 1970; i <= currentYear; i++) {
            bYear.addItem(i);
        }

        /* ============ leftmainField left hand side for JLabel ============ */
        JPanel leftmainLabel = new JPanel(new GridLayout(12, 1, 0, 0));
        leftmainLabel.setOpaque(false);

        //staff position label
        leftmainLabel.add(jlPosition);
        leftmainLabel.add(new JLabel(""));
        jlPosition.setForeground(Color.WHITE);
        jlPosition.setFont(FontStyle);

        //staff id label
        leftmainLabel.add(jlID);
        leftmainLabel.add(new JLabel(""));
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);

        //staff name label
        leftmainLabel.add(jlName);
        leftmainLabel.add(new JLabel(""));
        jlName.setForeground(Color.WHITE);
        jlName.setFont(FontStyle);

        //staff gender label
        leftmainLabel.add(jlGender);
        leftmainLabel.add(new JLabel(""));
        jlGender.setForeground(Color.WHITE);
        jlGender.setFont(FontStyle);

        //Date panel
        JPanel pDate = new JPanel(new GridLayout(1, 3, 10, 0));
        pDate.add(bDay);
        pDate.add(bMon);
        pDate.add(bYear);
        pDate.setOpaque(false);
        bDay.setBackground(Color.WHITE);
        bMon.setBackground(Color.WHITE);
        bYear.setBackground(Color.WHITE);

        //staff birthday label
        leftmainLabel.add(jlBday);
        leftmainLabel.add(new JLabel(""));
        jlBday.setForeground(Color.WHITE);
        jlBday.setFont(FontStyle);

        //staff IC label
        leftmainLabel.add(jlIC);
        leftmainLabel.add(new JLabel(""));
        jlIC.setForeground(Color.WHITE);
        jlIC.setFont(FontStyle);

        /* ============ rightmainLabel left hand side for JLabel ============ */
        JPanel rightmainLabel = new JPanel(new GridLayout(12, 1, 0, 0));
        rightmainLabel.setOpaque(false);

        //staff phone number label
        rightmainLabel.add(jlPhone);
        rightmainLabel.add(new JLabel(""));
        jlPhone.setForeground(Color.WHITE);
        jlPhone.setFont(FontStyle);

        //staff address label
        rightmainLabel.add(jlAddress);
        rightmainLabel.add(new JLabel(""));
        jlAddress.setForeground(Color.WHITE);
        jlAddress.setFont(FontStyle);

        //staff email label
        rightmainLabel.add(jlEmail);
        rightmainLabel.add(new JLabel(""));
        jlEmail.setForeground(Color.WHITE);
        jlEmail.setFont(FontStyle);

        //security question label
        rightmainLabel.add(jlSecurityQuestion);
        rightmainLabel.add(new JLabel(""));
        jlSecurityQuestion.setForeground(Color.WHITE);
        jlSecurityQuestion.setFont(FontStyle);

        //security answer label
        rightmainLabel.add(jlSecurityAnswer);
        rightmainLabel.add(new JLabel(""));
        jlSecurityAnswer.setForeground(Color.WHITE);
        jlSecurityAnswer.setFont(FontStyle);

        /* ============ leftmainField right hand side for text field or combo box ============ */
        JPanel leftmainField = new JPanel(new GridLayout(12, 1, 0, 0));
        leftmainField.setOpaque(false);

        //staff position combo box
        leftmainField.add(jcbPosition);
        leftmainField.add(new JLabel(""));
        jcbPosition.setBackground(Color.WHITE);

        //staff ID field
        leftmainField.add(jtfID);
        leftmainField.add(new JLabel(""));
        jtfID.setBackground(Color.WHITE);
        jtfID.setEditable(false);

        //staff name field
        leftmainField.add(jtfName);
        leftmainField.add(jlErrorMessageName);
        jtfName.setBackground(Color.WHITE);

        //staff gender combo box
        leftmainField.add(jcbGender);
        leftmainField.add(jlErrorMessageGender);
        jcbGender.setBackground(Color.WHITE);

        //staff date combo box      
        leftmainField.add(pDate);
        leftmainField.add(jlErrorMessageBday);

        //staff IC field
        leftmainField.add(jtfIC);
        leftmainField.add(jlErrorMessageIC);
        jtfIC.setBackground(Color.WHITE);

        //Button color
        jbtConfirm.setForeground(Color.WHITE);
        jbtConfirm.setBackground(new Color(74, 139, 245));

        /* ============ rightmainField right hand side for text field or combo box ============ */
        JPanel rightmainField = new JPanel(new GridLayout(12, 1, 0, 0));
        rightmainField.setOpaque(false);

        //staff phone number field
        rightmainField.add(jtfPhone);
        rightmainField.add(jlErrorMessagePhone);
        jtfPhone.setBackground(Color.WHITE);

        //staff Address field
        rightmainField.add(jtfAddress);
        rightmainField.add(jlErrorMessageAddress);
        jtfAddress.setBackground(Color.WHITE);

        //staff Address field
        rightmainField.add(jtfEmail);
        rightmainField.add(jlErrorMessageEmail);
        jtfEmail.setBackground(Color.WHITE);

        //Security question combo box
        rightmainField.add(jcbSecurity);
        rightmainField.add(jlErrorMessageSecurityQues);
        jcbSecurity.setBackground(Color.WHITE);

        //Security answer combo box
        rightmainField.add(jtfSecurityAns);
        rightmainField.add(jlErrorMessageSecurityAns);
        jtfSecurityAns.setBackground(Color.WHITE);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(new JLabel(""));
        buttons.add(jbtConfirm);
        buttons.setOpaque(false);
        jbtConfirm.addActionListener(new AddListener());

        //Set general Layout
        setLayout(new FlowLayout());

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

        /*   setUndecorated(false) ;
     setExtendedState(JFrame.MAXIMIZED_BOTH);
       setLocationRelativeTo(null);
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       setTitle("CineFlow");
       setVisible(true);    
    }
    
    public static void main(String[] args) 
    {
        AddStaff login = new AddStaff();   */
    }

    private void setStaffID() {
        Staff staff = progControl.lastStaffID();

        if (staff != null) {
            int id = Integer.parseInt(staff.getStaffId().substring(3)) + 1;
            String newID = "STF" + String.format("%07d", id);
            jtfID.setText(newID);
        }
        else {
            jtfID.setText("STF0000001");
        }
    }

    private class AddListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            //Clear the border color while have error happen is previous
            Border original = jtfID.getBorder();
            jtfName.setBorder(original);
            jtfPhone.setBorder(original);
            jtfEmail.setBorder(original);
            jtfAddress.setBorder(original);
            jtfIC.setBorder(original);
            jtfSecurityAns.setBorder(original);
            jlErrorMessageName.setText(null);
            jlErrorMessageAddress.setText(null);
            jlErrorMessageBday.setText(null);
            jlErrorMessageEmail.setText(null);
            jlErrorMessageIC.setText(null);
            jlErrorMessageGender.setText(null);
            jlErrorMessagePhone.setText(null);
            jlErrorMessageSecurityQues.setText(null);
            jlErrorMessageSecurityAns.setText(null);

            //Get the variable
            int year = bYear.getSelectedIndex();
            int month = bMon.getSelectedIndex();
            int day = bDay.getSelectedIndex();
            int securityQuest = jcbSecurity.getSelectedIndex();
            String bday = bDay.getSelectedItem() + "/" + String.format("%02d", bMon.getSelectedIndex()) + "/" + bYear.getSelectedItem();

            if (validation(year, month, day, bday)) {
                //Get the value
                String id = jtfID.getText();
                String name = jtfName.getText();
                String ic = jtfIC.getText();
                String phone = jtfPhone.getText();
                String email = jtfEmail.getText();
                String address = jtfAddress.getText();
                String securityAns = jtfSecurityAns.getText();
                String gender = jcbGender.getSelectedItem().toString();
                String position = jcbPosition.getSelectedItem().toString();

                Staff staff = new Staff(id, name, position, id, gender, bday, ic, phone, address, email, "Active", securityQuest, securityAns);

                //Add record to database
                progControl.addRecord(staff);

                //Pop up insert data successful message 
                JOptionPane.showMessageDialog(null, "Staff ID :" + jtfID.getText() + "\nInsert new record successful", "New Staff", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private boolean validation(int year, int month, int day, String bday) {
        String strName = jtfName.getText();
        String strPhone = jtfPhone.getText();
        String strEmail = jtfEmail.getText();
        String strAddress = jtfAddress.getText();

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
            jlErrorMessageIC.setText("E.g : " + str + "01-2345");
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

        //Check the security question
        else if (jcbSecurity.getSelectedIndex() == 0) {
            jlErrorMessageSecurityQues.setText("Please select the security question");
            jlErrorMessageSecurityQues.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check the security answer
        else if (jtfSecurityAns.getText().isEmpty()) {
            jlErrorMessageSecurityAns.setText("Please answer the security question");
            jlErrorMessageSecurityAns.setForeground(new Color(255, 114, 114));
            jtfSecurityAns.setBorder(border);
            jtfSecurityAns.requestFocus();
            return false;
        }
        return true;
    }

}
