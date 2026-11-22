package forms;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.Timer;

public class RoleToggle extends JPanel {

    private boolean counsellor = false;
    private double pos = 0;                  // 0 = Student, 1 = Counsellor (animated)
    private Consumer<String> onChange;
    private final Timer timer;

    public RoleToggle() {
        setOpaque(false);
        setPreferredSize(new Dimension(240, 44));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        timer = new Timer(10, e -> {
            double target = counsellor ? 1 : 0;
            pos += (target - pos) * 0.25;                    // eases into place
            if (Math.abs(target - pos) < 0.01) {
                pos = target;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                setCounsellor(e.getX() > getWidth() / 2);    // click either half
            }
        });
    }

    public boolean isCounsellor() { return counsellor; }
    public String getRole() { return counsellor ? "COUNSELLOR" : "STUDENT"; }
    public void setOnChange(Consumer<String> c) { onChange = c; }

    private void setCounsellor(boolean c) {
        if (c == counsellor) return;
        counsellor = c;
        timer.start();
        if (onChange != null) onChange.accept(getRole());
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // track
        g2.setColor(new Color(255, 255, 255, 90));
        g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
        g2.setColor(new Color(255, 255, 255, 140));
        g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);

        // sliding knob
        int pad = 4;
        int kw = w / 2 - pad, kh = h - 2 * pad;
        int kx = pad + (int) Math.round(pos * (w / 2 - pad));
        g2.setPaint(new GradientPaint(0, pad, Glass.GREEN, 0, pad + kh, Glass.GREEN_DARK));
        g2.fillRoundRect(kx, pad, kw, kh, kh, kh);

        // labels: the one under the knob turns white
        g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
        drawLabel(g2, "Student", 0, w / 2, h, 1 - pos);
        drawLabel(g2, "Counsellor", w / 2, w / 2, h, pos);
        g2.dispose();
    }

    private void drawLabel(Graphics2D g2, String s, int x, int width, int h, double t) {
        Color dark = new Color(50, 50, 50), light = Color.WHITE;
        g2.setColor(new Color(
                (int) (dark.getRed()   + (light.getRed()   - dark.getRed())   * t),
                (int) (dark.getGreen() + (light.getGreen() - dark.getGreen()) * t),
                (int) (dark.getBlue()  + (light.getBlue()  - dark.getBlue())  * t)));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, x + (width - fm.stringWidth(s)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
    }
}