package ui.Customer;

import domain.Customer;
import control.MaintainCustomer;
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
public class RetrieveCustomer extends JPanel
{
    //General
    private final MaintainCustomer progControl = new MaintainCustomer();
    private  Customer cus = new Customer();
    private  ArrayList<Customer> customersArrayList = new ArrayList<>();
    //Label
    private final JLabel jlID = new JLabel("Customer ID");
    private final JLabel jlName = new JLabel("Name");
    private final JLabel jlGender = new JLabel("Gender");
    private final JLabel jlPhone = new JLabel("Phone");
    private final JLabel jlBday = new JLabel("Birthday");
    private final JLabel jlAddress = new JLabel("Home Address");
    private final JLabel jlEmail = new JLabel("E-mail Address");
    private final JLabel jlIC = new JLabel("IC Number");
    //Text Field
    private final JTextField jtfName = new JTextField();
    private final JTextField jtfGender = new JTextField();
    private final JTextField jtfBday = new JTextField();
    private final JTextField jtfIC = new JTextField();
    private final JTextField jtfPhone = new JTextField();
    private final JTextField jtfAddress = new JTextField();
    private final JTextField jtfEmail = new JTextField();
    //Button & ComboBox
    private final JComboBox jcbCustomer = new JComboBox();   
    
    //Border style   
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240,240,225),3);
    
    //Border Title
    TitledBorder title = new TitledBorder(borderMain," Retrieve Customer ",TitledBorder.LEFT,
    TitledBorder.DEFAULT_POSITION, new Font ( "Arial", Font.BOLD, 20 ), new Color(255,255,153));
    
    //JLabel Style
    Font FontStyle = new Font("SansSerif",Font.BOLD, 14); 
    
    public RetrieveCustomer()
    {
        //Set the ID
        memberLists();        
        
        //ID Panel
        JPanel id = new JPanel(new GridLayout(1,2));
        id.add(jlID);
        id.add(jcbCustomer);
        id.setOpaque(false);
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);
        jcbCustomer.setBackground(Color.WHITE);
        jcbCustomer.addActionListener(new RetrieveListener());

        //Name Panel
        JPanel name = new JPanel(new GridLayout(1,2));
        name.add(jlName);
        name.add(jtfName);
        name.setOpaque(false);
        jlName.setForeground(Color.WHITE);
        jlName.setFont(FontStyle);
        jtfName.setBackground(Color.WHITE);
        jtfName.setEditable(false);
        
        //Gender Panel
        JPanel gender = new JPanel(new GridLayout(1,2));
        gender.add(jlGender);
        gender.add(jtfGender);
        gender.setOpaque(false);
        jlGender.setForeground(Color.WHITE);
        jlGender.setFont(FontStyle);
        jtfGender.setBackground(Color.WHITE);
        jtfGender.setEditable(false);
        
        //Birthday Panel
        JPanel birthday = new JPanel(new GridLayout(1,2));
        birthday.add(jlBday);
        birthday.add(jtfBday);
        birthday.setOpaque(false);
        jtfBday.setEditable(false);
        jlBday.setForeground(Color.WHITE);
        jlBday.setFont(FontStyle);
        
        //IC Panel
        JPanel IC = new JPanel(new GridLayout(1,2));
        IC.add(jlIC);
        IC.add(jtfIC);
        IC.setOpaque(false);
        jlIC.setForeground(Color.WHITE);
        jlIC.setFont(FontStyle);
        jtfIC.setBackground(Color.WHITE);
        jtfIC.setEditable(false);
        
        //Phone Panel
        JPanel phone = new JPanel(new GridLayout(1,2));
        phone.add(jlPhone);
        phone.add(jtfPhone);
        phone.setOpaque(false);
        jlPhone.setForeground(Color.WHITE);
        jlPhone.setFont(FontStyle);
        jtfPhone.setBackground(Color.WHITE);
        jtfPhone.setEditable(false);
        
        //Address Panel
        JPanel address = new JPanel(new GridLayout(1,2));
        address.add(jlAddress);
        address.add(jtfAddress);
        address.setOpaque(false);
        jlAddress.setForeground(Color.WHITE);
        jlAddress.setFont(FontStyle);
        jtfAddress.setBackground(Color.WHITE);
        jtfAddress.setEditable(false);
        
        //E-mail Panel
        JPanel email = new JPanel(new GridLayout(1,2));
        email.add(jlEmail);
        email.add(jtfEmail);
        email.setOpaque(false);
        jlEmail.setForeground(Color.WHITE);
        jlEmail.setFont(FontStyle);
        jtfEmail.setBackground(Color.WHITE);
        jtfEmail.setEditable(false);             
     
        
        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(9,1,0,15));
        main.add(id);
        main.add(name);
        main.add(gender);
        main.add(birthday);
        main.add(IC);
        main.add(phone);
        main.add(address);
        main.add(email);
     
        add(main);
        main.setOpaque(false);
        main.setBorder ( BorderFactory.createCompoundBorder ( title,BorderFactory.createEmptyBorder ( 20, 50, 5, 50 ) ) );
    }
    
    private void memberLists()
    {
        jcbCustomer.addItem("=================Select=================");
        customersArrayList = progControl.getAll();
        for (Customer customer : customersArrayList) 
        {
            if (!customer.getCusId().equals("NULL"))
                jcbCustomer.addItem(customer.getCusId());
        }
    }
    
    private class RetrieveListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            if (jcbCustomer.getSelectedIndex() != 0)
            {
                //Get the record form database
                cus = progControl.selectRecord(jcbCustomer.getSelectedItem().toString());

                //Show the record of member
                jtfName.setText(cus.getCusName());
                jtfGender.setText(cus.getCusGender());
                jtfBday.setText(cus.getCusBirthday());
                jtfIC.setText(cus.getCusIc());
                jtfPhone.setText(cus.getCusPhone());
                jtfAddress.setText(cus.getCusAddress());
                jtfEmail.setText(cus.getCusEmail());
            }
            else{
                //Clear the text field
                jtfName.setText(null);
                jtfGender.setText(null);
                jtfBday.setText(null);
                jtfIC.setText(null);
                jtfPhone.setText(null);
                jtfAddress.setText(null);
                jtfEmail.setText(null);
            }
        }
    }
    
   
}
