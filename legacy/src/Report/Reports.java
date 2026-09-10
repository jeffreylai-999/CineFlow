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
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
public class Reports extends JPanel {
    //Database and Report jrxml

    private final String host = "jdbc:derby://localhost:1527/cinemas";
    private final String user = "APP_USER";
    private final String password = "changeme";

    private Connection conn;
    private final String reportSourceException = "src/Report/Highest Sales Report.jrxml";
    private final String reportSourceTransaction = "src/Report/Transaction Report.jrxml";
    private final String reportSourceSummary = "src/Report/MemberSummaryReport.jrxml";
    private final String reportSourceSummary2 = "src/Report/profitSummaryReport.jrxml";
    private final Validation v = new Validation();

    //Button
    private final JButton jbtShow = new JButton("Show Report");

    //Label
    private final JLabel jlType = new JLabel("Report Type: ");
    private final JLabel jlDate = new JLabel("Date:");
    private final JLabel jlErrorMessageType = new JLabel();
    private final JLabel jlErrorMessageDate = new JLabel();

    private final String[] typeArray = {"================ Select ================",
        "Exception Report - Highest Sales", "Transaction Report - Reservation",
        "Summary - Member Summary", "Summary - Profit Summary"};

    private final String[] monthArray = {"Month", "January", "February",
        "March", "April", "May", "June", "July", "August", "September",
        "October", "November", "December"};

    //Combo Box
    private final JComboBox jcbType = new JComboBox(typeArray);
    private final JComboBox jcbDay = new JComboBox();
    private final JComboBox jcbMon = new JComboBox(monthArray);
    private final JComboBox jcbYear = new JComboBox();

    //Date Time
    private final LocalDate localDate = new LocalDate();
    private final int currentYear = localDate.getYear();

    //Border style
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " View Report ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    JPanel reportDate = new JPanel(new GridLayout(1, 3, 10, 0));

    public Reports() {
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

        //Report date Subpanel   
        reportDate.add(jcbMon);
        reportDate.add(jcbDay);
        reportDate.add(jcbYear);

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
        jbtShow.addActionListener(new ReportListener());

        //Button color
        jbtShow.setForeground(Color.WHITE);
        jbtShow.setBackground(new Color(74, 139, 245));

        //Set general Layout
        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(3, 1, 0, 0));

        main.add(typeReport);
        main.add(selectDate);
        main.add(buttons);
        main.setOpaque(false);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));

        add(main);

    }

    //Exit Button Action 
    private class ReportListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            Map<String, Object> params = new HashMap<String, Object>();

            try {
                Class.forName("org.apache.derby.jdbc.ClientDriver");
                conn = DriverManager.getConnection(host, user, password);

                //Clear the border color while
                jlErrorMessageDate.setText(null);
                jlErrorMessageType.setText(null);

                //Get the variable
                int reportYear = jcbYear.getSelectedIndex();
                int reportMon = jcbMon.getSelectedIndex();
                int reportDay = jcbDay.getSelectedIndex();

                if (jcbType.getSelectedIndex() == 0) {
                    if (validation(reportYear, reportMon, reportDay)) {

                    }
                }

                else if (jcbType.getSelectedIndex() == 1) {
                    if (jcbMon.getSelectedIndex() == 0) {
                        jlErrorMessageDate.setText("Please select the report month");
                        jlErrorMessageDate.setForeground(new Color(255, 114, 114));
                    }

                    else if (jcbMon.getSelectedIndex() != 0) {
                        jlErrorMessageDate.setText("");
                        JasperReport jasperReport = JasperCompileManager.compileReport(reportSourceException);
                        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                        JasperViewer.viewReport(jasperPrint, false);
                    }
                }

                else if (jcbType.getSelectedIndex() == 2) {
                    if (validation(reportYear, reportMon, reportDay)) {
                        JasperReport jasperReport = JasperCompileManager.compileReport(reportSourceTransaction);
                        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                        JasperViewer.viewReport(jasperPrint, false);
                    }
                }

                else if (jcbType.getSelectedIndex() == 3) {
                    if (validation(reportYear, reportMon, reportDay)) {
                        JasperReport jasperReport = JasperCompileManager.compileReport(reportSourceSummary);
                        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                        JasperViewer.viewReport(jasperPrint, false);
                    }
                }

                else if (jcbType.getSelectedIndex() == 4) {
                    if (validation(reportYear, reportMon, reportDay)) {
                        JasperReport jasperReport = JasperCompileManager.compileReport(reportSourceSummary2);
                        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                        JasperViewer.viewReport(jasperPrint, false);
                    }
                }

            }

            catch (JRException jrex) {
                JOptionPane.showMessageDialog(null, "error in generating report");
                jrex.printStackTrace();
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Unble to generate report~!");
                ex.printStackTrace();
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

    private class SetFieldListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            //Set the text field to null value
            if (jcbType.getSelectedIndex() == 0) {
                jcbDay.setVisible(false);
                jcbMon.setVisible(false);
                jcbYear.setVisible(false);
                jcbDay.setSelectedIndex(0);
                jcbMon.setSelectedIndex(0);
                jcbYear.setSelectedIndex(0);
            }

            else if (jcbType.getSelectedIndex() == 1) {
                jcbDay.setVisible(false);
                jcbMon.setVisible(true);
                jcbYear.setVisible(true);
                jcbDay.setSelectedIndex(0);
                jcbMon.setSelectedIndex(0);
                jcbYear.setSelectedIndex(0);
            }

            else if (jcbType.getSelectedIndex() == 2) {
                jcbDay.setVisible(true);
                jcbMon.setVisible(true);
                jcbYear.setVisible(true);
                jcbDay.setSelectedIndex(0);
                jcbMon.setSelectedIndex(0);
                jcbYear.setSelectedIndex(0);
            }

            else if (jcbType.getSelectedIndex() == 3) {
                jcbDay.setVisible(true);
                jcbMon.setVisible(true);
                jcbYear.setVisible(true);
                jcbDay.setSelectedIndex(0);
                jcbMon.setSelectedIndex(0);
                jcbYear.setSelectedIndex(0);
            }

            else if (jcbType.getSelectedIndex() == 4) {
                jcbDay.setVisible(true);
                jcbMon.setVisible(true);
                jcbYear.setVisible(true);
                jcbDay.setSelectedIndex(0);
                jcbMon.setSelectedIndex(0);
                jcbYear.setSelectedIndex(0);
            }
        }
    }
}
