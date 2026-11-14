package studentcounsellingsystem;

import forms.WelcomePanel;
import forms.student;
import java.awt.CardLayout;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The ONLY window of the application. Every screen is a JPanel (1100 x 700)
 * registered here and shown with showScreen("NAME").
 *
 * Teammates: add your panel in the constructor with
 *     addScreen("LOGIN", new LoginPanel());
 */
public class MainFrame extends javax.swing.JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Set<String> screens = new HashSet<>();
    private String selectedRole;     // "STUDENT", "COUNSELLOR" or "ADMIN" (set on the welcome page)
    private Point dragOffset;

    public MainFrame() {
        setTitle("USIU-Africa | Student Counselling System");
        setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(cards);

        // ---- register screens here ----
        addScreen("WELCOME", new WelcomePanel());
        // addScreen("LOGIN", new LoginPanel());
        addScreen("STUDENT_DASHBOARD", new student());
        // addScreen("COUNSELLOR_DASHBOARD", new CounsellorDashboardPanel());

        pack();                          // size comes from the panels (1100 x 700)
        setLocationRelativeTo(null);
        setupWindow();
        showScreen("WELCOME");
    }

    public void addScreen(String name, JPanel panel) {
        cards.add(panel, name);
        screens.add(name);
    }

    public void showScreen(String name) {
        if (!screens.contains(name)) {
            JOptionPane.showMessageDialog(this, "The " + name + " screen has not been added yet.");
            return;
        }
        cardLayout.show(cards, name);
    }

    public String getSelectedRole() { return selectedRole; }
    public void setSelectedRole(String role) { this.selectedRole = role; }

    /** Rounded corners, drag from anywhere, Esc to exit. */
    private void setupWindow() {
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                try { setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30)); }
                catch (UnsupportedOperationException ignored) { /* platform has no shaped windows */ }
            }
        });
        MouseAdapter drag = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragOffset = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) {
                if (dragOffset != null) { setLocation(e.getXOnScreen() - dragOffset.x, e.getYOnScreen() - dragOffset.y); }
            }
        };
        cards.addMouseListener(drag);
        cards.addMouseMotionListener(drag);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"), "exit");
        getRootPane().getActionMap().put("exit", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { System.exit(0); }
        });
    }
}
