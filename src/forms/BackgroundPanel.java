package forms;

import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class BackgroundPanel extends JPanel {

    private Image backgroundImage;

    public BackgroundPanel(String resourcePath) {
        try {
            backgroundImage = ImageIO.read(
                getClass().getResource(resourcePath)
            );
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading background: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(
                backgroundImage,
                0, 0,
                getWidth(),
                getHeight(),
                this
            );
        }
    }
}