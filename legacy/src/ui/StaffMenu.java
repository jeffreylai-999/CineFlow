/**
 *
 * @author : Jeffrey Lai & Cheong Pui Yee
 * @Date : 13/06/2015
 * @File : Main Menu
 *
 */
package ui;

import ui.Reservation.DeleteReservation;
import ui.Promotion.RetrievePromotion;
import ui.WalkIn.*;
import ui.Reservation.*;
import ui.Customer.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import ui.Movie.RetrieveMovie;
import ui.Security.ChangePassword;

public class StaffMenu extends JFrame {

    //middle menu button image    
    ImageIcon iconStaff = new ImageIcon(getClass().getResource("/images/staffIcon.jpg"));
    ImageIcon iconCus = new ImageIcon(getClass().getResource("/images/cusIcon.jpg"));
    ImageIcon iconWalkIn = new ImageIcon(getClass().getResource("/images/walkinIcon.jpg"));
    ImageIcon iconReserve = new ImageIcon(getClass().getResource("/images/reserveIcon.jpg"));
    ImageIcon iconPayment = new ImageIcon(getClass().getResource("/images/payIcon.jpg"));
    ImageIcon iconMovie = new ImageIcon(getClass().getResource("/images/movieIcon.png"));
    ImageIcon iconHall = new ImageIcon(getClass().getResource("/images/hallIcon.png"));
    ImageIcon iconPromo = new ImageIcon(getClass().getResource("/images/promoIcon.jpg"));
    ImageIcon iconReport = new ImageIcon(getClass().getResource("/images/reportIcon.jpg"));
    ImageIcon iconSearch = new ImageIcon(getClass().getResource("/images/searchIcon.jpg"));

    //top menu button image
    ImageIcon iconTopHome = new ImageIcon(getClass().getResource("/images/homeIcon.png"));
    ImageIcon iconTopCus = new ImageIcon(getClass().getResource("/images/topCus.png"));
    ImageIcon iconTopWalkIn = new ImageIcon(getClass().getResource("/images/topWalkin.png"));
    ImageIcon iconTopReserve = new ImageIcon(getClass().getResource("/images/topReserve.png"));
    ImageIcon iconTopMovie = new ImageIcon(getClass().getResource("/images/topMovie.png"));
    ImageIcon iconTopPromo = new ImageIcon(getClass().getResource("/images/topPromo.png"));

    //top menu button image (hover)
    ImageIcon iconTopHomeHover = new ImageIcon(getClass().getResource("/images/homeIconHover.png"));
    ImageIcon iconTopCusHover = new ImageIcon(getClass().getResource("/images/topCusHover.png"));
    ImageIcon iconTopWalkInHover = new ImageIcon(getClass().getResource("/images/topWalkinHover.png"));
    ImageIcon iconTopReserveHover = new ImageIcon(getClass().getResource("/images/topReserveHover.png"));
    ImageIcon iconTopMovieHover = new ImageIcon(getClass().getResource("/images/topMovieHover.png"));
    ImageIcon iconTopPromoHover = new ImageIcon(getClass().getResource("/images/topPromoHover.png"));

    //date and time format
    private final DateFormat dateFormat = new SimpleDateFormat("EEE dd/MM/yy");
    private final DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

    //top menu button
    private final JButton jbHome = new JButton("", new ImageIcon((iconTopHomeHover.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
    private final JButton jbTopCus = new JButton("", new ImageIcon((iconTopCusHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
    private final JButton jbTopWalkIn = new JButton("", new ImageIcon((iconTopWalkInHover.getImage().getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbTopReserve = new JButton("", new ImageIcon((iconTopReserveHover.getImage().getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbTopMovie = new JButton("", new ImageIcon((iconTopMovieHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
    private final JButton jbTopPromo = new JButton("", new ImageIcon((iconTopPromoHover.getImage().getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH))));

    //Left Panel CRUD button
    //WalkIn Button (CRU)
    private final JButton jbWalkInAdd = new JButton("Create Walk-In");
    private final JButton jbWalkInRetrieve = new JButton("Retrieve Walk-In");

    //Reservation Button (CRUD)
    private final JButton jbReserveAdd = new JButton("Create Reservation");
    private final JButton jbReserveRetrieve = new JButton("Retrieve Reservation");
    private final JButton jbReserveUpdate = new JButton("Reservation Confirmation");
    private final JButton jbReserveCancel = new JButton("Cancel Reservation");

    //Customer Button (CRUD)
    private final JButton jbCusAdd = new JButton("Create Customer");
    private final JButton jbCusRetrieve = new JButton("Retrieve Customer");
    private final JButton jbCusUpdate = new JButton("Update Customer");

    //Movie Button (R)
    private final JButton jbMovieRetrieve = new JButton("Retrieve Movie");

    //Promotion Button (CRUD)
    private final JButton jbPromoRetrieve = new JButton("Retrieve Promotion");

    //Search Button (CRUD)
    private final JButton jbSearchInfo = new JButton("Search Information");

    //Main Menu Button
    //private final JButton jbStaff = new JButton("<html><p style='margin:16 5 16 5'>Staff",new ImageIcon((iconStaff.getImage().getScaledInstance(95,95, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbCus = new JButton("Customer", new ImageIcon((iconCus.getImage().getScaledInstance(95, 95, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbWalkIn = new JButton("Walk-In", new ImageIcon((iconWalkIn.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbReserve = new JButton("<html><p style='margin:5 15 5 15'>Reservation", new ImageIcon((iconReserve.getImage().getScaledInstance(90, 90, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbPromo = new JButton("Promotion", new ImageIcon((iconPromo.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH))));
    //private final JButton jbHall = new JButton("Hall",new ImageIcon((iconHall.getImage().getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbMovie = new JButton("Movie", new ImageIcon((iconMovie.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH))));
    //private final JButton jbPayment = new JButton("Payment",new ImageIcon((iconPayment.getImage().getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH))));
    //private final JButton jbReport = new JButton("Report",new ImageIcon((iconReport.getImage().getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH))));
    //private final JButton jbSearch = new JButton("Search",new ImageIcon((iconSearch.getImage().getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbSettings = new JButton("Settings");
    private final JButton jbLogout = new JButton("Logout");

    //Login User Details Panel 
    private final JLabel loginTitle = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);'>Welcome to<br/>CINEFLOW</p></html>", SwingConstants.CENTER);
    private final JLabel jlblID = new JLabel("User ID :", SwingConstants.LEFT);
    private final JLabel jlblDate = new JLabel("Date      :", SwingConstants.LEFT);
    private final JLabel jlblTime = new JLabel("Time     :", SwingConstants.LEFT);
    private final JLabel fieldID = new JLabel("", SwingConstants.LEFT);
    private final JLabel fieldTime = new JLabel("", SwingConstants.LEFT);
    private final JLabel fieldDate = new JLabel("", SwingConstants.LEFT);
    private final JLabel TitleWalkin = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Walk-In", SwingConstants.CENTER);
    private final JLabel TitleReserve = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Reservation", SwingConstants.CENTER);
    private final JLabel TitleCus = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Customer", SwingConstants.CENTER);
    private final JLabel TitlePromo = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Promotion", SwingConstants.CENTER);
    private final JLabel TitleMovie = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Movie", SwingConstants.CENTER);
    private final JLabel TitleSearch = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Search", SwingConstants.CENTER);

    String temID;

    //Font Style
    Font jlblFont = new Font("SansSerif", Font.BOLD, 13);
    Font btnFont = new Font("SansSerif", Font.BOLD, 13);

    //Panels
    //Panel which consists passing JPanel to JPanel functions 
    JPanel center = new JPanel();

    //Main Menu
    JPanel menu = new JPanel(new BorderLayout(25, 20));

    //First row menu buttons
    JPanel menuRow1 = new JPanel(new GridLayout(1, 3, 2, 2));

    //Second row menu buttons
    JPanel menuRow2 = new JPanel(new GridLayout(1, 3, 2, 2));

    //Left Panel consists CRUD panel and Login status
    JPanel CRUDPanel = new JPanel(new BorderLayout(0, 0)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    //Left Panel consists Walkin CRUD buttons
    JPanel CRUDPanelWalkin = new JPanel(new GridLayout(5, 1)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    //Left Panel consists Reservation CRUD buttons
    JPanel CRUDPanelReserve = new JPanel(new GridLayout(5, 1)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    //Left Panel consists Customer CRUD buttons
    JPanel CRUDPanelCus = new JPanel(new GridLayout(5, 1)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    //Left Panel consists Payment CRUD buttons
    JPanel CRUDPanelMovie = new JPanel(new GridLayout(5, 1)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    //Left Panel consists Promotion CRUD buttons
    JPanel CRUDPanelPromo = new JPanel(new GridLayout(5, 1)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    JPanel contentPane = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            Image img = Toolkit.getDefaultToolkit().getImage(StaffMenu.class.getResource("/images/homePage.png"));
            g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);

        }
    };

    public StaffMenu(String ID) {
        //Top Left Panel 
        JPanel topLeft = new JPanel(new GridLayout(1, 1)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(179, 150);
            }
        };

        //Top Right Panel consists Top Menu Buttons Panel
        JPanel topRight = new JPanel(new GridLayout(1, 1)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 170);
            }
        };

        //Panel consists Top Menu Buttons
        JPanel topBtnPanel = new JPanel(null) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 140);
            }
        };

        //Top Menu Buttons
        //Set the location for top menu buttons
        jbHome.setBounds(22, 59, 78, 80);
        jbTopWalkIn.setBounds(100, 67, 140, 64);
        jbTopReserve.setBounds(232, 67, 140, 64);
        jbTopCus.setBounds(364, 67, 140, 64);
        jbTopPromo.setBounds(496, 67, 140, 64);
        jbTopMovie.setBounds(628, 67, 140, 64);

        jbHome.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopWalkIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopReserve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopCus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopPromo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopMovie.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        //Home button
        jbHome.setFocusPainted(false);
        jbHome.setContentAreaFilled(false);
        jbHome.setBorderPainted(false);
        jbHome.setOpaque(false);
        jbHome.addActionListener(new HomeListener());
        jbHome.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbHome.addMouseListener(new btnHome());

        //Top Menu Customer button    
        jbTopCus.setOpaque(false);
        jbTopCus.setVisible(false);
        jbTopCus.setFocusPainted(false);
        jbTopCus.setBorderPainted(false);
        jbTopCus.setContentAreaFilled(false);
        jbTopCus.addActionListener(new AddCusListener());

        //Top Menu WalkIn button    
        jbTopWalkIn.setBorderPainted(false);
        jbTopWalkIn.setFocusPainted(false);
        jbTopWalkIn.setOpaque(false);
        jbTopWalkIn.setContentAreaFilled(false);
        jbTopWalkIn.setVisible(false);
        jbTopWalkIn.setContentAreaFilled(false);
        jbTopWalkIn.addActionListener(new AddWalkInListener());

        //Top Menu Reservation button    
        jbTopReserve.setBorderPainted(false);
        jbTopReserve.setFocusPainted(false);
        jbTopReserve.setOpaque(false);
        jbTopReserve.setContentAreaFilled(false);
        jbTopReserve.setVisible(false);
        jbTopReserve.addActionListener(new AddReserveListener());

        //Top Menu Promotion button  
        jbTopPromo.setBorderPainted(false);
        jbTopPromo.setFocusPainted(false);
        jbTopPromo.setOpaque(false);
        jbTopPromo.setContentAreaFilled(false);
        jbTopPromo.setVisible(false);
        jbTopPromo.addActionListener(new RetrievePromoListener());

        //Top Menu Movie button  
        jbTopMovie.setBorderPainted(false);
        jbTopMovie.setFocusPainted(false);
        jbTopMovie.setOpaque(false);
        jbTopMovie.setContentAreaFilled(false);
        jbTopMovie.setVisible(false);
        jbTopMovie.addActionListener(new RetrieveMovieListener());

        topBtnPanel.add(jbHome);
        topBtnPanel.add(jbTopCus);
        topBtnPanel.add(jbTopWalkIn);
        topBtnPanel.add(jbTopReserve);
        topBtnPanel.add(jbTopPromo);
        topBtnPanel.add(jbTopMovie);

        topLeft.setOpaque(false);
        topRight.setOpaque(false);
        topBtnPanel.setOpaque(false);
        topRight.add(topBtnPanel, BorderLayout.SOUTH);

        //Panel where consists  topLeft and topRight Panel
        JPanel NorthPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(200, 140);
            }
        };

        NorthPanel.add(topLeft, BorderLayout.WEST);
        NorthPanel.add(topRight, BorderLayout.CENTER);
        NorthPanel.setOpaque(false);

        //Consists the LoginContent and LoginTitle panel
        JPanel LoginPanel = new JPanel(new FlowLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(190, 250);
            }
        };

        LoginPanel.setOpaque(false);

        //Consists the panel title
        JPanel LoginTitle = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(190, 60);
            }
        };

        LoginTitle.add(loginTitle);
        LoginTitle.setOpaque(false);

        //Consists the login user details
        JPanel LoginContent = new JPanel(new GridLayout(4, 2, 5, 5)) {
            @Override
            public Dimension getPreferredSize() {

                return new Dimension(190, 120);
            }
        };

        LoginContent.setOpaque(false);
        LoginContent.setBorder(new EmptyBorder(10, 2, 0, 15));
        LoginContent.add(jlblID);
        LoginContent.add(fieldID);
        LoginContent.add(jlblDate);
        LoginContent.add(fieldDate);
        LoginContent.add(jlblTime);
        LoginContent.add(fieldTime);
        LoginContent.add(jbSettings);
        LoginContent.add(jbLogout);

        //refresh the time every second
        ActionListener timerListener = (ActionEvent e) -> {
            Date date = new Date();
            String time = timeFormat.format(date);
            fieldTime.setText(time);
            fieldDate.setText(dateFormat.format(date));
        };

        Timer timer = new Timer(1000, timerListener);
        // to make sure it doesn't wait one second at the start
        timer.setInitialDelay(0);
        timer.start();

        jlblID.setFont(jlblFont);
        fieldID.setFont(jlblFont);
        jlblDate.setFont(jlblFont);
        fieldDate.setFont(jlblFont);
        jlblTime.setFont(jlblFont);
        fieldTime.setFont(jlblFont);

        jlblID.setForeground(new Color(255, 114, 114));
        fieldID.setForeground(Color.WHITE);

        //GET ID FROM LOGIN PAGE
        fieldID.setText(ID);

        jlblDate.setForeground(new Color(255, 114, 114));
        fieldDate.setForeground(Color.WHITE);

        jlblTime.setForeground(new Color(255, 114, 114));
        fieldTime.setForeground(Color.WHITE);

        //Settings Button
        jbSettings.setForeground(new Color(32, 32, 32));
        jbSettings.setBackground(new Color(192, 192, 192));
        jbSettings.setBorderPainted(false);
        jbSettings.setFocusPainted(false);
        jbSettings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbSettings.addActionListener(new SettingsListener());
        jbSettings.addMouseListener(new btnSettings());

        //Logout Button
        jbLogout.setForeground(new Color(32, 32, 32));
        jbLogout.setBackground(new Color(192, 192, 192));
        jbLogout.setBorderPainted(false);
        jbLogout.setFocusPainted(false);
        jbLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbLogout.addActionListener(new LogoutListener());
        jbLogout.addMouseListener(new btnLogout());

        LoginPanel.add(LoginTitle, BorderLayout.NORTH);
        LoginPanel.add(LoginContent, BorderLayout.CENTER);

        //Left Main Panel consists CRUDPanel and LoginPanel
        JPanel east = new JPanel(new BorderLayout(0, 0));
        east.setBorder(new EmptyBorder(0, 5, 0, 0));
        east.add(CRUDPanel, BorderLayout.NORTH);
        east.add(LoginPanel, BorderLayout.CENTER);
        east.setOpaque(false);

        CRUDPanel.setOpaque(false);
        CRUDPanel.setVisible(false);

        ////hide CRUDPanel when in home page
        CRUDPanelWalkin.setVisible(false);
        CRUDPanelReserve.setVisible(false);
        CRUDPanelCus.setVisible(false);
        CRUDPanelMovie.setVisible(false);
        CRUDPanelPromo.setVisible(false);

        //CRUDPanel buttons for customer
        CRUDPanelCus.add(TitleCus);
        CRUDPanelCus.add(jbCusAdd);
        jbCusAdd.addActionListener(new AddCusListener());
        CRUDPanelCus.add(jbCusRetrieve);
        CRUDPanelCus.add(jbCusUpdate);
        CRUDPanelCus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        //CRUDPanel buttons for Walk-In
        CRUDPanelWalkin.add(TitleWalkin);
        CRUDPanelWalkin.add(jbWalkInAdd);
        jbWalkInAdd.addActionListener(new AddWalkInListener());
        CRUDPanelWalkin.add(jbWalkInRetrieve);
        jbWalkInRetrieve.addActionListener(new RetrieveWalkInListener());

        //CRUDPanel buttons for Reservation
        CRUDPanelReserve.add(TitleReserve);
        CRUDPanelReserve.add(jbReserveAdd);
        jbReserveAdd.addActionListener(new AddReserveListener());
        CRUDPanelReserve.add(jbReserveRetrieve);
        jbReserveRetrieve.addActionListener(new RetrieveReserveListener());
        CRUDPanelReserve.add(jbReserveUpdate);
        jbReserveUpdate.addActionListener(new UpdateReserveListener());
        CRUDPanelReserve.add(jbReserveCancel);
        jbReserveCancel.addActionListener(new CancelReserveListener());

        //CRUDPanel buttons for Promotion
        CRUDPanelPromo.add(TitlePromo);
        CRUDPanelPromo.add(jbPromoRetrieve);
        jbPromoRetrieve.addActionListener(new RetrievePromoListener());

        //CRUDPanel buttons for Search
        CRUDPanelMovie.add(TitleMovie);
        CRUDPanelMovie.add(jbMovieRetrieve);
        jbMovieRetrieve.addActionListener(new RetrieveMovieListener());

        //Main Menu Buttons
        //WalkIn Button
        jbWalkIn.setFont(btnFont);
        jbWalkIn.setForeground(Color.WHITE);
        jbWalkIn.setBackground(Color.BLACK);
        jbWalkIn.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbWalkIn.addMouseListener(new btnWalkin());
        jbWalkIn.addActionListener(new AddWalkInListener());

        //Reservation Button
        jbReserve.setFont(btnFont);
        jbReserve.setForeground(Color.WHITE);
        jbReserve.setBackground(new Color(67, 181, 31));
        jbReserve.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbReserve.addMouseListener(new btnReserve());
        jbReserve.addActionListener(new AddReserveListener());

        //Customer Button
        jbCus.setFont(btnFont);
        jbCus.setForeground(Color.white);
        jbCus.setBackground(new Color(244, 172, 26));
        jbCus.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbCus.addMouseListener(new btnCus());
        jbCus.addActionListener(new AddCusListener());

        //Promotion Button
        jbPromo.setFont(btnFont);
        jbPromo.setForeground(Color.WHITE);
        jbPromo.setBackground(new Color(215, 20, 64));
        jbPromo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbPromo.addMouseListener(new btnPromo());
        jbPromo.addActionListener(new RetrievePromoListener());

        //Movie Button
        jbMovie.setFont(btnFont);
        jbMovie.setForeground(Color.WHITE);
        jbMovie.setBackground(new Color(0, 204, 153));
        jbMovie.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbMovie.addMouseListener(new btnMovie());
        jbMovie.addActionListener(new RetrieveMovieListener());

        //Main Menu Panel First Row Panel
        menuRow1.add(jbWalkIn);
        menuRow1.add(jbReserve);
        menuRow1.add(jbCus);
        menuRow1.setOpaque(false);

        //Main Menu Panel Second Row Panel
        menuRow2.add(jbPromo);
        menuRow2.add(jbMovie);
        menuRow2.setOpaque(false);

        menu.add(menuRow1, BorderLayout.NORTH);
        menu.add(menuRow2, BorderLayout.CENTER);
        menu.setBorder(new EmptyBorder(90, 10, 10, 10));
        menu.setVisible(true);
        menu.setOpaque(false);

        //set cursor for main menu button
        menuRow1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuRow2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        //hide top button when in home page
        jbTopCus.setVisible(false);
        jbTopWalkIn.setVisible(false);
        jbTopReserve.setVisible(false);
        jbTopPromo.setVisible(false);
        jbTopMovie.setVisible(false);

        contentPane.setBorder(new EmptyBorder(15, 15, 10, 10));
        contentPane.setLayout(new BorderLayout());
        contentPane.setOpaque(false);
        setContentPane(contentPane);

        contentPane.add(NorthPanel, BorderLayout.NORTH);
        contentPane.add(center);
        contentPane.add(east, BorderLayout.WEST);

        center.add(menu);
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(15, 15, 10, 10));

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("CineFlow");
        setVisible(true);
    }

    /* ================= MouseAdapter ================= */
    //Settings Button
    private class btnSettings extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbSettings.setBackground(new Color(32, 32, 32));
            jbSettings.setForeground(Color.WHITE);
            jbSettings.setContentAreaFilled(false);
            jbSettings.setOpaque(true);
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbSettings.setBackground(new Color(192, 192, 192));
            jbSettings.setForeground(new Color(32, 32, 32));
        }
    }

    //Logout Button
    private class btnLogout extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbLogout.setBackground(new Color(32, 32, 32));
            jbLogout.setForeground(Color.WHITE);
            jbLogout.setContentAreaFilled(false);
            jbLogout.setOpaque(true);
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbLogout.setBackground(new Color(192, 192, 192));
            jbLogout.setForeground(new Color(32, 32, 32));
        }
    }

    //Top Panel Home Button Mouse Effects
    private class btnHome extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbHome.setFocusPainted(false);
            jbHome.setContentAreaFilled(false);
            jbHome.setOpaque(false);
            jbHome.setIcon(new ImageIcon((iconTopHomeHover.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbHome.setFocusPainted(false);
            jbHome.setContentAreaFilled(false);
            jbHome.setOpaque(false);
            jbHome.setIcon(new ImageIcon((iconTopHomeHover.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbHome.setFocusPainted(false);
            jbHome.setContentAreaFilled(false);
            jbHome.setOpaque(false);
            jbHome.setIcon(new ImageIcon((iconTopHomeHover.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbHome.setFocusPainted(false);
            jbHome.setContentAreaFilled(false);
            jbHome.setOpaque(false);
            jbHome.setIcon(new ImageIcon((iconTopHomeHover.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));

        }
    }

    //Top Panel Home Button Mouse Hover Effects
    private class btnHomeHover extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbHome.setFocusPainted(false);
            jbHome.setContentAreaFilled(false);
            jbHome.setOpaque(false);
            jbHome.setIcon(new ImageIcon((iconTopHomeHover.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbHome.setFocusPainted(false);
            jbHome.setContentAreaFilled(false);
            jbHome.setOpaque(false);
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbHome.setFocusPainted(false);
            jbHome.setContentAreaFilled(false);
            jbHome.setOpaque(false);
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbHome.setFocusPainted(false);
            jbHome.setContentAreaFilled(false);
            jbHome.setOpaque(false);
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));

        }
    }

    //Top Menu WalkIn Button Mouse Effects
    private class btnTopWalkin extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopWalkIn.setFocusPainted(false);
            jbTopWalkIn.setContentAreaFilled(false);
            jbTopWalkIn.setOpaque(false);
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkInHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopWalkIn.setFocusPainted(false);
            jbTopWalkIn.setContentAreaFilled(false);
            jbTopWalkIn.setOpaque(false);
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkInHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopWalkIn.setFocusPainted(false);
            jbTopWalkIn.setContentAreaFilled(false);
            jbTopWalkIn.setOpaque(false);
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkInHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopWalkIn.setFocusPainted(false);
            jbTopWalkIn.setContentAreaFilled(false);
            jbTopWalkIn.setOpaque(false);
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkInHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu WalkIn Button Mouse Hover Effects
    private class btnTopWalkinHover extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopWalkIn.setFocusPainted(false);
            jbTopWalkIn.setContentAreaFilled(false);
            jbTopWalkIn.setOpaque(false);
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkInHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopWalkIn.setFocusPainted(false);
            jbTopWalkIn.setContentAreaFilled(false);
            jbTopWalkIn.setOpaque(false);
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopWalkIn.setFocusPainted(false);
            jbTopWalkIn.setContentAreaFilled(false);
            jbTopWalkIn.setOpaque(false);
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopWalkIn.setFocusPainted(false);
            jbTopWalkIn.setContentAreaFilled(false);
            jbTopWalkIn.setOpaque(false);
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

        }
    }

    //Top Menu Reservation Button Mouse Effects
    private class btnTopReserve extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopReserve.setFocusPainted(false);
            jbTopReserve.setContentAreaFilled(false);
            jbTopReserve.setOpaque(false);
            jbTopReserve.setIcon(new ImageIcon((iconTopReserveHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopReserve.setFocusPainted(false);
            jbTopReserve.setContentAreaFilled(false);
            jbTopReserve.setOpaque(false);
            jbTopReserve.setIcon(new ImageIcon((iconTopReserveHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopReserve.setFocusPainted(false);
            jbTopReserve.setContentAreaFilled(false);
            jbTopReserve.setOpaque(false);
            jbTopReserve.setIcon(new ImageIcon((iconTopReserveHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopReserve.setFocusPainted(false);
            jbTopReserve.setContentAreaFilled(false);
            jbTopReserve.setOpaque(false);
            jbTopReserve.setIcon(new ImageIcon((iconTopReserveHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

        }

    }

    //Top Menu Reservation Button Mouse Hover Effects
    private class btnTopReserveHover extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopReserve.setFocusPainted(false);
            jbTopReserve.setContentAreaFilled(false);
            jbTopReserve.setOpaque(false);
            jbTopReserve.setIcon(new ImageIcon((iconTopReserveHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopReserve.setFocusPainted(false);
            jbTopReserve.setContentAreaFilled(false);
            jbTopReserve.setOpaque(false);
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopReserve.setFocusPainted(false);
            jbTopReserve.setContentAreaFilled(false);
            jbTopReserve.setOpaque(false);
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopReserve.setFocusPainted(false);
            jbTopReserve.setContentAreaFilled(false);
            jbTopReserve.setOpaque(false);
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu Customer Button Mouse Effects
    private class btnTopCus extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopCus.setFocusPainted(false);
            jbTopCus.setContentAreaFilled(false);
            jbTopCus.setOpaque(false);
            jbTopCus.setIcon(new ImageIcon((iconTopCusHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopCus.setFocusPainted(false);
            jbTopCus.setContentAreaFilled(false);
            jbTopCus.setOpaque(false);
            jbTopCus.setIcon(new ImageIcon((iconTopCusHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopCus.setFocusPainted(false);
            jbTopCus.setContentAreaFilled(false);
            jbTopCus.setOpaque(false);
            jbTopCus.setIcon(new ImageIcon((iconTopCusHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopCus.setFocusPainted(false);
            jbTopCus.setContentAreaFilled(false);
            jbTopCus.setOpaque(false);
            jbTopCus.setIcon(new ImageIcon((iconTopCusHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu Customer Button Mouse Hover Effects
    private class btnTopCusHover extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopCus.setFocusPainted(false);
            jbTopCus.setContentAreaFilled(false);
            jbTopCus.setOpaque(false);
            jbTopCus.setIcon(new ImageIcon((iconTopCusHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopCus.setFocusPainted(false);
            jbTopCus.setContentAreaFilled(false);
            jbTopCus.setOpaque(false);
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopCus.setFocusPainted(false);
            jbTopCus.setContentAreaFilled(false);
            jbTopCus.setOpaque(false);
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopCus.setFocusPainted(false);
            jbTopCus.setContentAreaFilled(false);
            jbTopCus.setOpaque(false);
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu Promotion Button Mouse Effects
    private class btnTopPromo extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopPromo.setFocusPainted(false);
            jbTopPromo.setContentAreaFilled(false);
            jbTopPromo.setOpaque(false);
            jbTopPromo.setIcon(new ImageIcon((iconTopPromoHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopPromo.setFocusPainted(false);
            jbTopPromo.setContentAreaFilled(false);
            jbTopPromo.setOpaque(false);
            jbTopPromo.setIcon(new ImageIcon((iconTopPromoHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopPromo.setFocusPainted(false);
            jbTopPromo.setContentAreaFilled(false);
            jbTopPromo.setOpaque(false);
            jbTopPromo.setIcon(new ImageIcon((iconTopPromoHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopPromo.setFocusPainted(false);
            jbTopPromo.setContentAreaFilled(false);
            jbTopPromo.setOpaque(false);
            jbTopPromo.setIcon(new ImageIcon((iconTopPromoHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu Promotion Button Mouse Hover Effects
    private class btnTopPromoHover extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopPromo.setFocusPainted(false);
            jbTopPromo.setContentAreaFilled(false);
            jbTopPromo.setOpaque(false);
            jbTopPromo.setIcon(new ImageIcon((iconTopPromoHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopPromo.setFocusPainted(false);
            jbTopPromo.setContentAreaFilled(false);
            jbTopPromo.setOpaque(false);
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopPromo.setFocusPainted(false);
            jbTopPromo.setContentAreaFilled(false);
            jbTopPromo.setOpaque(false);
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopPromo.setFocusPainted(false);
            jbTopPromo.setContentAreaFilled(false);
            jbTopPromo.setOpaque(false);
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu Movie Button Mouse Effects
    private class btnTopMovie extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopMovie.setFocusPainted(false);
            jbTopMovie.setContentAreaFilled(false);
            jbTopMovie.setOpaque(false);
            jbTopMovie.setIcon(new ImageIcon((iconTopMovieHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopMovie.setFocusPainted(false);
            jbTopMovie.setContentAreaFilled(false);
            jbTopMovie.setOpaque(false);
            jbTopMovie.setIcon(new ImageIcon((iconTopMovieHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopMovie.setFocusPainted(false);
            jbTopMovie.setContentAreaFilled(false);
            jbTopMovie.setOpaque(false);
            jbTopMovie.setIcon(new ImageIcon((iconTopMovieHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopMovie.setFocusPainted(false);
            jbTopMovie.setContentAreaFilled(false);
            jbTopMovie.setOpaque(false);
            jbTopMovie.setIcon(new ImageIcon((iconTopMovieHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu Movie Button Mouse Hover Effects
    private class btnTopMovieHover extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopMovie.setFocusPainted(false);
            jbTopMovie.setContentAreaFilled(false);
            jbTopMovie.setOpaque(false);
            jbTopMovie.setIcon(new ImageIcon((iconTopMovieHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopMovie.setFocusPainted(false);
            jbTopMovie.setContentAreaFilled(false);
            jbTopMovie.setOpaque(false);
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopMovie.setFocusPainted(false);
            jbTopMovie.setContentAreaFilled(false);
            jbTopMovie.setOpaque(false);
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopMovie.setFocusPainted(false);
            jbTopMovie.setContentAreaFilled(false);
            jbTopMovie.setOpaque(false);
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //================= Main Menu ===================
    //Main Menu WalkIn Button Mouse Effects
    private class btnWalkin extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            //   jbWalkIn.setIcon(new ImageIcon((iconWalkIn.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));  
            jbWalkIn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 3));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbWalkIn.setFocusPainted(false);
            jbWalkIn.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            jbWalkIn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            jbWalkIn.setContentAreaFilled(false);
            jbWalkIn.setOpaque(true);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbWalkIn.setBorder(BorderFactory.createLineBorder(new Color(67, 181, 31), 2));
            jbWalkIn.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbWalkIn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            jbWalkIn.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    //Main Menu Reservation Button Mouse Effects
    private class btnReserve extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbReserve.setBorder(BorderFactory.createLineBorder(new Color(204, 255, 153), 3));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbReserve.setFocusPainted(false);
            jbReserve.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            jbReserve.setContentAreaFilled(false);
            jbReserve.setOpaque(true);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbReserve.setBorder(BorderFactory.createLineBorder(new Color(67, 181, 31), 2));
            jbReserve.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbReserve.setBorder(BorderFactory.createLineBorder(new Color(67, 181, 31), 2));
            jbReserve.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }

    }

    //Main Menu Customer Button Mouse Effects
    private class btnCus extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbCus.setBorder(BorderFactory.createLineBorder(new Color(178, 89, 0), 3));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbCus.setFocusPainted(false);
            jbCus.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            jbCus.setContentAreaFilled(false);
            jbCus.setOpaque(true);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbCus.setBorder(BorderFactory.createLineBorder(new Color(244, 172, 26, 2)));
            jbCus.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbCus.setBorder(BorderFactory.createLineBorder(new Color(244, 172, 26, 2)));
            jbCus.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    //Main Menu Promotion Button Mouse Effects
    private class btnPromo extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbPromo.setBorder(BorderFactory.createLineBorder(new Color(255, 191, 191), 3));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbPromo.setFocusPainted(false);
            jbPromo.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            jbPromo.setContentAreaFilled(false);
            jbPromo.setOpaque(true);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbPromo.setBorder(BorderFactory.createLineBorder(new Color(191, 255, 255), 2));
            jbPromo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbPromo.setBorder(BorderFactory.createLineBorder(new Color(191, 255, 255), 2));
            jbPromo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    //Main Menu Movie Button Mouse Effects
    private class btnMovie extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbMovie.setBorder(BorderFactory.createLineBorder(new Color(191, 255, 239), 3));
            // jbMovie.setIcon(new ImageIcon((iconMovie.getImage()).getScaledInstance(104,104, java.awt.Image.SCALE_SMOOTH)));        
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbMovie.setFocusPainted(false);
            jbMovie.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            jbMovie.setContentAreaFilled(false);
            jbMovie.setOpaque(true);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbMovie.setBorder(BorderFactory.createLineBorder(new Color(0, 204, 153), 2));
            // jbMovie.setIcon(new ImageIcon((iconMovie.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbMovie.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbMovie.setBorder(BorderFactory.createLineBorder(new Color(0, 204, 153), 2));
            //jbMovie.setIcon(new ImageIcon((iconMovie.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbMovie.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    //Top Panel Home Button Action
    private class HomeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelPromo.setVisible(false);

            CRUDPanel.setVisible(false);
            menu.setVisible(true);
            jbTopCus.setVisible(false);
            jbTopWalkIn.setVisible(false);
            jbTopReserve.setVisible(false);
            jbTopPromo.setVisible(false);
            jbTopMovie.setVisible(false);
            jbHome.addMouseListener(new btnHome());

            center.removeAll();
            center.add(menu);
            center.revalidate();
            center.repaint();

        }

    }

    //Top Panel and Main Menu Walk-In Button and Left Panel Create Walk-In Customer Button Action 
    private class AddWalkInListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            AddWalkIn obj = new AddWalkIn();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkin());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());

            //set icon image (only walk-in will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkInHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            CRUDPanel.setVisible(true);
            CRUDPanelWalkin.setVisible(true);
            CRUDPanel.add(CRUDPanelWalkin);
            CRUDPanelWalkin.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            CRUDPanelWalkin.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelWalkin.setOpaque(false);

            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelPromo.setVisible(false);

            jbWalkInAdd.setContentAreaFilled(false);
            jbWalkInAdd.setFocusPainted(false);
            jbWalkInAdd.setOpaque(false);
            jbWalkInAdd.setForeground(Color.WHITE);
            jbWalkInAdd.setEnabled(false);
            jbWalkInAdd.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            jbWalkInRetrieve.setForeground(Color.WHITE);
            jbWalkInRetrieve.setContentAreaFilled(false);
            jbWalkInRetrieve.setFocusPainted(false);
            jbWalkInRetrieve.setOpaque(false);
            jbWalkInRetrieve.setEnabled(true);
            //jbWalkInRetrieve.addActionListener(new RetrieveWalkInListener());
            jbWalkInRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Retrieve Walk-In Customer Button Action 
    private class RetrieveWalkInListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RetrieveWalkIn obj = new RetrieveWalkIn();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbWalkInAdd.setEnabled(true);
            jbWalkInRetrieve.setEnabled(false);
            jbWalkInAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbWalkInRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    //Top Panel and Main Menu Reservation Button and Left Panel Create Reservation Button Action 
    private class AddReserveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            AddReservation obj = new AddReservation();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserve());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only promotion will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserveHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            CRUDPanel.setVisible(true);
            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(true);
            CRUDPanelCus.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelPromo.setVisible(false);

            CRUDPanel.add(CRUDPanelReserve);
            CRUDPanelReserve.setOpaque(false);
            CRUDPanelReserve.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelReserve.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            jbReserveAdd.setContentAreaFilled(false);
            jbReserveAdd.setFocusPainted(false);
            jbReserveAdd.setOpaque(false);
            jbReserveAdd.setForeground(Color.WHITE);
            jbReserveAdd.setEnabled(false);
            jbReserveAdd.addActionListener(new AddReserveListener());

            jbReserveRetrieve.setForeground(Color.WHITE);
            jbReserveRetrieve.setContentAreaFilled(false);
            jbReserveRetrieve.setFocusPainted(false);
            jbReserveRetrieve.setOpaque(false);
            jbReserveRetrieve.setEnabled(true);
            jbReserveRetrieve.addActionListener(new RetrieveReserveListener());
            jbReserveRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            jbReserveUpdate.setForeground(Color.WHITE);
            jbReserveUpdate.setContentAreaFilled(false);
            jbReserveUpdate.setFocusPainted(false);
            jbReserveUpdate.setOpaque(false);
            jbReserveUpdate.setEnabled(true);
            jbReserveUpdate.addActionListener(new UpdateReserveListener());
            jbReserveUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            jbReserveCancel.setForeground(Color.WHITE);
            jbReserveCancel.setContentAreaFilled(false);
            jbReserveCancel.setFocusPainted(false);
            jbReserveCancel.setOpaque(false);
            jbReserveCancel.setEnabled(true);
            jbReserveCancel.addActionListener(new CancelReserveListener());
            jbReserveCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Retrieve Reservation Button Action 
    private class RetrieveReserveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RetrieveReservation obj = new RetrieveReservation();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbReserveAdd.setEnabled(true);
            jbReserveRetrieve.setEnabled(false);
            jbReserveUpdate.setEnabled(true);
            jbReserveCancel.setEnabled(true);

            jbReserveAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbReserveRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbReserveUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbReserveCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Update Reservation Button Action 
    private class UpdateReserveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            UpdateReservation obj = new UpdateReservation();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbReserveAdd.setEnabled(true);
            jbReserveRetrieve.setEnabled(true);
            jbReserveUpdate.setEnabled(false);
            jbReserveCancel.setEnabled(true);

            jbReserveAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbReserveRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbReserveUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbReserveCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Cancel Reservation Button Action 
    private class CancelReserveListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            DeleteReservation obj = new DeleteReservation();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbReserveAdd.setEnabled(true);
            jbReserveRetrieve.setEnabled(true);
            jbReserveUpdate.setEnabled(true);
            jbReserveCancel.setEnabled(false);

            jbReserveAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbReserveRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbReserveUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbReserveCancel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    //Top Panel and Main Menu Customer Button and Left Panel Create Customer Button Action 
    private class AddCusListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            AddCustomer obj = new AddCustomer();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCus());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only promotion will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCusHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            CRUDPanel.setVisible(true);
            CRUDPanel.add(CRUDPanelCus);
            CRUDPanelCus.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(true);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelPromo.setVisible(false);

            CRUDPanelCus.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelCus.setOpaque(false);

            jbCusAdd.setContentAreaFilled(false);
            jbCusAdd.setFocusPainted(false);
            jbCusAdd.setOpaque(false);
            jbCusAdd.setForeground(Color.WHITE);
            jbCusAdd.setEnabled(false);

            jbCusRetrieve.setForeground(Color.WHITE);
            jbCusRetrieve.setContentAreaFilled(false);
            jbCusRetrieve.setFocusPainted(false);
            jbCusRetrieve.setOpaque(false);
            jbCusRetrieve.setEnabled(true);
            jbCusRetrieve.addActionListener(new RetrieveCusListener());
            jbCusRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            jbCusUpdate.setForeground(Color.WHITE);
            jbCusUpdate.setContentAreaFilled(false);
            jbCusUpdate.setFocusPainted(false);
            jbCusUpdate.setOpaque(false);
            jbCusUpdate.setEnabled(true);
            jbCusUpdate.addActionListener(new UpdateCusListener());
            jbCusUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Retrieve Customer Button Action 
    private class RetrieveCusListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RetrieveCustomer obj = new RetrieveCustomer();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbCusAdd.setEnabled(true);
            jbCusRetrieve.setEnabled(false);
            jbCusUpdate.setEnabled(true);

            jbCusAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbCusUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Update Customer Button Action 
    private class UpdateCusListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            UpdateCustomer obj = new UpdateCustomer();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbCusAdd.setEnabled(true);
            jbCusRetrieve.setEnabled(true);
            jbCusUpdate.setEnabled(false);

            jbCusAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    //Top Panel and Main Menu Walk-In Button and Left Panel Promotion Button Action (CRUD Buttons)
    private class RetrievePromoListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RetrievePromotion obj = new RetrievePromotion();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();
            center.setBorder(new EmptyBorder(0, 0, 0, 0));

            obj.setOpaque(false);
            menu.setVisible(false);

            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopPromo.addMouseListener(new btnTopPromo());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only promotion will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromoHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //CRUDPanel
            CRUDPanel.setVisible(true);
            CRUDPanel.add(CRUDPanelPromo);
            CRUDPanelPromo.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelPromo.setVisible(true);

            CRUDPanelPromo.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelPromo.setOpaque(false);

            jbPromoRetrieve.setForeground(Color.WHITE);
            jbPromoRetrieve.setFocusPainted(false);
            jbPromoRetrieve.setContentAreaFilled(false);
            jbPromoRetrieve.setOpaque(false);
            jbPromoRetrieve.setEnabled(false);
        }
    }

    //Left Panel Retrieve Movie Button Action
    private class RetrieveMovieListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RetrieveMovie obj = new RetrieveMovie();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopMovie.addMouseListener(new btnTopMovie());

            //set icon image (only promotion will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovieHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //CRUDPanel
            CRUDPanel.setVisible(true);
            CRUDPanel.add(CRUDPanelMovie);
            CRUDPanelMovie.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelMovie.setVisible(true);
            CRUDPanelPromo.setVisible(false);

            CRUDPanelMovie.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelMovie.setOpaque(false);

            jbMovieRetrieve.setForeground(Color.WHITE);
            jbMovieRetrieve.setFocusPainted(false);
            jbMovieRetrieve.setContentAreaFilled(false);
            jbMovieRetrieve.setOpaque(false);
            jbMovieRetrieve.setEnabled(false);
        }
    }

    //Account Settin Button Action 
    private class SettingsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ChangePassword obj = new ChangePassword();

            CRUDPanel.setVisible(false);
            menu.setVisible(false);
            jbTopCus.setVisible(false);
            jbTopWalkIn.setVisible(false);
            jbTopReserve.setVisible(false);
            jbTopPromo.setVisible(false);
            jbHome.setVisible(true);
            jbHome.addMouseListener(new btnHome());

            center.removeAll();
            center.add(obj);
            obj.setOpaque(false);
            center.revalidate();
            center.repaint();
        }
    }

    //Exit Button Action 
    private class LogoutListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            new Login();
            setVisible(false);
        }
    }

}
