package forms;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class Glass {

    // ---- tweak these ----
    static final Color GREEN       = new Color(70, 74, 79);
    static final Color GREEN_HOVER = new Color(92, 97, 104);
    static final Color GREEN_DARK  = new Color(44, 47, 51);
    static final int CARD_ARC = 30;      // corner roundness
    static final int CARD_ALPHA = 70;    // 0 = invisible, 255 = solid
    static final int CARD_SHADOW = 17;   // shadow size in pixels

    private static Graphics2D prep(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g2;
    }

    private static void paintGlass(Graphics g, JComponent c, int arc, int fillAlpha) {
        Graphics2D g2 = prep(g);
        g2.setColor(new Color(255, 255, 255, fillAlpha));
        g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, arc, arc);
        g2.setColor(new Color(255, 255, 255, 140));
        g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, arc, arc);
        g2.dispose();
    }

    public static class Panel extends JPanel {
        public Panel() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = prep(g);
            int m = CARD_SHADOW;
            RoundRectangle2D card = new RoundRectangle2D.Double(
                    m, m, getWidth() - 2 * m - 1, getHeight() - 2 * m - 1, CARD_ARC, CARD_ARC);

            // shadow, drawn only outside the card so it doesn't darken the glass
            Area outside = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
            outside.subtract(new Area(card));
            g2.setClip(outside);
            for (int i = 1; i <= m; i++) {
                g2.setColor(new Color(0, 0, 0, 4));
                g2.fill(new RoundRectangle2D.Double(m - i, m - i + 3,
                        card.getWidth() + 2 * i, card.getHeight() + 2 * i, CARD_ARC + 2 * i, CARD_ARC + 2 * i));
            }
            g2.setClip(null);

            // glass
            g2.setColor(new Color(255, 255, 255, CARD_ALPHA));
            g2.fill(card);
            g2.setColor(new Color(255, 255, 255, 140));
            g2.draw(card);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class TextField extends JTextField {
        public TextField() { setOpaque(false); setBorder(null); }
        @Override public void setBorder(Border b) { super.setBorder(new EmptyBorder(0, 12, 0, 12)); }
        @Override protected void paintComponent(Graphics g) {
            paintGlass(g, this, 20, 90);
            super.paintComponent(g);
        }
    }   // <- TextField closes here

    public static class ComboBox extends JComboBox<String> {
        public ComboBox() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 6, 0, 6));
            setUI(new BasicComboBoxUI() {
                @Override protected JButton createArrowButton() {
                    Color none = new Color(0, 0, 0, 0);
                    BasicArrowButton b = new BasicArrowButton(SwingConstants.SOUTH,
                            none, none, new Color(70, 70, 70), none);
                    b.setOpaque(false);
                    b.setContentAreaFilled(false);
                    b.setBorder(null);
                    return b;
                }
                @Override public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean hasFocus) { }
            });
            setRenderer(new DefaultListCellRenderer() {
                @Override public java.awt.Component getListCellRendererComponent(
                        JList<?> list, Object value, int index, boolean sel, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, sel, focus);
                    setOpaque(index >= 0);                 // closed box stays see-through
                    setBorder(new EmptyBorder(0, 10, 0, 0));
                    return this;
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            paintGlass(g, this, 20, 90);
            super.paintComponent(g);
        }
    }   // <- ComboBox closes here

    public static class PasswordField extends JPasswordField {
    private final char defaultEcho = getEchoChar();
    private boolean shown = false;
    private static final int EYE_ZONE = 40;   // clickable area on the right

    public PasswordField() {
        setOpaque(false);
        setBorder(null);
        MouseAdapter m = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getX() > getWidth() - EYE_ZONE) {
                    shown = !shown;
                    setEchoChar(shown ? (char) 0 : defaultEcho);
                    repaint();
                }
            }
            @Override public void mouseMoved(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(
                        e.getX() > getWidth() - EYE_ZONE ? Cursor.HAND_CURSOR : Cursor.TEXT_CURSOR));
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    @Override public void setBorder(Border b) { super.setBorder(new EmptyBorder(0, 12, 0, EYE_ZONE)); }

    @Override protected void paintComponent(Graphics g) {
        paintGlass(g, this, 20, 90);
        super.paintComponent(g);

        Graphics2D g2 = prep(g);
        int cx = getWidth() - 20, cy = getHeight() / 2;
    g2.setColor(new Color(90, 90, 90));
    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    Path2D eye = new Path2D.Double();           // almond-shaped eye outline
    eye.moveTo(cx - 7, cy);
    eye.quadTo(cx, cy - 8, cx + 7, cy);
    eye.quadTo(cx, cy + 8, cx - 7, cy);
    g2.draw(eye);
    g2.fillOval(cx - 2, cy - 2, 4, 4);           // pupil

    if (!shown) {                                // slash when the password is hidden
        g2.drawLine(cx - 6, cy + 6, cx + 6, cy - 6);
    }
    g2.dispose();
}
    }

    public static class Button extends JButton {
        public Button() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = prep(g);
            boolean pressed = getModel().isPressed();
            int m = 5;                                  // room for the shadow
            int w = getWidth() - 2 * m;
            int h = getHeight() - 2 * m - 2;
            int y = pressed ? m + 2 : m;                // pushes down when clicked

            if (!pressed) {                             // shadow
                for (int i = 0; i < m; i++) {
                    g2.setColor(new Color(0, 0, 0, 18));
                    g2.fillRoundRect(m - i, y - i + 3, w + 2 * i, h + 2 * i, 20 + 2 * i, 20 + 2 * i);
                }
            }
            Color top = getModel().isRollover() ? GREEN_HOVER : GREEN;
            g2.setPaint(new GradientPaint(0, y, top, 0, y + h, GREEN_DARK));
            g2.fillRoundRect(m, y, w, h, 20, 20);
            g2.setColor(new Color(255, 255, 255, 90));  // light edge on top
            g2.drawRoundRect(m, y, w - 1, h - 1, 20, 20);
            g2.dispose();
            super.paintComponent(g);                    // draws the text
        }
    }
}