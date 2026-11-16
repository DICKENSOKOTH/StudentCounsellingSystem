package utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A living background: a navy gradient, slowly drifting colour "auras",
 * rising particles and gentle waves - all painted with Java2D and driven
 * by a javax.swing.Timer (~60 fps). Use it as a content pane.
 */
public class AnimatedBackground extends JPanel {

    private static final class Aura {
        final float fx, fy, radius, phase; final Color color;
        Aura(float fx, float fy, float radius, float phase, Color color) {
            this.fx = fx; this.fy = fy; this.radius = radius; this.phase = phase; this.color = color;
        }
    }

    private static final class Particle {
        float fx, fy, size, speed, phase;
    }

    private final List<Aura> auras = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final Timer timer;
    private float time;

    public AnimatedBackground() {
        setOpaque(true);

        auras.add(new Aura(0.12f, 0.20f, 0.46f, 0.0f, Theme.alpha(Theme.TEAL, 80)));
        auras.add(new Aura(0.88f, 0.12f, 0.42f, 1.7f, Theme.alpha(new Color(0x3A, 0x7B, 0xD5), 120)));
        auras.add(new Aura(0.72f, 0.95f, 0.50f, 3.1f, Theme.alpha(Theme.GOLD, 46)));
        auras.add(new Aura(0.05f, 0.98f, 0.36f, 4.6f, Theme.alpha(Theme.LAVENDER, 54)));

        Random rnd = new Random(7);
        for (int i = 0; i < 55; i++) {
            Particle p = new Particle();
            p.fx = rnd.nextFloat();
            p.fy = rnd.nextFloat();
            p.size = 1.5f + rnd.nextFloat() * 3.5f;
            p.speed = 0.0003f + rnd.nextFloat() * 0.0011f;
            p.phase = rnd.nextFloat() * 6.28f;
            particles.add(p);
        }

        timer = new Timer(16, e -> {
            time += 0.016f;
            for (Particle p : particles) {
                p.fy -= p.speed;
                if (p.fy < -0.03f) { p.fy = 1.03f; }
            }
            repaint();
        });
    }

    @Override public void addNotify()    { super.addNotify();    timer.start(); }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.hints(g2);
        int w = getWidth(), h = getHeight();

        // 1. Base gradient
        g2.setPaint(new GradientPaint(0, 0, Theme.NAVY_DEEP, w, h, Theme.NAVY));
        g2.fillRect(0, 0, w, h);

        // 2. Drifting colour auras
        int big = Math.max(w, h);
        for (Aura a : auras) {
            float cx = (a.fx + 0.04f * (float) Math.sin(time * 0.6f + a.phase)) * w;
            float cy = (a.fy + 0.05f * (float) Math.cos(time * 0.5f + a.phase)) * h;
            float r = a.radius * big;
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), r,
                    new float[]{0f, 1f}, new Color[]{a.color, Theme.alpha(a.color, 0)}));
            g2.fill(new Rectangle2D.Float(cx - r, cy - r, r * 2, r * 2));
        }

        // 3. Decorative concentric rings (top-right)
        g2.setStroke(new BasicStroke(1f));
        for (int i = 1; i <= 4; i++) {
            float d = 160 + i * 110 + 8 * (float) Math.sin(time + i);
            g2.setColor(Theme.alpha(Color.WHITE, 12 - i * 2));
            g2.draw(new Ellipse2D.Float(w * 0.92f - d / 2, h * 0.30f - d / 2, d, d));
        }

        // 4. Rising, twinkling particles
        for (Particle p : particles) {
            float x = (p.fx + 0.012f * (float) Math.sin(time * 0.8f + p.phase)) * w;
            float y = p.fy * h;
            int a = (int) (50 + 70 * (0.5 + 0.5 * Math.sin(time * 2f + p.phase)));
            g2.setColor(Theme.alpha(Theme.GOLD_LIGHT, a));
            g2.fill(new Ellipse2D.Float(x, y, p.size, p.size));
        }

        // 5. Two soft waves at the bottom
        paintWave(g2, w, h, 0.90f, 14f, 0.008f, 0.9f, Theme.alpha(Color.WHITE, 12));
        paintWave(g2, w, h, 0.94f, 10f, 0.011f, -1.2f, Theme.alpha(Theme.TEAL, 20));

        g2.dispose();
    }

    private void paintWave(Graphics2D g2, int w, int h, float base, float amp, float freq, float speed, Color c) {
        Path2D wave = new Path2D.Float();
        wave.moveTo(0, h);
        for (int x = 0; x <= w; x += 8) {
            double y = h * base + amp * Math.sin(x * freq + time * speed);
            wave.lineTo(x, y);
        }
        wave.lineTo(w, h);
        wave.closePath();
        g2.setColor(c);
        g2.fill(wave);
    }
}
