package ui.Customer;

import control.MaintainCustomer;
import domain.Customer;
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
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.joda.time.LocalDate;
import ui.Validation;

/**
 *
 * @author Jeffrey
 */
public class UpdateCustomer extends JPanel
{
    //General
    private static final MaintainCustomer progControl = new MaintainCustomer();
    private  ArrayList<Customer> customersArrayList = new ArrayList<>();
    private  Customer cus = new Customer();
    private  static final Validation va = new Validation();
    
    private static final String[] genderArray = {"=================Select=================",
        "Male", "Female"};
    private static final String[] monthArray = {"Month", "January", "February",
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
    private final JTextField jtfName = new JTextField();
    private final JTextField jtfPhone = new JTextField();
    private final JTextField jtfAddress = new JTextField();
    private final JTextField jtfEmail = new JTextField();
    private final JTextField jtfIC = new JTextField();
    //Button & ComboBox
    private final JButton jbtUpdate = new JButton("    Save Change    ");
    private final JComboBox customerID = new JComboBox();
    private final JComboBox jcbGender = new JComboBox(genderArray);
    private final JComboBox bDay = new JComboBox();
    private final JComboBox bMon = new JComboBox(monthArray);
    private final JComboBox bYear = new JComboBox();
    //Date Time
    private final LocalDate localDate = new LocalDate();
    private final int currentYear = localDate.getYear();
    
    private final Border original = jtfEmail.getBorder();
     //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED,2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240,240,225),3);
    
    //Border Title
    TitledBorder title = new TitledBorder(borderMain," Update Customer ",TitledBorder.LEFT,
    TitledBorder.DEFAULT_POSITION, new Font ( "Arial", Font.BOLD, 20 ), new Color(255,255,153));
    
    //JLabel Style
    Font FontStyle = new Font("SansSerif",Font.BOLD, 14); 
    
    
    public UpdateCustomer()
    {
        //List the Member ID
        CustomerList();
        
        //Set drop-down list of Birthday
        bDay.addItem("Day");        //Set days
        for(int i = 1; i <= 31; i++)      
            bDay.addItem(String.format("%02d", i));
        
        bYear.addItem("Year");      //Set Year until the current year
        for(int i = 1970; i <= currentYear; i++)
            bYear.addItem(i);
        
        //Button color
        jbtUpdate.setForeground(Color.WHITE);
        jbtUpdate.setBackground(new Color(74,139,245));
        
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
        id.add(customerID);
        id.add(new JLabel());
        id.add(new JLabel());
        id.setOpaque(false);      
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);
        customerID.setBackground(Color.WHITE);
        customerID.addActionListener(new CheckListener());
        
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
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT,1,0));
        buttons.add(jbtUpdate);
        buttons.setOpaque(false);
        jbtUpdate.addActionListener(new UpdateListener());
        
        
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
      
        main.setBorder (BorderFactory.createCompoundBorder ( title,BorderFactory.createEmptyBorder ( 20, 50, 5, 50 )  ) );
    }

    private void CustomerList()
    {
        customerID.addItem("=================Select=================");
        customersArrayList = progControl.getAll();
        for (Customer customer : customersArrayList) 
        {
            if (!customer.getCusId().equals("NULL"))
                customerID.addItem(customer.getCusId());
        }
    }
    
    private void clearErrorMessages()
    {
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
    }
    
    private class CheckListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            if (customerID.getSelectedIndex() == 0)
            {
                clearErrorMessages();
                jtfName.setText(null);
                jtfIC.setText(null);
                jtfPhone.setText(null);
                jtfAddress.setText(null);
                jtfEmail.setText(null);
                bDay.setSelectedIndex(0);
                bMon.setSelectedIndex(0);
                bYear.setSelectedIndex(0);
                jcbGender.setSelectedIndex(0);
            }
            else
            {
                //Clear the border color to origin
                clearErrorMessages();
                
                cus = progControl.selectRecord(customerID.getSelectedItem().toString());
                
                //Show the record of member
                jtfName.setText(cus.getCusName());
                jtfIC.setText(cus.getCusIc());
                jtfPhone.setText(cus.getCusPhone());
                jtfAddress.setText(cus.getCusAddress());
                jtfEmail.setText(cus.getCusEmail());
                
                //Get the member gender
                if (cus.getCusGender().equals("Male"))
                    jcbGender.setSelectedIndex(1);
                else 
                    jcbGender.setSelectedIndex(2);
                
                //Get the member Birthday
                String[] birth = cus.getCusBirthday().split("/");
                bDay.setSelectedIndex(Integer.parseInt(birth[0]));
                bMon.setSelectedIndex(Integer.parseInt(birth[1]));
                bYear.setSelectedIndex(Integer.parseInt(birth[2])-1969);
            }
        }
    }
    
    private class UpdateListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            //Clear the border color
            clearErrorMessages();

            //Get value
            String bday = bDay.getSelectedItem() + "/" + String.format("%02d",bMon.getSelectedIndex()) + "/" + bYear.getSelectedItem();
            
            if (customerID.getSelectedIndex() == 0)
                JOptionPane.showMessageDialog(null, "Please select a ID", "Error", JOptionPane.ERROR_MESSAGE);
            //Check the Validation
            else if (validation(bYear.getSelectedIndex(), bMon.getSelectedIndex(), bDay.getSelectedIndex(), bday))
            {
                //Get the values
                String name = jtfName.getText();
                String ic = jtfIC.getText();
                String phone = jtfPhone.getText();
                String email = jtfEmail.getText();
                String address = jtfAddress.getText();
                String gender = jcbGender.getSelectedItem().toString();
                
            
                Customer customer = new Customer(cus.getCusId(), name, gender, bday, ic, phone,address, email);
                progControl.updateRecord(customer);
                JOptionPane.showMessageDialog(null, "Customer ID : " + customerID.getSelectedItem() + "\nUpdated customer details successfully", "Updated Details",JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private boolean validation(int year, int month, int day, String bday)
    {
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
            jlErrorMessageName.setForeground(Color.red);
            return false;
        }
        
        //Check gender is selected
        else if(jcbGender.getSelectedIndex() == 0)
        {
            jlErrorMessageGender.setText("Please select the gender");
            jlErrorMessageGender.setForeground(Color.red);
            return false;
        }
        
        //Check Birthday is selected
        else if(day == 0 || month == 0 || year == 0)
        {
            jlErrorMessageBday.setText("Please select the birthday");
            jlErrorMessageBday.setForeground(Color.red);
            return false;
        }
        
        //Check is valid date
        else if(!va.isValidDate(bday))
        {
            jlErrorMessageBday.setText("Wrong format of date");
            jlErrorMessageBday.setForeground(Color.red);
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
            jlErrorMessageIC.setForeground(Color.red);  
            return false;
        }
        //Check phone is empty & is phone number is valid
        else if(strPhone.isEmpty() || !va.isValidPhoneNumber(strPhone))
        {
            jtfPhone.setBorder(border);
            jtfPhone.setText(null);
            jtfPhone.requestFocus();
            jlErrorMessagePhone.setText("E.g : 012-3456789");
            jlErrorMessagePhone.setForeground(Color.red);   
            return false;
        }
        
        //Check address is empty
        else if(strAddress.isEmpty())
        {
            jtfAddress.setBorder(border);
            jtfAddress.setText(null);
            jtfAddress.requestFocus();
            jlErrorMessageAddress.setText("Address can not be empty");
            jlErrorMessageAddress.setForeground(Color.red);
            return false;
        }
        
        //Check e-mail is empty
        else if(strEmail.isEmpty() || !va.isValidEmailAddress(strEmail))
        {
            jtfEmail.setBorder(border);
            jtfEmail.setText(null);
            jtfEmail.requestFocus();
            jlErrorMessageEmail.setText("E.g: username@hostname.com");
            jlErrorMessageEmail.setForeground(Color.red);
            return false;
        }
        return true;
    }
  
}
