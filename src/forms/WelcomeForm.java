/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package forms;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.Timer;
import utils.Theme;

/**
 * Welcome / landing page of the USIU-Africa Student Counselling System.
 * Undecorated (no title bar), rounded and draggable. Layout lives in the Design tab.
 *
 * @author Student Counselling System team
 */
public class WelcomeForm extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(WelcomeForm.class.getName());

    private static final String[] AFFIRMATIONS = {
        "It is okay to ask for help. That is a sign of strength.",
        "Your mental health matters as much as your grades.",
        "Small steps every day lead to big change.",
        "You are not alone on this journey.",
        "Every conversation with a counsellor is confidential."
    };

    private java.awt.Point dragOffset;
    private int affirmationIndex;

    /**
     * Creates new form WelcomeForm
     */
    public WelcomeForm() {
        initComponents();
        applyTheme();
        setupWindow();
    }

    /** Fonts, logo and small touches the property sheet can't express. */
    private void applyTheme() {
        // fonts (Theme picks the best installed family)
        lblGreeting.setFont(Theme.font(Font.BOLD, 14f));
        lblTitle1.setFont(Theme.font(Font.BOLD, 42f));
        lblTitle2.setFont(Theme.font(Font.BOLD, 42f));
        lblIntro.setFont(Theme.font(Font.PLAIN, 16f));
        lblFacts.setFont(Theme.font(Font.PLAIN, 14f));
        lblAffTitle.setFont(Theme.tracked(Theme.font(Font.BOLD, 10.5f), 0.2f));
        lblAffirmation.setFont(Theme.font(Font.ITALIC, 15f));
        lblContinueAs.setFont(Theme.tracked(Theme.font(Font.BOLD, 12f), 0.2f));
        lblRegister.setFont(Theme.font(Font.PLAIN, 13f));
        lblCentreTitle.setFont(Theme.font(Font.BOLD, 20f));
        lblCentreSub.setFont(Theme.tracked(Theme.font(Font.PLAIN, 11f), 0.14f));
        lblClock.setFont(Theme.font(Font.PLAIN, 13f));
        lblCopyright.setFont(Theme.font(Font.PLAIN, 12f));
        lblContact.setFont(Theme.font(Font.PLAIN, 12f));
        btnGetStarted.setFont(Theme.font(Font.BOLD, 15f));
        btnBook.setFont(Theme.font(Font.BOLD, 15f));
        btnMinimise.setFont(Theme.font(Font.BOLD, 22f));
        btnClose.setFont(Theme.font(Font.BOLD, 22f));
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{pnlStudentTitle, pnlCounsellorTitle, pnlAdminTitle}) {
            l.setFont(Theme.font(Font.BOLD, 19f));
        }
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{pnlStudentDesc, pnlCounsellorDesc, pnlAdminDesc}) {
            l.setFont(Theme.font(Font.PLAIN, 13f));
        }

        // no padding so the round window buttons show their symbol
        btnMinimise.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnClose.setMargin(new java.awt.Insets(0, 0, 0, 0));

        // hand cursor on clickable items
        for (java.awt.Component c : new java.awt.Component[]{btnGetStarted, btnBook, btnMinimise, btnClose, lblRegister}) {
            c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        // dynamic text
        lblGreeting.setText(greeting() + "  |  your wellbeing matters");
        lblCopyright.setText("\u00A9 " + Calendar.getInstance().get(Calendar.YEAR)
                + " USIU-Africa  |  Student Counselling System  |  OOAD Project");

        // bundled USIU-Africa logo
        javax.swing.ImageIcon logo = loadLogo(64);
        if (logo != null) {
            lblLogo.setText(null);
            lblLogo.setIcon(logo);
        }
    }

    /** Window behaviour: size, rounded corners, drag, clock, rotating affirmations, Esc, fade-in. */
    private void setupWindow() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1200, screen.width - 40), Math.min(720, screen.height - 60));
        setLocationRelativeTo(null);

        // rounded corners
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                try { setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30)); }
                catch (UnsupportedOperationException ignored) { /* platform has no shaped windows */ }
            }
        });

        // drag the undecorated window from the background
        MouseAdapter drag = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragOffset = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) {
                if (dragOffset != null) { setLocation(e.getXOnScreen() - dragOffset.x, e.getYOnScreen() - dragOffset.y); }
            }
        };
        background.addMouseListener(drag);
        background.addMouseMotionListener(drag);

        // live clock
        SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy   HH:mm:ss");
        lblClock.setText(fmt.format(new Date()));
        new Timer(1000, e -> lblClock.setText(fmt.format(new Date()))).start();

        // rotating affirmation
        lblAffirmation.setText(AFFIRMATIONS[0]);
        new Timer(5000, e -> {
            affirmationIndex = (affirmationIndex + 1) % AFFIRMATIONS.length;
            lblAffirmation.setText(AFFIRMATIONS[affirmationIndex]);
        }).start();

        // Esc closes the app
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"), "exit");
        getRootPane().getActionMap().put("exit", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { System.exit(0); }
        });

        // fade in
        try {
            setOpacity(0f);
            Timer t = new Timer(15, null);
            t.addActionListener(e -> {
                float o = Math.min(1f, getOpacity() + 0.06f);
                try { setOpacity(o); } catch (Exception ex) { t.stop(); }
                if (o >= 1f) { t.stop(); }
            });
            t.start();
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.FINE, "Window translucency not supported", ex);
        }
    }

    /** Loads /images/usiu.jpeg (inside the project) and rounds its corners. */
    private javax.swing.ImageIcon loadLogo(int height) {
        try {
            java.awt.image.BufferedImage src = javax.imageio.ImageIO.read(getClass().getResource("/images/usiu.jpeg"));
            int w = src.getWidth() * height / src.getHeight();
            java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(w, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = out.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setClip(new RoundRectangle2D.Float(0, 0, w, height, 16, 16));
            g.drawImage(src, 0, 0, w, height, null);
            g.dispose();
            return new javax.swing.ImageIcon(out);
        } catch (java.io.IOException | RuntimeException ex) {
            logger.log(java.util.logging.Level.WARNING, "Logo not found: put usiu.jpeg in the 'images' package", ex);
            return null;
        }
    }

    private static String greeting() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening";
    }

    /** Placeholder until the login forms exist. */
    private void openPortal(String role) {
        // TODO: new LoginForm(role).setVisible(true); dispose();
        javax.swing.JOptionPane.showMessageDialog(this, role + " portal: the login form is the next screen.");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        background = new utils.AnimatedBackground();
        topBar = new javax.swing.JPanel();
        brandPanel = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        namesPanel = new javax.swing.JPanel();
        lblCentreTitle = new javax.swing.JLabel();
        lblCentreSub = new javax.swing.JLabel();
        rightPanel = new javax.swing.JPanel();
        lblClock = new javax.swing.JLabel();
        btnMinimise = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();
        heroPanel = new javax.swing.JPanel();
        lblGreeting = new javax.swing.JLabel();
        lblTitle1 = new javax.swing.JLabel();
        lblTitle2 = new javax.swing.JLabel();
        lblIntro = new javax.swing.JLabel();
        btnGetStarted = new javax.swing.JButton();
        btnBook = new javax.swing.JButton();
        lblFacts = new javax.swing.JLabel();
        lblAffTitle = new javax.swing.JLabel();
        lblAffirmation = new javax.swing.JLabel();
        roleColumn = new javax.swing.JPanel();
        lblContinueAs = new javax.swing.JLabel();
        pnlStudent = new utils.GlassPanel(utils.Theme.TEAL, utils.GlassPanel.Icon.STUDENT);
        pnlStudentTitle = new javax.swing.JLabel();
        pnlStudentDesc = new javax.swing.JLabel();
        pnlCounsellor = new utils.GlassPanel(utils.Theme.GOLD, utils.GlassPanel.Icon.COUNSELLOR);
        pnlCounsellorTitle = new javax.swing.JLabel();
        pnlCounsellorDesc = new javax.swing.JLabel();
        pnlAdmin = new utils.GlassPanel(utils.Theme.LAVENDER, utils.GlassPanel.Icon.ADMIN);
        pnlAdminTitle = new javax.swing.JLabel();
        pnlAdminDesc = new javax.swing.JLabel();
        lblRegister = new javax.swing.JLabel();
        footerPanel = new javax.swing.JPanel();
        lblCopyright = new javax.swing.JLabel();
        lblContact = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("USIU-Africa | Student Counselling System");
        setUndecorated(true);

        topBar.setOpaque(false);

        brandPanel.setOpaque(false);

        lblLogo.setForeground(new java.awt.Color(255, 255, 255));
        lblLogo.setText("USIU-Africa");

        namesPanel.setOpaque(false);

        lblCentreTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblCentreTitle.setText("Student Counselling Centre");

        lblCentreSub.setForeground(new java.awt.Color(180, 195, 216));
        lblCentreSub.setText("FREIDA BROWN STUDENT CENTER  |  2ND FLOOR");

        javax.swing.GroupLayout namesPanelLayout = new javax.swing.GroupLayout(namesPanel);
        namesPanel.setLayout(namesPanelLayout);
        namesPanelLayout.setHorizontalGroup(
            namesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblCentreTitle)
            .addComponent(lblCentreSub)
        );
        namesPanelLayout.setVerticalGroup(
            namesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namesPanelLayout.createSequentialGroup()
                .addComponent(lblCentreTitle)
                .addGap(2, 2, 2)
                .addComponent(lblCentreSub))
        );

        javax.swing.GroupLayout brandPanelLayout = new javax.swing.GroupLayout(brandPanel);
        brandPanel.setLayout(brandPanelLayout);
        brandPanelLayout.setHorizontalGroup(
            brandPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(brandPanelLayout.createSequentialGroup()
                .addComponent(lblLogo)
                .addGap(16, 16, 16)
                .addComponent(namesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        brandPanelLayout.setVerticalGroup(
            brandPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(lblLogo)
            .addComponent(namesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        rightPanel.setOpaque(false);

        lblClock.setForeground(new java.awt.Color(180, 195, 216));
        lblClock.setText("Wed, 11 Nov 2026   10:00:00");

        btnMinimise.setBorderPainted(false);
        btnMinimise.setContentAreaFilled(false);
        btnMinimise.setFocusPainted(false);
        btnMinimise.setForeground(new java.awt.Color(255, 255, 255));
        btnMinimise.setText("–");
        btnMinimise.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMinimiseActionPerformed(evt);
            }
        });

        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setForeground(new java.awt.Color(255, 255, 255));
        btnClose.setText("×");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addComponent(lblClock)
                .addGap(18, 18, 18)
                .addComponent(btnMinimise, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addComponent(lblClock)
            .addComponent(btnMinimise, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout topBarLayout = new javax.swing.GroupLayout(topBar);
        topBar.setLayout(topBarLayout);
        topBarLayout.setHorizontalGroup(
            topBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topBarLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(brandPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );
        topBarLayout.setVerticalGroup(
            topBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topBarLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(topBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(brandPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        centerPanel.setOpaque(false);

        heroPanel.setOpaque(false);

        lblGreeting.setForeground(new java.awt.Color(46, 196, 182));
        lblGreeting.setText("Welcome  |  your wellbeing matters");

        lblTitle1.setForeground(new java.awt.Color(255, 255, 255));
        lblTitle1.setText("Student Counselling");

        lblTitle2.setForeground(new java.awt.Color(255, 215, 122));
        lblTitle2.setText("System at USIU-Africa");

        lblIntro.setForeground(new java.awt.Color(180, 195, 216));
        lblIntro.setText("<html>A safe, confidential space where students can<br>book sessions, share how they feel and connect<br>with caring, professional counsellors.</html>");

        btnGetStarted.setBackground(new java.awt.Color(245, 179, 53));
        btnGetStarted.setBorderPainted(false);
        btnGetStarted.setContentAreaFilled(false);
        btnGetStarted.setFocusPainted(false);
        btnGetStarted.setForeground(new java.awt.Color(6, 22, 43));
        btnGetStarted.setOpaque(true);
        btnGetStarted.setText("Get Started");
        btnGetStarted.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGetStartedActionPerformed(evt);
            }
        });

        btnBook.setBackground(new java.awt.Color(26, 75, 132));
        btnBook.setForeground(new java.awt.Color(255, 255, 255));
        btnBook.setText("Book a Session");
        btnBook.setBorderPainted(false);
        btnBook.setContentAreaFilled(false);
        btnBook.setFocusPainted(false);
        btnBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookActionPerformed(evt);
            }
        });

        lblFacts.setForeground(new java.awt.Color(255, 215, 122));
        lblFacts.setText("Confidential  |  Free of charge  |  Mon - Fri, 8am - 5pm");

        lblAffTitle.setForeground(new java.awt.Color(245, 179, 53));
        lblAffTitle.setText("DAILY AFFIRMATION");

        lblAffirmation.setForeground(new java.awt.Color(180, 195, 216));
        lblAffirmation.setText("It is okay to ask for help. That is a sign of strength.");

        javax.swing.GroupLayout heroPanelLayout = new javax.swing.GroupLayout(heroPanel);
        heroPanel.setLayout(heroPanelLayout);
        heroPanelLayout.setHorizontalGroup(
            heroPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblGreeting)
            .addComponent(lblTitle1)
            .addComponent(lblTitle2)
            .addComponent(lblIntro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(heroPanelLayout.createSequentialGroup()
                .addComponent(btnGetStarted, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(btnBook, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(lblFacts)
            .addComponent(lblAffTitle)
            .addComponent(lblAffirmation, javax.swing.GroupLayout.PREFERRED_SIZE, 470, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        heroPanelLayout.setVerticalGroup(
            heroPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(heroPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblGreeting)
                .addGap(18, 18, 18)
                .addComponent(lblTitle1)
                .addGap(0, 0, 0)
                .addComponent(lblTitle2)
                .addGap(14, 14, 14)
                .addComponent(lblIntro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(heroPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btnGetStarted, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBook, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addComponent(lblFacts)
                .addGap(26, 26, 26)
                .addComponent(lblAffTitle)
                .addGap(6, 6, 6)
                .addComponent(lblAffirmation)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        roleColumn.setOpaque(false);

        lblContinueAs.setForeground(new java.awt.Color(245, 179, 53));
        lblContinueAs.setText("CONTINUE AS");

        pnlStudent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlStudentMouseClicked(evt);
            }
        });

        pnlStudentTitle.setForeground(new java.awt.Color(255, 255, 255));
        pnlStudentTitle.setText("Student");

        pnlStudentDesc.setForeground(new java.awt.Color(180, 195, 216));
        pnlStudentDesc.setText("<html>Book appointments, track your mood and<br>access wellbeing resources.</html>");

        javax.swing.GroupLayout pnlStudentLayout = new javax.swing.GroupLayout(pnlStudent);
        pnlStudent.setLayout(pnlStudentLayout);
        pnlStudentLayout.setHorizontalGroup(
            pnlStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStudentLayout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addGroup(pnlStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlStudentTitle)
                    .addComponent(pnlStudentDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(70, 117, Short.MAX_VALUE))
        );
        pnlStudentLayout.setVerticalGroup(
            pnlStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStudentLayout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addComponent(pnlStudentTitle)
                .addGap(3, 3, 3)
                .addComponent(pnlStudentDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pnlCounsellor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlCounsellorMouseClicked(evt);
            }
        });

        pnlCounsellorTitle.setForeground(new java.awt.Color(255, 255, 255));
        pnlCounsellorTitle.setText("Counsellor");

        pnlCounsellorDesc.setForeground(new java.awt.Color(180, 195, 216));
        pnlCounsellorDesc.setText("<html>Manage your schedule, sessions and<br>confidential student notes.</html>");

        javax.swing.GroupLayout pnlCounsellorLayout = new javax.swing.GroupLayout(pnlCounsellor);
        pnlCounsellor.setLayout(pnlCounsellorLayout);
        pnlCounsellorLayout.setHorizontalGroup(
            pnlCounsellorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCounsellorLayout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addGroup(pnlCounsellorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlCounsellorTitle)
                    .addComponent(pnlCounsellorDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(70, 70, Short.MAX_VALUE))
        );
        pnlCounsellorLayout.setVerticalGroup(
            pnlCounsellorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCounsellorLayout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addComponent(pnlCounsellorTitle)
                .addGap(3, 3, 3)
                .addComponent(pnlCounsellorDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pnlAdmin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlAdminMouseClicked(evt);
            }
        });

        pnlAdminTitle.setForeground(new java.awt.Color(255, 255, 255));
        pnlAdminTitle.setText("Administrator");

        pnlAdminDesc.setForeground(new java.awt.Color(180, 195, 216));
        pnlAdminDesc.setText("<html>Manage users, view reports and<br>configure the system.</html>");

        javax.swing.GroupLayout pnlAdminLayout = new javax.swing.GroupLayout(pnlAdmin);
        pnlAdmin.setLayout(pnlAdminLayout);
        pnlAdminLayout.setHorizontalGroup(
            pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdminLayout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addGroup(pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlAdminTitle)
                    .addComponent(pnlAdminDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(70, 70, Short.MAX_VALUE))
        );
        pnlAdminLayout.setVerticalGroup(
            pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdminLayout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addComponent(pnlAdminTitle)
                .addGap(3, 3, 3)
                .addComponent(pnlAdminDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        lblRegister.setForeground(new java.awt.Color(255, 215, 122));
        lblRegister.setText("New student?  Create your account");
        lblRegister.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblRegisterMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout roleColumnLayout = new javax.swing.GroupLayout(roleColumn);
        roleColumn.setLayout(roleColumnLayout);
        roleColumnLayout.setHorizontalGroup(
            roleColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlStudent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlCounsellor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(roleColumnLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(roleColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblContinueAs)
                    .addComponent(lblRegister)))
        );
        roleColumnLayout.setVerticalGroup(
            roleColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roleColumnLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblContinueAs)
                .addGap(8, 8, 8)
                .addComponent(pnlStudent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(pnlCounsellor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(pnlAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblRegister)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout centerPanelLayout = new javax.swing.GroupLayout(centerPanel);
        centerPanel.setLayout(centerPanelLayout);
        centerPanelLayout.setHorizontalGroup(
            centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(centerPanelLayout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addComponent(heroPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(40, 40, 40)
                .addComponent(roleColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56))
        );
        centerPanelLayout.setVerticalGroup(
            centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(heroPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(roleColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        footerPanel.setOpaque(false);

        lblCopyright.setForeground(new java.awt.Color(180, 195, 216));
        lblCopyright.setText("(c) 2026 USIU-Africa  |  Student Counselling System  |  OOAD Project");

        lblContact.setForeground(new java.awt.Color(180, 195, 216));
        lblContact.setText("counsel@usiu.ac.ke   |   Esc to exit");

        javax.swing.GroupLayout footerPanelLayout = new javax.swing.GroupLayout(footerPanel);
        footerPanel.setLayout(footerPanelLayout);
        footerPanelLayout.setHorizontalGroup(
            footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(footerPanelLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(lblCopyright)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblContact)
                .addGap(36, 36, 36))
        );
        footerPanelLayout.setVerticalGroup(
            footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(footerPanelLayout.createSequentialGroup()
                .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCopyright)
                    .addComponent(lblContact))
                .addGap(16, 16, 16))
        );

        javax.swing.GroupLayout backgroundLayout = new javax.swing.GroupLayout(background);
        background.setLayout(backgroundLayout);
        backgroundLayout.setHorizontalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(centerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(footerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        backgroundLayout.setVerticalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addComponent(topBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(centerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(footerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGetStartedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetStartedActionPerformed
        openPortal("Student");   // quick start: same as the Student card
    }//GEN-LAST:event_btnGetStartedActionPerformed

    private void btnBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookActionPerformed
        javax.swing.JOptionPane.showMessageDialog(this, "Appointment booking is the next module.");
    }//GEN-LAST:event_btnBookActionPerformed

    private void btnMinimiseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMinimiseActionPerformed
        setState(java.awt.Frame.ICONIFIED);
    }//GEN-LAST:event_btnMinimiseActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void pnlStudentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlStudentMouseClicked
        openPortal("Student");
    }//GEN-LAST:event_pnlStudentMouseClicked

    private void pnlCounsellorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlCounsellorMouseClicked
        openPortal("Counsellor");
    }//GEN-LAST:event_pnlCounsellorMouseClicked

    private void pnlAdminMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlAdminMouseClicked
        openPortal("Administrator");
    }//GEN-LAST:event_pnlAdminMouseClicked

    private void lblRegisterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblRegisterMouseClicked
        javax.swing.JOptionPane.showMessageDialog(this, "Student registration form is coming next.");
    }//GEN-LAST:event_lblRegisterMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new WelcomeForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private utils.AnimatedBackground background;
    private javax.swing.JPanel brandPanel;
    private javax.swing.JButton btnBook;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnGetStarted;
    private javax.swing.JButton btnMinimise;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JPanel heroPanel;
    private javax.swing.JLabel lblAffTitle;
    private javax.swing.JLabel lblAffirmation;
    private javax.swing.JLabel lblCentreSub;
    private javax.swing.JLabel lblCentreTitle;
    private javax.swing.JLabel lblClock;
    private javax.swing.JLabel lblContact;
    private javax.swing.JLabel lblContinueAs;
    private javax.swing.JLabel lblCopyright;
    private javax.swing.JLabel lblFacts;
    private javax.swing.JLabel lblGreeting;
    private javax.swing.JLabel lblIntro;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblRegister;
    private javax.swing.JLabel lblTitle1;
    private javax.swing.JLabel lblTitle2;
    private javax.swing.JPanel namesPanel;
    private utils.GlassPanel pnlAdmin;
    private javax.swing.JLabel pnlAdminDesc;
    private javax.swing.JLabel pnlAdminTitle;
    private utils.GlassPanel pnlCounsellor;
    private javax.swing.JLabel pnlCounsellorDesc;
    private javax.swing.JLabel pnlCounsellorTitle;
    private utils.GlassPanel pnlStudent;
    private javax.swing.JLabel pnlStudentDesc;
    private javax.swing.JLabel pnlStudentTitle;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel roleColumn;
    private javax.swing.JPanel topBar;
    // End of variables declaration//GEN-END:variables
}
