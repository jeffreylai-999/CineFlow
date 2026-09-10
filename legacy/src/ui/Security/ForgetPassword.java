/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.Security;

import control.MaintainStaff;
import domain.Staff;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import ui.Login;

/**
 *
 * @author Jeffrey
 */
public class ForgetPassword extends JFrame {

    private Staff staff = new Staff();
    private final MaintainStaff progControl = new MaintainStaff();
    private final String[] questionArray = {"===============Select================",
        "What is your first phone number?",
        "What is the last name of your first lover?",
        "What is your fovarites food?",
        "What is the middle name of your best friend?"};

    //Label
    private final JLabel jlNotice = new JLabel("<html> Please fill in the User ID, Security Question and Answer below. "
            + "Once your reset your password, you can log in. <br><br>" + "<font color=\"rgb(218,165,32 )\">If you need assistance, please contact Administrator.<br><hr></font></html>");
    private final JLabel jlStaffID = new JLabel("User ID");
    private final JLabel jlSecurityQuestion = new JLabel("Security Question");
    private final JLabel jlSecurityAnswer = new JLabel("Answer");
    private final JLabel jlErrorMessageStaffID = new JLabel();
    private final JLabel jlErrorMessageSecurityQues = new JLabel();
    private final JLabel jlErrorMessageSecurityAns = new JLabel();
    //Text Field
    private final JTextField jtfStaffID = new JTextField();
    private final JTextField jtfSecurityAns = new JTextField();
    //Buttons & ComboBox
    private final JButton jbtResetPassword = new JButton("   Reset Password  ");
    private final JComboBox jcbSecurity = new JComboBox(questionArray);

    //Border style   
    private final Border border = BorderFactory.createLineBorder(new Color(255, 114, 114), 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);
    private final Border original = jtfStaffID.getBorder();

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Forgot Password ? ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 22), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 16);

    public ForgetPassword() {
        //Button color
        jbtResetPassword.setForeground(Color.WHITE);
        jbtResetPassword.setBackground(new Color(74, 139, 245));

        //Set general Layout
        setLayout(null);

        //Notice Panel as a notice board
        JPanel notice = new JPanel(new BorderLayout(10, 0));

        notice.add(jlNotice);
        notice.setOpaque(false);
        jlNotice.setForeground(new Color(211, 211, 211));
        jlNotice.setFont(FontStyle);

        JPanel topTitle = new JPanel(new GridLayout(1, 1));
        topTitle.setOpaque(false);
        topTitle.add(notice);

        //consists JLabel, JTextField and buttons 
        JPanel content = new JPanel(new GridLayout(3, 1));

        //ID Panel
        JPanel id = new JPanel(new GridLayout(2, 2));
        id.add(jlStaffID);
        id.add(jtfStaffID);
        id.add(new JLabel());
        id.add(jlErrorMessageStaffID);
        id.setOpaque(false);
        jlStaffID.setForeground(Color.WHITE);
        jlStaffID.setFont(FontStyle);
        jtfStaffID.setBackground(Color.WHITE);

        //Security question
        JPanel securityQuestion = new JPanel(new GridLayout(2, 2));
        securityQuestion.add(jlSecurityQuestion);
        securityQuestion.add(jcbSecurity);
        securityQuestion.add(new JLabel());
        securityQuestion.add(jlErrorMessageSecurityQues);
        securityQuestion.setOpaque(false);
        jcbSecurity.setBackground(Color.WHITE);
        jlSecurityQuestion.setForeground(Color.WHITE);
        jlSecurityQuestion.setFont(FontStyle);

        //Security answer
        JPanel securityAns = new JPanel(new GridLayout(2, 2));
        securityAns.add(jlSecurityAnswer);
        securityAns.add(jtfSecurityAns);
        securityAns.add(new JLabel());
        securityAns.add(jlErrorMessageSecurityAns);
        securityAns.setOpaque(false);
        jlSecurityAnswer.setForeground(Color.WHITE);
        jlSecurityAnswer.setFont(FontStyle);
        jtfSecurityAns.setBackground(Color.WHITE);

        content.add(id);
        content.add(securityQuestion);
        content.add(securityAns);
        content.setOpaque(false);

        JPanel main = new JPanel(new GridLayout(3, 1));  //Button & Function

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel());
        buttons.add(jbtResetPassword);
        buttons.setOpaque(false);
        jbtResetPassword.addActionListener(new ResetPasswordListener());

        main.add(topTitle);
        main.add(content);
        main.add(buttons);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(10, 60, 0, 60)));

        /* Panel set Background Image */
        JPanel contentPane = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                Image img = Toolkit.getDefaultToolkit().getImage(Login.class.getResource("/images/forgetPasswordBkGround.jpg"));
                g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        };

        //setting inner panel
        JPanel topEmptyPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(170, 150);
            }
        };
        topEmptyPanel.setOpaque(false);

        JPanel leftEmptyPanel = new JPanel(new BorderLayout(44, 34)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 130);
            }
        };
        leftEmptyPanel.setOpaque(false);

        JPanel rightEmptyPanel = new JPanel(new BorderLayout(34, 34)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 230);
            }
        };
        rightEmptyPanel.setOpaque(false);

        JPanel bottomEmptyPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(170, 100);
            }
        };
        bottomEmptyPanel.setOpaque(false);

        contentPane.setBorder(new EmptyBorder(5, 35, 5, 35));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        contentPane.add(topEmptyPanel, BorderLayout.NORTH);
        contentPane.add(leftEmptyPanel, BorderLayout.EAST);
        contentPane.add(rightEmptyPanel, BorderLayout.WEST);
        contentPane.add(main, BorderLayout.CENTER);
        contentPane.add(bottomEmptyPanel, BorderLayout.SOUTH);

        //setUndecorated(true) ;
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("CineFlow");
        setVisible(true);
    }

    private boolean validation() {
        jlErrorMessageStaffID.setText(null);
        jlErrorMessageSecurityQues.setText(null);
        jlErrorMessageSecurityAns.setText(null);
        jtfSecurityAns.setBorder(original);
        jtfStaffID.setBorder(original);

        staff = progControl.selectRecord(jtfStaffID.getText().toUpperCase());

        if (jtfStaffID.getText().isEmpty()) {
            jlErrorMessageStaffID.setText("ID can not be empty");
            jlErrorMessageStaffID.setForeground(new Color(255, 114, 114));
            jtfStaffID.setBorder(border);
            jtfStaffID.requestFocus();
            return false;
        }

        else if (staff == null) {
            jlErrorMessageStaffID.setText("Can not found the ID");
            jlErrorMessageStaffID.setForeground(new Color(255, 114, 114));
            jtfStaffID.setText(null);
            jtfStaffID.setBorder(border);
            jtfStaffID.requestFocus();
            return false;
        }

        else if (jcbSecurity.getSelectedIndex() == 0) {
            jlErrorMessageSecurityQues.setText("Please select a security question");
            jlErrorMessageSecurityQues.setForeground(new Color(255, 114, 114));
            return false;
        }

        else if (jtfSecurityAns.getText().isEmpty()) {
            jlErrorMessageSecurityAns.setText("Please fill in security question answer");
            jlErrorMessageSecurityAns.setForeground(new Color(255, 114, 114));
            jtfSecurityAns.setBorder(border);
            jtfSecurityAns.requestFocus();
            return false;
        }

        else if (jcbSecurity.getSelectedIndex() != staff.getStaffQuestion()
                || !jtfSecurityAns.getText().equalsIgnoreCase(staff.getStaffAnswer())) {
            JOptionPane.showMessageDialog(null, "Access Denied", "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        else {
            return true;
        }
    }

    private class ResetPasswordListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (validation()) {
                staff.setStaffPassword(staff.getStaffId());
                progControl.updateRecord(staff);
                JOptionPane.showMessageDialog(null, "Password is already reset to default.\n" + "This is your default password " + staff.getStaffPassword() + "\nPlease change your password as soon as possible!", "Authorized Access", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        ForgetPassword fp = new ForgetPassword();
    }

}
