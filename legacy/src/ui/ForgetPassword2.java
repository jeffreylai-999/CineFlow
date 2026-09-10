/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.joda.time.LocalDate;

/**
 *
 * @author Ksy Cheong
 */
public class ForgetPassword2 extends JFrame {

    private final String[] positionArray = {"Staff", "Administrator"};
    private final String[] questionArray = {"=================Select=================",
        "What is your first phone number?",
        "What is the last name of your first lover?",
        "What is your fovarites food?",
        "What is the middle name of your best friend?"};

    //Class validation
    private static final Validation va = new Validation();

    //Label
    private final JLabel jlPosition = new JLabel("Position");
    private final JLabel jlID = new JLabel("Staff ID");
    private final JLabel jlName = new JLabel("Name");
    private final JLabel jlSecurityQuestion = new JLabel("Security Question");
    private final JLabel jlSecurityAnswer = new JLabel("Answer");
    private final JLabel jlErrorMessageSecurityQues = new JLabel();
    private final JLabel jlErrorMessageSecurityAns = new JLabel();
    private final JLabel jlErrorMessagePosition = new JLabel();

    //Text Field
    private final JTextField jtfID = new JTextField();
    private final JTextField jtfName = new JTextField();

    //Button
    private JButton jbtShow = new JButton("Show Report");

    //ComboBox
    private final JButton jbtConfirm = new JButton("    Confirm    ");
    private final JComboBox jcbPosition = new JComboBox(positionArray);
    private final JComboBox bYear = new JComboBox();
    private final JComboBox jcbSecurity = new JComboBox(questionArray);

    //Date Time
    private final LocalDate localDate = new LocalDate();
    private final int currentYear = localDate.getYear();

    //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED, 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " View Report ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    JPanel reportDate = new JPanel(new GridLayout(1, 3, 10, 0));

    public ForgetPassword2() {
        //Position Panel
        JPanel positionPanel = new JPanel(new GridLayout(2, 2));
        positionPanel.add(jlPosition);
        positionPanel.add(jcbPosition);
        positionPanel.add(new JLabel());
        positionPanel.add(jlErrorMessagePosition);
        positionPanel.setOpaque(false);
        jlPosition.setForeground(Color.WHITE);
        jlPosition.setFont(FontStyle);
        jcbPosition.setBackground(Color.WHITE);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(new JLabel(""));
        buttons.add(jbtShow);
        buttons.setOpaque(false);
        //jbtShow.addActionListener(new ReportListener());

        //Button color
        jbtShow.setForeground(Color.WHITE);
        jbtShow.setBackground(new Color(74, 139, 245));

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(3, 1, 0, 0));

        //main.add(typeReport);
        //main.add(selectDate);      
        main.add(buttons);
        main.setOpaque(false);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));

        add(main);
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        ForgetPassword2 frame = new ForgetPassword2();

    }

}
