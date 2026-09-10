/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Report;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ui.Validation;

/**
 *
 * @author Ksy Cheong
 */
public class ReportsTesting extends JFrame {
    //Database and Report jrxml

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";

    private Connection conn;
    private final String Exception = "src/Report/Highest Sales Report.jrxml";
    private final String Transaction = "src/Report/Transaction Report.jrxml";
    private final String MemSummary = "src/Report/MemberSummaryReport.jrxml";
    private final String MemPerDailySummary = "src/Report/MemberSummaryReport -  daily.jrxml";
    private final String ProfitSummary = "src/Report/profitSummaryReport - monthly.jrxml";
    private final String ProfitPerDailySummary = "src/Report/profitSummaryReport - daily.jrxml";
    private final Validation v = new Validation();

    //Button
    private final JButton jbtShow = new JButton("Show Report");

    //Label
    private final JLabel jlType = new JLabel("Report Type: ");
    private final JLabel jlShowBy = new JLabel("Show By:");
    private final JLabel jlDate = new JLabel("Date:");
    private final JLabel jlErrorMessageType = new JLabel();
    private final JLabel jlErrorMessageShow = new JLabel();
    private final JLabel jlErrorMessageDate = new JLabel();

    private final String[] typeArray = {"================ Select ================",
        "Highest Sales Report", "Transaction Report",
        "Member Summary Report", "Sales Summary Report"};

    private final String[] showByArray = {"================ Select ================",
        "Monthly", "Daily"};

    private final String[] monthArray = {"Month", "January", "February",
        "March", "April", "May", "June", "July", "August", "September",
        "October", "November", "December"};

    //Combo Box
    private final JComboBox jcbType = new JComboBox(typeArray);
    private final JComboBox jcbShow = new JComboBox(showByArray);
    private final JComboBox jcbDay = new JComboBox();
    private final JComboBox jcbMon = new JComboBox(monthArray);
    private final JComboBox jcbYear = new JComboBox();

    //Date Time
    private final LocalDate localDate = new LocalDate();
    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
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

    public ReportsTesting() {
        //Set drop-down list of view the selected date of report information
        jcbDay.addItem("Day");        //Set days
        for (int i = 1; i <= 31; i++) {
            jcbDay.addItem(String.format("%02d", i));
        }

        jcbYear.addItem("Year");      //Set Year until the current year
        for (int i = currentYear; i <= currentYear + 1; i++) {
            jcbYear.addItem(i);
        }

        //Release Date Panel
        JPanel typeReport = new JPanel(new GridLayout(2, 2));
        typeReport.add(jlType);
        typeReport.add(jcbType);
        typeReport.add(new JLabel());
        typeReport.add(jlErrorMessageType);
        typeReport.setOpaque(false);
        jlType.setForeground(Color.WHITE);
        jlType.setFont(FontStyle);
        jcbType.setBackground(Color.WHITE);

        jcbType.addActionListener(new SetFieldListener());

        //Release Date Panel
        JPanel showByReport = new JPanel(new GridLayout(2, 2));
        showByReport.add(jlShowBy);
        showByReport.add(jcbShow);
        showByReport.add(new JLabel());
        showByReport.add(jlErrorMessageShow);
        showByReport.setOpaque(false);
        jlShowBy.setForeground(Color.WHITE);
        jlShowBy.setFont(FontStyle);
        jcbShow.setBackground(Color.WHITE);

        jcbShow.addActionListener(new SetShowFieldListener());

        //Report date Subpanel  
        reportDate.add(jcbYear);
        reportDate.add(jcbMon);
        reportDate.add(jcbDay);

        jcbDay.setVisible(false);
        jcbMon.setVisible(false);
        jcbYear.setVisible(false);

        reportDate.setOpaque(false);
        jcbDay.setBackground(Color.WHITE);
        jcbMon.setBackground(Color.WHITE);
        jcbYear.setBackground(Color.WHITE);

        //Report Date Panel
        JPanel selectDate = new JPanel(new GridLayout(2, 2));
        selectDate.add(jlDate);
        selectDate.add(reportDate);
        selectDate.add(new JLabel());
        selectDate.add(jlErrorMessageDate);
        selectDate.setOpaque(false);
        jlDate.setForeground(Color.WHITE);
        jlDate.setFont(FontStyle);

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(new JLabel(""));
        buttons.add(jbtShow);
        buttons.setOpaque(false);
        jcbShow.setEnabled(false);
        jbtShow.addActionListener(new ReportListener());

        //Button color
        jbtShow.setForeground(Color.WHITE);
        jbtShow.setBackground(new Color(74, 139, 245));

        JPanel main = new JPanel(new GridLayout(4, 1, 0, 0));

        main.add(typeReport);
        main.add(showByReport);
        main.add(selectDate);
        main.add(buttons);
        main.setOpaque(false);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
        add(main);

        //Set general Layout
        setLayout(new FlowLayout());

        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        ReportsTesting frame = new ReportsTesting();

    }

    //Exit Button Action 
    private class ReportListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedYear = jcbYear.getSelectedItem().toString();
            String selectedMonth = jcbMon.getSelectedItem().toString();
            String selectedDay = jcbDay.getSelectedItem().toString();
            System.out.println(selectedDay);

            int reportMon = 0;

            switch (selectedMonth) {
                case "January":
                    reportMon = 1;
                    break;
                case "February":
                    reportMon = 2;
                    break;
                case "March":
                    reportMon = 3;
                    break;
                case "April":
                    reportMon = 4;
                    break;
                case "May":
                    reportMon = 5;
                    break;
                case "June":
                    reportMon = 6;
                    break;
                case "July":
                    reportMon = 7;
                    break;
                case "August":
                    reportMon = 8;
                    break;
                case "September":
                    reportMon = 9;
                    break;
                case "October":
                    reportMon = 10;
                    break;
                case "November":
                    reportMon = 11;
                    break;
                case "December":
                    reportMon = 12;
                    break;
                default:
                    break;
            }

            String payMon, payDay, show, showDay;

            payMon = "%" + reportMon + "/" + selectedYear;
            show = "(" + selectedMonth + ")";

            payDay = "%" + selectedDay + "/" + "%" + reportMon + "/" + selectedYear;
            showDay = "(" + selectedDay + " " + selectedMonth + " " + selectedYear + ")";

            System.out.println(payDay);
            Map<String, Object> params = new HashMap<>();
            params.put("pay_month", payMon);
            params.put("showMonth", show);

            Map<String, Object> paramsDay = new HashMap<>();
            paramsDay.put("pay_Day", payDay);
            paramsDay.put("showDay", showDay);

            try {
                Class.forName("org.apache.derby.jdbc.ClientDriver");
                conn = DriverManager.getConnection(host, user, password);

                //Clear the border color while
                jlErrorMessageDate.setText(null);
                jlErrorMessageShow.setText(null);
                jlErrorMessageType.setText(null);

                //Get the variable
                int reportYearIndex = jcbYear.getSelectedIndex();
                int reportMonIndex = jcbMon.getSelectedIndex();
                int reportDayIndex = jcbDay.getSelectedIndex();

                switch (jcbType.getSelectedIndex()) {
                    case 0:
                        if (validation(reportYearIndex, reportMonIndex, reportDayIndex)) {
                            //  JasperReport jasperReport = JasperCompileManager.compileReport(reportSource);
                            //  JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                            //  JasperViewer.viewReport(jasperPrint, false);
                        }   break;
                    case 1:
                        if (MonthValidation(reportYearIndex, reportMonIndex)) {
                            JasperReport jasperReport = JasperCompileManager.compileReport(Exception);
                            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                            JasperViewer.viewReport(jasperPrint, false);
                        }   break;
                    case 2:
                        if (jcbShow.getSelectedIndex() == 2) {
                            if (validation(reportYearIndex, reportMonIndex, reportDayIndex)) {
                                JasperReport jasperReport = JasperCompileManager.compileReport(Transaction);
                                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                                JasperViewer.viewReport(jasperPrint, false);
                            }
                        }
                        
                        else if (jcbShow.getSelectedIndex() == 1) {
                            if (MonthValidation(reportYearIndex, reportMonIndex)) {
                                JasperReport jasperReport = JasperCompileManager.compileReport(Transaction);
                                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                                JasperViewer.viewReport(jasperPrint, false);
                            }
                        }   break;
                    case 3:
                        if (jcbShow.getSelectedIndex() == 1) {
                            if (MonthValidation(reportYearIndex, reportMonIndex)) {
                                JasperReport jasperReport = JasperCompileManager.compileReport(MemSummary);
                                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                                JasperViewer.viewReport(jasperPrint, false);
                            }
                        }
                        
                        else if (jcbShow.getSelectedIndex() == 2) {
                            if (validation(reportYearIndex, reportMonIndex, reportDayIndex)) {
                                JasperReport jasperReport = JasperCompileManager.compileReport(MemPerDailySummary);
                                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, paramsDay, conn);
                                JasperViewer.viewReport(jasperPrint, false);
                            }
                        }   break;
                    case 4:
                        if (jcbShow.getSelectedIndex() == 1) {
                            if (MonthValidation(reportYearIndex, reportMonIndex)) {
                                JasperReport jasperReport = JasperCompileManager.compileReport(ProfitSummary);
                                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                                JasperViewer.viewReport(jasperPrint, false);
                            }
                        }
                        
                        else if (jcbShow.getSelectedIndex() == 2) {
                            if (validation(reportYearIndex, reportMonIndex, reportDayIndex)) {
                                JasperReport jasperReport = JasperCompileManager.compileReport(ProfitPerDailySummary);
                                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, paramsDay, conn);
                                JasperViewer.viewReport(jasperPrint, false);
                            }
                        }   break;
                    default:
                        break;
                }
            }

            catch (JRException jrex) {
                JOptionPane.showMessageDialog(null, "Error in generating report");
                jlErrorMessageType.setText("");
                jlErrorMessageShow.setText("");
                jlErrorMessageDate.setText("");
            }
            catch (ClassNotFoundException | SQLException ex) {
                JOptionPane.showMessageDialog(null, "Unable to generate report~!");
                jlErrorMessageType.setText("");
                jlErrorMessageShow.setText("");
                jlErrorMessageDate.setText("");
            }
        }
    }

    private boolean validation(int relYear, int relMon, int relDay) {

        String strReleaseDate = String.format("%02d", relDay) + "/" + String.format("%02d", relMon) + "/" + jcbYear.getSelectedItem();

        //Check genre
        if (jcbType.getSelectedIndex() == 0) {
            jlErrorMessageType.setText("Please select the report type");
            jlErrorMessageType.setForeground(new Color(255, 114, 114));
            return false;
        }

        else if (jcbShow.getSelectedIndex() == 0) {
            jlErrorMessageShow.setText("Please select the show by option");
            jlErrorMessageShow.setForeground(new Color(255, 114, 114));
            return false;
        }

        //Check the release date format
        else if (relDay == 0 || relMon == 0 || relYear == 0) {
            jlErrorMessageDate.setText("Please select the report date");
            jlErrorMessageDate.setForeground(new Color(255, 114, 114));
            return false;
        }

        else if (!v.isValidDate(strReleaseDate)) {
            jlErrorMessageDate.setText("Invalid date format");
            jlErrorMessageDate.setForeground(new Color(255, 114, 114));
            return false;
        }
        return true;
    }

    private boolean MonthValidation(int relYear, int relMon) {
        //Check the release date format
        if (relYear == 0 || relMon == 0) {
            jlErrorMessageDate.setText("Please select the report date");
            jlErrorMessageDate.setForeground(new Color(255, 114, 114));
            return false;
        }
        return true;
    }

    //Type of report Combo Box check determine whether is exception,transaction or summary and effect of show by combo box 
    private class SetFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            //Set the text field to null value
            switch (jcbType.getSelectedIndex()) {
                case 0:
                    jlErrorMessageType.setText("");
                    jlErrorMessageShow.setText("");
                    jlErrorMessageDate.setText("");
                    jcbShow.setSelectedIndex(0);
                    jcbShow.setEnabled(false);
                    break;
                case 1:
                    jlErrorMessageType.setText("");
                    jlErrorMessageShow.setText("");
                    jlErrorMessageDate.setText("");
                    jcbShow.setEnabled(false);
                    jcbShow.setSelectedIndex(1);
                    break;
                case 2:
                    jlErrorMessageType.setText("");
                    jlErrorMessageShow.setText("");
                    jlErrorMessageDate.setText("");
                    jcbShow.setEnabled(true);
                    jcbShow.setSelectedIndex(0);
                    break;
                case 3:
                    jlErrorMessageType.setText("");
                    jlErrorMessageShow.setText("");
                    jlErrorMessageDate.setText("");
                    jcbShow.setEnabled(true);
                    jcbShow.setSelectedIndex(0);
                    break;
                case 4:
                    jlErrorMessageType.setText("");
                    jlErrorMessageShow.setText("");
                    jlErrorMessageDate.setText("");
                    jcbShow.setEnabled(true);
                    jcbShow.setSelectedIndex(0);
                    break;
                default:
                    break;
            }

        }
    }

    //Show By Combo Box check determine whether is montly or daily report and effect of date combo box 
    private class SetShowFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            switch (jcbShow.getSelectedIndex()) {
                case 0:
                    jlErrorMessageType.setText("");
                    jlErrorMessageShow.setText("");
                    jlErrorMessageDate.setText("");
                    jcbDay.setVisible(false);
                    jcbMon.setVisible(false);
                    jcbYear.setVisible(false);
                    jcbDay.setSelectedIndex(0);
                    jcbMon.setSelectedIndex(0);
                    jcbYear.setSelectedIndex(0);
                    break;
                case 1:
                    jlErrorMessageType.setText("");
                    jlErrorMessageShow.setText("");
                    jlErrorMessageDate.setText("");
                    jcbDay.setVisible(false);
                    jcbMon.setVisible(true);
                    jcbYear.setVisible(true);
                    jcbDay.setSelectedIndex(0);
                    jcbMon.setSelectedIndex(0);
                    jcbYear.setSelectedIndex(0);
                    break;
                default:
                    jlErrorMessageType.setText("");
                    jlErrorMessageShow.setText("");
                    jlErrorMessageDate.setText("");
                    jcbDay.setVisible(true);
                    jcbMon.setVisible(true);
                    jcbYear.setVisible(true);
                    jcbDay.setSelectedIndex(0);
                    jcbMon.setSelectedIndex(0);
                    jcbYear.setSelectedIndex(0);
                    break;
            }
        }
    }
}
