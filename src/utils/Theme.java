package utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Shared colours, fonts and drawing helpers so every form looks consistent.
 * Usage: Theme.GOLD, Theme.font(Font.BOLD, 18f)
 */
public final class Theme {

    private Theme() { }

    // ---- Palette: deep navy + warm gold, with calming teal accents ----
    public static final Color NAVY_DEEP  = new Color(0x06, 0x16, 0x2B);
    public static final Color NAVY       = new Color(0x0C, 0x2A, 0x4D);
    public static final Color BLUE       = new Color(0x1A, 0x4B, 0x84);
    public static final Color TEAL       = new Color(0x2E, 0xC4, 0xB6);
    public static final Color GOLD       = new Color(0xF5, 0xB3, 0x35);
    public static final Color GOLD_LIGHT = new Color(0xFF, 0xD7, 0x7A);
    public static final Color CORAL      = new Color(0xFF, 0x7A, 0x7A);
    public static final Color LAVENDER   = new Color(0xA8, 0x8B, 0xFF);
    public static final Color TEXT       = new Color(0xF4, 0xF7, 0xFB);
    public static final Color TEXT_MUTED = new Color(0xB4, 0xC3, 0xD8);

    private static String family;

    /** Returns the best available modern font family on this machine. */
    public static Font font(int style, float size) {
        if (family == null) {
            Set<String> installed = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            family = Font.SANS_SERIF;
            for (String f : new String[]{"Segoe UI", "Inter", "Helvetica Neue", "SF Pro Text", "Ubuntu", "Noto Sans"}) {
                if (installed.contains(f)) { family = f; break; }
            }
        }
        return new Font(family, style, Math.round(size)).deriveFont(size);
    }

    /** Adds letter-spacing (tracking) to a font - great for small caps labels. */
    public static Font tracked(Font f, float tracking) {
        Map<TextAttribute, Object> m = new HashMap<>();
        m.put(TextAttribute.TRACKING, tracking);
        return f.deriveFont(m);
    }

    public static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    /** Linear blend between two colours (t = 0 gives a, t = 1 gives b). */
    public static Color mix(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }

    /** Turns on every quality switch Java2D offers. */
    public static void hints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }
}
