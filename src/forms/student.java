/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package forms;

import Student.StudentAppointment;
import Student.StudentAppointmentStore;
import Student.StudentCounsellor;
import Student.StudentDatabaseException;
import models.User;
import studentcounsellingsystem.MainFrame;
import utils.Theme;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Student dashboard.
 *
 * The NetBeans-generated initComponents() below is kept only so the .form
 * file stays valid. At runtime buildModernDashboard() discards those
 * components and builds the real interface in code, so edits to this class
 * will NOT appear in the Design tab.
 *
 * Layout:  [collapsible sidebar] | [dashboard content + slide-out panels]
 * Slide-out panels (booking / reschedule / profile / all appointments) share
 * one overlay; only one is open at a time.
 *
 * @author user
 */
public class student extends javax.swing.JPanel {

    // ------------------------------------------------------------------
    // Look and feel (reuses utils.Theme so it matches the Welcome page)
    // ------------------------------------------------------------------
    private static final Color INK = Theme.TEXT;
    private static final Color MUTED = Theme.TEXT_MUTED;
    private static final Color GLASS_FILL = new Color(255, 255, 255, 20);
    private static final Color GLASS_EDGE = new Color(255, 255, 255, 44);
    private static final Color PANEL_BG
            = Theme.mix(Theme.NAVY_DEEP, Theme.NAVY, 0.55f);
    private static final Color FIELD_BG
            = Theme.mix(Theme.NAVY_DEEP, Color.WHITE, 0.08f);

    private static final DateTimeFormatter DATE_FMT
            = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT
            = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter LONG_DATE_FMT
            = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

    private static final int SIDEBAR_EXPANDED = 236;
    private static final int SIDEBAR_COLLAPSED = 76;
    private static final int SLIDE_MS = 240;
    private static final int SIDEBAR_MS = 220;

    private enum PanelKind { NONE, BOOKING, PROFILE, APPOINTMENTS, FEEDBACK }

    // ------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------
    private JLabel upcomingValue;
    private JLabel pendingValue;
    private JLabel completedValue;
    private JLabel greetingLabel;
    private JLabel subtitleLabel;
    private String studentRegNo = "";   // Student ID (registration number)
    private AppointmentTablePanel dashboardTable;
    private DatePicker dashboardCalendar;

    // sidebar
    private SidebarPanel navSidebar;
    private NavButton[] navButtons;
    private boolean sidebarExpanded = true;
    private int sidebarWidth = SIDEBAR_EXPANDED;
    private Timer sidebarTimer;

    // overlay
    private JPanel host;
    private JPanel backdrop;
    private JPanel scrim;
    private JPanel slidePanel;
    private JLabel slideTitle;
    private FlatButton slideAction;   // pencil (profile only)
    private JPanel slideBody;
    private ToastLabel toast;
    private Timer toastTimer;
    private Timer slideTimer;
    private float slideProgress = 0f;
    private float slideFrom = 0f;
    private float slideTarget = 0f;
    private long slideStart;
    private int slideWidth = 420;
    private PanelKind currentPanel = PanelKind.NONE;

    // "My Appointments" panel
    private AppointmentTablePanel historyTable;
    private JLabel historyCount;
    private FlatButton[] historyFilters;
    private int historyFilter = 0; // 0 all, 1 active, 2 completed, 3 cancelled

    /**
     * Creates new form student
     */
    public student() {
        initComponents();
        buildModernDashboard();

        // TESTING ONLY: uncomment to preload one appointment of every status
        // for the demo student (id 1) so counts and history have data.
        // StudentAppointmentStore.seedDemoData(1);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                onDashboardShown();
            }
        });
    }

    // ==================================================================
    // Build
    // ==================================================================
    private void buildModernDashboard() {

        // Retain the generated form, but replace its runtime presentation.
        removeAll();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1100, 700));
        setBackground(Theme.NAVY_DEEP);

        navSidebar = buildSidebar();
        host = buildHost();

        add(navSidebar, BorderLayout.WEST);
        add(host, BorderLayout.CENTER);

        refreshDashboard();
        updateNavHighlight();

        revalidate();
        repaint();
    }

    // ------------------------------------------------------------------
    // Sidebar
    // ------------------------------------------------------------------
    private SidebarPanel buildSidebar() {

        SidebarPanel sidebar = new SidebarPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(sidebarWidth, 700));
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 12, 20, 12));

        BrandPanel brand = new BrandPanel();
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        brand.setPreferredSize(new Dimension(200, 56));
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(18));

        // Menu / collapse toggle
        NavButton menu = new NavButton("Menu", GlyphIcon.Kind.MENU,
                this::toggleSidebar);
        menu.setToolTipText("Collapse / expand the menu");
        sidebar.add(menu);
        sidebar.add(Box.createVerticalStrut(10));

        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(14));

        navButtons = new NavButton[]{
            new NavButton("Dashboard", GlyphIcon.Kind.DASHBOARD,
            this::openDashboard),
            new NavButton("Book Appointment", GlyphIcon.Kind.BOOK,
            this::openBooking),
            new NavButton("My Appointments", GlyphIcon.Kind.LIST,
            this::openAllAppointments),
            new NavButton("My Profile", GlyphIcon.Kind.PROFILE,
            this::openProfile)
        };

        for (NavButton button : navButtons) {
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(12));

        // Account / leave actions: grouped under the menu (not pinned to the
        // bottom) so they stay visible even in a short window.
        NavButton backButton = new NavButton("Back to Welcome",
                GlyphIcon.Kind.BACK, this::backToWelcome);
        NavButton logoutButton = new NavButton("Logout",
                GlyphIcon.Kind.LOGOUT, this::logoutStudent);
        NavButton deleteButton = new NavButton("Delete Account",
                GlyphIcon.Kind.TRASH, this::deleteAccount);
        deleteButton.setIconColor(Theme.CORAL);
        NavButton exitButton = new NavButton("Exit",
                GlyphIcon.Kind.POWER, this::exitApplication);

        NavButton[] accountButtons
                = {backButton, logoutButton, deleteButton, exitButton};
        for (NavButton button : accountButtons) {
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // remember every button so tooltips follow the collapsed state
        navAll = new NavButton[1 + navButtons.length + accountButtons.length];
        navAll[0] = menu;
        System.arraycopy(navButtons, 0, navAll, 1, navButtons.length);
        System.arraycopy(accountButtons, 0, navAll, 1 + navButtons.length,
                accountButtons.length);

        return sidebar;
    }

    private static JPanel makeDivider() {

        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 30));
                g.fillRect(8, 0, getWidth() - 16, 1);
            }
        };
        divider.setOpaque(false);
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        divider.setPreferredSize(new Dimension(10, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return divider;
    }

    private NavButton[] navAll;

    private void toggleSidebar() {

        sidebarExpanded = !sidebarExpanded;

        final int from = sidebarWidth;
        final int to = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        final long start = System.nanoTime();

        for (NavButton button : navAll) {
            button.setCollapsedTooltip(!sidebarExpanded);
        }

        if (sidebarTimer != null) {
            sidebarTimer.stop();
        }

        sidebarTimer = new Timer(12, null);
        sidebarTimer.addActionListener(e -> {
            float t = Math.min(1f,
                    (System.nanoTime() - start) / (SIDEBAR_MS * 1_000_000f));
            float eased = 1f - (1f - t) * (1f - t) * (1f - t);

            sidebarWidth = Math.round(from + (to - from) * eased);
            navSidebar.setPreferredSize(new Dimension(sidebarWidth, 700));
            navSidebar.revalidate();
            repaint();

            if (t >= 1f) {
                sidebarTimer.stop();
            }
        });
        sidebarTimer.start();
    }

    private void updateNavHighlight() {

        if (navButtons == null) {
            return;
        }

        int active;
        switch (currentPanel) {
            case BOOKING:
                active = 1;
                break;
            case APPOINTMENTS:
            case FEEDBACK:
                active = 2;
                break;
            case PROFILE:
                active = 3;
                break;
            default:
                active = 0;
        }

        for (int i = 0; i < navButtons.length; i++) {
            navButtons[i].setActive(i == active);
        }
    }

    // ------------------------------------------------------------------
    // Host: dashboard content + scrim + slide panel + toast
    // ------------------------------------------------------------------
    private JPanel buildHost() {

        JPanel layered = new JPanel() {
            // children overlap, so Swing must not assume they do not
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        layered.setOpaque(false);
        layered.setLayout(new HostLayout());

        backdrop = new BackdropPanel();
        backdrop.setLayout(new BorderLayout());
        backdrop.add(buildDashboardContent(), BorderLayout.CENTER);

        scrim = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(2, 8, 18,
                        Math.round(150 * Math.max(0f, slideProgress))));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        scrim.setOpaque(false);
        scrim.setVisible(false);
        scrim.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Clicking outside closes read-only panels; the booking form
                // is only closed with its x so typed text is not lost.
                if (currentPanel == PanelKind.PROFILE
                        || currentPanel == PanelKind.APPOINTMENTS) {
                    closePanel();
                }
            }
        });
        scrim.addMouseMotionListener(new MouseAdapter() {
        });

        slidePanel = buildSlideShell();
        slidePanel.setVisible(false);

        toast = new ToastLabel();
        toast.setVisible(false);

        layered.add(toast);
        layered.add(slidePanel);
        layered.add(scrim);
        layered.add(backdrop);

        return layered;
    }

    /** Lays out the four overlapping layers. */
    private class HostLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return backdrop.getPreferredSize();
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(300, 300);
        }

        @Override
        public void layoutContainer(Container parent) {

            int w = parent.getWidth();
            int h = parent.getHeight();

            backdrop.setBounds(0, 0, w, h);
            scrim.setBounds(0, 0, w, h);

            int panelWidth = Math.max(280, Math.min(slideWidth, w - 36));
            int x = Math.round(w - panelWidth * slideProgress);
            slidePanel.setBounds(x, 0, panelWidth, h);

            Dimension t = toast.getPreferredSize();
            toast.setBounds((w - t.width) / 2, h - t.height - 90,
                    t.width, t.height);
        }
    }

    private JPanel buildSlideShell() {

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(PANEL_BG);
        shell.setBorder(BorderFactory.createMatteBorder(
                0, 1, 0, 0, GLASS_EDGE));

        // swallow mouse events so the window is not dragged from the panel
        shell.addMouseListener(new MouseAdapter() {
        });
        shell.addMouseMotionListener(new MouseAdapter() {
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 14, 16));

        slideTitle = new JLabel("");
        slideTitle.setFont(Theme.font(Font.BOLD, 19f));
        slideTitle.setForeground(INK);

        FlatButton close = new FlatButton("\u00D7", FlatButton.Kind.GHOST);
        close.setFont(Theme.font(Font.BOLD, 24f));
        close.setPreferredSize(new Dimension(38, 38));
        close.setToolTipText("Close");
        close.addActionListener(e -> closePanel());

        slideAction = new FlatButton("", FlatButton.Kind.GHOST);
        slideAction.setGlyph(GlyphIcon.Kind.PENCIL);
        slideAction.setPreferredSize(new Dimension(38, 38));
        slideAction.setToolTipText("Edit profile");
        slideAction.setVisible(false);
        slideAction.addActionListener(e -> showProfileEdit());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);
        actions.add(slideAction);
        actions.add(close);

        header.add(slideTitle, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        slideBody = new JPanel(new BorderLayout());
        slideBody.setOpaque(false);

        shell.add(header, BorderLayout.NORTH);
        shell.add(slideBody, BorderLayout.CENTER);

        return shell;
    }

    // ---- slide panel control -------------------------------------------
    private void showSlidePanel(PanelKind kind, String title,
            JComponent body, int width) {

        currentPanel = kind;
        slideTitle.setText(title);
        slideAction.setVisible(kind == PanelKind.PROFILE);
        slideBody.removeAll();
        slideBody.add(body, BorderLayout.CENTER);
        slideWidth = width;

        slidePanel.setVisible(true);
        scrim.setVisible(true);
        host.revalidate();
        animateSlide(1f);
        updateNavHighlight();
    }

    private void closePanel() {

        if (currentPanel == PanelKind.NONE && slideProgress <= 0f) {
            return;
        }
        currentPanel = PanelKind.NONE;
        animateSlide(0f);
        updateNavHighlight();
    }

    /** Closes immediately with no animation (used on logout / re-entry). */
    private void closePanelNow() {

        if (slideTimer != null) {
            slideTimer.stop();
        }
        slideProgress = 0f;
        slideTarget = 0f;
        currentPanel = PanelKind.NONE;
        if (slidePanel != null) {
            slidePanel.setVisible(false);
            scrim.setVisible(false);
            slideBody.removeAll();
            toast.setVisible(false);
            host.revalidate();
            host.repaint();
        }
        updateNavHighlight();
    }

    private void animateSlide(float target) {

        slideFrom = slideProgress;
        slideTarget = target;
        slideStart = System.nanoTime();

        if (slideTimer != null) {
            slideTimer.stop();
        }

        slideTimer = new Timer(12, null);
        slideTimer.addActionListener(e -> {
            float t = Math.min(1f,
                    (System.nanoTime() - slideStart) / (SLIDE_MS * 1_000_000f));
            float eased = 1f - (1f - t) * (1f - t) * (1f - t);

            slideProgress = slideFrom + (slideTarget - slideFrom) * eased;
            host.doLayout();
            host.repaint();

            if (t >= 1f) {
                slideTimer.stop();
                slideProgress = slideTarget;
                if (slideTarget <= 0f) {
                    slidePanel.setVisible(false);
                    scrim.setVisible(false);
                    slideBody.removeAll();
                }
                host.revalidate();
                host.repaint();
            }
        });
        slideTimer.start();
    }

    private void showToast(String message, boolean error) {

        toast.setMessage(message, error);
        toast.setVisible(true);
        host.revalidate();
        host.repaint();

        if (toastTimer != null) {
            toastTimer.stop();
        }
        toastTimer = new Timer(3400, e -> {
            toast.setVisible(false);
            host.repaint();
        });
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    // ------------------------------------------------------------------
    // Dashboard content
    // ------------------------------------------------------------------
    private JPanel buildDashboardContent() {

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(24, 30, 22, 30));

        // ---- top: header, summary cards, section title -------------------
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JLabel eyebrow = new JLabel("STUDENT PORTAL");
        eyebrow.setFont(Theme.tracked(Theme.font(Font.BOLD, 11f), 0.12f));
        eyebrow.setForeground(Theme.TEAL);

        greetingLabel = new JLabel(getGreeting());
        greetingLabel.setFont(Theme.font(Font.BOLD, 27f));
        greetingLabel.setForeground(Color.WHITE);

        JLabel description = new JLabel(
                "Here's an overview of your counselling activity.");
        description.setFont(Theme.font(Font.PLAIN, 13f));
        description.setForeground(MUTED);
        subtitleLabel = description;

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        greetingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        headingPanel.add(eyebrow);
        headingPanel.add(Box.createVerticalStrut(3));
        headingPanel.add(greetingLabel);
        headingPanel.add(Box.createVerticalStrut(2));
        headingPanel.add(description);

        JLabel date = new JLabel(LocalDate.now().format(LONG_DATE_FMT));
        date.setFont(Theme.font(Font.PLAIN, 13f));
        date.setForeground(MUTED);
        date.setVerticalAlignment(SwingConstants.TOP);

        FlatButton minimise = new FlatButton("", FlatButton.Kind.GHOST);
        minimise.setGlyph(GlyphIcon.Kind.MINIMISE);
        minimise.setPreferredSize(new Dimension(32, 32));
        minimise.setToolTipText("Minimise");
        minimise.addActionListener(e -> minimiseWindow());

        FlatButton exitWindow = new FlatButton("", FlatButton.Kind.GHOST);
        exitWindow.setGlyph(GlyphIcon.Kind.CLOSE);
        exitWindow.setPreferredSize(new Dimension(32, 32));
        exitWindow.setToolTipText("Exit application");
        exitWindow.addActionListener(e -> exitApplication());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);
        controls.add(date);
        controls.add(minimise);
        controls.add(exitWindow);

        JPanel east = new JPanel(new BorderLayout());
        east.setOpaque(false);
        east.add(controls, BorderLayout.NORTH);

        header.add(headingPanel, BorderLayout.WEST);
        header.add(east, BorderLayout.EAST);

        top.add(header);
        top.add(Box.createVerticalStrut(16));

        JPanel summary = new JPanel(new GridLayout(1, 3, 14, 0));
        summary.setOpaque(false);
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.setPreferredSize(new Dimension(100, 96));
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        summary.add(createStatCard("UPCOMING APPOINTMENTS",
                "Active bookings from today", Theme.TEAL,
                l -> upcomingValue = l));
        summary.add(createStatCard("PENDING APPROVAL",
                "Waiting for a counsellor", Theme.GOLD,
                l -> pendingValue = l));
        summary.add(createStatCard("COMPLETED SESSIONS",
                "Sessions you have attended", Theme.LAVENDER,
                l -> completedValue = l));

        top.add(summary);
        top.add(Box.createVerticalStrut(18));

        JPanel sectionRow = new JPanel(new BorderLayout());
        sectionRow.setOpaque(false);
        sectionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel sectionTitle = new JLabel("Upcoming Appointments");
        sectionTitle.setFont(Theme.font(Font.BOLD, 18f));
        sectionTitle.setForeground(Color.WHITE);

        FlatButton viewAll = new FlatButton("View all",
                FlatButton.Kind.OUTLINE);
        viewAll.setPreferredSize(new Dimension(96, 32));
        viewAll.setToolTipText("Open your complete appointment history");
        viewAll.addActionListener(e -> openAllAppointments());

        sectionRow.add(sectionTitle, BorderLayout.WEST);
        sectionRow.add(viewAll, BorderLayout.EAST);

        top.add(sectionRow);
        top.add(Box.createVerticalStrut(10));

        content.add(top, BorderLayout.NORTH);

        // ---- middle: table + calendar -------------------------------------
        JPanel middle = new JPanel(new BorderLayout(16, 0));
        middle.setOpaque(false);

        GlassCard tableCard = new GlassCard(null);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        dashboardTable = new AppointmentTablePanel(false);
        tableCard.add(dashboardTable, BorderLayout.CENTER);

        GlassCard calendarCard = new GlassCard(null);
        calendarCard.setLayout(new BorderLayout(0, 6));
        calendarCard.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        calendarCard.setPreferredSize(new Dimension(278, 100));

        dashboardCalendar = new DatePicker(LocalDate.now().plusDays(1), true);
        dashboardCalendar.setToolTipText("Click an available date to book");
        dashboardCalendar.setListener(d -> {
            openBooking(d);
            dashboardCalendar.clearSelection();
        });

        JLabel legend = new JLabel("\u25CF  Dates with your appointments");
        legend.setFont(Theme.font(Font.PLAIN, 11f));
        legend.setForeground(MUTED);

        calendarCard.add(dashboardCalendar, BorderLayout.CENTER);
        calendarCard.add(legend, BorderLayout.SOUTH);

        middle.add(tableCard, BorderLayout.CENTER);
        middle.add(calendarCard, BorderLayout.EAST);

        content.add(middle, BorderLayout.CENTER);

        // ---- bottom: quick actions -----------------------------------------
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        quick.setOpaque(false);
        quick.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        FlatButton bookButton = new FlatButton("Book an Appointment",
                FlatButton.Kind.PRIMARY);
        bookButton.addActionListener(e -> openBooking());

        FlatButton rescheduleButton = new FlatButton("Reschedule",
                FlatButton.Kind.SECONDARY);
        rescheduleButton.setToolTipText("Select an appointment first");
        rescheduleButton.addActionListener(e
                -> startReschedule(dashboardTable.getSelected()));

        FlatButton cancelButton = new FlatButton("Cancel Appointment",
                FlatButton.Kind.DANGER);
        cancelButton.setToolTipText("Select an appointment first");
        cancelButton.addActionListener(e
                -> confirmAndCancel(dashboardTable.getSelected()));

        quick.add(bookButton);
        quick.add(rescheduleButton);
        quick.add(cancelButton);

        content.add(quick, BorderLayout.SOUTH);

        return content;
    }

    private JPanel createStatCard(String title, String caption, Color accent,
            Consumer<JLabel> valueSink) {

        GlassCard card = new GlassCard(accent);
        card.setLayout(new BorderLayout(0, 2));
        card.setBorder(BorderFactory.createEmptyBorder(13, 18, 10, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.tracked(Theme.font(Font.BOLD, 10.5f), 0.08f));
        titleLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel("0");
        valueLabel.setFont(Theme.font(Font.BOLD, 32f));
        valueLabel.setForeground(Color.WHITE);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(Theme.font(Font.PLAIN, 11f));
        captionLabel.setForeground(accent);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(captionLabel, BorderLayout.SOUTH);

        valueSink.accept(valueLabel);
        return card;
    }

    // ==================================================================
    // Current user helpers
    // ==================================================================
    private MainFrame getMainFrame() {

        java.awt.Window window
                = javax.swing.SwingUtilities.getWindowAncestor(this);

        if (window instanceof MainFrame) {
            return (MainFrame) window;
        }

        return null;
    }

    private User getCurrentUser() {

        MainFrame frame = getMainFrame();
        return frame == null ? null : frame.getCurrentUser();
    }

    /** -1 when nobody is logged in, so no other student's data can show. */
    private int getCurrentStudentId() {

        User user = getCurrentUser();
        return user == null ? -1 : user.userId();
    }

    private String getStudentName() {

        User user = getCurrentUser();

        if (user == null || user.fullName() == null
                || user.fullName().isBlank()) {
            return "Student";
        }
        return user.fullName().trim();
    }

    private String getGreeting() {

        User user = getCurrentUser();

        if (user == null || user.fullName() == null
                || user.fullName().isBlank()) {
            return "Welcome to your dashboard";
        }

        int hour = java.time.LocalTime.now().getHour();
        String part = hour < 12 ? "Good morning"
                : hour < 17 ? "Good afternoon" : "Good evening";

        return part + ", " + user.fullName().trim();
    }

    // ==================================================================
    // Refresh
    // ==================================================================
    private void refreshDashboard() {

        int studentId = getCurrentStudentId();
        greetingLabel.setText(getGreeting());

        List<StudentAppointment> appointments = new ArrayList<>();
        String problem = null;

        try {
            studentRegNo = StudentAppointmentStore
                    .getRegistrationNumber(studentId);
            appointments = StudentAppointmentStore
                    .getAppointmentsForStudent(studentId);
        } catch (StudentDatabaseException ex) {
            problem = ex.getMessage();
        }

        subtitleLabel.setText(subtitleText());

        List<StudentAppointment> upcoming = new ArrayList<>();
        Set<LocalDate> marked = new HashSet<>();
        int pending = 0;
        int completed = 0;

        for (StudentAppointment appointment : appointments) {
            if (StudentAppointmentStore.isActive(appointment)) {
                marked.add(appointment.getAppointmentDate());
            }
            if (StudentAppointmentStore.isUpcoming(appointment)) {
                upcoming.add(appointment);
            }
            if (appointment.getStatus().equals(StudentAppointment.PENDING)) {
                pending++;
            }
            if (appointment.getStatus()
                    .equals(StudentAppointment.COMPLETED)) {
                completed++;
            }
        }

        upcomingValue.setText(String.valueOf(upcoming.size()));
        pendingValue.setText(String.valueOf(pending));
        completedValue.setText(String.valueOf(completed));

        upcoming.sort(byDateAscending());

        dashboardTable.setData(upcoming, problem != null ? problem
                : "No upcoming appointments. Use \"Book an Appointment\" "
                + "to get started.");
        dashboardCalendar.setMarked(marked);

        if (problem != null) {
            showToast(problem, true);
        }

        if (historyTable != null && currentPanel == PanelKind.APPOINTMENTS) {
            refreshHistory();
        }
    }

    private String subtitleText() {
        return (studentRegNo == null || studentRegNo.isBlank()
                ? "" : "Student ID: " + studentRegNo + "   \u00B7   ")
                + "Here's an overview of your counselling activity.";
    }

    private static Comparator<StudentAppointment> byDateAscending() {
        return Comparator.comparing(StudentAppointment::getAppointmentDate)
                .thenComparing(StudentAppointment::getAppointmentTime);
    }

    // ==================================================================
    // Navigation actions
    // ==================================================================
    private void openDashboard() {

        closePanel();
        refreshDashboard();
    }

    private void openBooking() {
        openBooking(null);
    }

    private void openBooking(LocalDate presetDate) {

        BookingForm form = new BookingForm(null, presetDate);
        showSlidePanel(PanelKind.BOOKING, "Book an Appointment",
                form.getComponent(), 410);
    }

    private void startReschedule(StudentAppointment appointment) {

        if (appointment == null) {
            showToast("Select an appointment from the table first.", true);
            return;
        }
        if (!StudentAppointmentStore.isActive(appointment)) {
            showToast("Only pending or approved appointments can be "
                    + "rescheduled.", true);
            return;
        }

        BookingForm form = new BookingForm(appointment, null);
        showSlidePanel(PanelKind.BOOKING, "Reschedule Appointment",
                form.getComponent(), 410);
    }

    private void openAllAppointments() {

        showSlidePanel(PanelKind.APPOINTMENTS, "My Appointments",
                buildHistoryBody(), 800);
        refreshHistory();
    }

    private void openProfile() {

        User user = getCurrentUser();

        if (user == null) {
            showToast("No student profile is available.", true);
            return;
        }
        showSlidePanel(PanelKind.PROFILE, "My Profile",
                buildProfileBody(user), 400);
    }

    private void confirmAndCancel(StudentAppointment appointment) {

        if (appointment == null) {
            showToast("Select an appointment from the table first.", true);
            return;
        }
        if (!StudentAppointmentStore.isActive(appointment)) {
            showToast("Only pending or approved appointments can be "
                    + "cancelled.", true);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cancel your appointment with "
                + appointment.getCounsellorName() + " on "
                + appointment.getAppointmentDate().format(DATE_FMT)
                + " at " + appointment.getAppointmentTime().format(TIME_FMT)
                + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean cancelled;
        try {
            cancelled = StudentAppointmentStore.cancelAppointment(
                    appointment.getAppointmentId(), getCurrentStudentId());
        } catch (StudentDatabaseException ex) {
            showToast(ex.getMessage(), true);
            return;
        }

        if (cancelled) {
            showToast("Appointment cancelled.", false);
        } else {
            showToast("This appointment cannot be cancelled.", true);
        }

        refreshDashboard();
    }

    private void logoutStudent() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {

            MainFrame frame = getMainFrame();

            if (frame != null) {
                closePanelNow();
                frame.setCurrentUser(null);
                frame.showScreen("WELCOME");
            }
        }
    }

    /** Leaves the dashboard for the Welcome page (no confirmation). */
    private void backToWelcome() {

        MainFrame frame = getMainFrame();

        if (frame != null) {
            closePanelNow();
            frame.setCurrentUser(null);
            frame.showScreen("WELCOME");
        }
    }

    private void minimiseWindow() {

        Window window = javax.swing.SwingUtilities.getWindowAncestor(this);

        if (window instanceof java.awt.Frame) {
            ((java.awt.Frame) window).setState(java.awt.Frame.ICONIFIED);
        }
    }

    private void exitApplication() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Exit the Student Counselling System?",
                "Exit",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * Deletes the student's data (two confirmations) and signs out.
     * Removes the student's appointments and then the account itself from
     * the database (all or nothing).
     */
    private void deleteAccount() {

        int studentId = getCurrentStudentId();

        if (studentId < 0) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "<html>Delete your account?<br>All of your appointments will "
                + "be permanently removed and you will be signed out.<br>"
                + "This cannot be undone.</html>",
                "Delete Account",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String typed = JOptionPane.showInputDialog(
                this,
                "Type DELETE to confirm:",
                "Confirm Account Deletion",
                JOptionPane.WARNING_MESSAGE
        );

        if (typed == null || !typed.trim().equals("DELETE")) {
            showToast("Account deletion cancelled.", false);
            return;
        }

        int removed;
        try {
            removed = StudentAppointmentStore.deleteAccount(studentId);
        } catch (StudentDatabaseException ex) {
            showToast(ex.getMessage(), true);
            return;
        }

        MainFrame frame = getMainFrame();
        closePanelNow();

        if (frame != null) {
            frame.setCurrentUser(null);
            frame.showScreen("WELCOME");
        }

        JOptionPane.showMessageDialog(
                frame != null ? frame : this,
                "Your account data was deleted (" + removed
                + (removed == 1 ? " appointment" : " appointments")
                + ") and you have been signed out.",
                "Account Deleted",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void onDashboardShown() {

        closePanelNow();
        refreshDashboard();
        javax.swing.SwingUtilities.invokeLater(this::fitWindowToScreen);
    }

    /**
     * MainFrame sizes the window to its largest screen, which can be taller
     * than a laptop display. Anything below the visible edge (Submit, Logout,
     * the quick actions) is then unreachable, so shrink the window to the
     * usable screen area (excluding the taskbar) and re-centre it.
     */
    private void fitWindowToScreen() {

        Window window = javax.swing.SwingUtilities.getWindowAncestor(this);

        if (window == null) {
            return;
        }

        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();

        int w = Math.min(window.getWidth(), usable.width);
        int h = Math.min(window.getHeight(), usable.height);

        boolean resized = w != window.getWidth() || h != window.getHeight();
        boolean offScreen = window.getY() < usable.y
                || window.getY() + h > usable.y + usable.height;

        if (resized) {
            window.setSize(w, h);
        }
        if (resized || offScreen) {
            window.setLocation(usable.x + (usable.width - w) / 2,
                    usable.y + (usable.height - h) / 2);
        }
    }

    // ==================================================================
    // Profile panel
    // ==================================================================
    private JComponent buildProfileBody(User user) {

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(8, 28, 24, 28));

        Avatar avatar = new Avatar(initialsOf(user.fullName()));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatar.setPreferredSize(new Dimension(92, 92));
        avatar.setMaximumSize(new Dimension(92, 92));

        JLabel name = new JLabel(valueOrDash(user.fullName()));
        name.setFont(Theme.font(Font.BOLD, 20f));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel role = new JLabel(valueOrDash(user.role()));
        role.setFont(Theme.tracked(Theme.font(Font.BOLD, 11f), 0.1f));
        role.setForeground(Theme.TEAL);
        role.setAlignmentX(Component.CENTER_ALIGNMENT);

        body.add(avatar);
        body.add(Box.createVerticalStrut(14));
        body.add(name);
        body.add(Box.createVerticalStrut(4));
        body.add(role);
        body.add(Box.createVerticalStrut(26));

        body.add(detailRow("STUDENT ID", valueOrDash(studentRegNo)));
        body.add(detailRow("FULL NAME", valueOrDash(user.fullName())));
        body.add(detailRow("EMAIL", valueOrDash(user.email())));
        body.add(detailRow("PHONE NUMBER", valueOrDash(user.phone())));
        body.add(Box.createVerticalGlue());

        return body;
    }

    private void swapSlideBody(JComponent body) {
        slideBody.removeAll();
        slideBody.add(body, BorderLayout.CENTER);
        slideBody.revalidate();
        slideBody.repaint();
    }

    private void showProfileView() {

        User user = getCurrentUser();

        if (user == null) {
            closePanel();
            return;
        }
        slideTitle.setText("My Profile");
        slideAction.setVisible(true);
        swapSlideBody(buildProfileBody(user));
    }

    private void showProfileEdit() {

        User user = getCurrentUser();

        if (user == null) {
            return;
        }
        slideTitle.setText("Edit Profile");
        slideAction.setVisible(false);
        swapSlideBody(buildProfileEditBody(user));
    }

    private static final Pattern EMAIL_PATTERN
            = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN
            = Pattern.compile("^\\+?[0-9]{9,15}$");

    /** @return an error message, or null when the details are acceptable */
    private static String validateProfile(String name, String email,
            String phone) {

        if (name.length() < 2) {
            return "Please enter your full name (at least 2 characters).";
        }
        if (name.length() > 60) {
            return "The name is too long (60 characters maximum).";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Please enter a valid email address.";
        }
        if (!PHONE_PATTERN.matcher(phone.replaceAll("[\\s-]", "")).matches()) {
            return "Please enter a valid phone number "
                    + "(9 to 15 digits, optional + at the start).";
        }
        return null;
    }

    private JComponent buildProfileEditBody(User user) {

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(4, 28, 24, 28));

        Avatar avatar = new Avatar(initialsOf(user.fullName()));
        avatar.setPreferredSize(new Dimension(76, 76));
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarRow.setOpaque(false);
        avatarRow.add(avatar);

        JTextField nameField = new JTextField(
                user.fullName() == null ? "" : user.fullName());
        JTextField emailField = new JTextField(
                user.email() == null ? "" : user.email());
        JTextField phoneField = new JTextField(
                user.phone() == null ? "" : user.phone());
        JTextField idField = new JTextField(valueOrDash(studentRegNo));

        styleField(nameField, true);
        styleField(emailField, true);
        styleField(phoneField, true);
        styleField(idField, false);
        idField.setForeground(MUTED);

        JTextArea message = messageArea();

        body.add(left(avatarRow));
        body.add(Box.createVerticalStrut(16));
        body.add(sectionLabel("FULL NAME"));
        body.add(left(nameField));
        body.add(Box.createVerticalStrut(12));
        body.add(sectionLabel("EMAIL"));
        body.add(left(emailField));
        body.add(Box.createVerticalStrut(12));
        body.add(sectionLabel("PHONE NUMBER"));
        body.add(left(phoneField));
        body.add(Box.createVerticalStrut(12));
        body.add(sectionLabel("STUDENT ID (CANNOT BE CHANGED)"));
        body.add(left(idField));
        body.add(Box.createVerticalStrut(10));
        body.add(left(message));
        body.add(Box.createVerticalStrut(6));

        FlatButton save = new FlatButton("Save changes",
                FlatButton.Kind.PRIMARY);
        FlatButton cancel = new FlatButton("Cancel", FlatButton.Kind.OUTLINE);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.add(save);
        buttons.add(cancel);
        body.add(left(buttons));
        body.add(Box.createVerticalGlue());

        cancel.addActionListener(e -> showProfileView());
        save.addActionListener(e -> {

            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            String problem = validateProfile(name, email, phone);
            MainFrame frame = getMainFrame();

            if (problem == null && frame == null) {
                problem = "Your profile could not be saved right now.";
            }
            if (problem != null) {
                message.setForeground(Theme.CORAL);
                message.setText(problem);
                return;
            }

            String saveError;
            try {
                saveError = StudentAppointmentStore.updateProfile(
                        user.userId(), name, email, phone);
            } catch (StudentDatabaseException ex) {
                saveError = ex.getMessage();
            }
            if (saveError != null) {
                message.setForeground(Theme.CORAL);
                message.setText(saveError);
                return;
            }

            // User is an immutable record, so the screen copy is replaced
            // with what was just saved to the database.
            frame.setCurrentUser(new User(user.userId(), name,
                    email.toLowerCase(), phone.replaceAll("[\\s-]", ""),
                    user.passwordHash(), user.role()));

            refreshDashboard();
            showProfileView();
            showToast("Profile updated.", false);
        });

        return body;
    }

    private JPanel detailRow(String label, String value) {

        GlassCard row = new GlassCard(null);
        row.setLayout(new BorderLayout(0, 3));
        row.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setPreferredSize(new Dimension(100, 64));

        JLabel l = new JLabel(label);
        l.setFont(Theme.tracked(Theme.font(Font.BOLD, 10f), 0.1f));
        l.setForeground(MUTED);

        JLabel v = new JLabel(value);
        v.setFont(Theme.font(Font.PLAIN, 14f));
        v.setForeground(INK);

        row.add(l, BorderLayout.NORTH);
        row.add(v, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        wrapper.add(row, BorderLayout.CENTER);
        return wrapper;
    }

    private static String valueOrDash(String s) {
        return s == null || s.isBlank() ? "\u2014" : s.trim();
    }

    private static String initialsOf(String fullName) {

        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1
                ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    // ==================================================================
    // "My Appointments" panel (full history)
    // ==================================================================
    private JComponent buildHistoryBody() {

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(0, 22, 20, 22));

        // filter chips
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);

        String[] names = {"All", "Active", "Completed", "Cancelled"};
        historyFilters = new FlatButton[names.length];
        historyFilter = 0;

        for (int i = 0; i < names.length; i++) {
            final int index = i;
            FlatButton chip = new FlatButton(names[i], FlatButton.Kind.CHIP);
            chip.setPreferredSize(new Dimension(96, 32));
            chip.setActiveColor(Theme.TEAL, false);
            chip.addActionListener(e -> {
                historyFilter = index;
                refreshHistory();
            });
            historyFilters[i] = chip;
            filters.add(chip);
        }

        historyTable = new AppointmentTablePanel(true);
        GlassCard tableCard = new GlassCard(null);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        tableCard.add(historyTable, BorderLayout.CENTER);

        // actions
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);

        historyCount = new JLabel(" ");
        historyCount.setFont(Theme.font(Font.PLAIN, 12f));
        historyCount.setForeground(MUTED);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        FlatButton reschedule = new FlatButton("Reschedule selected",
                FlatButton.Kind.SECONDARY);
        reschedule.addActionListener(e
                -> startReschedule(historyTable.getSelected()));

        FlatButton cancel = new FlatButton("Cancel selected",
                FlatButton.Kind.DANGER);
        cancel.addActionListener(e
                -> confirmAndCancel(historyTable.getSelected()));

        FlatButton feedback = new FlatButton("Give feedback",
                FlatButton.Kind.PRIMARY);
        feedback.addActionListener(e
                -> startFeedback(historyTable.getSelected()));

        buttons.add(feedback);
        buttons.add(reschedule);
        buttons.add(cancel);

        actions.add(historyCount, BorderLayout.WEST);
        actions.add(buttons, BorderLayout.EAST);

        body.add(filters, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);

        return body;
    }

    private void refreshHistory() {

        if (historyTable == null) {
            return;
        }

        List<StudentAppointment> all;
        try {
            all = StudentAppointmentStore
                    .getAppointmentsForStudent(getCurrentStudentId());
        } catch (StudentDatabaseException ex) {
            showToast(ex.getMessage(), true);
            all = new ArrayList<>();
        }

        // newest first, so recent and future records are on top
        all.sort(byDateAscending().reversed());

        List<StudentAppointment> shown = new ArrayList<>();
        for (StudentAppointment a : all) {
            boolean keep;
            switch (historyFilter) {
                case 1:
                    keep = StudentAppointmentStore.isActive(a);
                    break;
                case 2:
                    keep = a.getStatus().equals(StudentAppointment.COMPLETED);
                    break;
                case 3:
                    keep = a.getStatus().equals(StudentAppointment.CANCELLED);
                    break;
                default:
                    keep = true;
            }
            if (keep) {
                shown.add(a);
            }
        }

        historyTable.setData(shown, all.isEmpty()
                ? "You have not booked any appointments yet."
                : "No appointments match this filter.");
        historyCount.setText("Showing " + shown.size() + " of " + all.size()
                + (all.size() == 1 ? " appointment" : " appointments"));

        for (int i = 0; i < historyFilters.length; i++) {
            historyFilters[i].setActive(i == historyFilter);
        }
    }

    // ==================================================================
    // Feedback on a completed appointment
    // ==================================================================
    private void startFeedback(StudentAppointment appointment) {

        if (appointment == null) {
            showToast("Select a completed appointment from the table "
                    + "first.", true);
            return;
        }
        if (!appointment.getStatus().equals(StudentAppointment.COMPLETED)) {
            showToast("Feedback can only be given for completed "
                    + "appointments.", true);
            return;
        }
        if (appointment.getRating() > 0) {
            showToast("You rated this appointment "
                    + appointment.getRating() + "/5. Thank you!", false);
            return;
        }

        showSlidePanel(PanelKind.FEEDBACK, "Give Feedback",
                buildFeedbackBody(appointment), 410);
    }

    private JComponent buildFeedbackBody(StudentAppointment appointment) {

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(4, 28, 24, 28));

        JTextArea summary = messageArea();
        summary.setForeground(INK);
        summary.setText("Your session with "
                + appointment.getCounsellorName() + " on "
                + appointment.getAppointmentDate().format(DATE_FMT)
                + " at " + appointment.getAppointmentTime().format(TIME_FMT)
                + ". How was it?");

        StarRating stars = new StarRating();

        JTextArea comments = new JTextArea(5, 20);
        comments.setLineWrap(true);
        comments.setWrapStyleWord(true);
        comments.setOpaque(true);
        comments.setBackground(FIELD_BG);
        comments.setForeground(INK);
        comments.setCaretColor(INK);
        comments.setFont(Theme.font(Font.PLAIN, 13f));
        comments.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane commentScroll = new JScrollPane(comments,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        commentScroll.getViewport().setBackground(FIELD_BG);
        commentScroll.setBorder(BorderFactory.createLineBorder(GLASS_EDGE));
        commentScroll.setPreferredSize(new Dimension(100, 120));

        JTextArea message = messageArea();

        FlatButton submit = new FlatButton("Submit feedback",
                FlatButton.Kind.PRIMARY);
        FlatButton back = new FlatButton("Back to my appointments",
                FlatButton.Kind.OUTLINE);

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 8));
        buttons.setOpaque(false);
        buttons.add(submit);
        buttons.add(back);

        body.add(left(summary));
        body.add(Box.createVerticalStrut(18));
        body.add(sectionLabel("YOUR RATING"));
        body.add(left(stars));
        body.add(Box.createVerticalStrut(18));
        body.add(sectionLabel("COMMENTS (OPTIONAL)"));
        body.add(left(commentScroll));
        body.add(Box.createVerticalStrut(10));
        body.add(left(message));
        body.add(Box.createVerticalStrut(6));
        body.add(left(buttons));
        body.add(Box.createVerticalGlue());

        back.addActionListener(e -> openAllAppointments());
        submit.addActionListener(e -> {

            String text = comments.getText().trim();

            if (stars.getValue() < 1) {
                message.setForeground(Theme.CORAL);
                message.setText("Please choose a rating from 1 to 5 stars.");
                return;
            }
            if (text.length() > 500) {
                message.setForeground(Theme.CORAL);
                message.setText("Please keep your comments under 500 "
                        + "characters.");
                return;
            }

            String error;
            try {
                error = StudentAppointmentStore.saveFeedback(
                        appointment.getAppointmentId(),
                        getCurrentStudentId(), stars.getValue(), text);
            } catch (StudentDatabaseException ex) {
                error = ex.getMessage();
            }

            if (error != null) {
                message.setForeground(Theme.CORAL);
                message.setText(error);
                return;
            }

            showToast("Thank you! Your feedback was submitted.", false);
            openAllAppointments();
        });

        return body;
    }

    /** Five clickable stars. getValue() is 0 until one is chosen. */
    private static class StarRating extends JComponent {

        private static final int CELL = 46;
        private int value = 0;
        private int hover = 0;

        StarRating() {
            setPreferredSize(new Dimension(CELL * 5, CELL));
            setMinimumSize(getPreferredSize());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hover = starAt(e.getX());
                    repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hover = 0;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    int chosen = starAt(e.getX());
                    if (chosen > 0) {
                        value = chosen;
                        repaint();
                    }
                }
            });
        }

        int getValue() {
            return value;
        }

        private int starAt(int x) {
            int n = x / CELL + 1;
            return n >= 1 && n <= 5 ? n : 0;
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);
            int lit = hover > 0 ? hover : value;

            for (int i = 0; i < 5; i++) {
                double cx = i * CELL + CELL / 2.0;
                double cy = CELL / 2.0;
                Path2D star = new Path2D.Double();
                for (int k = 0; k < 10; k++) {
                    double r = k % 2 == 0 ? 17 : 7.5;
                    double a = -Math.PI / 2 + k * Math.PI / 5;
                    double px = cx + r * Math.cos(a);
                    double py = cy + r * Math.sin(a);
                    if (k == 0) {
                        star.moveTo(px, py);
                    } else {
                        star.lineTo(px, py);
                    }
                }
                star.closePath();

                if (i < lit) {
                    g2.setColor(Theme.GOLD);
                    g2.fill(star);
                } else {
                    g2.setColor(new Color(255, 255, 255, 24));
                    g2.fill(star);
                    g2.setColor(new Color(255, 255, 255, 90));
                    g2.setStroke(new BasicStroke(1.4f));
                    g2.draw(star);
                }
            }
            g2.dispose();
        }
    }

    // ==================================================================
    // Booking / reschedule form
    // ==================================================================
    private class BookingForm {

        private final StudentAppointment editing;
        private final List<StudentCounsellor> counsellors;
        private String loadProblem = null;
        private int counsellorIndex = -1;
        private LocalDate date;
        private LocalTime time;

        private final JTextField nameField = new JTextField();
        private final JTextField idField = new JTextField();
        private final JTextField dateField = new JTextField();
        private final JTextArea reasonArea = new JTextArea(4, 20);
        private final DatePicker picker;
        private final List<FlatButton> counsellorButtons = new ArrayList<>();
        private final List<FlatButton> timeButtons = new ArrayList<>();
        private final List<LocalTime> slots = new ArrayList<>();
        private final JTextArea message = new JTextArea(2, 20);
        private final FlatButton submit;
        private final JPanel root = new JPanel(new BorderLayout());
        private ScrollBody formBody;
        private JComponent counsellorBlock;
        private JComponent timeBlock;
        private JComponent reasonBlock;

        BookingForm(StudentAppointment editing, LocalDate presetDate) {

            this.editing = editing;

            List<StudentCounsellor> loaded = new ArrayList<>();
            try {
                loaded = StudentAppointmentStore.getCounsellors();
            } catch (StudentDatabaseException ex) {
                loadProblem = ex.getMessage();
            }
            counsellors = loaded;

            // ---- fields -----------------------------------------------
            nameField.setText(editing != null
                    && !editing.getStudentName().isBlank()
                    ? editing.getStudentName() : getStudentName());
            // The database keeps no separate booking name, so this is the
            // name on the student's account (change it in My Profile).
            styleField(nameField, false);
            nameField.setForeground(MUTED);
            nameField.setToolTipText("The name on your account. "
                    + "Change it in My Profile.");

            idField.setText(valueOrDash(editing != null
                    && !editing.getStudentRegNo().isBlank()
                    ? editing.getStudentRegNo() : studentRegNo));
            styleField(idField, false);
            idField.setForeground(MUTED);
            idField.setToolTipText("Your registration number");

            dateField.setEditable(false);
            styleField(dateField, false);

            reasonArea.setLineWrap(true);
            reasonArea.setWrapStyleWord(true);
            reasonArea.setOpaque(true);
            reasonArea.setBackground(FIELD_BG);
            reasonArea.setForeground(INK);
            reasonArea.setCaretColor(INK);
            reasonArea.setFont(Theme.font(Font.PLAIN, 13f));
            reasonArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

            JScrollPane reasonScroll = new JScrollPane(reasonArea,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            reasonScroll.getViewport().setBackground(FIELD_BG);
            reasonScroll.setBorder(BorderFactory.createLineBorder(GLASS_EDGE));
            reasonScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
            reasonScroll.setPreferredSize(new Dimension(100, 96));
            reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
            reasonArea.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    reasonScroll.setBorder(
                            BorderFactory.createLineBorder(Theme.TEAL));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    reasonScroll.setBorder(
                            BorderFactory.createLineBorder(GLASS_EDGE));
                }
            });

            // ---- date picker -------------------------------------------
            picker = new DatePicker(LocalDate.now().plusDays(1), true);
            picker.setAlignmentX(Component.LEFT_ALIGNMENT);
            picker.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    picker.getPreferredSize().height));
            picker.setListener(this::onDateChosen);

            // ---- counsellor chooser -------------------------------------
            JPanel counsellorPanel = new JPanel(new GridLayout(0, 1, 0, 6));
            counsellorPanel.setOpaque(false);
            for (int i = 0; i < counsellors.size(); i++) {
                final int index = i;
                FlatButton b = new FlatButton(counsellors.get(i).getName(),
                        FlatButton.Kind.CHIP);
                b.setAlignLeft(true);
                b.setSecondary(counsellors.get(i).getSpecialization());
                b.setActiveColor(Theme.TEAL, false);
                b.setPreferredSize(new Dimension(100, 40));
                b.addActionListener(e -> {
                    counsellorIndex = index;
                    syncCounsellorButtons();
                    updateAvailability();
                    clearMessage();
                });
                counsellorButtons.add(b);
                counsellorPanel.add(b);
            }
            if (counsellors.isEmpty()) {
                JLabel none = new JLabel("No counsellors are available yet.");
                none.setFont(Theme.font(Font.PLAIN, 12.5f));
                none.setForeground(MUTED);
                counsellorPanel.add(none);
            }
            limitHeight(counsellorPanel);

            // ---- time slots ---------------------------------------------
            JPanel timePanel = new JPanel(new GridLayout(0, 4, 6, 6));
            timePanel.setOpaque(false);
            for (LocalTime t = StudentAppointmentStore.FIRST_SLOT;
                    !t.isAfter(StudentAppointmentStore.LAST_SLOT);
                    t = t.plusMinutes(StudentAppointmentStore.SLOT_MINUTES)) {

                final LocalTime slot = t;
                FlatButton b = new FlatButton(t.format(TIME_FMT),
                        FlatButton.Kind.CHIP);
                b.setActiveColor(Theme.GOLD, true);
                b.setPreferredSize(new Dimension(60, 34));
                b.addActionListener(e -> {
                    time = slot;
                    updateAvailability();
                    clearMessage();
                });
                slots.add(t);
                timeButtons.add(b);
                timePanel.add(b);
            }
            limitHeight(timePanel);

            // ---- assemble scrolling body --------------------------------
            ScrollBody form = new ScrollBody();
            formBody = form;
            counsellorBlock = counsellorPanel;
            timeBlock = timePanel;
            reasonBlock = reasonScroll;
            form.setOpaque(false);
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBorder(BorderFactory.createEmptyBorder(0, 24, 8, 24));

            if (editing != null) {
                JLabel note = new JLabel("<html>Choose a new date and time.<br>"
                        + "A rescheduled appointment returns to "
                        + "<b>Pending</b><br>until your counsellor confirms "
                        + "it.</html>");
                note.setFont(Theme.font(Font.PLAIN, 12f));
                note.setForeground(MUTED);
                note.setAlignmentX(Component.LEFT_ALIGNMENT);
                form.add(note);
                form.add(Box.createVerticalStrut(14));
            }

            form.add(sectionLabel("STUDENT NAME"));
            form.add(left(nameField));
            form.add(Box.createVerticalStrut(14));

            form.add(sectionLabel("STUDENT ID"));
            form.add(left(idField));
            form.add(Box.createVerticalStrut(14));

            form.add(sectionLabel("COUNSELLOR"));
            form.add(left(counsellorPanel));
            form.add(Box.createVerticalStrut(14));

            form.add(sectionLabel("APPOINTMENT DATE"));
            form.add(left(dateField));
            form.add(Box.createVerticalStrut(8));
            form.add(picker);
            form.add(Box.createVerticalStrut(14));

            form.add(sectionLabel("TIME"));
            form.add(left(timePanel));
            form.add(Box.createVerticalStrut(14));

            form.add(sectionLabel("REASON FOR APPOINTMENT"));
            form.add(reasonScroll);
            form.add(Box.createVerticalStrut(6));

            JScrollPane scroll = new JScrollPane(form,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            styleScroll(scroll);

            // ---- footer: message + submit -------------------------------
            JPanel footer = new JPanel(new BorderLayout(0, 8));
            footer.setOpaque(false);
            footer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, GLASS_EDGE),
                    BorderFactory.createEmptyBorder(10, 24, 18, 24)));

            message.setFont(Theme.font(Font.BOLD, 12f));
            message.setPreferredSize(new Dimension(100, 34));
            message.setEditable(false);
            message.setFocusable(false);
            message.setOpaque(false);
            message.setLineWrap(true);
            message.setWrapStyleWord(true);
            message.setBorder(BorderFactory.createEmptyBorder());

            submit = new FlatButton(
                    editing == null ? "Submit Appointment"
                    : "Confirm Reschedule", FlatButton.Kind.PRIMARY);
            submit.setPreferredSize(new Dimension(100, 44));
            submit.addActionListener(e -> submit());

            footer.add(message, BorderLayout.NORTH);
            footer.add(submit, BorderLayout.CENTER);

            root.setOpaque(false);
            root.add(scroll, BorderLayout.CENTER);
            root.add(footer, BorderLayout.SOUTH);

            // ---- initial state -------------------------------------------
            if (editing != null) {
                reasonArea.setText(editing.getReason());
                reasonArea.setEditable(false);
                reasonArea.setForeground(MUTED);

                for (int i = 0; i < counsellors.size(); i++) {
                    if (counsellors.get(i).getCounsellorId()
                            == editing.getCounsellorId()) {
                        counsellorIndex = i;
                    }
                }
                for (FlatButton b : counsellorButtons) {
                    b.setEnabled(false); // counsellor is fixed when moving
                }
            }

            if (presetDate != null) {
                picker.setSelected(presetDate);
                onDateChosen(presetDate);
            } else {
                showDatePlaceholder();
            }

            syncCounsellorButtons();
            updateAvailability();

            if (loadProblem != null) {
                showMessage(loadProblem, true);
            }
        }

        JComponent getComponent() {
            return root;
        }

        private int currentCounsellorId() {
            if (editing != null) {
                return editing.getCounsellorId();
            }
            return counsellorIndex >= 0
                    ? counsellors.get(counsellorIndex).getCounsellorId() : -1;
        }

        private void onDateChosen(LocalDate chosen) {
            date = chosen;
            dateField.setText(chosen.format(LONG_DATE_FMT));
            dateField.setForeground(INK);
            updateAvailability();
            clearMessage();
        }

        private void showDatePlaceholder() {
            dateField.setText("No date selected \u2014 pick one below");
            dateField.setForeground(MUTED);
        }

        private void syncCounsellorButtons() {
            for (int i = 0; i < counsellorButtons.size(); i++) {
                counsellorButtons.get(i).setActive(i == counsellorIndex);
            }
        }

        /** Greys out slots the chosen counsellor/student cannot take. */
        private void updateAvailability() {

            int counsellorId = currentCounsellorId();
            boolean ready = counsellorId >= 0 && date != null;
            int excludeId = editing == null ? -1 : editing.getAppointmentId();

            // one database query for the whole day, not one per slot
            Set<LocalTime> unavailable = new HashSet<>();
            if (ready) {
                try {
                    unavailable = StudentAppointmentStore.getUnavailableTimes(
                            counsellorId, getCurrentStudentId(), date,
                            excludeId);
                } catch (StudentDatabaseException ex) {
                    showMessage(ex.getMessage(), true);
                }
            }

            for (int i = 0; i < slots.size(); i++) {

                LocalTime slot = slots.get(i);
                boolean busy = ready && unavailable.contains(slot);

                if (busy && slot.equals(time)) {
                    time = null;
                }

                FlatButton b = timeButtons.get(i);
                b.setEnabled(!busy);
                b.setToolTipText(busy ? "Not available" : null);
                b.setActive(slot.equals(time));
            }
        }

        private void showMessage(String text, boolean error) {
            message.setText(text);
            message.setForeground(error ? Theme.CORAL : Theme.TEAL);
        }

        private void clearMessage() {
            message.setText("");
        }

        /** Scrolls the form so the field named in the message is visible. */
        private void revealProblemField(String problem) {

            JComponent target = null;
            String p = problem.toLowerCase();

            if (p.contains("student name")) {
                target = nameField;
            } else if (p.contains("counsellor")) {
                target = counsellorBlock;
            } else if (p.contains("date")) {
                target = picker;
            } else if (p.contains("time") || p.contains("slot")) {
                target = timeBlock;
            } else if (p.contains("reason")) {
                target = reasonBlock;
            }

            if (target != null && target.getParent() == formBody) {
                Rectangle r = target.getBounds();
                r.grow(0, 8);
                formBody.scrollRectToVisible(r);
            }
        }

        private String validateInputs() {

            if (editing == null) {
                String studentName = nameField.getText().trim();
                if (studentName.length() < 2) {
                    return "Please enter the student name "
                            + "(at least 2 characters).";
                }
                if (studentName.length() > 60) {
                    return "The student name is too long "
                            + "(60 characters maximum).";
                }
            }

            if (editing == null && counsellorIndex < 0) {
                return "Please choose a counsellor.";
            }
            if (date == null) {
                return "Please select an appointment date.";
            }
            if (time == null) {
                return "Please select a time slot.";
            }

            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) {
                return "Please enter a reason for the appointment.";
            }
            if (reason.length() < 5) {
                return "Please describe the reason in a little more detail.";
            }
            if (reason.length() > 300) {
                return "The reason is too long (300 characters maximum).";
            }

            return StudentAppointmentStore.validateSlot(date, time);
        }

        private void submit() {

            String problem = validateInputs();
            if (problem != null) {
                showMessage(problem, true);
                revealProblemField(problem);
                return;
            }

            int studentId = getCurrentStudentId();

            if (studentId < 0) {
                showMessage("You are not logged in.", true);
                return;
            }

            try {
                if (editing == null) {

                    StudentAppointmentStore.bookAppointment(
                            studentId,
                            nameField.getText().trim(),
                            counsellors.get(counsellorIndex)
                                    .getCounsellorId(),
                            counsellors.get(counsellorIndex).getName(),
                            date,
                            time,
                            reasonArea.getText().trim());

                    showToast("Appointment request submitted. "
                            + "Your counsellor will confirm it shortly.",
                            false);

                } else {

                    String error = StudentAppointmentStore.checkReschedule(
                            editing.getAppointmentId(), studentId, date, time);

                    if (error != null) {
                        showMessage(error, true);
                        updateAvailability();
                        return;
                    }

                    if (!StudentAppointmentStore.rescheduleAppointment(
                            editing.getAppointmentId(), studentId, date,
                            time)) {
                        showMessage("That time was just taken. "
                                + "Please choose another.", true);
                        updateAvailability();
                        return;
                    }

                    showToast("Appointment rescheduled. It is now pending "
                            + "approval.", false);
                }

                closePanel();
                refreshDashboard();

            } catch (IllegalArgumentException | StudentDatabaseException ex) {
                showMessage(ex.getMessage(), true);
                updateAvailability();
            }
        }
    }

    // ---- small form helpers ---------------------------------------------
    private static JLabel sectionLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(Theme.tracked(Theme.font(Font.BOLD, 10.5f), 0.1f));
        label.setForeground(MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return label;
    }

    private static <T extends JComponent> T left(T component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        limitHeight(component);
        return component;
    }

    private static void limitHeight(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                component.getPreferredSize().height));
    }

    private static javax.swing.border.Border fieldBorder(Color line) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line),
                BorderFactory.createEmptyBorder(9, 12, 9, 12));
    }

    private static void styleField(JTextField field, boolean editable) {

        field.setEditable(editable);
        field.setOpaque(true);
        field.setBackground(FIELD_BG);
        field.setForeground(INK);
        field.setCaretColor(INK);
        field.setFont(Theme.font(Font.PLAIN, 13f));
        field.setBorder(fieldBorder(GLASS_EDGE));

        if (editable) {
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    field.setBorder(fieldBorder(Theme.TEAL));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    field.setBorder(fieldBorder(GLASS_EDGE));
                }
            });
        }
    }

    /** Wrapping, read-only text area used for validation messages. */
    private static JTextArea messageArea() {

        JTextArea area = new JTextArea(2, 20);
        area.setFont(Theme.font(Font.BOLD, 12f));
        area.setPreferredSize(new Dimension(100, 34));
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder());
        return area;
    }

    private static void styleScroll(JScrollPane scroll) {

        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        // must be non-null: JTable replaces a null border with the L&F's grey one
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
    }

    private static String titleCase(String status) {
        return status.substring(0, 1).toUpperCase()
                + status.substring(1).toLowerCase();
    }

    private static Color statusColor(String status) {
        switch (status) {
            case StudentAppointment.APPROVED:
                return Theme.TEAL;
            case StudentAppointment.COMPLETED:
                return Theme.LAVENDER;
            case StudentAppointment.CANCELLED:
                return Theme.CORAL;
            default:
                return Theme.GOLD;
        }
    }

    // ==================================================================
    // Reusable painted components
    // ==================================================================

    /** Navy gradient with soft teal / gold glows (echoes the Welcome page). */
    private static class BackdropPanel extends JPanel {

        BackdropPanel() {
            setOpaque(true);
            setBackground(Theme.NAVY_DEEP);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);
            int w = getWidth();
            int h = getHeight();

            g2.setPaint(new GradientPaint(0, 0, Theme.NAVY,
                    w, h, Theme.NAVY_DEEP));
            g2.fillRect(0, 0, w, h);

            glow(g2, w * 0.92f, h * 0.05f, Math.max(w, h) * 0.55f,
                    Theme.TEAL, 46, w, h);
            glow(g2, w * 0.05f, h * 1.0f, Math.max(w, h) * 0.50f,
                    Theme.GOLD, 26, w, h);
            glow(g2, w * 0.55f, h * 0.55f, Math.max(w, h) * 0.45f,
                    Theme.LAVENDER, 14, w, h);
            g2.dispose();
        }

        private static void glow(Graphics2D g2, float cx, float cy, float r,
                Color c, int alpha, int w, int h) {
            g2.setPaint(new RadialGradientPaint(cx, cy, r,
                    new float[]{0f, 1f},
                    new Color[]{Theme.alpha(c, alpha), Theme.alpha(c, 0)}));
            g2.fillRect(0, 0, w, h);
        }
    }

    /** Collapsible left sidebar background. */
    private static class SidebarPanel extends JPanel {

        SidebarPanel() {
            setOpaque(true);
            setBackground(Theme.NAVY_DEEP);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0,
                    Theme.mix(Theme.BLUE, Theme.NAVY, 0.55f),
                    0, getHeight(), Theme.NAVY_DEEP));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 34));
            g2.fillRect(getWidth() - 1, 0, 1, getHeight());
            g2.dispose();
        }
    }

    /** Logo block; the text part fades out as the sidebar collapses. */
    private static class BrandPanel extends JComponent {

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);

            int size = 44;
            int x = 4;
            int y = (getHeight() - size) / 2;

            g2.setPaint(new GradientPaint(x, y, Theme.TEAL,
                    x + size, y + size, Theme.BLUE));
            g2.fill(new RoundRectangle2D.Float(x, y, size, size, 14, 14));

            g2.setFont(Theme.font(Font.BOLD, 22f));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("U", x + (size - fm.stringWidth("U")) / 2,
                    y + (size - fm.getHeight()) / 2 + fm.getAscent());

            float fade = Math.max(0f, Math.min(1f, (getWidth() - 110) / 70f));
            if (fade > 0f) {
                int tx = x + size + 12;
                g2.setComposite(java.awt.AlphaComposite.SrcOver
                        .derive(fade));

                g2.setFont(Theme.font(Font.BOLD, 15f));
                g2.setColor(Color.WHITE);
                g2.drawString("USIU-AFRICA", tx, y + 15);

                g2.setFont(Theme.tracked(Theme.font(Font.BOLD, 9.5f), 0.1f));
                g2.setColor(Theme.TEAL);
                g2.drawString("STUDENT PORTAL", tx, y + 29);

                g2.setFont(Theme.tracked(Theme.font(Font.PLAIN, 8.5f), 0.08f));
                g2.setColor(MUTED);
                g2.drawString("COUNSELLING SERVICES", tx, y + 41);
            }
            g2.dispose();
        }
    }

    /** Translucent rounded "glass" card, optionally with an accent bar. */
    private static class GlassCard extends JPanel {

        private final Color accent;

        GlassCard(Color accent) {
            this.accent = accent;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);
            int w = getWidth();
            int h = getHeight();
            Shape shape = new RoundRectangle2D.Float(0.5f, 0.5f,
                    w - 1, h - 1, 18, 18);

            g2.setColor(GLASS_FILL);
            g2.fill(shape);

            if (accent != null) {
                g2.setClip(shape);
                g2.setColor(accent);
                g2.fillRect(0, 0, w, 3);
                g2.setClip(null);
            }

            g2.setColor(GLASS_EDGE);
            g2.draw(shape);
            g2.dispose();
        }
    }

    /** Circle with the student's initials. */
    private static class Avatar extends JComponent {

        private final String initials;

        Avatar(String initials) {
            this.initials = initials;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);
            int s = Math.min(getWidth(), getHeight());

            g2.setPaint(new GradientPaint(0, 0, Theme.TEAL, s, s, Theme.BLUE));
            g2.fill(new Ellipse2D.Float(0, 0, s, s));

            g2.setFont(Theme.font(Font.BOLD, 32f));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initials, (s - fm.stringWidth(initials)) / 2f,
                    (s - fm.getHeight()) / 2f + fm.getAscent());
            g2.dispose();
        }
    }

    /** Pop-up message at the bottom of the content area. */
    private static class ToastLabel extends JComponent {

        private String text = "";
        private boolean error;

        void setMessage(String text, boolean error) {
            this.text = text;
            this.error = error;
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(Font.BOLD, 13f));
            return new Dimension(fm.stringWidth(text) + 56, 42);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);

            Color base = error ? Theme.CORAL : Theme.TEAL;
            g2.setColor(Theme.mix(Theme.NAVY_DEEP, base, 0.30f));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(),
                    getHeight(), 21, 21));
            g2.setColor(base);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1,
                    getHeight() - 1, 21, 21));

            g2.setColor(base);
            g2.fill(new Ellipse2D.Float(16, getHeight() / 2f - 4, 8, 8));

            g2.setFont(Theme.font(Font.BOLD, 13f));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, 34,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Scrollable panel that always matches the viewport width. */
    private static class ScrollBody extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return 80;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static class DarkScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(255, 255, 255, 70);
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private static JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Vector icons (no image files needed)
    // ------------------------------------------------------------------
    private static class GlyphIcon implements Icon {

        enum Kind {
            MENU, DASHBOARD, BOOK, LIST, PROFILE, LOGOUT,
            BACK, TRASH, POWER, PENCIL, MINIMISE, CLOSE
        }

        private final Kind kind;
        private Color color;

        GlyphIcon(Kind kind, Color color) {
            this.kind = kind;
            this.color = color;
        }

        void setColor(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));

            switch (kind) {
                case MENU:
                    g2.drawLine(3, 5, 17, 5);
                    g2.drawLine(3, 10, 17, 10);
                    g2.drawLine(3, 15, 17, 15);
                    break;
                case DASHBOARD:
                    g2.draw(new RoundRectangle2D.Float(2.5f, 2.5f, 6, 6, 2, 2));
                    g2.draw(new RoundRectangle2D.Float(11.5f, 2.5f, 6, 6, 2, 2));
                    g2.draw(new RoundRectangle2D.Float(2.5f, 11.5f, 6, 6, 2, 2));
                    g2.draw(new RoundRectangle2D.Float(11.5f, 11.5f, 6, 6, 2, 2));
                    break;
                case BOOK:
                    g2.draw(new RoundRectangle2D.Float(2.5f, 4.5f, 15, 13, 3, 3));
                    g2.drawLine(3, 8, 17, 8);
                    g2.drawLine(6, 2, 6, 6);
                    g2.drawLine(14, 2, 14, 6);
                    g2.drawLine(10, 11, 10, 15);
                    g2.drawLine(8, 13, 12, 13);
                    break;
                case LIST:
                    g2.drawLine(8, 5, 17, 5);
                    g2.drawLine(8, 10, 17, 10);
                    g2.drawLine(8, 15, 17, 15);
                    g2.fill(new Ellipse2D.Float(2.2f, 3.7f, 2.6f, 2.6f));
                    g2.fill(new Ellipse2D.Float(2.2f, 8.7f, 2.6f, 2.6f));
                    g2.fill(new Ellipse2D.Float(2.2f, 13.7f, 2.6f, 2.6f));
                    break;
                case PROFILE:
                    g2.draw(new Ellipse2D.Float(6.5f, 2.5f, 7, 7));
                    g2.draw(new Arc2D.Float(3f, 12f, 14, 12, 0, 180,
                            Arc2D.OPEN));
                    break;
                case LOGOUT:
                    Path2D door = new Path2D.Float();
                    door.moveTo(8, 3);
                    door.lineTo(4, 3);
                    door.lineTo(4, 17);
                    door.lineTo(8, 17);
                    g2.draw(door);
                    g2.drawLine(8, 10, 17, 10);
                    g2.drawLine(14, 6, 17, 10);
                    g2.drawLine(14, 14, 17, 10);
                    break;
                case BACK:
                    g2.drawLine(16, 10, 4, 10);
                    g2.drawLine(9, 5, 4, 10);
                    g2.drawLine(9, 15, 4, 10);
                    break;
                case TRASH:
                    g2.drawLine(3, 5, 17, 5);
                    g2.drawLine(8, 5, 8, 3);
                    g2.drawLine(8, 3, 12, 3);
                    g2.drawLine(12, 3, 12, 5);
                    Path2D can = new Path2D.Float();
                    can.moveTo(5, 5);
                    can.lineTo(6, 17);
                    can.lineTo(14, 17);
                    can.lineTo(15, 5);
                    g2.draw(can);
                    g2.drawLine(9, 8, 9, 14);
                    g2.drawLine(11, 8, 11, 14);
                    break;
                case POWER:
                    g2.draw(new Arc2D.Float(3.5f, 4.5f, 13, 13, 120, 300,
                            Arc2D.OPEN));
                    g2.drawLine(10, 2, 10, 9);
                    break;
                case PENCIL:
                    Path2D pencil = new Path2D.Float();
                    pencil.moveTo(3, 17);
                    pencil.lineTo(4, 12.5f);
                    pencil.lineTo(13.5f, 3);
                    pencil.lineTo(17, 6.5f);
                    pencil.lineTo(7.5f, 16);
                    pencil.closePath();
                    g2.draw(pencil);
                    g2.drawLine(11, 6, 14, 9);
                    break;
                case MINIMISE:
                    g2.drawLine(4, 14, 16, 14);
                    break;
                case CLOSE:
                    g2.drawLine(5, 5, 15, 15);
                    g2.drawLine(15, 5, 5, 15);
                    break;
                default:
                    break;
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Sidebar button
    // ------------------------------------------------------------------
    private static class NavButton extends JButton {

        private final String label;
        private final GlyphIcon icon;
        private Color iconColor = null;
        private boolean active;

        NavButton(String label, GlyphIcon.Kind kind, Runnable action) {
            super(label);   // text is painted by paintComponent below; kept for accessibility
            this.label = label;
            this.icon = new GlyphIcon(kind, MUTED);

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(200, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            getAccessibleContext().setAccessibleName(label);
            addActionListener(e -> action.run());
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        void setIconColor(Color color) {
            this.iconColor = color;
        }

        void setCollapsedTooltip(boolean collapsed) {
            setToolTipText(collapsed ? label : null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);
            int w = getWidth();
            int h = getHeight();
            boolean hover = getModel().isRollover();

            if (active) {
                g2.setColor(Theme.alpha(Theme.TEAL, 44));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
                g2.setColor(Theme.TEAL);
                g2.fill(new RoundRectangle2D.Float(0, 10, 3, h - 20, 3, 3));
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 24));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
            }

            Color c = iconColor != null ? iconColor
                    : active ? Theme.TEAL : hover ? INK : MUTED;
            icon.setColor(c);
            icon.paintIcon(this, g2, 14, (h - 20) / 2);

            float fade = Math.max(0f, Math.min(1f, (w - 100) / 70f));
            if (fade > 0f) {
                g2.setComposite(java.awt.AlphaComposite.SrcOver.derive(fade));
                g2.setFont(Theme.font(active ? Font.BOLD : Font.PLAIN, 13.5f));
                g2.setColor(active || hover ? Color.WHITE
                        : iconColor != null ? iconColor : INK);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, 14 + 20 + 14,
                        (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Buttons
    // ------------------------------------------------------------------
    private static class FlatButton extends JButton {

        enum Kind { PRIMARY, SECONDARY, OUTLINE, DANGER, CHIP, GHOST }

        private final Kind kind;
        private boolean active;
        private boolean activeSolid;
        private Color activeColor = Theme.TEAL;
        private boolean alignLeft;
        private String secondary;
        private GlyphIcon glyph;

        FlatButton(String text, Kind kind) {
            super(text);
            this.kind = kind;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.font(Font.BOLD, 13f));
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        void setActiveColor(Color color, boolean solid) {
            this.activeColor = color;
            this.activeSolid = solid;
        }

        void setAlignLeft(boolean alignLeft) {
            this.alignLeft = alignLeft;
        }

        void setSecondary(String secondary) {
            this.secondary = secondary;
        }

        /** Shows a vector icon instead of text (e.g. pencil, close). */
        void setGlyph(GlyphIcon.Kind kind) {
            this.glyph = new GlyphIcon(kind, MUTED);
        }

        @Override
        public Dimension getPreferredSize() {
            if (isPreferredSizeSet()) {
                return super.getPreferredSize();
            }
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 40,
                    kind == Kind.CHIP ? 36 : 42);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);
            int w = getWidth();
            int h = getHeight();
            boolean hover = getModel().isRollover() && isEnabled();
            boolean pressed = getModel().isPressed() && isEnabled();

            if (!isEnabled()) {
                g2.setComposite(java.awt.AlphaComposite.SrcOver
                        .derive(active ? 0.75f : 0.38f));
            }

            Color fill = null;
            Color border = null;
            Color text = INK;

            switch (kind) {
                case PRIMARY:
                    fill = hover ? Theme.GOLD_LIGHT : Theme.GOLD;
                    text = Theme.NAVY_DEEP;
                    break;
                case SECONDARY:
                    fill = Theme.mix(Theme.BLUE, Color.WHITE,
                            hover ? 0.18f : 0f);
                    text = Color.WHITE;
                    break;
                case OUTLINE:
                    fill = hover ? Theme.alpha(Theme.TEAL, 46) : null;
                    border = Theme.TEAL;
                    text = Theme.TEAL;
                    break;
                case DANGER:
                    fill = hover ? Theme.alpha(Theme.CORAL, 46) : null;
                    border = Theme.CORAL;
                    text = Theme.CORAL;
                    break;
                case GHOST:
                    fill = hover ? new Color(255, 255, 255, 30) : null;
                    text = hover ? Color.WHITE : MUTED;
                    break;
                case CHIP:
                    fill = new Color(255, 255, 255, hover ? 30 : 16);
                    border = GLASS_EDGE;
                    if (active) {
                        if (activeSolid) {
                            fill = hover ? Theme.GOLD_LIGHT : activeColor;
                            border = activeColor;
                            text = Theme.NAVY_DEEP;
                        } else {
                            fill = Theme.alpha(activeColor, 48);
                            border = activeColor;
                            text = Color.WHITE;
                        }
                    }
                    break;
                default:
                    break;
            }

            if (pressed && fill != null) {
                fill = Theme.mix(fill, Color.BLACK, 0.15f);
            }

            int arc = kind == Kind.CHIP ? 12 : 14;
            Shape shape = new RoundRectangle2D.Float(0.5f, 0.5f,
                    w - 1, h - 1, arc, arc);

            if (fill != null) {
                g2.setColor(fill);
                g2.fill(shape);
            }
            if (border != null) {
                g2.setStroke(new BasicStroke(1.3f));
                g2.setColor(border);
                g2.draw(shape);
            }

            if (glyph != null) {
                glyph.setColor(text);
                glyph.paintIcon(this, g2, (w - 20) / 2, (h - 20) / 2);
                g2.dispose();
                return;
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            int tx = alignLeft ? 14 : (w - fm.stringWidth(getText())) / 2;

            g2.setColor(text);
            g2.drawString(getText(), tx, ty);

            if (secondary != null) {
                Font small = Theme.font(Font.PLAIN, 11f);
                g2.setFont(small);
                FontMetrics sfm = g2.getFontMetrics();
                g2.setColor(active ? Color.WHITE : MUTED);
                g2.drawString(secondary, w - 14 - sfm.stringWidth(secondary),
                        (h - sfm.getHeight()) / 2 + sfm.getAscent());
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Interactive date picker
    // ------------------------------------------------------------------
    private static class DatePicker extends JComponent {

        private static final int HEADER_H = 36;
        private static final int WEEK_H = 22;
        private static final int CELL_H = 32;

        private YearMonth shown;
        private LocalDate selected;
        private LocalDate hoverDate;
        private int hoverNav;
        private final LocalDate minDate;
        private final boolean skipWeekends;
        private Set<LocalDate> marked = new HashSet<>();
        private Consumer<LocalDate> listener;

        DatePicker(LocalDate minDate, boolean skipWeekends) {
            this.minDate = minDate;
            this.skipWeekends = skipWeekends;

            LocalDate first = minDate;
            while (!isSelectable(first)) {
                first = first.plusDays(1);
            }
            shown = YearMonth.from(first);

            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    track(e.getPoint());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverDate = null;
                    hoverNav = 0;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    track(e.getPoint());
                    if (hoverNav < 0) {
                        shown = shown.minusMonths(1);
                    } else if (hoverNav > 0) {
                        shown = shown.plusMonths(1);
                    } else if (hoverDate != null) {
                        selected = hoverDate;
                        if (listener != null) {
                            listener.accept(selected);
                        }
                    }
                    track(e.getPoint());
                    repaint();
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        void setListener(Consumer<LocalDate> listener) {
            this.listener = listener;
        }

        void setMarked(Set<LocalDate> marked) {
            this.marked = marked;
            repaint();
        }

        void setSelected(LocalDate date) {
            this.selected = date;
            if (date != null) {
                shown = YearMonth.from(date);
            }
            repaint();
        }

        void clearSelection() {
            this.selected = null;
            repaint();
        }

        private boolean isSelectable(LocalDate d) {
            if (d.isBefore(minDate)) {
                return false;
            }
            if (skipWeekends) {
                java.time.DayOfWeek dow = d.getDayOfWeek();
                return dow != java.time.DayOfWeek.SATURDAY
                        && dow != java.time.DayOfWeek.SUNDAY;
            }
            return true;
        }

        private boolean canGoPrev() {
            return shown.isAfter(YearMonth.from(minDate));
        }

        private boolean canGoNext() {
            return shown.isBefore(YearMonth.from(minDate).plusMonths(11));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(7 * 38, HEADER_H + WEEK_H + 6 * CELL_H);
        }

        private LocalDate dateAt(Point p) {
            if (p.y < HEADER_H + WEEK_H) {
                return null;
            }
            double cellW = getWidth() / 7.0;
            int row = (p.y - HEADER_H - WEEK_H) / CELL_H;
            int col = (int) (p.x / cellW);
            if (row > 5 || col < 0 || col > 6) {
                return null;
            }
            int offset = shown.atDay(1).getDayOfWeek().getValue() - 1;
            int day = row * 7 + col - offset + 1;
            if (day < 1 || day > shown.lengthOfMonth()) {
                return null;
            }
            return shown.atDay(day);
        }

        private void track(Point p) {
            hoverNav = 0;
            hoverDate = null;

            if (p.y < HEADER_H) {
                if (p.x < 44 && canGoPrev()) {
                    hoverNav = -1;
                } else if (p.x > getWidth() - 44 && canGoNext()) {
                    hoverNav = 1;
                }
            } else {
                LocalDate d = dateAt(p);
                if (d != null && isSelectable(d)) {
                    hoverDate = d;
                }
            }

            setCursor(hoverNav != 0 || hoverDate != null
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hints(g2);
            int w = getWidth();
            double cellW = w / 7.0;

            // ---- header -------------------------------------------------
            g2.setFont(Theme.font(Font.BOLD, 14.5f));
            FontMetrics fm = g2.getFontMetrics();
            String title = shown.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            g2.setColor(Color.WHITE);
            g2.drawString(title, (w - fm.stringWidth(title)) / 2,
                    (HEADER_H - fm.getHeight()) / 2 + fm.getAscent());

            drawChevron(g2, 22, HEADER_H / 2, -1, canGoPrev(), hoverNav < 0);
            drawChevron(g2, w - 22, HEADER_H / 2, 1, canGoNext(), hoverNav > 0);

            // ---- weekday letters ----------------------------------------
            String[] days = {"M", "T", "W", "T", "F", "S", "S"};
            g2.setFont(Theme.font(Font.BOLD, 10.5f));
            fm = g2.getFontMetrics();
            g2.setColor(MUTED);
            for (int i = 0; i < 7; i++) {
                g2.drawString(days[i],
                        (int) ((i + 0.5) * cellW - fm.stringWidth(days[i]) / 2.0),
                        HEADER_H + (WEEK_H - fm.getHeight()) / 2 + fm.getAscent());
            }

            // ---- days ---------------------------------------------------
            int offset = shown.atDay(1).getDayOfWeek().getValue() - 1;
            LocalDate today = LocalDate.now();
            g2.setFont(Theme.font(Font.PLAIN, 12.5f));
            fm = g2.getFontMetrics();
            double d = Math.min(28, cellW - 4);

            for (int day = 1; day <= shown.lengthOfMonth(); day++) {

                LocalDate date = shown.atDay(day);
                int idx = offset + day - 1;
                double cx = (idx % 7 + 0.5) * cellW;
                double cy = HEADER_H + WEEK_H + (idx / 7) * CELL_H + CELL_H / 2.0;

                boolean enabled = isSelectable(date);
                boolean isSelected = date.equals(selected);
                boolean isHover = date.equals(hoverDate);

                Ellipse2D circle = new Ellipse2D.Double(
                        cx - d / 2, cy - d / 2, d, d);

                Color text = enabled ? INK : new Color(180, 195, 216, 85);

                if (isSelected) {
                    g2.setColor(Theme.GOLD);
                    g2.fill(circle);
                    text = Theme.NAVY_DEEP;
                } else if (isHover) {
                    g2.setColor(Theme.alpha(Theme.TEAL, 80));
                    g2.fill(circle);
                    text = Color.WHITE;
                }
                if (date.equals(today) && !isSelected) {
                    g2.setStroke(new BasicStroke(1.4f));
                    g2.setColor(Theme.TEAL);
                    g2.draw(circle);
                }

                String s = String.valueOf(day);
                g2.setFont(Theme.font(isSelected ? Font.BOLD : Font.PLAIN, 12.5f));
                g2.setColor(text);
                g2.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0),
                        (float) (cy - fm.getHeight() / 2.0 + fm.getAscent()));

                if (marked.contains(date)) {
                    g2.setColor(isSelected ? Theme.NAVY_DEEP : Theme.GOLD);
                    g2.fill(new Ellipse2D.Double(cx - 2, cy + d / 2 - 4, 4, 4));
                }
            }
            g2.dispose();
        }

        private void drawChevron(Graphics2D g2, int cx, int cy, int dir,
                boolean enabled, boolean hover) {
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g2.setColor(!enabled ? new Color(180, 195, 216, 55)
                    : hover ? Theme.TEAL : MUTED);
            g2.drawLine(cx + 3 * -dir, cy - 6, cx - 3 * -dir, cy);
            g2.drawLine(cx - 3 * -dir, cy, cx + 3 * -dir, cy + 6);
        }
    }

    // ------------------------------------------------------------------
    // Appointment table (used by the dashboard and the history panel)
    // ------------------------------------------------------------------
    private static class AppointmentTablePanel extends JPanel {

        private final boolean withReason;
        private final DefaultTableModel model;
        private final JTable table;
        private final List<StudentAppointment> rows = new ArrayList<>();
        private final CardLayout cards = new CardLayout();
        private final JLabel emptyLabel = new JLabel("", SwingConstants.CENTER);
        private final int statusColumn;

        AppointmentTablePanel(boolean withReason) {

            this.withReason = withReason;
            setLayout(cards);
            setOpaque(false);

            String[] columns = withReason
                    ? new String[]{"Appt ID", "Student ID", "Counsellor",
                        "Date", "Time", "Reason", "Status", "Feedback"}
                    : new String[]{"Appointment ID", "Counsellor", "Date",
                        "Time", "Status"};
            statusColumn = withReason ? 6 : 4;

            model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            table = new JTable(model);
            table.setOpaque(false);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.setRowHeight(40);
            table.setFillsViewportHeight(true);
            table.setFocusable(false);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setDefaultRenderer(Object.class, new CellRenderer());

            int[] widths = withReason
                    ? new int[]{62, 84, 124, 88, 48, 118, 90, 80}
                    : new int[]{116, 132, 96, 52, 100};
            for (int i = 0; i < widths.length; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            JTableHeader header = table.getTableHeader();
            header.setOpaque(false);
            header.setReorderingAllowed(false);
            header.setResizingAllowed(false);
            header.setPreferredSize(new Dimension(0, 36));
            header.setDefaultRenderer(new HeaderRenderer());

            JScrollPane scroll = new JScrollPane(table);
            // created explicitly: JScrollPane only builds the column header
            // once the table is displayed, which is too late to style it
            scroll.setColumnHeaderView(header);
            styleScroll(scroll);
            scroll.getColumnHeader().setOpaque(false);

            emptyLabel.setFont(Theme.font(Font.PLAIN, 13f));
            emptyLabel.setForeground(MUTED);

            add(scroll, "table");
            add(emptyLabel, "empty");
        }

        void setData(List<StudentAppointment> list, String emptyMessage) {

            rows.clear();
            model.setRowCount(0);

            for (StudentAppointment a : list) {
                rows.add(a);
                Object id = a.getAppointmentId();
                String date = a.getAppointmentDate().format(DATE_FMT);
                String time = a.getAppointmentTime().format(TIME_FMT);

                String regNo = valueOrDash(a.getStudentRegNo());

                if (withReason) {
                    String feedback = a.getRating() > 0
                            ? a.getRating() + " / 5"
                            : a.getStatus().equals(StudentAppointment.COMPLETED)
                            ? "Not rated" : "\u2014";
                    model.addRow(new Object[]{id, regNo, a.getCounsellorName(),
                        date, time, a.getReason(), a.getStatus(), feedback});
                } else {
                    model.addRow(new Object[]{id, a.getCounsellorName(),
                        date, time, a.getStatus()});
                }
            }

            emptyLabel.setText("<html><body style='text-align:center;width:260px'>"
                    + emptyMessage + "</body></html>");
            cards.show(this, rows.isEmpty() ? "empty" : "table");
        }

        /** The appointment on the selected row, or null. */
        StudentAppointment getSelected() {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                return null;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            return modelRow < rows.size() ? rows.get(modelRow) : null;
        }

        private class HeaderRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, false, false, row, col);
                setText(String.valueOf(v));
                setOpaque(false);
                setFont(Theme.font(Font.BOLD, 10.5f));
                setForeground(MUTED);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 2));
                return this;
            }

            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 18));
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        }

        private class CellRenderer extends DefaultTableCellRenderer {

            private int row;
            private boolean selected;
            private Color chip;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {

                super.getTableCellRendererComponent(t, v, false, false, row, col);

                this.row = row;
                this.selected = sel;
                this.chip = null;

                setOpaque(false);
                setFont(Theme.font(Font.PLAIN, 12.5f));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 2));
                setToolTipText(null);

                int modelRow = t.convertRowIndexToModel(row);
                boolean cancelled = modelRow < rows.size()
                        && rows.get(modelRow).getStatus()
                                .equals(StudentAppointment.CANCELLED);
                setForeground(cancelled ? MUTED : INK);

                if (col == statusColumn) {
                    String status = String.valueOf(v);
                    chip = statusColor(status);
                    setText(titleCase(status));
                } else if (withReason && col == 5) {
                    setToolTipText(String.valueOf(v));
                }
                return this;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.hints(g2);
                int w = getWidth();
                int h = getHeight();

                if (selected) {
                    g2.setColor(Theme.alpha(Theme.TEAL, 52));
                    g2.fillRect(0, 0, w, h);
                } else if (row % 2 == 1) {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fillRect(0, 0, w, h);
                }
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fillRect(0, h - 1, w, 1);

                if (chip != null) {
                    g2.setFont(Theme.font(Font.BOLD, 11.5f));
                    FontMetrics fm = g2.getFontMetrics();
                    int cw = fm.stringWidth(getText()) + 22;
                    int ch = 24;
                    int cx = 4;
                    int cy = (h - ch) / 2;
                    g2.setColor(Theme.alpha(chip, 44));
                    g2.fill(new RoundRectangle2D.Float(cx, cy, cw, ch, 24, 24));
                    g2.setColor(chip);
                    g2.drawString(getText(), cx + 11,
                            cy + (ch - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                    return;
                }
                g2.dispose();
                super.paintComponent(g);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sidebarpanel = new javax.swing.JPanel();
        titlelabel = new javax.swing.JLabel();
        studentdashboard = new javax.swing.JButton();
        bookappointment = new javax.swing.JButton();
        myappointments = new javax.swing.JButton();
        studentprofile = new javax.swing.JButton();
        logout = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        studentcontentpanel = new javax.swing.JPanel();
        dashboardheading = new javax.swing.JLabel();
        WelcomeMessage = new javax.swing.JLabel();
        upcomingcardPanel = new javax.swing.JPanel();
        upcommingapts = new javax.swing.JLabel();
        upcomingcount = new javax.swing.JLabel();
        pendingcardPanel = new javax.swing.JPanel();
        pendingapts = new javax.swing.JLabel();
        pendingcount = new javax.swing.JLabel();
        completedcardPanel = new javax.swing.JPanel();
        completedapts = new javax.swing.JLabel();
        completedcount = new javax.swing.JLabel();
        appointmentsHeading = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        aptstable = new javax.swing.JTable();
        quickactions = new javax.swing.JLabel();
        quickbook = new javax.swing.JButton();
        quickcancel = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(1100, 700));
        setLayout(new java.awt.BorderLayout());

        sidebarpanel.setBackground(new java.awt.Color(0, 51, 102));
        sidebarpanel.setPreferredSize(new java.awt.Dimension(220, 700));

        titlelabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        titlelabel.setForeground(new java.awt.Color(255, 255, 255));
        titlelabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titlelabel.setText("STUDENT PORTAL");
        titlelabel.setToolTipText("");

        studentdashboard.setText("Dashboard");
        studentdashboard.addActionListener(this::studentdashboardActionPerformed);

        bookappointment.setText("Book Appointment");

        myappointments.setText("My Appointments");
        myappointments.addActionListener(this::myappointmentsActionPerformed);

        studentprofile.setText("My Profile");

        logout.setText("Logout");

        jButton1.setText("Delete Account");

        javax.swing.GroupLayout sidebarpanelLayout = new javax.swing.GroupLayout(sidebarpanel);
        sidebarpanel.setLayout(sidebarpanelLayout);
        sidebarpanelLayout.setHorizontalGroup(
            sidebarpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarpanelLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(sidebarpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(titlelabel)
                    .addGroup(sidebarpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(logout, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(studentprofile, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(myappointments, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(studentdashboard, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(bookappointment, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        sidebarpanelLayout.setVerticalGroup(
            sidebarpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebarpanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(titlelabel)
                .addGap(35, 35, 35)
                .addComponent(studentdashboard)
                .addGap(44, 44, 44)
                .addComponent(bookappointment)
                .addGap(40, 40, 40)
                .addComponent(studentprofile)
                .addGap(40, 40, 40)
                .addComponent(myappointments)
                .addGap(41, 41, 41)
                .addComponent(logout)
                .addGap(61, 61, 61)
                .addComponent(jButton1)
                .addContainerGap(187, Short.MAX_VALUE))
        );

        add(sidebarpanel, java.awt.BorderLayout.LINE_START);

        studentcontentpanel.setBackground(new java.awt.Color(204, 204, 255));
        studentcontentpanel.setPreferredSize(new java.awt.Dimension(880, 700));

        dashboardheading.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        dashboardheading.setForeground(new java.awt.Color(51, 51, 51));
        dashboardheading.setText("Student Dashboard");

        WelcomeMessage.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        WelcomeMessage.setForeground(new java.awt.Color(102, 102, 102));
        WelcomeMessage.setText("Welcome back! Here's an overview of your counseling activity.");

        upcomingcardPanel.setBackground(new java.awt.Color(204, 255, 255));
        upcomingcardPanel.setPreferredSize(new java.awt.Dimension(230, 120));

        upcommingapts.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        upcommingapts.setText("UPCOMING APPOINTMENTS");

        upcomingcount.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        upcomingcount.setText("0");

        javax.swing.GroupLayout upcomingcardPanelLayout = new javax.swing.GroupLayout(upcomingcardPanel);
        upcomingcardPanel.setLayout(upcomingcardPanelLayout);
        upcomingcardPanelLayout.setHorizontalGroup(
            upcomingcardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upcomingcardPanelLayout.createSequentialGroup()
                .addGroup(upcomingcardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(upcomingcardPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(upcommingapts))
                    .addGroup(upcomingcardPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(upcomingcount, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(65, Short.MAX_VALUE))
        );
        upcomingcardPanelLayout.setVerticalGroup(
            upcomingcardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upcomingcardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(upcommingapts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(upcomingcount)
                .addContainerGap(48, Short.MAX_VALUE))
        );

        pendingcardPanel.setBackground(new java.awt.Color(255, 255, 153));
        pendingcardPanel.setPreferredSize(new java.awt.Dimension(230, 120));

        pendingapts.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        pendingapts.setText("PENDING APPOINTMENTS");

        pendingcount.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        pendingcount.setText("0");

        javax.swing.GroupLayout pendingcardPanelLayout = new javax.swing.GroupLayout(pendingcardPanel);
        pendingcardPanel.setLayout(pendingcardPanelLayout);
        pendingcardPanelLayout.setHorizontalGroup(
            pendingcardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pendingcardPanelLayout.createSequentialGroup()
                .addGroup(pendingcardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pendingcardPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pendingapts))
                    .addGroup(pendingcardPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(pendingcount, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(77, Short.MAX_VALUE))
        );
        pendingcardPanelLayout.setVerticalGroup(
            pendingcardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pendingcardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pendingapts)
                .addGap(18, 18, 18)
                .addComponent(pendingcount)
                .addContainerGap(42, Short.MAX_VALUE))
        );

        completedcardPanel.setBackground(new java.awt.Color(153, 255, 153));
        completedcardPanel.setPreferredSize(new java.awt.Dimension(230, 120));

        completedapts.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        completedapts.setText("COMPLETED APPOINTMENTS");

        completedcount.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        completedcount.setText("0");

        javax.swing.GroupLayout completedcardPanelLayout = new javax.swing.GroupLayout(completedcardPanel);
        completedcardPanel.setLayout(completedcardPanelLayout);
        completedcardPanelLayout.setHorizontalGroup(
            completedcardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(completedcardPanelLayout.createSequentialGroup()
                .addGroup(completedcardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(completedcardPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(completedapts))
                    .addGroup(completedcardPanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(completedcount, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(62, Short.MAX_VALUE))
        );
        completedcardPanelLayout.setVerticalGroup(
            completedcardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(completedcardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(completedapts)
                .addGap(18, 18, 18)
                .addComponent(completedcount)
                .addContainerGap(42, Short.MAX_VALUE))
        );

        appointmentsHeading.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        appointmentsHeading.setText("UPCOMING APPOINTMENTS");

        aptstable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Appointment IIDD", "Counsellor", "Date", "Time", "Status"
            }
        ));
        jScrollPane1.setViewportView(aptstable);

        quickactions.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        quickactions.setText("QUICK ACTIONS");

        quickbook.setBackground(new java.awt.Color(0, 102, 153));
        quickbook.setForeground(new java.awt.Color(255, 255, 255));
        quickbook.setText("Book an Appointment");

        quickcancel.setBackground(new java.awt.Color(102, 102, 102));
        quickcancel.setText("Cancel / Reschedule");
        quickcancel.addActionListener(this::quickcancelActionPerformed);

        jButton2.setText("View Appointments");

        jButton3.setText("Give Feedback");

        javax.swing.GroupLayout studentcontentpanelLayout = new javax.swing.GroupLayout(studentcontentpanel);
        studentcontentpanel.setLayout(studentcontentpanelLayout);
        studentcontentpanelLayout.setHorizontalGroup(
            studentcontentpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentcontentpanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(studentcontentpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dashboardheading)
                    .addGroup(studentcontentpanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(studentcontentpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(studentcontentpanelLayout.createSequentialGroup()
                                .addComponent(upcomingcardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pendingcardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(completedcardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(WelcomeMessage)
                            .addGroup(studentcontentpanelLayout.createSequentialGroup()
                                .addComponent(quickbook)
                                .addGap(36, 36, 36)
                                .addComponent(quickcancel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3))
                            .addGroup(studentcontentpanelLayout.createSequentialGroup()
                                .addGap(95, 95, 95)
                                .addComponent(quickactions))
                            .addGroup(studentcontentpanelLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(appointmentsHeading))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        studentcontentpanelLayout.setVerticalGroup(
            studentcontentpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(studentcontentpanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dashboardheading)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(WelcomeMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(studentcontentpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(upcomingcardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pendingcardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(completedcardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(appointmentsHeading)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(quickactions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(studentcontentpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(quickbook)
                    .addComponent(quickcancel)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap(8, Short.MAX_VALUE))
        );

        add(studentcontentpanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void quickcancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickcancelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_quickcancelActionPerformed

    private void myappointmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myappointmentsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_myappointmentsActionPerformed

    private void studentdashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_studentdashboardActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_studentdashboardActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel WelcomeMessage;
    private javax.swing.JLabel appointmentsHeading;
    private javax.swing.JTable aptstable;
    private javax.swing.JButton bookappointment;
    private javax.swing.JLabel completedapts;
    private javax.swing.JPanel completedcardPanel;
    private javax.swing.JLabel completedcount;
    private javax.swing.JLabel dashboardheading;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton logout;
    private javax.swing.JButton myappointments;
    private javax.swing.JLabel pendingapts;
    private javax.swing.JPanel pendingcardPanel;
    private javax.swing.JLabel pendingcount;
    private javax.swing.JLabel quickactions;
    private javax.swing.JButton quickbook;
    private javax.swing.JButton quickcancel;
    private javax.swing.JPanel sidebarpanel;
    private javax.swing.JPanel studentcontentpanel;
    private javax.swing.JButton studentdashboard;
    private javax.swing.JButton studentprofile;
    private javax.swing.JLabel titlelabel;
    private javax.swing.JPanel upcomingcardPanel;
    private javax.swing.JLabel upcomingcount;
    private javax.swing.JLabel upcommingapts;
    // End of variables declaration//GEN-END:variables
}
