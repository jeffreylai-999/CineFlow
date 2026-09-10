package ui.Customer;

/**
 *
 * @author Jeffrey
 */
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
import javax.swing.JFrame;
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
public class DeleteCustomer extends JPanel
{
    //General
    private final MaintainCustomer progControl = new MaintainCustomer();
    private  Customer mem = new Customer();
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
    private final JTextField jtfName = new JTextField(25);
    private final JTextField jtfGender = new JTextField();
    private final JTextField jtfBday = new JTextField();
    private final JTextField jtfIC = new JTextField();
    private final JTextField jtfPhone = new JTextField();
    private final JTextField jtfAddress = new JTextField();
    private final JTextField jtfEmail = new JTextField();
    //Button & ComboBox
    private final JComboBox customerID = new JComboBox();
    private final JButton jbtDelete = new JButton("    Remove    ");
    
    private final Border original = jtfEmail.getBorder();
     //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED,2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240,240,225),3);
    
    //Border Title
    TitledBorder title = new TitledBorder(borderMain," Delete Customer ",TitledBorder.LEFT,
    TitledBorder.DEFAULT_POSITION, new Font ( "Arial", Font.BOLD, 20 ), new Color(255,255,153));
    
    //JLabel Style
    Font FontStyle = new Font("SansSerif",Font.BOLD, 14); 
    
    
    public DeleteCustomer()
    {
        //Set the ID
        memberIDLists();
        
        //ID Panel
        JPanel id = new JPanel(new GridLayout(1,2));
        id.add(jlID);
        id.add(customerID);
        id.setOpaque(false);
        customerID.setBackground(Color.WHITE);
        customerID.addActionListener(new RetrieveListener());
        jlID.setForeground(Color.WHITE);
        jlID.setFont(FontStyle);
                
        //Name Panel
        JPanel name = new JPanel(new GridLayout(1,2));
        name.add(jlName);
        name.add(jtfName);
        name.setOpaque(false);
        jtfName.setBackground(Color.WHITE);
        jtfName.setEditable(false);
        jlName.setForeground(Color.WHITE);
        jlName.setFont(FontStyle);
        
        //Gender Panel
        JPanel gender = new JPanel(new GridLayout(1,2));
        gender.add(jlGender);
        gender.add(jtfGender);
        gender.setOpaque(false);
        jtfGender.setBackground(Color.WHITE);
        jtfGender.setEditable(false);
        jlGender.setForeground(Color.WHITE);
        jlGender.setFont(FontStyle);
        
        //Birthday Panel
        JPanel birthday = new JPanel(new GridLayout(1,2));
        birthday.add(jlBday);
        birthday.add(jtfBday);
        birthday.setOpaque(false);
        jtfBday.setEditable(false);
        jtfBday.setBackground(Color.WHITE);
        jlBday.setForeground(Color.WHITE);
        jlBday.setFont(FontStyle);
        
        //IC Panel
        JPanel IC = new JPanel(new GridLayout(1,2));
        IC.add(jlIC);
        IC.add(jtfIC);
        IC.setOpaque(false);
        jtfIC.setBackground(Color.WHITE);
        jtfIC.setEditable(false);
        jlIC.setForeground(Color.WHITE);
        jlIC.setFont(FontStyle);
        
        //Phone Panel
        JPanel phone = new JPanel(new GridLayout(1,2));
        phone.add(jlPhone);
        phone.add(jtfPhone);
        phone.setOpaque(false);
        jtfPhone.setBackground(Color.WHITE);
        jtfPhone.setEditable(false);
        jlPhone.setForeground(Color.WHITE);
        jlPhone.setFont(FontStyle);
        
        //Address Panel
        JPanel address = new JPanel(new GridLayout(1,2));
        address.add(jlAddress);
        address.add(jtfAddress);
        address.setOpaque(false);
        jtfAddress.setBackground(Color.WHITE);
        jtfAddress.setEditable(false);
        jlAddress.setForeground(Color.WHITE);
        jlAddress.setFont(FontStyle);
        
        //E-mail Panel
        JPanel email = new JPanel(new GridLayout(1,2));
        email.add(jlEmail);
        email.add(jtfEmail);
        email.setOpaque(false);
        jtfEmail.setBackground(Color.WHITE);
        jtfEmail.setEditable(false);       
        jlEmail.setForeground(Color.WHITE);
        jlEmail.setFont(FontStyle);
        
        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel());
        buttons.add(jbtDelete);
        buttons.setOpaque(false);
        jbtDelete.addActionListener(new DeleteListener());
        
        //Button color
        jbtDelete.setForeground(Color.WHITE);       
        jbtDelete.setBackground(new Color(74,139,245));   
        
        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(9,1,0,20));
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
    
    private void memberIDLists()
    {
        customersArrayList = progControl.getAll();
        customerID.addItem("===============Select===============");
        
        if (customersArrayList.size() > 0)
        {
            for (Customer customer : customersArrayList) 
            {
                customerID.addItem(customer.getCusId());
            }
        }
        else
        {
            customerID.removeAllItems();
            customerID.addItem("No customer record");
        }
    }
    
    private class DeleteListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            if (customerID.getSelectedIndex() == 0)
                JOptionPane.showMessageDialog(null, "Please select an ID", "Error", JOptionPane.ERROR_MESSAGE);
            else
            {
                int option = JOptionPane.showConfirmDialog(null, "Confirm detele the customer " + customerID.getSelectedItem(), "Confirmation", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION)
                {
                    JPanel pass = new JPanel(new GridLayout(2, 1));
                    pass.add(new JLabel("Password"));
                    JPasswordField password = new JPasswordField(10);
                    pass.add(password);
                    String[] options = {"OK", "Cancel"};
                    int a = JOptionPane.showOptionDialog(null, pass, "Administrator", JOptionPane.NO_OPTION, JOptionPane.WARNING_MESSAGE,null,options,options[1]);
                
                    if (a == 0)
                    {
                        if (password.getText().equals("confirm-delete"))
                        {
                            progControl.deleteRecord(customerID.getSelectedItem().toString());
                            JOptionPane.showMessageDialog(null, "Customer ID : " + customerID.getSelectedItem() + "\nDelete successful", "Delete is successful",JOptionPane.INFORMATION_MESSAGE);
                        }
                        else
                            JOptionPane.showMessageDialog(null, "Access Denied!", "Administrator",JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
    
    private class RetrieveListener implements ActionListener 
    {
        @Override
        public void actionPerformed (ActionEvent event) 
        {
            //Show the record of member
            if (customerID.getSelectedIndex() != 0)
            {
                mem = progControl.selectRecord(customerID.getSelectedItem().toString());
                
                jtfName.setText(mem.getCusName());
                jtfGender.setText(mem.getCusGender());
                jtfBday.setText(mem.getCusBirthday());
                jtfIC.setText(mem.getCusIc());
                jtfPhone.setText(mem.getCusPhone());
                jtfAddress.setText(mem.getCusAddress());
                jtfEmail.setText(mem.getCusEmail());
            }
            else{
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
