/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.Security;

import control.MaintainStaff;
import domain.Staff;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import ui.Validation;

/**
 *
 * @author Jeffrey
 */
public class ChangePassword extends JPanel {

    private Staff staff = new Staff();
    private final MaintainStaff progControl = new MaintainStaff();
    private final Validation v = new Validation();

    private final String[] questionArray = {"==================Select==================",
        "What is your first phone number?",
        "What is the last name of your first lover?",
        "What is your fovarites food?",
        "What is the middle name of your best friend?"};
    //Label
    private final JLabel jlPassword = new JLabel("Password");
    private final JLabel jlRePassword = new JLabel("Re-enter Password");
    private final JLabel jlSecurityQuestion = new JLabel("Security Question");
    private final JLabel jlSecurityAnswer = new JLabel("Answer");
    private final JLabel jlErrorMessagePassword = new JLabel();
    private final JLabel jlErrorMessageRePassword = new JLabel();
    private final JLabel jlErrorMessageSecurityQues = new JLabel();
    private final JLabel jlErrorMessageSecurityAns = new JLabel();
    //Text Field
    private final JPasswordField jtfPassword = new JPasswordField();
    private final JPasswordField jtfRePassword = new JPasswordField();
    private final JTextField jtfSecurityAns = new JTextField();
    //Buttons & ComboBox
    private final JButton jbtResetPassword = new JButton("   Reset Password  ");
    private final JComboBox jcbSecurity = new JComboBox(questionArray);

    //Border style   
    private final Border border = BorderFactory.createLineBorder(new Color(255, 114, 114), 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);
    private final Border original = jtfPassword.getBorder();

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Change Password ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 22), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 16);

    public ChangePassword() {
        staff = progControl.selectRecord("STF0000001");

        //Button color
        jbtResetPassword.setForeground(Color.WHITE);
        jbtResetPassword.setBackground(new Color(74, 139, 245));

        //New Password
        JPanel pass = new JPanel(new GridLayout(2, 2));
        pass.add(jlPassword);
        pass.add(jtfPassword);
        pass.add(new JLabel());
        pass.add(jlErrorMessagePassword);
        pass.setOpaque(false);
        jlPassword.setForeground(Color.WHITE);
        jlPassword.setFont(FontStyle);

        //Reenter Password
        JPanel repass = new JPanel(new GridLayout(2, 2));
        repass.add(jlRePassword);
        repass.add(jtfRePassword);
        repass.add(new JLabel());
        repass.add(jlErrorMessageRePassword);
        repass.setOpaque(false);
        jlRePassword.setForeground(Color.WHITE);
        jlRePassword.setFont(FontStyle);

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
        jtfSecurityAns.setBackground(Color.WHITE);
        jlSecurityAnswer.setForeground(Color.WHITE);
        jlSecurityAnswer.setFont(FontStyle);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel(""));
        buttons.add(jbtResetPassword);
        buttons.setOpaque(false);
        jbtResetPassword.addActionListener(new ResetPasswordListener());

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(5, 1));
        main.add(pass);
        main.add(repass);
        main.add(securityQuestion);
        main.add(securityAns);
        main.add(buttons);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
    }

    private boolean validation() {
        jlErrorMessagePassword.setText(null);
        jlErrorMessageRePassword.setText(null);
        jlErrorMessageSecurityQues.setText(null);
        jlErrorMessageSecurityAns.setText(null);
        jtfSecurityAns.setBorder(original);
        jtfPassword.setBorder(original);
        jtfRePassword.setBorder(original);

        if (jtfPassword.getText().isEmpty()) {
            jlErrorMessagePassword.setText("Please enter the password");
            jlErrorMessagePassword.setForeground(new Color(255, 114, 114));
            jtfPassword.requestFocus();
            jtfPassword.setBorder(border);
            return false;
        }

        else if (!v.isValidPassword(jtfPassword.getText())) {
            jlErrorMessagePassword.setText("At least 8 characters and contain 1 letter and 1 number");
            jlErrorMessagePassword.setForeground(new Color(255, 114, 114));
            jtfPassword.requestFocus();
            jtfPassword.setText(null);
            jtfRePassword.setText(null);
            jtfPassword.setBorder(border);
            return false;
        }

        else if (!jtfPassword.getText().equals(jtfRePassword.getText())) {
            jlErrorMessageRePassword.setText("Re-enter password must same with the new password");
            jlErrorMessageRePassword.setForeground(new Color(255, 114, 114));
            jtfRePassword.requestFocus();
            jtfRePassword.setText(null);
            jtfRePassword.setBorder(border);
            return false;
        }

        else if (jcbSecurity.getSelectedIndex() == 0) {
            jlErrorMessageSecurityQues.setText("Please the the security question");
            jlErrorMessageSecurityQues.setForeground(new Color(255, 114, 114));
            return false;
        }

        else if (jtfSecurityAns.getText().isEmpty()) {
            jlErrorMessageSecurityAns.setText("Please fill in the security answer");
            jlErrorMessageSecurityAns.setForeground(new Color(255, 114, 114));
            jtfSecurityAns.setBorder(border);
            jtfSecurityAns.requestFocus();
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
                staff.setStaffPassword(jtfPassword.getText());
                staff.setStaffQuestion(jcbSecurity.getSelectedIndex());
                staff.setStaffAnswer(jtfSecurityAns.getText());

                progControl.updateRecord(staff);
                JOptionPane.showMessageDialog(null, "Change password successfully", "Authorized Access", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
