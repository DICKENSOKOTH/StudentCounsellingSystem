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
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;


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

    // ------------------------------------------------------------------
    public static class Panel extends JPanel {
        private String note = null;                 // small red message under a child component
        private Component noteAnchor = null;

        public Panel() { setOpaque(false); }

        /** Shows a small red message just under the anchor component. Pass null to clear it. */
        public void setNote(Component anchor, String text) {
            noteAnchor = anchor;
            note = text;
            repaint();
        }

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

            if (note != null && noteAnchor != null) {   // error message under the anchor
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
                g2.setColor(new Color(200, 40, 40));
                g2.drawString(note, noteAnchor.getX() + 4, noteAnchor.getY() + noteAnchor.getHeight() + 13);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------------
    public static class TextField extends JTextField {
        public TextField() { setOpaque(false); setBorder(null); }
        @Override public void setBorder(Border b) { super.setBorder(new EmptyBorder(0, 12, 0, 12)); }
        @Override protected void paintComponent(Graphics g) {
            paintGlass(g, this, 20, 90);
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------------
    public static class ComboBox extends JComboBox<String> {
        public ComboBox() {
            setOpaque(false);
            setForeground(new Color(40, 40, 40));
            setBorder(new EmptyBorder(0, 6, 0, 6));
            setUI(new BasicComboBoxUI() {
                @Override protected JButton createArrowButton() {
                    // a plain button that paints only a small chevron (no square fill, no bevel)
                    JButton b = new JButton() {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = prep(g);
                            g2.setColor(new Color(70, 70, 70));
                            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            int cx = getWidth() / 2, cy = getHeight() / 2;
                            g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                            g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                            g2.dispose();
                        }
                    };
                    b.setOpaque(false);
                    b.setContentAreaFilled(false);
                    b.setBorderPainted(false);
                    b.setFocusPainted(false);
                    b.setBorder(null);
                    b.setBackground(new Color(0, 0, 0, 0));
                    return b;
                }
                @Override protected ComboPopup createPopup() {
                    BasicComboPopup p = (BasicComboPopup) super.createPopup();
                    p.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                    return p;
                }
                @Override public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean hasFocus) { }
            });
            setRenderer(new DefaultListCellRenderer() {
                @Override public java.awt.Component getListCellRendererComponent(
                        JList<?> list, Object value, int index, boolean sel, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, sel, focus);
                    setBorder(new EmptyBorder(6, 10, 6, 10));
                    if (index >= 0) {                                   // items in the open list
                        setOpaque(true);
                        setBackground(sel ? GREEN : new Color(250, 250, 250));
                        setForeground(sel ? Color.WHITE : new Color(40, 40, 40));
                    } else {                                            // the closed box stays see-through
                        setOpaque(false);
                        setForeground(new Color(40, 40, 40));
                    }
                    return this;
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            paintGlass(g, this, 20, 90);
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------------
    public static class PasswordField extends JPasswordField {
        private final char defaultEcho = getEchoChar();
        private boolean shown = false;
        private boolean error = false;                 // red outline when true
        private Timer shakeTimer;
        private static final int EYE_ZONE = 40;        // clickable area on the right

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

        /** Turns the red outline on or off. */
        public void setError(boolean e) {
            error = e;
            repaint();
        }

        /** Shakes the field left and right once. */
        public void shake() {
            if (shakeTimer != null && shakeTimer.isRunning()) return;
            final Point origin = getLocation();
            final int[] offsets = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
            final int[] i = {0};
            shakeTimer = new Timer(35, e -> {
                setLocation(origin.x + offsets[i[0]], origin.y);
                if (++i[0] >= offsets.length) {
                    ((Timer) e.getSource()).stop();
                    setLocation(origin);
                }
            });
            shakeTimer.start();
        }

        @Override public void setBorder(Border b) { super.setBorder(new EmptyBorder(0, 12, 0, EYE_ZONE)); }

        @Override protected void paintComponent(Graphics g) {
            paintGlass(g, this, 20, 90);
            super.paintComponent(g);

            if (error) {                                 // red outline
                Graphics2D r = prep(g);
                r.setColor(new Color(220, 60, 60));
                r.setStroke(new BasicStroke(1.8f));
                r.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                r.dispose();
            }

            Graphics2D g2 = prep(g);
            int cx = getWidth() - 20, cy = getHeight() / 2;
            g2.setColor(new Color(90, 90, 90));
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D eye = new Path2D.Double();            // almond-shaped eye outline
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

    // ------------------------------------------------------------------
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
