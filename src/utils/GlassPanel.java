package utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Frosted-glass card (a normal JPanel you can drop labels into).
 * Paints a soft shadow, an accent icon on the left and an arrow on the right,
 * and glows when the mouse hovers. Use mouseClicked in the form to handle clicks.
 */
public class GlassPanel extends JPanel {

    public enum Icon { STUDENT, COUNSELLOR, ADMIN }

    private final Color accent;
    private final Icon icon;
    private float hover;
    private boolean hovering;
    private final Timer anim;

    public GlassPanel() { this(Theme.TEAL, Icon.STUDENT); }

    public GlassPanel(Color accent, Icon icon) {
        this.accent = accent;
        this.icon = icon;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        anim = new Timer(15, null);
        anim.addActionListener(e -> {
            float target = hovering ? 1f : 0f;
            hover += (target - hover) * 0.2f;
            if (Math.abs(target - hover) < 0.01f) { hover = target; anim.stop(); }
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true;  anim.start(); }
            @Override public void mouseExited(MouseEvent e)  { hovering = false; anim.start(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.hints(g2);
        int bx = 6, by = 4, bw = getWidth() - 12, bh = getHeight() - 14, arc = 30;

        // soft shadow
        for (int i = 1; i <= 9; i++) {
            g2.setColor(new Color(0, 0, 0, (int) ((5 + 9 * hover) * (1 - i / 10f))));
            g2.fill(new RoundRectangle2D.Float(bx - i, by - i + 6, bw + 2 * i, bh + 2 * i, arc + i, arc + i));
        }
        // glass body + border (accent glow on hover)
        RoundRectangle2D body = new RoundRectangle2D.Float(bx, by, bw, bh, arc, arc);
        g2.setPaint(new GradientPaint(bx, by, Theme.alpha(Color.WHITE, 40 + (int) (22 * hover)),
                bx, by + bh, Theme.alpha(Color.WHITE, 14 + (int) (10 * hover))));
        g2.fill(body);
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(Theme.alpha(Color.WHITE, 50));
        g2.draw(body);
        g2.setColor(Theme.alpha(accent, (int) (200 * hover)));
        g2.draw(body);

        // icon disc
        int d = 64, ix = bx + 22, iy = by + (bh - d) / 2;
        float cx = ix + d / 2f, cy = iy + d / 2f;
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), 56f, new float[]{0.4f, 1f},
                new Color[]{Theme.alpha(accent, (int) (40 + 70 * hover)), Theme.alpha(accent, 0)}));
        g2.fill(new Ellipse2D.Float(cx - 56, cy - 56, 112, 112));
        g2.setPaint(new GradientPaint(ix, iy, Theme.mix(accent, Color.WHITE, 0.25f), ix + d, iy + d, Theme.mix(accent, Theme.NAVY, 0.35f)));
        g2.fill(new Ellipse2D.Float(ix, iy, d, d));
        drawIcon(g2, cx, cy);

        // arrow button (fills with the accent on hover)
        float ax = bx + bw - 34 + hover * 3, ay = by + bh / 2f;
        g2.setColor(Theme.alpha(accent, (int) (28 + 227 * hover)));
        g2.fill(new Ellipse2D.Float(ax - 16, ay - 16, 32, 32));
        g2.setColor(Theme.mix(accent, Theme.NAVY_DEEP, hover));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(Math.round(ax - 6), Math.round(ay), Math.round(ax + 6), Math.round(ay));
        g2.drawLine(Math.round(ax + 1), Math.round(ay - 5), Math.round(ax + 6), Math.round(ay));
        g2.drawLine(Math.round(ax + 1), Math.round(ay + 5), Math.round(ax + 6), Math.round(ay));
        g2.dispose();
    }

    /** Vector icons, no image files needed. */
    private void drawIcon(Graphics2D g2, float cx, float cy) {
        g2.setColor(Color.WHITE);
        switch (icon) {
            case STUDENT: {
                g2.fill(new Ellipse2D.Float(cx - 8, cy - 18, 16, 16));
                Graphics2D c = (Graphics2D) g2.create();
                c.clip(new Rectangle(Math.round(cx - 22), Math.round(cy - 1), 44, 21));
                c.fill(new Ellipse2D.Float(cx - 17, cy + 1, 34, 34));
                c.dispose();
                break;
            }
            case COUNSELLOR: {
                Path2D heart = new Path2D.Float();
                heart.moveTo(cx, cy + 15);
                heart.curveTo(cx - 28, cy - 2, cx - 13, cy - 24, cx, cy - 9);
                heart.curveTo(cx + 13, cy - 24, cx + 28, cy - 2, cx, cy + 15);
                heart.closePath();
                g2.fill(heart);
                break;
            }
            default: { // ADMIN: shield with a tick
                Path2D shield = new Path2D.Float();
                shield.moveTo(cx, cy - 19);
                shield.lineTo(cx + 15, cy - 12);
                shield.lineTo(cx + 15, cy + 2);
                shield.curveTo(cx + 15, cy + 12, cx + 6, cy + 17, cx, cy + 20);
                shield.curveTo(cx - 6, cy + 17, cx - 15, cy + 12, cx - 15, cy + 2);
                shield.lineTo(cx - 15, cy - 12);
                shield.closePath();
                g2.fill(shield);
                g2.setColor(Theme.mix(accent, Theme.NAVY, 0.45f));
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(Math.round(cx - 6), Math.round(cy + 1), Math.round(cx - 1), Math.round(cy + 7));
                g2.drawLine(Math.round(cx - 1), Math.round(cy + 7), Math.round(cx + 8), Math.round(cy - 6));
            }
        }
    }
}
