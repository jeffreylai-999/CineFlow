/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui.Search;

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
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Jeffrey
 */
public class Search extends JPanel {

    private final String[] search = {"=============Select=============", "Customer", "Staff", "Movie", "Hall", "Reservation", "WalkIn"};
    private final JLabel jlSearch = new JLabel("Search");
    private final JLabel jlErrorMessage = new JLabel();
    private final JComboBox jcbSearch = new JComboBox(search);
    private final JButton jbtSearch = new JButton("    Confirm    ");

    //Border style
    private final Border border = BorderFactory.createLineBorder(Color.RED, 2);
    private final Border borderMain = BorderFactory.createLineBorder(new Color(240, 240, 225), 3);

    //Border Title
    TitledBorder title = new TitledBorder(borderMain, " Search Information ", TitledBorder.LEFT,
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 20), new Color(255, 255, 153));

    //JLabel Style
    Font FontStyle = new Font("SansSerif", Font.BOLD, 14);

    public Search() {
        JPanel searchPanel = new JPanel(new GridLayout(2, 2));
        searchPanel.add(jlSearch);
        searchPanel.add(jcbSearch);
        searchPanel.add(new JLabel());
        searchPanel.add(jlErrorMessage);
        searchPanel.setOpaque(false);
        jcbSearch.setBackground(Color.WHITE);
        jlSearch.setForeground(Color.WHITE);
        jlSearch.setFont(FontStyle);

        //Button color
        jbtSearch.setForeground(Color.WHITE);
        jbtSearch.setBackground(new Color(74, 139, 245));

        //Button & Function
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JLabel());
        buttons.add(jbtSearch);
        buttons.setOpaque(false);
        jbtSearch.addActionListener(new SearchListener());

        setLayout(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(2, 1));
        main.add(searchPanel);
        main.add(buttons);
        main.setOpaque(false);
        add(main);

        main.setBorder(BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(20, 50, 5, 50)));
    }

    private class SearchListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            jlErrorMessage.setText(null);

            if (jcbSearch.getSelectedIndex() != 0) {
                String a = jcbSearch.getSelectedItem().toString().toLowerCase();
                TableSearch t = new TableSearch(a);

                removeAll();
                add(t);
                t.setOpaque(false);
                revalidate();
                repaint();
            }
            else {
                jlErrorMessage.setText("Please select an option");
                jlErrorMessage.setForeground(new Color(255, 114, 114));
            }
        }
    }
}
