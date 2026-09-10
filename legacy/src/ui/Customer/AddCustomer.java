package ui.Customer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import domain.Customer;
import control.MaintainCustomer;
import javax.swing.border.TitledBorder;
import org.joda.time.LocalDate;
import ui.Validation;

/**
 *
 * @author Jeffrey Lai
 * @Date   : 09/06/2015
 * 
 */
public class AddCustomer extends JPanel
{
    //General
    private final MaintainCustomer progControl = new MaintainCustomer();
    private final Validation va = new Validation();
   
    private final String[] genderArray = {"=================Select=================",
        "Male", "Female"};
    private final String[] monthArray = {"Month", "January", "February",
        "March", "April", "May", "June", "July", "August", "September",
        "October", "November", "December"};
    //Label
    private final JLabel jlID = new JLabel("Customer ID");
    private final JLabel jlName = new JLabel("Name");
    private final JLabel jlGender = new JLabel("Gender");
    private final JLabel jlPhone = new JLabel("Phone");
    private final JLabel jlBday = new JLabel("Birthday");
    private final JLabel jlAddress = new JLabel("Home Address");
    private final JLabel jlEmail = new JLabel("E-mail Address");
    private final JLabel jlIC = new JLabel("IC Number");
    private final JLabel jlErrorMessageName = new JLabel();
    private final JLabel jlErrorMessageGender = new JLabel();
    private final JLabel jlErrorMessagePhone = new JLabel();
    private final JLabel jlErrorMessageAddress = new JLabel();
    private final JLabel jlErrorMessageEmail = new JLabel();
    private final JLabel jlErrorMessageBday = new JLabel();
    private final JLabel jlErrorMessageIC = new JLabel();
    //Text Field
    private final JTextField jtfID = new JTextField();
    private final JTextField jtfName = new JTextField();
    private final JTextField jtfPhone = new JTextField();
    private final JTextField jtfAddress = new JTextField();
    private final JTextField jtfEmail = new JTextField();
    private final JTextField jtfIC = new JTextField();
    //Button & ComboBox
    private final JButton jbtSignup = new JButton("    Sign Up    ");
    private final JButton jbtBack = new JButton("    Back    ");
    private final JComboBox jcbGender = new JComboBox(genderArray);
    private final JComboBox bDay = new JComboBox();
    private final JComboBox bMon = new JComboBox(monthArray);
    private final JComboBox bYear = new JComboBox();
    //Date Time
    private final LocalDate localDate = new LocalDate();
    private final int currentYear = localDate.getYear();
    
     //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED,2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240,240,225),3);
    
    //Border Title
    TitledBorder title = new TitledBorder(borderMain," Create Customer ",TitledBorder.LEFT,
    TitledBorder.DEFAULT_POSITION, new Font ( "Arial", Font.BOLD, 20 ), new Color(255,255,153));
    
    //JLabel Style
    Font FontStyle = new Font("SansSerif",Font.BOLD, 14); 
    
    public AddCustomer()
    {
        //Set the new ID
        setMemberID();
        
        //Set drop-down list of Birthday
        bDay.addItem("Day");        //Set days
        for(int i = 1; i <= 31; i++)      
            bDay.addItem(String.format("%02d", i));
        
        bYear.addItem("Year");      //Set Year until the current year
        for(int i = 1970; i <= currentYear; i++)
            bYear.addItem(i);
        
        //Button color
        jbtSignup.setForeground(Color.WHITE);       
        jbtSignup.setBackground(new Color(74,139,245));      
        
        //Birthday
        JPanel pDate = new JPanel(new GridLayout(1,3,10,0));
        pDate.add(bDay);
        pDate.add(bMon);
        pDate.add(bYear);
        pDate.setOpaque(false);
        bDay.setBackground(Color.WHITE);
        bMon.setBackground(Color.WHITE);
        bYear.setBackground(Color.WHITE);
        
        //ID Panel
        JPanel id = new JPanel(new GridLayout(2,2));
        id.add(jlID);
        id.add(jtfID);
        id.add(new JLabel());
        id.add(new JLabel());
        id.setOpaque(false);
        jtfID.setBackground(Color.WHITE);
        jtfID.setEditable(false);        
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);
        
        //Name Panel
        JPanel name = new JPanel(new GridLayout(2,2));
        name.add(jlName);
        name.add(jtfName);
        name.add(new JLabel());
        name.add(jlErrorMessageName);
        name.setOpaque(false);
        jlName.setForeground(Color.WHITE);
        jlName.setFont(FontStyle);
        jtfName.setBackground(Color.WHITE);
        
        //Gender Panel
        JPanel gender = new JPanel(new GridLayout(2,2));
        gender.add(jlGender);
        gender.add(jcbGender);
        gender.add(new JLabel());
        gender.add(jlErrorMessageGender);
        gender.setOpaque(false);
        jlGender.setForeground(Color.WHITE);
        jlGender.setFont(FontStyle);
        jcbGender.setBackground(Color.WHITE);
        
        //Birthday Panel
        JPanel birthday = new JPanel(new GridLayout(2,2));
        birthday.add(jlBday);
        birthday.add(pDate);
        birthday.add(new JLabel());
        birthday.add(jlErrorMessageBday);
        birthday.setOpaque(false);
        jlBday.setForeground(Color.WHITE);
        jlBday.setFont(FontStyle);        
        
        //IC Panel
        JPanel IC = new JPanel(new GridLayout(2,2));
        IC.add(jlIC);
        IC.add(jtfIC);
        IC.add(new JLabel());
        IC.add(jlErrorMessageIC);
        IC.setOpaque(false);
        jlIC.setForeground(Color.WHITE);
        jlIC.setFont(FontStyle);
        jtfIC.setBackground(Color.WHITE);
        
        //Phone Panel
        JPanel phone = new JPanel(new GridLayout(2,2));
        phone.add(jlPhone);
        phone.add(jtfPhone);
        phone.add(new JLabel());
        phone.add(jlErrorMessagePhone);
        phone.setOpaque(false);
        jlPhone.setForeground(Color.WHITE);
        jlPhone.setFont(FontStyle);
        jtfPhone.setBackground(Color.WHITE);
        
        //Address Panel
        JPanel address = new JPanel(new GridLayout(2,2));
        address.add(jlAddress);
        address.add(jtfAddress);
        address.add(new JLabel());
        address.add(jlErrorMessageAddress);
        address.setOpaque(false);
        jlAddress.setForeground(Color.WHITE);
        jlAddress.setFont(FontStyle);
        jtfAddress.setBackground(Color.WHITE);
        
        //E-mail Panel
        JPanel email = new JPanel(new GridLayout(2,2) );
        email.add(jlEmail);
        email.add(jtfEmail);
        email.add(new JLabel());
        email.add(jlErrorMessageEmail);
        email.setOpaque(false);
        jlEmail.setForeground(Color.WHITE);
        jlEmail.setFont(FontStyle);
        jtfEmail.setBackground(Color.WHITE);
        
        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));    
        buttons.add(jbtSignup);
        buttons.setOpaque(false);
        jbtSignup.addActionListener(new AddListener());

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(9,1));     
        
        main.add(id);
        main.add(name);
        main.add(gender);
        main.add(birthday);
        main.add(IC);
        main.add(phone);
        main.add(address);
        main.add(email);
        main.add(buttons);
        main.setOpaque(false);
        add(main); 
        
         main.setBorder ( BorderFactory.createCompoundBorder ( title,BorderFactory.createEmptyBorder ( 20, 50, 5, 50)));
    }
    
    private void setMemberID()
    {      
        Customer cus = progControl.lastCustomerID();
        
        if (cus != null)
        {
            int id = Integer.parseInt(cus.getCusId().substring(3)) + 1;
            String newID = "CUS" + String.format("%07d", id);
            jtfID.setText(newID);
        }
        else
            jtfID.setText("CUS0000001");
    }
    
    private class AddListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            //Clear the border color to origin
            Border original = jtfID.getBorder();
            jtfName.setBorder(original);
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
            
            //Get the variable
            String bday = bDay.getSelectedItem() + "/" + String.format("%02d",bMon.getSelectedIndex()) + "/" + bYear.getSelectedItem();
            
            //Check the Validation
            if (validation(bday))
            {
                //Get the values
                String id = jtfID.getText();
                String name = jtfName.getText();
                String ic = jtfIC.getText();
                String phone = jtfPhone.getText();
                String email = jtfEmail.getText();
                String address = jtfAddress.getText();
                String gender = jcbGender.getSelectedItem().toString();
                
                //Add customer details
                Customer cus = new Customer(id, name, gender, bday, ic, phone,address, email);
                progControl.addRecord(cus);
                JOptionPane.showMessageDialog(null, "Customer ID : " + jtfID.getText() + "\nInsert new record successful", "New Customer",JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private boolean validation(String bday)
    {
        int year = bYear.getSelectedIndex();
        int month = bMon.getSelectedIndex();
        int day = bDay.getSelectedIndex();
        String strName = jtfName.getText();
        String strPhone = jtfPhone.getText();
        String strEmail = jtfEmail.getText();
        String strAddress = jtfAddress.getText();
        
        //Check the name is empty 
        if(strName.isEmpty() || !va.isValidName(strName))
        {
            jtfName.setBorder(border);
            jtfName.setText(null);
            jtfName.requestFocus();
            jlErrorMessageName.setText("Name is empty or not valid");
            jlErrorMessageName.setForeground(new Color(255,114,114));
            return false;
        }
        
        //Check gender is selected
        else if(jcbGender.getSelectedIndex() == 0)
        {
            jlErrorMessageGender.setText("Please select the gender");
            jlErrorMessageGender.setForeground(new Color(255,114,114));
            return false;
        }
        
        //Check Birthday is selected
        else if(day == 0 || month == 0 || year == 0)
        {
            jlErrorMessageBday.setText("Please select the birthday");
            jlErrorMessageBday.setForeground(new Color(255,114,114));
            return false;
        }
        
        //Check is valid date
        else if(!va.isValidDate(bday))
        {
            jlErrorMessageBday.setText("Wrong format of date");
            jlErrorMessageBday.setForeground(new Color(255,114,114));
            return false;
        }
        
        //Check IC is empty & valid
        else if(!va.isValidIC(year, month, day, jtfIC.getText()))
        {
            String str = String.format("%02d", (year + 1969) % 100) + String.format("%02d", month) + bDay.getSelectedItem() + "-";
            jtfIC.setBorder(border);
            jtfIC.setText(str);
            jtfIC.requestFocus();
            jlErrorMessageIC.setText("E.g : " + str + "01-2345");
            jlErrorMessageIC.setForeground(new Color(255,114,114));  
            return false;
        }
        //Check phone is empty & is phone number is valid
        else if(strPhone.isEmpty() || !va.isValidPhoneNumber(strPhone))
        {
            jtfPhone.setBorder(border);
            jtfPhone.setText(null);
            jtfPhone.requestFocus();
            jlErrorMessagePhone.setText("E.g: 012-3456789");
            jlErrorMessagePhone.setForeground(new Color(255,114,114));   
            return false;
        }
        
        //Check address is empty
        else if(strAddress.isEmpty())
        {
            jtfAddress.setBorder(border);
            jtfAddress.setText(null);
            jtfAddress.requestFocus();
            jlErrorMessageAddress.setText("Address can not be empty");
            jlErrorMessageAddress.setForeground(new Color(255,114,114));
            return false;
        }
        
        //Check e-mail is empty
        else if(strEmail.isEmpty() || !va.isValidEmailAddress(strEmail))
        {
            jtfEmail.setBorder(border);
            jtfEmail.setText(null);
            jtfEmail.requestFocus();
            jlErrorMessageEmail.setText("E.g : username@hostname.com");
            jlErrorMessageEmail.setForeground(new Color(255,114,114));
            return false;
        }
        return true;
    }
    
  
}
