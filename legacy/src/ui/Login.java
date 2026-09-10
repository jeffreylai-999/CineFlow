/**
 *
 * @author : Jeffrey Lai & Cheong Pui Yee
 * @Date : 11/06/2015
 * @File : Login
 *
 */
package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.ColorUIResource;
import domain.Staff;
import control.MaintainStaff;
import ui.Security.ForgetPassword;

public class Login extends JFrame {

    //Staff Database
    private static final MaintainStaff staffControl = new MaintainStaff();
    private Staff staff = new Staff();

    //validation class
    private final Validation va = new Validation();

    //Notice 
    final String s = "<html>Please enter your user ID and <br>password to access the system.</html>";
    final String s2 = "<html> <br>If you forgot your password, <br>"
            + "click <font color=\"rgb(218,165,32 )\">\"Forgot password\"</font> below the Forgot Password button. "
            + "<br>The following page will display.<br></html>";

    //Label
    private final JLabel jlblTitle = new JLabel("CineFlow");
    private final JLabel jlblID = new JLabel(" User ID ");
    private final JLabel jlblPassword = new JLabel(" Password");
    private final JLabel contentTitle = new JLabel(s);
    private final JLabel content = new JLabel(s2);
    private final JLabel jlblErrorMsgID = new JLabel();
    private final JLabel jlblErrorMsgPassword = new JLabel();

    //Text Field
    private JPasswordField jtfPassword = new JPasswordField(20);
    private JTextField jtfID = new JTextField(25);

    //Button
    private final JButton jbLogin = new JButton("Login");
    private final JButton jbExit = new JButton("Exit");
    private final JButton jbForget = new JButton("Forgot Password?");

    //CheckBox
    private final JCheckBox tick = new JCheckBox("Show Password");

    //Font Style
    Font Title = new Font("SansSerif", Font.BOLD, 25);
    Font jlblFont = new Font("SansSerif", Font.BOLD, 16);
    Font jlblAlertFont = new Font("SansSerif", Font.BOLD, 12);
    Font conTitle = new Font("SansSerif", Font.BOLD, 22);
    Font contentFont = new Font("SansSerif", Font.ITALIC, 14);

    //ToolTip 
    ColorUIResource background = new ColorUIResource(255, 255, 255);//use for set tooltip text background color
    ColorUIResource foreground = new ColorUIResource(0, 0, 0);//use for set tooltip text foreground color

    //Border
    private final Border original = jtfID.getBorder();
    private static final Border border = BorderFactory.createLineBorder(Color.RED, 2);

    public Login() {
        setLayout(null);

        //Main Panel consists all Components in Panel
        JPanel main = new JPanel(new BorderLayout());

        //Left Panel
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);

        //Set color for tooltip text
        UIManager.put("ToolTip.background", background);
        UIManager.put("ToolTip.foreground", foreground);

        //transparent boackground for checkbox 
        tick.setOpaque(false);

        //Add Labels,Text Fields,Buttons & others
        panel.add(jlblTitle);
        panel.add(jlblID);
        panel.add(jlblErrorMsgID);
        panel.add(jlblPassword);
        panel.add(jlblErrorMsgPassword);
        panel.add(jtfID);
        panel.add(jtfPassword);
        panel.add(tick);
        panel.add(jbLogin);
        panel.add(jbExit);
        panel.add(jbForget);
        panel.add(contentTitle);
        panel.add(content);

        /* setBounds */
        //label
        jlblTitle.setBounds(255, 225, 350, 100);
        jlblID.setBounds(280, 300, 100, 50);
        jlblPassword.setBounds(280, 380, 100, 50);
        jlblErrorMsgID.setBounds(355, 302, 250, 50);
        jlblErrorMsgPassword.setBounds(375, 382, 250, 50);

        //textfields
        jtfID.setBounds(285, 340, 240, 30);
        jtfPassword.setBounds(285, 420, 240, 30);

        //checkbox
        tick.setBounds(280, 450, 130, 20);

        //buttons
        jbLogin.setBounds(375, 500, 70, 30);
        jbExit.setBounds(455, 500, 70, 30);
        jbForget.setBounds(810, 400, 150, 30);

        //html content        
        contentTitle.setBounds(805, 230, 500, 100);
        content.setBounds(810, 300, 500, 100);

        /* font style */
        //label
        jlblTitle.setFont(Title);
        jlblID.setFont(jlblFont);
        jlblPassword.setFont(jlblFont);
        jlblErrorMsgID.setFont(jlblAlertFont);
        jlblErrorMsgPassword.setFont(jlblAlertFont);

        //textfield
        jtfID.setFont(jlblFont);
        jtfPassword.setFont(jlblFont);

        //Notice content
        contentTitle.setFont(conTitle);
        content.setFont(contentFont);

        /* Font color*/
        //label
        jlblTitle.setForeground(new Color(255, 114, 114));
        jlblID.setForeground(Color.WHITE);
        jlblPassword.setForeground(Color.WHITE);

        //check box
        tick.setForeground(new Color(160, 160, 160));

        //notice content
        contentTitle.setForeground(new Color(224, 224, 224));
        content.setForeground(new Color(211, 211, 211));

        //buttons
        jbLogin.setForeground(Color.WHITE);
        jbExit.setForeground(Color.WHITE);
        jbForget.setForeground(new Color(32, 32, 32));

        /* Button Background Color */
        //Button Background
        jbLogin.setBackground(new Color(32, 32, 32));
        jbExit.setBackground(new Color(32, 32, 32));
        jbForget.setBackground(new Color(192, 192, 192));

        jbLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbExit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbForget.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        /* Border Painted */
        //set Border
        tick.setBorderPainted(false);
        jbLogin.setBorderPainted(false);
        jbExit.setBorderPainted(false);
        jbForget.setBorderPainted(false);

        /* Focus Painted */
        //set Button Focus Border
        tick.setFocusPainted(false);
        jbLogin.setFocusPainted(false);
        jbExit.setFocusPainted(false);
        jbForget.setFocusPainted(false);

        /* MouseListener */
        jbLogin.addMouseListener(new btnLogin());
        jbExit.addMouseListener(new btnExit());
        jbForget.addMouseListener(new btnForget());

        /* ActionListener */
        tick.addActionListener(new TickListener());
        jbLogin.addActionListener(new LoginListener());
        jbExit.addActionListener(new ExitListener());
        jbForget.addActionListener(new ForgetPassListener());

        //Set shortcut keys for buttons
        jlblID.setDisplayedMnemonic('U');
        jlblID.setLabelFor(jtfID);
        jlblPassword.setDisplayedMnemonic('P');
        jlblPassword.setLabelFor(jtfPassword);
        jbLogin.setMnemonic('L');
        jbExit.setMnemonic('E');
        jbForget.setMnemonic('F');
        tick.setMnemonic('S');

        //Set Tooltip text for buttons
        jtfID.setToolTipText("Enter your User ID (Alt+U)");
        jtfPassword.setToolTipText("Enter your Password (Alt+P)");
        tick.setToolTipText("Show Password (Alt+S)");
        jbLogin.setToolTipText("Login system (Alt+L)");
        jbExit.setToolTipText("Exit program (Alt+E)");
        jbForget.setToolTipText("Click here if you want to recover your password. \nPassword Recovery (Alt+F).");

        /* Panel set Background Image */
        JPanel contentPane = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                Image img = Toolkit.getDefaultToolkit().getImage(Login.class.getResource("/images/loginPage2.jpg"));
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };

        //setting main panel
        main.add(new JLabel(""));
        main.add(panel, FlowLayout.LEFT);
        main.setOpaque(false);
        add(main);

        //setting inner panel
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        contentPane.add(main);

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("CineFlow");
        setVisible(true);
    }

    /* Mouse Event */
    //Login Button
    private class btnLogin extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbLogin.setBackground(new Color(192, 192, 192));
            jbLogin.setForeground(new Color(32, 32, 32));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbLogin.setBackground(new Color(32, 32, 32));
            jbLogin.setForeground(Color.WHITE);
        }
    };

    //Exit Button
    private class btnExit extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbExit.setBackground(new Color(192, 192, 192));
            jbExit.setForeground(new Color(32, 32, 32));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbExit.setBackground(new Color(32, 32, 32));
            jbExit.setForeground(Color.WHITE);
        }
    };

    //Forget Button
    private class btnForget extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbForget.setBackground(new Color(32, 32, 32));
            jbForget.setForeground(Color.WHITE);
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbForget.setBackground(new Color(192, 192, 192));
            jbForget.setForeground(new Color(32, 32, 32));
        }
    };

    /* Keyboard event */
    //user ID textField
    private class IDkeyIn implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            jtfID.setBorder(original);
            jlblErrorMsgID.setText("");
        }

        @Override
        public void keyPressed(KeyEvent e) {
            jtfID.setBorder(original);
            jlblErrorMsgID.setText("");
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    };

    //Password textField
    private class PasskeyIn implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            jtfPassword.setBorder(original);
            jlblErrorMsgPassword.setText("");
            jlblErrorMsgID.setText("");
        }

        @Override
        public void keyPressed(KeyEvent e) {
            jtfPassword.setBorder(original);
            jlblErrorMsgPassword.setText("");
            jlblErrorMsgID.setText("");
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    };

    /* ActionListener */
    //Tick CheckBox Action
    private class TickListener implements ActionListener {

        //store original symbol
        char ori = jtfPassword.getEchoChar();

        @Override
        public void actionPerformed(ActionEvent e) {
            if (tick.isSelected()) {
                //Show Password
                jtfPassword.setEchoChar((char) 0); //password = JPasswordField
            }
            else {
                //Hide Password
                jtfPassword.setEchoChar(ori);
            }
        }
    };

    //Login Button Action
    private class LoginListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String id = jtfID.getText().toUpperCase();
            String password = jtfPassword.getText();
            staff = staffControl.selectRecord(id);

            //If user ID and password are empty show error message
            if (id.equals("") && password.equals("")) {
                jlblErrorMsgID.setText(" Please enter the user ID. ");
                jlblErrorMsgPassword.setText(" Please enter the Password. ");
                jlblErrorMsgID.setForeground(new Color(255, 114, 114));
                jlblErrorMsgPassword.setForeground(new Color(255, 114, 114));
                jtfID.setBorder(border);
                jtfPassword.setBorder(border);
                jtfID.requestFocus();
                jtfID.addKeyListener(new IDkeyIn());
                jtfPassword.addKeyListener(new PasskeyIn());
            }
            else {
                //If user ID is empty show error message
                if (id.equals("")) {
                    jlblErrorMsgID.setText(" Please enter the user ID. ");
                    jtfID.setBorder(border);
                    jtfID.requestFocus();
                    jtfPassword.setBorder(original);
                    jtfID.addKeyListener(new IDkeyIn());
                } //If user ID not empty show error message
                else if (!id.equals("")) {
                    //user ID correct
                    if (staff != null) {
                        String storedPass = String.valueOf(staff.getStaffPassword());

                        //Show message and go to main page if password is same with particular user ID where stored in database
                        if (password.equals(storedPass)) {
                            jtfID.setBorder(original);
                            jtfPassword.setBorder(original);
                            JOptionPane.showMessageDialog(null, "Welcome, " + staff.getStaffName() + ".\n\nYou have logged in sucessfully", "LOGIN SUCESSFUL", JOptionPane.INFORMATION_MESSAGE);

                            setVisible(false);

                            if (staff.getStaffPosition().equals("Staff")) {
                                new StaffMenu(jtfID.getText().toUpperCase());
                            }
                            else if (staff.getStaffPosition().equals("Administrator")) {
                                new AdminMenu(jtfID.getText().toUpperCase());
                            }
                        } //Password is empty show error message
                        else if (password.equals("")) {
                            jlblErrorMsgPassword.setText(" Please enter the password. ");
                            jtfPassword.setBorder(border);
                            jlblErrorMsgPassword.setForeground(new Color(255, 114, 114));
                            jtfPassword.requestFocus();
                            jtfID.setBorder(original);
                            jtfPassword.addKeyListener(new PasskeyIn());
                        } //Password is invalid show error message
                        else {
                            jlblErrorMsgID.setFont(jlblAlertFont);
                            jlblErrorMsgID.setBounds(285, 355, 350, 50);
                            jlblErrorMsgID.setForeground(new Color(255, 114, 114));
                            jlblErrorMsgID.setText("User ID or Password is incorrect. Please re-enter again.");
                            jtfID.requestFocus();
                        }
                    } //user ID is invalid show error message
                    else {
                        jlblErrorMsgID.setBounds(285, 355, 350, 50);
                        jlblErrorMsgID.setFont(jlblAlertFont);
                        jlblErrorMsgID.setForeground(new Color(255, 114, 114));
                        jlblErrorMsgID.setText("User ID or Password is incorrect. Please re-enter again.");

                        jtfID.requestFocus();
                    }
                }
            }

        }
    };

    //Forget Password Button Action
    private class ForgetPassListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ForgetPassword forgetPassword = new ForgetPassword();
        }
    };

    //Exit Button Action 
    private class ExitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int option = JOptionPane.showConfirmDialog(null, "Confirm exit the system?", "YES_NO OPTION", JOptionPane.OK_CANCEL_OPTION);

            //If user click ok button then terminate program
            if (option == JOptionPane.OK_OPTION) {
                System.exit(0);
            } //Else back to Login page
            else if (option == JOptionPane.CANCEL_OPTION) {
                jtfID.setBorder(original);
                jtfPassword.setBorder(original);
                jtfID.setText("");
                jtfPassword.setText("");
                jtfID.requestFocus();
            }
        }
    }

    public static void main(String[] args) {
        Login login = new Login();
    }
}
