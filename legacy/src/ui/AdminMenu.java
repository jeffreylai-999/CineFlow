/**
 *
 * @author : Jeffrey Lai & Cheong Pui Yee
 * @Date : 13/06/2015
 * @File : Main Menu
 *
 */
package ui;

import ui.Search.Search;
import ui.Movie.DeleteMovie;
import Report.Reports;
import ui.Promotion.*;
import ui.Staff.*;
import ui.Movie.*;
import ui.Hall.*;
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
import ui.Payment.RetrieveCreditCard;
import ui.Payment.RetrievePayment;
import ui.Payment.UpdateCreditCard;
import ui.Security.ChangePassword;

public class AdminMenu extends JFrame {

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
    ImageIcon iconTopStaff = new ImageIcon(getClass().getResource("/images/topStaff.png"));
    ImageIcon iconTopPayment = new ImageIcon(getClass().getResource("/images/topPayment.png"));
    ImageIcon iconTopPromo = new ImageIcon(getClass().getResource("/images/topPromo.png"));
    ImageIcon iconTopHall = new ImageIcon(getClass().getResource("/images/topHall.png"));
    ImageIcon iconTopMovie = new ImageIcon(getClass().getResource("/images/topMovie.png"));
    //ImageIcon iconTopReport=new ImageIcon(getClass().getResource("/images/topReport.png"));

    ImageIcon iconTopHomeHover = new ImageIcon(getClass().getResource("/images/homeIconHover.png"));
    ImageIcon iconTopCusHover = new ImageIcon(getClass().getResource("/images/topCusHover.png"));
    ImageIcon iconTopWalkInHover = new ImageIcon(getClass().getResource("/images/topWalkinHover.png"));
    ImageIcon iconTopReserveHover = new ImageIcon(getClass().getResource("/images/topReserveHover.png"));
    ImageIcon iconTopStaffHover = new ImageIcon(getClass().getResource("/images/topStaffHover.png"));
    ImageIcon iconTopPaymentHover = new ImageIcon(getClass().getResource("/images/topPaymentHover.png"));
    ImageIcon iconTopPromoHover = new ImageIcon(getClass().getResource("/images/topPromoHover.png"));
    ImageIcon iconTopHallHover = new ImageIcon(getClass().getResource("/images/topHallHover.png"));
    ImageIcon iconTopMovieHover = new ImageIcon(getClass().getResource("/images/topMovieHover.png"));
    // ImageIcon iconTopReportHover=new ImageIcon(getClass().getResource("/images/topReportHover.png"));

    //date and time format
    private final DateFormat dateFormat = new SimpleDateFormat("EEE dd/MM/yy");
    private final DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

    //top menu button
    private final JButton jbHome = new JButton("", new ImageIcon((iconTopHomeHover.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
    private final JButton jbTopCus = new JButton("", new ImageIcon((iconTopCusHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
    private final JButton jbTopWalkIn = new JButton("", new ImageIcon((iconTopWalkInHover.getImage().getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbTopReserve = new JButton("", new ImageIcon((iconTopReserveHover.getImage().getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbTopStaff = new JButton("", new ImageIcon((iconTopStaffHover.getImage().getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbTopPayment = new JButton("", new ImageIcon((iconTopPaymentHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
    private final JButton jbTopPromo = new JButton("", new ImageIcon((iconTopPromoHover.getImage().getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbTopHall = new JButton("", new ImageIcon((iconTopHallHover.getImage().getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbTopMovie = new JButton("", new ImageIcon((iconTopMovieHover.getImage().getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH))));

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
    private final JButton jbCusDelete = new JButton("Delete Customer");
    private final JButton jbCusUpdate = new JButton("Update Customer");

    //Staff Button (CRUD)
    private final JButton jbStaffAdd = new JButton("Create Staff");
    private final JButton jbStaffRetrieve = new JButton("Retrieve Staff");
    private final JButton jbStaffUpdate = new JButton("Update Staff");

    //Payment Button (R)
    //private final JButton jbPaymentAdd = new JButton("Create Payment");
    private final JButton jbPaymentRetrieve = new JButton("Retrieve Payment");
    private final JButton jbCardRetrieve = new JButton("Retrieve Credit Card");
    private final JButton jbCardUpdate = new JButton("Update Credit Card");

    //Promotion Button (CRUD)
    private final JButton jbPromoAdd = new JButton("Create Promotion");
    private final JButton jbPromoRetrieve = new JButton("Retrieve Promotion");
    private final JButton jbPromoUpdate = new JButton("Update Promotion");
    private final JButton jbPromoDelete = new JButton("Delete Promotion");

    //Hall Button (CRUD)
    private final JButton jbHallAdd = new JButton("Create Hall");
    private final JButton jbHallRetrieve = new JButton("Retrieve Hall");
    private final JButton jbHallUpdate = new JButton("Update Hall");
    private final JButton jbHallDelete = new JButton("Delete Hall");

    //Movie Button (CRUD)
    private final JButton jbMovieAdd = new JButton("Create Movie");
    private final JButton jbMovieRetrieve = new JButton("Retrieve Movie");
    private final JButton jbMovieUpdate = new JButton("Update Movie");
    private final JButton jbMovieDelete = new JButton("Delete Movie");

    //Report Button (CRUD)
    private final JButton jbReportView = new JButton("View Report");

    //Search Button (CRUD)
    private final JButton jbSearchInfo = new JButton("Search Information");

    //Main Menu Button
    private final JButton jbStaff = new JButton("<html><p style='margin:16 5 16 5'>Staff", new ImageIcon((iconStaff.getImage().getScaledInstance(95, 95, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbCus = new JButton("Customer", new ImageIcon((iconCus.getImage().getScaledInstance(95, 95, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbWalkIn = new JButton("Walk-In", new ImageIcon((iconWalkIn.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbReserve = new JButton("<html><p style='margin:5 15 5 15'>Reservation", new ImageIcon((iconReserve.getImage().getScaledInstance(90, 90, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbPromo = new JButton("Promotion", new ImageIcon((iconPromo.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbHall = new JButton("Hall", new ImageIcon((iconHall.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbMovie = new JButton("Movie", new ImageIcon((iconMovie.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbPayment = new JButton("Payment", new ImageIcon((iconPayment.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbReport = new JButton("Report", new ImageIcon((iconReport.getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH))));
    private final JButton jbSearch = new JButton("Search", new ImageIcon((iconSearch.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH))));
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
    private final JLabel TitleStaff = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Staff", SwingConstants.CENTER);
    private final JLabel TitlePayment = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Payment", SwingConstants.CENTER);
    private final JLabel TitlePromo = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Promotion", SwingConstants.CENTER);
    private final JLabel TitleHall = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Hall", SwingConstants.CENTER);
    private final JLabel TitleMovie = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Movie", SwingConstants.CENTER);
    private final JLabel TitleReport = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Report", SwingConstants.CENTER);
    private final JLabel TitleSearch = new JLabel("<html><p style='margin:5 5 10 5;color:rgb(255,255,191);font-size:20'>Search", SwingConstants.CENTER);

    //Font Style
    Font jlblFont = new Font("SansSerif", Font.BOLD, 13);
    Font btnFont = new Font("SansSerif", Font.BOLD, 13);
    Font crudFont = new Font("SansSerif", Font.BOLD, 13);

    //Panels
    //Panel which consists passing JPanel to JPanel functions 
    JPanel center = new JPanel();

    //Main Menu
    JPanel menu = new JPanel(new BorderLayout(25, 20));

    //First row menu buttons
    JPanel menuRow1 = new JPanel(new GridLayout(1, 3, 2, 2));

    //Second row menu buttons
    JPanel menuRow2 = new JPanel(new GridLayout(2, 3, 2, 2));

    //Second row menu buttons
    JPanel menuRow3 = new JPanel(new GridLayout(1, 2, 2, 2));

    //Left Panel consists CRUD buttons and Login status
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

    //Left Panel consists Staff CRUD buttons
    JPanel CRUDPanelStaff = new JPanel(new GridLayout(5, 1)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    //Left Panel consists Payment CRUD buttons
    JPanel CRUDPanelPayment = new JPanel(new GridLayout(5, 1)) {
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

    //Left Panel consists Hall CRUD buttons
    JPanel CRUDPanelHall = new JPanel(new GridLayout(5, 1)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    //Left Panel consists Movie CRUD buttons
    JPanel CRUDPanelMovie = new JPanel(new GridLayout(5, 1)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    //Left Panel consists Report CRUD buttons
    JPanel CRUDPanelReport = new JPanel(new GridLayout(5, 1)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    //Left Panel consists Report CRUD buttons
    JPanel CRUDPanelSearch = new JPanel(new GridLayout(5, 1)) {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(170, 330);
        }
    };

    JPanel contentPane = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            Image img = Toolkit.getDefaultToolkit().getImage(AdminMenu.class.getResource("/images/homePage.png"));
            g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);

        }
    };

    public AdminMenu(String ID) {
        //jbWalkIn.setMnemonic('W');         
        //jbTopWalkIn.setMnemonic('W');               
        //Top Left Panel 
        JPanel topLeft = new JPanel(new GridLayout(1, 1)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(179, 150);
            }
        };
        topLeft.setOpaque(false);

        //Panel consists Top Menu Buttons
        JPanel topBtnPanel = new JPanel(null) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 140);
            }
        };
        topBtnPanel.setOpaque(false);

        //Top Menu Buttons
        //Set the location for top menu buttons
        jbHome.setBounds(22, 59, 78, 80);
        jbTopWalkIn.setBounds(100, 67, 140, 64);
        jbTopReserve.setBounds(232, 67, 140, 64);
        jbTopCus.setBounds(364, 67, 140, 64);
        jbTopStaff.setBounds(496, 67, 140, 64);
        jbTopPromo.setBounds(628, 67, 140, 64);
        jbTopPayment.setBounds(760, 67, 140, 64);
        jbTopHall.setBounds(892, 67, 140, 64);
        jbTopMovie.setBounds(1024, 67, 140, 64);

        jbHome.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopWalkIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopReserve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopCus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopStaff.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopPromo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopPayment.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopHall.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jbTopMovie.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        //Home button
        jbHome.setFocusPainted(false);
        jbHome.setContentAreaFilled(false);
        jbHome.setBorderPainted(false);
        jbHome.setOpaque(false);
        jbHome.addActionListener(new HomeListener());

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

        //Top Menu Staff button  
        jbTopStaff.setBorderPainted(false);
        jbTopStaff.setFocusPainted(false);
        jbTopStaff.setOpaque(false);
        jbTopStaff.setContentAreaFilled(false);
        jbTopStaff.setVisible(false);
        jbTopStaff.addActionListener(new AddStaffListener());

        //Top Menu Payment button  
        jbTopPayment.setBorderPainted(false);
        jbTopPayment.setFocusPainted(false);
        jbTopPayment.setOpaque(false);
        jbTopPayment.setContentAreaFilled(false);
        jbTopPayment.setVisible(false);
        jbTopPayment.addActionListener(new RetrievePaymentListener());

        //Top Menu Promotion button  
        jbTopPromo.setBorderPainted(false);
        jbTopPromo.setFocusPainted(false);
        jbTopPromo.setOpaque(false);
        jbTopPromo.setContentAreaFilled(false);
        jbTopPromo.setVisible(false);
        jbTopPromo.addActionListener(new AddPromoListener());

        //Top Menu Hall button  
        jbTopHall.setBorderPainted(false);
        jbTopHall.setFocusPainted(false);
        jbTopHall.setOpaque(false);
        jbTopHall.setContentAreaFilled(false);
        jbTopHall.setVisible(false);
        jbTopHall.addActionListener(new AddHallListener());

        //Top Menu Movie button  
        jbTopMovie.setBorderPainted(false);
        jbTopMovie.setFocusPainted(false);
        jbTopMovie.setOpaque(false);
        jbTopMovie.setContentAreaFilled(false);
        jbTopMovie.setVisible(false);
        jbTopMovie.addActionListener(new AddMovieListener());

        topBtnPanel.add(jbHome);
        topBtnPanel.add(jbTopCus);
        topBtnPanel.add(jbTopWalkIn);
        topBtnPanel.add(jbTopReserve);
        topBtnPanel.add(jbTopStaff);
        topBtnPanel.add(jbTopPromo);
        topBtnPanel.add(jbTopPayment);
        topBtnPanel.add(jbTopHall);
        topBtnPanel.add(jbTopMovie);

        //hide top button when in home page
        jbTopWalkIn.setVisible(false);
        jbTopReserve.setVisible(false);
        jbTopCus.setVisible(false);
        jbTopStaff.setVisible(false);
        jbTopPayment.setVisible(false);
        jbTopPromo.setVisible(false);
        jbTopHall.setVisible(false);
        jbTopMovie.setVisible(false);

        //Top Right Panel consists Top Menu Buttons Panel
        JPanel topRight = new JPanel(new GridLayout(1, 1)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 170);
            }
        };
        topRight.setOpaque(false);
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

        //Consists the panel title
        JPanel LoginTitle = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(190, 60);
            }
        };
        LoginTitle.setOpaque(false);
        LoginTitle.add(loginTitle);

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

        //Consists the LoginContent and LoginTitle panel
        JPanel LoginPanel = new JPanel(new FlowLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(190, 250);
            }
        };

        LoginPanel.add(LoginTitle, BorderLayout.NORTH);
        LoginPanel.add(LoginContent, BorderLayout.CENTER);
        LoginPanel.setOpaque(false);

        //Left Main Panel consists all CRUDPanel and LoginPanel
        JPanel east = new JPanel(new BorderLayout(0, 0));
        east.setBorder(new EmptyBorder(0, 5, 0, 0));
        east.add(CRUDPanel, BorderLayout.NORTH);
        east.add(LoginPanel, BorderLayout.CENTER);
        east.setOpaque(false);

        //A panel consists each of CRUD Panel for the functions
        CRUDPanel.setOpaque(false);
        CRUDPanel.setVisible(false);

        //hide CRUDPanel when in home page
        CRUDPanelWalkin.setVisible(false);
        CRUDPanelReserve.setVisible(false);
        CRUDPanelCus.setVisible(false);
        CRUDPanelStaff.setVisible(false);
        CRUDPanelPayment.setVisible(false);
        CRUDPanelPromo.setVisible(false);
        CRUDPanelHall.setVisible(false);
        CRUDPanelMovie.setVisible(false);
        CRUDPanelReport.setVisible(false);

        //CRUDPanel buttons for customer
        CRUDPanelCus.add(TitleCus);
        CRUDPanelCus.add(jbCusAdd);
        jbCusAdd.addActionListener(new AddCusListener());
        jbCusAdd.setFont(crudFont);
        CRUDPanelCus.add(jbCusRetrieve);
        jbCusRetrieve.setFont(crudFont);
        CRUDPanelCus.add(jbCusUpdate);
        jbCusUpdate.setFont(crudFont);
        CRUDPanelCus.add(jbCusDelete);
        jbCusDelete.setFont(crudFont);

        //CRUDPanel buttons for Walk-In
        CRUDPanelWalkin.add(TitleWalkin);
        CRUDPanelWalkin.add(jbWalkInAdd);
        jbWalkInAdd.setFont(crudFont);
        jbWalkInAdd.addActionListener(new AddWalkInListener());
        CRUDPanelWalkin.add(jbWalkInRetrieve);
        jbWalkInRetrieve.setFont(crudFont);
        jbWalkInRetrieve.addActionListener(new RetrieveWalkInListener());

        //CRUDPanel buttons for Reservation
        CRUDPanelReserve.add(TitleReserve);
        CRUDPanelReserve.add(jbReserveAdd);
        jbReserveAdd.setFont(crudFont);
        jbReserveAdd.addActionListener(new AddReserveListener());
        CRUDPanelReserve.add(jbReserveRetrieve);
        jbReserveRetrieve.setFont(crudFont);
        jbReserveRetrieve.addActionListener(new RetrieveReserveListener());
        CRUDPanelReserve.add(jbReserveUpdate);
        jbReserveUpdate.setFont(crudFont);
        jbReserveUpdate.addActionListener(new UpdateReserveListener());
        CRUDPanelReserve.add(jbReserveCancel);
        jbReserveCancel.setFont(crudFont);
        jbReserveCancel.addActionListener(new CancelReserveListener());

        //CRUDPanel buttons for Staff
        CRUDPanelStaff.add(TitleStaff);
        CRUDPanelStaff.add(jbStaffAdd);
        jbStaffAdd.setFont(crudFont);
        jbStaffAdd.addActionListener(new AddStaffListener());
        CRUDPanelStaff.add(jbStaffRetrieve);
        jbStaffRetrieve.setFont(crudFont);
        jbStaffRetrieve.addActionListener(new RetrieveStaffListener());
        CRUDPanelStaff.add(jbStaffUpdate);
        jbStaffUpdate.setFont(crudFont);
        jbStaffUpdate.addActionListener(new UpdateStaffListener());

        //CRUDPanel buttons for Payment & credit card
        CRUDPanelPayment.add(TitlePayment);
        CRUDPanelPayment.add(jbPaymentRetrieve);
        jbPaymentRetrieve.setFont(crudFont);
        jbPaymentRetrieve.addActionListener(new RetrievePaymentListener());
        CRUDPanelPayment.add(jbCardRetrieve);
        jbCardRetrieve.setFont(crudFont);
        jbCardRetrieve.addActionListener(new RetrieveCardListener());
        CRUDPanelPayment.add(jbCardUpdate);
        jbCardUpdate.setFont(crudFont);
        jbCardUpdate.addActionListener(new UpdateCardListener());

        //CRUDPanel buttons for Promotion
        CRUDPanelPromo.add(TitlePromo);
        CRUDPanelPromo.add(jbPromoAdd);
        jbPromoAdd.setFont(crudFont);
        jbPromoAdd.addActionListener(new AddPromoListener());
        CRUDPanelPromo.add(jbPromoRetrieve);
        jbPromoRetrieve.setFont(crudFont);
        jbPromoRetrieve.addActionListener(new RetrievePromoListener());
        CRUDPanelPromo.add(jbPromoUpdate);
        jbPromoUpdate.setFont(crudFont);
        jbPromoUpdate.addActionListener(new UpdatePromoListener());
        CRUDPanelPromo.add(jbPromoDelete);
        jbPromoDelete.setFont(crudFont);
        jbPromoDelete.addActionListener(new DeletePromoListener());

        //CRUDPanel buttons for Hall
        CRUDPanelHall.add(TitleHall);
        CRUDPanelHall.add(jbHallAdd);
        jbHallAdd.setFont(crudFont);
        jbHallAdd.addActionListener(new AddHallListener());
        CRUDPanelHall.add(jbHallRetrieve);
        jbHallRetrieve.setFont(crudFont);
        jbHallRetrieve.addActionListener(new RetrieveHallListener());
        CRUDPanelHall.add(jbHallUpdate);
        jbHallUpdate.setFont(crudFont);
        jbHallUpdate.addActionListener(new UpdateHallListener());
        CRUDPanelHall.add(jbHallDelete);
        jbHallDelete.setFont(crudFont);
        jbHallDelete.addActionListener(new DeleteHallListener());

        //CRUDPanel buttons for Movie
        CRUDPanelMovie.add(TitleMovie);
        CRUDPanelMovie.add(jbMovieAdd);
        jbMovieAdd.setFont(crudFont);
        jbMovieAdd.addActionListener(new AddMovieListener());
        CRUDPanelMovie.add(jbMovieRetrieve);
        jbMovieRetrieve.setFont(crudFont);
        jbMovieRetrieve.addActionListener(new RetrieveMovieListener());
        CRUDPanelMovie.add(jbMovieUpdate);
        jbMovieUpdate.setFont(crudFont);
        jbMovieUpdate.addActionListener(new UpdateMovieListener());
        CRUDPanelMovie.add(jbMovieDelete);
        jbMovieDelete.setFont(crudFont);
        jbMovieDelete.addActionListener(new DeleteMovieListener());

        //CRUDPanel buttons for Report
        CRUDPanelReport.add(TitleReport);
        CRUDPanelReport.add(jbReportView);
        jbReportView.addActionListener(new ViewReportListener());

        //CRUDPanel buttons for Search
        CRUDPanelSearch.add(TitleSearch);
        CRUDPanelSearch.add(jbSearchInfo);
        jbSearchInfo.addActionListener(new SearchInfoListener());

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

        //Staff Button
        jbStaff.setFont(btnFont);
        jbStaff.setForeground(Color.WHITE);
        jbStaff.setBackground(new Color(47, 90, 120));
        jbStaff.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbStaff.addMouseListener(new btnStaff());
        jbStaff.addActionListener(new AddStaffListener());

        //Promotion Button
        jbPromo.setFont(btnFont);
        jbPromo.setForeground(Color.WHITE);
        jbPromo.setBackground(new Color(215, 20, 64));
        jbPromo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbPromo.addMouseListener(new btnPromo());
        jbPromo.addActionListener(new AddPromoListener());

        //Payment Button
        jbPayment.setFont(btnFont);
        jbPayment.setForeground(Color.WHITE);
        jbPayment.setBackground(new Color(0, 132, 193));
        jbPayment.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbPayment.addMouseListener(new btnPayment());
        jbPayment.addActionListener(new RetrievePaymentListener());

        //Hall Button
        jbHall.setFont(btnFont);
        jbHall.setForeground(Color.BLACK);
        jbHall.setBackground(new Color(240, 240, 225));
        jbHall.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbHall.addMouseListener(new btnHall());
        jbHall.addActionListener(new AddHallListener());

        //Movie Button
        jbMovie.setFont(btnFont);
        jbMovie.setForeground(Color.WHITE);
        jbMovie.setBackground(new Color(0, 204, 153));
        jbMovie.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbMovie.addMouseListener(new btnMovie());
        jbMovie.addActionListener(new AddMovieListener());

        //Report Button
        jbReport.setFont(btnFont);
        jbReport.setForeground(Color.WHITE);
        jbReport.setBackground(new Color(89, 0, 178));
        jbReport.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbReport.addMouseListener(new btnReport());
        jbReport.addActionListener(new ViewReportListener());

        //Search Button
        jbSearch.setFont(btnFont);
        jbSearch.setForeground(Color.WHITE);
        jbSearch.setBackground(new Color(123, 191, 106));
        jbSearch.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        jbSearch.addMouseListener(new btnSearch());
        jbSearch.addActionListener(new SearchInfoListener());

        //Main Menu Panel First Row Panel
        menuRow1.add(jbWalkIn);
        menuRow1.add(jbReserve);
        menuRow1.add(jbCus);
        menuRow1.setOpaque(false);

        //Main Menu Panel Second Row Panel
        menuRow2.add(jbStaff);
        menuRow2.add(jbPayment);
        menuRow2.add(jbPromo);
        menuRow2.add(jbHall);
        menuRow2.add(jbMovie);
        menuRow2.setOpaque(false);

        menuRow3.add(jbReport);
        menuRow3.add(jbSearch);
        menuRow3.setOpaque(false);

        menu.add(menuRow1, BorderLayout.NORTH);
        menu.add(menuRow2, BorderLayout.CENTER);
        menu.add(menuRow3, BorderLayout.SOUTH);
        menu.setBorder(new EmptyBorder(50, 10, 10, 10));
        menu.setVisible(true);
        menu.setOpaque(false);
        menuRow1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuRow2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menuRow3.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        center.add(menu);
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(15, 15, 10, 10));

        contentPane.setBorder(new EmptyBorder(15, 15, 10, 10));
        contentPane.setLayout(new BorderLayout());
        contentPane.setOpaque(false);
        setContentPane(contentPane);

        contentPane.add(NorthPanel, BorderLayout.NORTH);
        contentPane.add(center);
        contentPane.add(east, BorderLayout.WEST);

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

    //Top Menu Staff Button Mouse Effects
    private class btnTopStaff extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopStaff.setFocusPainted(false);
            jbTopStaff.setContentAreaFilled(false);
            jbTopStaff.setOpaque(false);
            jbTopStaff.setIcon(new ImageIcon((iconTopStaffHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopStaff.setFocusPainted(false);
            jbTopStaff.setContentAreaFilled(false);
            jbTopStaff.setOpaque(false);
            jbTopStaff.setIcon(new ImageIcon((iconTopStaffHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopStaff.setFocusPainted(false);
            jbTopStaff.setContentAreaFilled(false);
            jbTopStaff.setOpaque(false);
            jbTopStaff.setIcon(new ImageIcon((iconTopStaffHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopStaff.setFocusPainted(false);
            jbTopStaff.setContentAreaFilled(false);
            jbTopStaff.setOpaque(false);
            jbTopStaff.setIcon(new ImageIcon((iconTopStaffHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu Staff Button Mouse Hover Effects
    private class btnTopStaffHover extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopStaff.setFocusPainted(false);
            jbTopStaff.setContentAreaFilled(false);
            jbTopStaff.setOpaque(false);
            jbTopStaff.setIcon(new ImageIcon((iconTopStaffHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopStaff.setFocusPainted(false);
            jbTopStaff.setContentAreaFilled(false);
            jbTopStaff.setOpaque(false);
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopStaff.setFocusPainted(false);
            jbTopStaff.setContentAreaFilled(false);
            jbTopStaff.setOpaque(false);
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopStaff.setFocusPainted(false);
            jbTopStaff.setContentAreaFilled(false);
            jbTopStaff.setOpaque(false);
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
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

    //Top Menu Payment Button Mouse Effects
    private class btnTopPayment extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopPayment.setFocusPainted(false);
            jbTopPayment.setContentAreaFilled(false);
            jbTopPayment.setOpaque(false);
            jbTopPayment.setIcon(new ImageIcon((iconTopPaymentHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopPayment.setFocusPainted(false);
            jbTopPayment.setContentAreaFilled(false);
            jbTopPayment.setOpaque(false);
            jbTopPayment.setIcon(new ImageIcon((iconTopPaymentHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopPayment.setFocusPainted(false);
            jbTopPayment.setContentAreaFilled(false);
            jbTopPayment.setOpaque(false);
            jbTopPayment.setIcon(new ImageIcon((iconTopPaymentHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopPayment.setFocusPainted(false);
            jbTopPayment.setContentAreaFilled(false);
            jbTopPayment.setOpaque(false);
            jbTopPayment.setIcon(new ImageIcon((iconTopPaymentHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu Payment Button Mouse Hover Effects
    private class btnTopPaymentHover extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopPayment.setFocusPainted(false);
            jbTopPayment.setContentAreaFilled(false);
            jbTopPayment.setOpaque(false);
            jbTopPayment.setIcon(new ImageIcon((iconTopPaymentHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopPayment.setFocusPainted(false);
            jbTopPayment.setContentAreaFilled(false);
            jbTopPayment.setOpaque(false);
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopPayment.setFocusPainted(false);
            jbTopPayment.setContentAreaFilled(false);
            jbTopPayment.setOpaque(false);
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopPayment.setFocusPainted(false);
            jbTopPayment.setContentAreaFilled(false);
            jbTopPayment.setOpaque(false);
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu Hall Button Mouse Effects
    private class btnTopHall extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopHall.setFocusPainted(false);
            jbTopHall.setContentAreaFilled(false);
            jbTopHall.setOpaque(false);
            jbTopHall.setIcon(new ImageIcon((iconTopHallHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopHall.setFocusPainted(false);
            jbTopHall.setContentAreaFilled(false);
            jbTopHall.setOpaque(false);
            jbTopHall.setIcon(new ImageIcon((iconTopHallHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopHall.setFocusPainted(false);
            jbTopHall.setContentAreaFilled(false);
            jbTopHall.setOpaque(false);
            jbTopHall.setIcon(new ImageIcon((iconTopHallHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopHall.setFocusPainted(false);
            jbTopHall.setContentAreaFilled(false);
            jbTopHall.setOpaque(false);
            jbTopHall.setIcon(new ImageIcon((iconTopHallHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    //Top Menu Hall Button Mouse Hover Effects
    private class btnTopHallHover extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbTopHall.setFocusPainted(false);
            jbTopHall.setContentAreaFilled(false);
            jbTopHall.setOpaque(false);
            jbTopHall.setIcon(new ImageIcon((iconTopHallHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbTopHall.setFocusPainted(false);
            jbTopHall.setContentAreaFilled(false);
            jbTopHall.setOpaque(false);
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbTopHall.setFocusPainted(false);
            jbTopHall.setContentAreaFilled(false);
            jbTopHall.setOpaque(false);
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbTopHall.setFocusPainted(false);
            jbTopHall.setContentAreaFilled(false);
            jbTopHall.setOpaque(false);
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
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

    //Main Menu MouseAdapter
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
            //  jbWalkIn.setIcon(new ImageIcon((iconWalkIn.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbWalkIn.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            //  jbWalkIn.setIcon(new ImageIcon((iconWalkIn.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbWalkIn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            jbWalkIn.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    //Main Menu Reservation Button Mouse Effects
    private class btnReserve extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            //jbReserve.setIcon(new ImageIcon((iconReserve.getImage()).getScaledInstance(90,90, java.awt.Image.SCALE_SMOOTH)));        
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
            // jbReserve.setIcon(new ImageIcon((iconReserve.getImage()).getScaledInstance(90,90, java.awt.Image.SCALE_SMOOTH)));
            jbReserve.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbReserve.setBorder(BorderFactory.createLineBorder(new Color(67, 181, 31), 2));
            //  jbReserve.setIcon(new ImageIcon((iconReserve.getImage()).getScaledInstance(90,90, java.awt.Image.SCALE_SMOOTH)));
            jbReserve.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }

    }

    //Main Menu Customer Button Mouse Effects
    private class btnCus extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            // jbCus.setIcon(new ImageIcon((iconCus.getImage()).getScaledInstance(104,104, java.awt.Image.SCALE_SMOOTH)));  
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
            //jbCus.setIcon(new ImageIcon((iconCus.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbCus.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbCus.setBorder(BorderFactory.createLineBorder(new Color(244, 172, 26, 2)));
            // jbCus.setIcon(new ImageIcon((iconCus.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbCus.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    //Main Menu Staff Button Mouse Effects
    private class btnStaff extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            // jbStaff.setIcon(new ImageIcon((iconStaff.getImage()).getScaledInstance(104,104, java.awt.Image.SCALE_SMOOTH)));        
            jbStaff.setBorder(BorderFactory.createLineBorder(new Color(191, 255, 255), 3));
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbStaff.setFocusPainted(false);
            jbStaff.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            jbStaff.setContentAreaFilled(false);
            jbStaff.setOpaque(true);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbStaff.setBorder(BorderFactory.createLineBorder(new Color(244, 172, 26, 2)));
            //jbStaff.setIcon(new ImageIcon((iconStaff.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbStaff.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbStaff.setBorder(BorderFactory.createLineBorder(new Color(244, 172, 26)));
            // jbStaff.setIcon(new ImageIcon((iconStaff.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbStaff.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    //Main Menu Promotion Button Mouse Effects
    private class btnPromo extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbPromo.setBorder(BorderFactory.createLineBorder(new Color(255, 191, 191), 3));
            //   jbPromo.setIcon(new ImageIcon((iconPromo.getImage()).getScaledInstance(104,104, java.awt.Image.SCALE_SMOOTH)));
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
            // jbPromo.setIcon(new ImageIcon((iconPromo.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbPromo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbPromo.setBorder(BorderFactory.createLineBorder(new Color(191, 255, 255), 2));
            // jbPromo.setIcon(new ImageIcon((iconPromo.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbPromo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    //Main Menu Payment Button Mouse Effects
    private class btnPayment extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbPayment.setBorder(BorderFactory.createLineBorder(new Color(191, 239, 255), 3));
            //jbPayment.setIcon(new ImageIcon((iconPayment.getImage()).getScaledInstance(104,104, java.awt.Image.SCALE_SMOOTH)));        
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbPayment.setFocusPainted(false);
            jbPayment.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            jbPayment.setContentAreaFilled(false);
            jbPayment.setOpaque(true);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbPayment.setBorder(BorderFactory.createLineBorder(new Color(0, 132, 193), 2));
            // jbPayment.setIcon(new ImageIcon((iconPayment.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbPayment.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbPayment.setBorder(BorderFactory.createLineBorder(new Color(0, 132, 193), 2));
            //jbPayment.setIcon(new ImageIcon((iconPayment.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbPayment.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    //Main Menu Hall Button Mouse Effects
    private class btnHall extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbHall.setBorder(BorderFactory.createLineBorder(new Color(104, 104, 89), 3));
            //jbHall.setIcon(new ImageIcon((iconHall.getImage()).getScaledInstance(104,104, java.awt.Image.SCALE_SMOOTH)));        
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbHall.setFocusPainted(false);
            jbHall.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            jbHall.setContentAreaFilled(false);
            jbHall.setOpaque(true);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbHall.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 225), 2));
            //jbHall.setIcon(new ImageIcon((iconHall.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbHall.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbHall.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 225), 2));
            // jbHall.setIcon(new ImageIcon((iconHall.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbHall.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
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

    //Main Menu Report Button Mouse Effects
    private class btnReport extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbReport.setBorder(BorderFactory.createLineBorder(new Color(223, 191, 255), 3));
            //jbReport.setIcon(new ImageIcon((iconReport.getImage()).getScaledInstance(104,104, java.awt.Image.SCALE_SMOOTH)));        
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbReport.setFocusPainted(false);
            jbReport.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            jbReport.setContentAreaFilled(false);
            jbReport.setOpaque(true);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbReport.setBorder(BorderFactory.createLineBorder(new Color(89, 0, 178), 2));
            //jbReport.setIcon(new ImageIcon((iconReport.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbReport.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbReport.setBorder(BorderFactory.createLineBorder(new Color(89, 0, 178), 2));
            // jbReport.setIcon(new ImageIcon((iconReport.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbReport.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    //Main Menu Search Button Mouse Effects
    private class btnSearch extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evt) {
            jbSearch.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 26), 3));
            //jbReport.setIcon(new ImageIcon((iconReport.getImage()).getScaledInstance(104,104, java.awt.Image.SCALE_SMOOTH)));        
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            jbSearch.setFocusPainted(false);
            jbSearch.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            jbSearch.setContentAreaFilled(false);
            jbSearch.setOpaque(true);
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            jbSearch.setBorder(BorderFactory.createLineBorder(new Color(123, 191, 106), 2));
            //jbReport.setIcon(new ImageIcon((iconReport.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbSearch.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            jbSearch.setBorder(BorderFactory.createLineBorder(new Color(123, 191, 106), 2));
            // jbReport.setIcon(new ImageIcon((iconReport.getImage()).getScaledInstance(100,100, java.awt.Image.SCALE_SMOOTH)));
            jbSearch.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    /* ================= ActionListener  ================= */
    //Top Panel Home Button Action
    private class HomeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            CRUDPanel.setVisible(false);
            menu.setVisible(true);
            jbTopCus.setVisible(false);
            jbTopWalkIn.setVisible(false);
            jbTopReserve.setVisible(false);
            jbTopStaff.setVisible(false);
            jbTopPayment.setVisible(false);
            jbTopPromo.setVisible(false);
            jbTopHall.setVisible(false);
            jbTopMovie.setVisible(false);
            jbHome.setVisible(true);
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

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkin());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopStaff.addMouseListener(new btnTopStaffHover());
            jbTopPayment.addMouseListener(new btnTopPaymentHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopHall.addMouseListener(new btnTopHallHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only walk-in will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkInHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //show top button
            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopStaff.setVisible(true);
            jbTopPayment.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopHall.setVisible(true);
            jbTopMovie.setVisible(true);

            CRUDPanel.setVisible(true);
            CRUDPanel.add(CRUDPanelWalkin);
            CRUDPanelWalkin.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            CRUDPanelWalkin.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelWalkin.setOpaque(false);

            CRUDPanelWalkin.setVisible(true);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelStaff.setVisible(false);
            CRUDPanelPayment.setVisible(false);
            CRUDPanelPromo.setVisible(false);
            CRUDPanelHall.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelReport.setVisible(false);
            CRUDPanelSearch.setVisible(false);

            jbWalkInAdd.setContentAreaFilled(false);
            jbWalkInAdd.setFocusPainted(false);
            jbWalkInAdd.setOpaque(false);
            jbWalkInAdd.setForeground(Color.WHITE);
            jbWalkInAdd.setEnabled(false);

            jbWalkInRetrieve.setForeground(Color.WHITE);
            jbWalkInRetrieve.setContentAreaFilled(false);
            jbWalkInRetrieve.setFocusPainted(false);
            jbWalkInRetrieve.setOpaque(false);
            jbWalkInRetrieve.setEnabled(true);
            jbWalkInRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            //jbWalkInRetrieve.addActionListener(new RetrieveWalkInListener());

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

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserve());
            jbTopStaff.addMouseListener(new btnTopStaffHover());
            jbTopPayment.addMouseListener(new btnTopPaymentHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopHall.addMouseListener(new btnTopHallHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only reservation will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserveHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //top button menu
            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopStaff.setVisible(true);
            jbTopPayment.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopHall.setVisible(true);
            jbTopMovie.setVisible(true);

            //CRUD panel 
            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(true);
            CRUDPanelCus.setVisible(false);
            CRUDPanelStaff.setVisible(false);
            CRUDPanelPayment.setVisible(false);
            CRUDPanelPromo.setVisible(false);
            CRUDPanelHall.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelReport.setVisible(false);
            CRUDPanelSearch.setVisible(false);

            CRUDPanel.setVisible(true);
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
            jbReserveRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbReserveRetrieve.addActionListener(new RetrieveReserveListener());

            jbReserveUpdate.setForeground(Color.WHITE);
            jbReserveUpdate.setContentAreaFilled(false);
            jbReserveUpdate.setFocusPainted(false);
            jbReserveUpdate.setOpaque(false);
            jbReserveUpdate.setEnabled(true);
            jbReserveUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbReserveUpdate.addActionListener(new UpdateReserveListener());

            jbReserveCancel.setForeground(Color.WHITE);
            jbReserveCancel.setContentAreaFilled(false);
            jbReserveCancel.setFocusPainted(false);
            jbReserveCancel.setOpaque(false);
            jbReserveCancel.setEnabled(true);
            jbReserveCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbReserveCancel.addActionListener(new CancelReserveListener());
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

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCus());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopStaff.addMouseListener(new btnTopStaffHover());
            jbTopPayment.addMouseListener(new btnTopPaymentHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopHall.addMouseListener(new btnTopHallHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only customer will hover)            
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCusHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //top button menu
            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopStaff.setVisible(true);
            jbTopPayment.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopHall.setVisible(true);
            jbTopMovie.setVisible(true);

            //CRUDPanel
            CRUDPanel.setVisible(true);
            CRUDPanel.add(CRUDPanelCus);
            CRUDPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            //All CRUD panel 
            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(true);
            CRUDPanelStaff.setVisible(false);
            CRUDPanelPayment.setVisible(false);
            CRUDPanelPromo.setVisible(false);
            CRUDPanelHall.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelReport.setVisible(false);
            CRUDPanelSearch.setVisible(false);

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
            jbCusRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusRetrieve.addActionListener(new RetrieveCusListener());

            jbCusUpdate.setForeground(Color.WHITE);
            jbCusUpdate.setContentAreaFilled(false);
            jbCusUpdate.setFocusPainted(false);
            jbCusUpdate.setOpaque(false);
            jbCusUpdate.setEnabled(true);
            jbCusUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusUpdate.addActionListener(new UpdateCusListener());

            jbCusDelete.setForeground(Color.WHITE);
            jbCusDelete.setContentAreaFilled(false);
            jbCusDelete.setFocusPainted(false);
            jbCusDelete.setOpaque(false);
            jbCusDelete.setEnabled(true);
            jbCusDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusDelete.addActionListener(new DeleteCusListener());
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
            jbCusDelete.setEnabled(true);

            jbCusAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbCusUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbCusAdd.setEnabled(true);
            jbCusRetrieve.setEnabled(true);
            jbCusUpdate.setEnabled(false);
            jbCusDelete.setEnabled(true);

            jbCusAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbCusDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Delete Customer Button Action 
    private class DeleteCusListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            DeleteCustomer obj = new DeleteCustomer();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbCusAdd.setEnabled(true);
            jbCusRetrieve.setEnabled(true);
            jbCusUpdate.setEnabled(true);
            jbCusDelete.setEnabled(false);

            jbCusAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCusDelete.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    //Top Panel and Main Menu Staff Button and Left Panel Create Staff Button Action (CRUD Buttons)
    private class AddStaffListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            AddStaff obj = new AddStaff();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();
            center.setBorder(new EmptyBorder(0, 0, 0, 0));
            obj.setOpaque(false);
            menu.setVisible(false);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopStaff.addMouseListener(new btnTopStaff());
            jbTopPayment.addMouseListener(new btnTopPaymentHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopHall.addMouseListener(new btnTopHallHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only staff will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopStaff.setIcon(new ImageIcon((iconTopStaffHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //top button menu
            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopStaff.setVisible(true);
            jbTopPayment.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopHall.setVisible(true);
            jbTopMovie.setVisible(true);

            //CRUD panel 
            CRUDPanel.setVisible(true);
            CRUDPanel.add(CRUDPanelStaff);

            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelStaff.setVisible(true);
            CRUDPanelPayment.setVisible(false);
            CRUDPanelPromo.setVisible(false);
            CRUDPanelHall.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelReport.setVisible(false);
            CRUDPanelSearch.setVisible(false);

            CRUDPanelStaff.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelStaff.setOpaque(false);
            CRUDPanelStaff.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            jbStaffAdd.setContentAreaFilled(false);
            jbStaffAdd.setFocusPainted(false);
            jbStaffAdd.setOpaque(false);
            jbStaffAdd.setForeground(Color.WHITE);
            jbStaffAdd.setEnabled(false);
            jbStaffAdd.addActionListener(new AddStaffListener());

            jbStaffRetrieve.setContentAreaFilled(false);
            jbStaffRetrieve.setFocusPainted(false);
            jbStaffRetrieve.setOpaque(false);
            jbStaffRetrieve.setForeground(Color.WHITE);
            jbStaffRetrieve.setEnabled(true);
            jbStaffRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbStaffRetrieve.addActionListener(new RetrieveStaffListener());

            jbStaffUpdate.setForeground(Color.WHITE);
            jbStaffUpdate.setContentAreaFilled(false);
            jbStaffUpdate.setFocusPainted(false);
            jbStaffUpdate.setOpaque(false);
            jbStaffUpdate.setEnabled(true);
            jbStaffUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbStaffUpdate.addActionListener(new UpdateStaffListener());

            jbStaffUpdate.setForeground(Color.WHITE);
            jbStaffUpdate.setContentAreaFilled(false);
            jbStaffUpdate.setFocusPainted(false);
            jbStaffUpdate.setOpaque(false);
            jbStaffUpdate.setEnabled(true);
            jbStaffUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbStaffUpdate.addActionListener(new UpdateStaffListener());
        }
    }

    //Left Panel Retrieve Staff Button Action 
    private class RetrieveStaffListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RetrieveStaff obj = new RetrieveStaff();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbStaffAdd.setEnabled(true);
            jbStaffRetrieve.setEnabled(false);
            jbStaffUpdate.setEnabled(true);

            jbStaffAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbStaffRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbStaffUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Update Staff Button Action 
    private class UpdateStaffListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            UpdateStaff obj = new UpdateStaff();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbStaffAdd.setEnabled(true);
            jbStaffRetrieve.setEnabled(true);
            jbStaffUpdate.setEnabled(false);

            jbStaffAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbStaffRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbStaffUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    //Top Panel and Main Menu Payment Button and Left Panel Payment Button Action (CRUD Buttons)
    private class RetrievePaymentListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RetrievePayment obj = new RetrievePayment();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();
            center.setBorder(new EmptyBorder(0, 0, 0, 0));

            obj.setOpaque(false);
            menu.setVisible(false);

            //top button menu
            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopStaff.setVisible(true);
            jbTopPayment.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopHall.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopStaff.addMouseListener(new btnTopStaffHover());
            jbTopPayment.addMouseListener(new btnTopPayment());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopHall.addMouseListener(new btnTopHallHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only staff will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPayment.setIcon(new ImageIcon((iconTopPaymentHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //CRUD panel            
            CRUDPanel.setVisible(true);
            CRUDPanel.add(CRUDPanelPayment);

            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelStaff.setVisible(false);
            CRUDPanelPayment.setVisible(true);
            CRUDPanelPromo.setVisible(false);
            CRUDPanelHall.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelReport.setVisible(false);
            CRUDPanelSearch.setVisible(false);

            CRUDPanelPayment.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelPayment.setOpaque(false);
            CRUDPanelPayment.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            jbPaymentRetrieve.setForeground(Color.WHITE);
            jbPaymentRetrieve.setFocusPainted(false);
            jbPaymentRetrieve.setContentAreaFilled(false);
            jbPaymentRetrieve.setOpaque(false);
            jbPaymentRetrieve.setEnabled(false);

            jbCardRetrieve.setForeground(Color.WHITE);
            jbCardRetrieve.setFocusPainted(false);
            jbCardRetrieve.setContentAreaFilled(false);
            jbCardRetrieve.setOpaque(false);
            jbCardRetrieve.setEnabled(true);
            jbCardRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            jbCardUpdate.setForeground(Color.WHITE);
            jbCardUpdate.setFocusPainted(false);
            jbCardUpdate.setContentAreaFilled(false);
            jbCardUpdate.setOpaque(false);
            jbCardUpdate.setEnabled(true);
            jbCardUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Credit Card Button Action (RU Buttons)
    private class RetrieveCardListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RetrieveCreditCard obj = new RetrieveCreditCard();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbPaymentRetrieve.setEnabled(true);
            jbCardRetrieve.setEnabled(false);
            jbCardUpdate.setEnabled(true);

            jbPaymentRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCardRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbCardUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Credit Card Button Action (RU Buttons)
    private class UpdateCardListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            UpdateCreditCard obj = new UpdateCreditCard();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbPaymentRetrieve.setEnabled(true);
            jbCardRetrieve.setEnabled(true);
            jbCardUpdate.setEnabled(false);

            jbPaymentRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCardRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbCardUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    //Top Panel and Main Menu Walk-In Button and Left Panel Promotion Button Action (CRUD Buttons)
    private class AddPromoListener implements ActionListener {

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

            //top button menu
            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopStaff.setVisible(true);
            jbTopPayment.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopHall.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopStaff.addMouseListener(new btnTopStaffHover());
            jbTopPayment.addMouseListener(new btnTopPaymentHover());
            jbTopPromo.addMouseListener(new btnTopPromo());
            jbTopHall.addMouseListener(new btnTopHallHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only promotion will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromoHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //CRUD panel 
            CRUDPanel.add(CRUDPanelPromo);
            CRUDPanel.setVisible(true);

            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelStaff.setVisible(false);
            CRUDPanelPayment.setVisible(false);
            CRUDPanelPromo.setVisible(true);
            CRUDPanelHall.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelReport.setVisible(false);
            CRUDPanelSearch.setVisible(false);

            CRUDPanelPromo.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelPromo.setOpaque(false);
            CRUDPanelPromo.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            jbPromoAdd.setForeground(Color.WHITE);
            jbPromoAdd.setFocusPainted(false);
            jbPromoAdd.setContentAreaFilled(false);
            jbPromoAdd.setOpaque(false);
            jbPromoAdd.setEnabled(false);

            jbPromoRetrieve.setForeground(Color.WHITE);
            jbPromoRetrieve.setFocusPainted(false);
            jbPromoRetrieve.setContentAreaFilled(false);
            jbPromoRetrieve.setOpaque(false);
            jbPromoRetrieve.setEnabled(true);
            jbPromoRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            jbPromoUpdate.setForeground(Color.WHITE);
            jbPromoUpdate.setFocusPainted(false);
            jbPromoUpdate.setContentAreaFilled(false);
            jbPromoUpdate.setOpaque(false);
            jbPromoUpdate.setEnabled(true);
            jbPromoUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            jbPromoDelete.setForeground(Color.WHITE);
            jbPromoDelete.setFocusPainted(false);
            jbPromoDelete.setContentAreaFilled(false);
            jbPromoDelete.setOpaque(false);
            jbPromoDelete.setEnabled(true);
            jbPromoDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

            obj.setOpaque(false);
            menu.setVisible(false);

            jbPromoAdd.setEnabled(true);
            jbPromoRetrieve.setEnabled(false);
            jbPromoUpdate.setEnabled(true);
            jbPromoDelete.setEnabled(true);

            jbPromoAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbPromoRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbPromoUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbPromoDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Top Panel and Main Menu Walk-In Button and Left Panel Promotion Button Action (CRUD Buttons)
    private class UpdatePromoListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            UpdatePromotion obj = new UpdatePromotion();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbPromoAdd.setEnabled(true);
            jbPromoRetrieve.setEnabled(true);
            jbPromoUpdate.setEnabled(false);
            jbPromoDelete.setEnabled(true);

            jbPromoAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbPromoRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbPromoUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbPromoDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Top Panel and Main Menu Promotion Button and Left Panel Promotion Button Action (CRUD Buttons)
    private class DeletePromoListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            DeletePromotion obj = new DeletePromotion();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbPromoAdd.setEnabled(true);
            jbPromoRetrieve.setEnabled(true);
            jbPromoUpdate.setEnabled(true);
            jbPromoDelete.setEnabled(false);

            jbPromoAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbPromoRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbPromoUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbPromoDelete.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    //Top Panel and Main Menu Hall Button and Left Panel Hall Button Action (CRUD Buttons)
    private class AddHallListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            AddHall obj = new AddHall();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            //top button menu
            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopStaff.setVisible(true);
            jbTopPayment.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopHall.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopStaff.addMouseListener(new btnTopStaffHover());
            jbTopPayment.addMouseListener(new btnTopPaymentHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopHall.addMouseListener(new btnTopHall());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only hall will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopHall.setIcon(new ImageIcon((iconTopHallHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //CRUD panel 
            CRUDPanel.add(CRUDPanelHall);
            CRUDPanel.setVisible(true);
            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelStaff.setVisible(false);
            CRUDPanelPayment.setVisible(false);
            CRUDPanelPromo.setVisible(false);
            CRUDPanelHall.setVisible(true);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelReport.setVisible(false);
            CRUDPanelSearch.setVisible(false);

            CRUDPanelHall.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelHall.setOpaque(false);
            CRUDPanelHall.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            jbHallAdd.setContentAreaFilled(false);
            jbHallAdd.setFocusPainted(false);
            jbHallAdd.setOpaque(false);
            jbHallAdd.setForeground(Color.WHITE);
            jbHallAdd.setEnabled(false);
            jbHallAdd.addActionListener(new AddHallListener());

            jbHallRetrieve.setForeground(Color.WHITE);
            jbHallRetrieve.setContentAreaFilled(false);
            jbHallRetrieve.setFocusPainted(false);
            jbHallRetrieve.setOpaque(false);
            jbHallRetrieve.setEnabled(true);
            jbHallRetrieve.addActionListener(new RetrieveHallListener());
            jbHallRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            jbHallUpdate.setForeground(Color.WHITE);
            jbHallUpdate.setContentAreaFilled(false);
            jbHallUpdate.setFocusPainted(false);
            jbHallUpdate.setOpaque(false);
            jbHallUpdate.setEnabled(true);
            jbHallUpdate.addActionListener(new UpdateHallListener());
            jbHallUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            jbHallDelete.setForeground(Color.WHITE);
            jbHallDelete.setContentAreaFilled(false);
            jbHallDelete.setFocusPainted(false);
            jbHallDelete.setOpaque(false);
            jbHallDelete.setEnabled(true);
            jbHallDelete.addActionListener(new DeleteHallListener());
            jbHallDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Retrieve Hall Button Action (CRUD Buttons)
    private class RetrieveHallListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            RetrieveHall obj = new RetrieveHall();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbHallAdd.setEnabled(true);
            jbHallRetrieve.setEnabled(false);
            jbHallUpdate.setEnabled(true);
            jbHallDelete.setEnabled(true);

            jbHallAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbHallRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbHallUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbHallDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Update Hall Button Action (CRUD Buttons)
    private class UpdateHallListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            UpdateHall obj = new UpdateHall();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbHallAdd.setEnabled(true);
            jbHallRetrieve.setEnabled(true);
            jbHallUpdate.setEnabled(false);
            jbHallDelete.setEnabled(true);

            jbHallAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbHallRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbHallUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbHallDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Delete Hall Button Action (CRUD Buttons)
    private class DeleteHallListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            DeleteHall obj = new DeleteHall();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbHallAdd.setEnabled(true);
            jbHallRetrieve.setEnabled(true);
            jbHallUpdate.setEnabled(true);
            jbHallDelete.setEnabled(false);

            jbHallAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbHallRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbHallUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbHallDelete.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    //Top Panel and Main Menu Movie Button and Left Panel Create Movie Button Action 
    private class AddMovieListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            AddMovie obj = new AddMovie();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();
            center.setBorder(new EmptyBorder(0, 0, 0, 0));
            obj.setOpaque(false);
            menu.setVisible(false);

            //top button menu
            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopStaff.setVisible(true);
            jbTopPayment.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopHall.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopStaff.addMouseListener(new btnTopStaffHover());
            jbTopPayment.addMouseListener(new btnTopPaymentHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopHall.addMouseListener(new btnTopHallHover());
            jbTopMovie.addMouseListener(new btnTopMovie());

            //set icon image (only staff will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovieHover.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //CRUD panel 
            CRUDPanel.add(CRUDPanelMovie);
            CRUDPanel.setVisible(true);
            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelStaff.setVisible(false);
            CRUDPanelPayment.setVisible(false);
            CRUDPanelPromo.setVisible(false);
            CRUDPanelHall.setVisible(false);
            CRUDPanelMovie.setVisible(true);
            CRUDPanelReport.setVisible(false);
            CRUDPanelSearch.setVisible(false);

            CRUDPanelMovie.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelMovie.setOpaque(false);
            CRUDPanelMovie.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            jbMovieAdd.setContentAreaFilled(false);
            jbMovieAdd.setFocusPainted(false);
            jbMovieAdd.setOpaque(false);
            jbMovieAdd.setForeground(Color.WHITE);
            jbMovieAdd.setEnabled(false);
            jbMovieAdd.addActionListener(new AddMovieListener());

            jbMovieRetrieve.setForeground(Color.WHITE);
            jbMovieRetrieve.setContentAreaFilled(false);
            jbMovieRetrieve.setFocusPainted(false);
            jbMovieRetrieve.setOpaque(false);
            jbMovieRetrieve.setEnabled(true);
            jbMovieRetrieve.addActionListener(new RetrieveMovieListener());
            jbMovieRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            jbMovieUpdate.setForeground(Color.WHITE);
            jbMovieUpdate.setContentAreaFilled(false);
            jbMovieUpdate.setFocusPainted(false);
            jbMovieUpdate.setOpaque(false);
            jbMovieUpdate.setEnabled(true);
            jbMovieUpdate.addActionListener(new UpdateMovieListener());
            jbMovieUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            jbMovieDelete.setForeground(Color.WHITE);
            jbMovieDelete.setContentAreaFilled(false);
            jbMovieUpdate.setFocusPainted(false);
            jbMovieDelete.setOpaque(false);
            jbMovieDelete.setEnabled(true);
            jbMovieDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbMovieDelete.addActionListener(new DeleteMovieListener());

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

            jbMovieAdd.setEnabled(true);
            jbMovieRetrieve.setEnabled(false);
            jbMovieUpdate.setEnabled(true);
            jbMovieDelete.setEnabled(true);

            jbMovieAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbMovieRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbMovieUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbMovieDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Update Movie Button Action 
    private class UpdateMovieListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            UpdateMovie obj = new UpdateMovie();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();

            obj.setOpaque(false);
            menu.setVisible(false);

            jbMovieAdd.setEnabled(true);
            jbMovieRetrieve.setEnabled(true);
            jbMovieUpdate.setEnabled(false);
            jbMovieDelete.setEnabled(true);

            jbMovieAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbMovieRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbMovieUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            jbMovieDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    //Left Panel Delete Movie Button Action 
    private class DeleteMovieListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            DeleteMovie obj = new DeleteMovie();

            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();
            center.setBorder(new EmptyBorder(0, 0, 0, 0));
            obj.setOpaque(false);
            menu.setVisible(false);

            jbMovieAdd.setEnabled(true);
            jbMovieRetrieve.setEnabled(true);
            jbMovieUpdate.setEnabled(true);
            jbMovieDelete.setEnabled(false);

            jbMovieAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbMovieRetrieve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbMovieUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            jbMovieDelete.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    //Left Panel View Report Button Action 
    private class ViewReportListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Reports obj = new Reports();
            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();
            center.setBorder(new EmptyBorder(0, 0, 0, 0));
            obj.setOpaque(false);
            menu.setVisible(false);

            //top button menu
            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopStaff.setVisible(true);
            jbTopPayment.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopHall.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopStaff.addMouseListener(new btnTopStaffHover());
            jbTopPayment.addMouseListener(new btnTopPaymentHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopHall.addMouseListener(new btnTopHallHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only staff will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //CRUD panel 
            CRUDPanel.add(CRUDPanelReport);
            CRUDPanel.setVisible(true);
            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelStaff.setVisible(false);
            CRUDPanelPayment.setVisible(false);
            CRUDPanelPromo.setVisible(false);
            CRUDPanelHall.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelReport.setVisible(true);
            CRUDPanelSearch.setVisible(false);

            CRUDPanelReport.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelReport.setOpaque(false);
            CRUDPanelReport.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            jbReportView.setContentAreaFilled(false);
            jbReportView.setFocusPainted(false);
            jbReportView.setOpaque(false);
            jbReportView.setForeground(Color.WHITE);
            jbReportView.setEnabled(false);
        }
    }

    //Left Panel Search Information Button Action 
    private class SearchInfoListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Search obj = new Search();
            center.removeAll();
            center.add(obj);
            center.revalidate();
            center.repaint();
            center.setBorder(new EmptyBorder(0, 0, 0, 0));
            obj.setOpaque(false);
            menu.setVisible(false);

            //top button menu
            jbTopCus.setVisible(true);
            jbTopWalkIn.setVisible(true);
            jbTopReserve.setVisible(true);
            jbTopStaff.setVisible(true);
            jbTopPayment.setVisible(true);
            jbTopPromo.setVisible(true);
            jbTopHall.setVisible(true);
            jbTopMovie.setVisible(true);

            //top button menu mouse listener
            jbHome.addMouseListener(new btnHomeHover());
            jbTopCus.addMouseListener(new btnTopCusHover());
            jbTopWalkIn.addMouseListener(new btnTopWalkinHover());
            jbTopReserve.addMouseListener(new btnTopReserveHover());
            jbTopStaff.addMouseListener(new btnTopStaffHover());
            jbTopPayment.addMouseListener(new btnTopPaymentHover());
            jbTopPromo.addMouseListener(new btnTopPromoHover());
            jbTopHall.addMouseListener(new btnTopHallHover());
            jbTopMovie.addMouseListener(new btnTopMovieHover());

            //set icon image (only staff will hover)
            jbHome.setIcon(new ImageIcon((iconTopHome.getImage()).getScaledInstance(80, 63, java.awt.Image.SCALE_SMOOTH)));
            jbTopReserve.setIcon(new ImageIcon((iconTopReserve.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopWalkIn.setIcon(new ImageIcon((iconTopWalkIn.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopCus.setIcon(new ImageIcon((iconTopCus.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopStaff.setIcon(new ImageIcon((iconTopStaff.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPayment.setIcon(new ImageIcon((iconTopPayment.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopPromo.setIcon(new ImageIcon((iconTopPromo.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopHall.setIcon(new ImageIcon((iconTopHall.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));
            jbTopMovie.setIcon(new ImageIcon((iconTopMovie.getImage()).getScaledInstance(128, 66, java.awt.Image.SCALE_SMOOTH)));

            //CRUD panel 
            CRUDPanel.add(CRUDPanelSearch);
            CRUDPanel.setVisible(true);
            CRUDPanelWalkin.setVisible(false);
            CRUDPanelReserve.setVisible(false);
            CRUDPanelCus.setVisible(false);
            CRUDPanelStaff.setVisible(false);
            CRUDPanelPayment.setVisible(false);
            CRUDPanelPromo.setVisible(false);
            CRUDPanelHall.setVisible(false);
            CRUDPanelMovie.setVisible(false);
            CRUDPanelReport.setVisible(false);
            CRUDPanelSearch.setVisible(true);

            CRUDPanelSearch.setBorder(new EmptyBorder(10, 0, 20, 5));
            CRUDPanelSearch.setOpaque(false);
            CRUDPanelSearch.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            jbSearchInfo.setContentAreaFilled(false);
            jbSearchInfo.setFocusPainted(false);
            jbSearchInfo.setOpaque(false);
            jbSearchInfo.setForeground(Color.WHITE);
            jbSearchInfo.setEnabled(false);
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
            jbTopStaff.setVisible(false);
            jbTopPayment.setVisible(false);
            jbTopPromo.setVisible(false);
            jbTopHall.setVisible(false);
            jbTopMovie.setVisible(false);
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
