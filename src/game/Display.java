package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Display extends JPanel {
    int nW;
    int nH;
    ArrayList<int []> elements;
    Point position;
    int nEnemyTypes;
    BufferedImage [] monsterSprites;
    BufferedImage heroSprite;
    BufferedImage background;
    public Display(int nW, int nH, ArrayList<int []> elements, Point position, int nEnemyTypes) {
        this.nW = nW;
        this.nH = nH;
        this.elements = elements;
        this.position = position;
        this.nEnemyTypes = nEnemyTypes;
        setOpaque(true);
    }

    BufferedImage loadSprite(URL file, int w, int h) {
        return loadSprite(file, w, h, true);
    }
    BufferedImage loadSprite(URL file, int w, int h, boolean BOTH_AXES) {
        try {
            final BufferedImage original = ImageIO.read(file);
            final BufferedImage resized = new BufferedImage(w, w, original.getType());
            final Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                               RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (!BOTH_AXES) {
                int _h = (int) (original.getHeight() * ((float) w / original.getWidth()));
                if (_h > h)
                    h = _h;
                else
                    w = (int) (original.getWidth() * ((float) h / original.getHeight()));
            }
            g.drawImage(original, 0, 0, w, h, 0, 0, original.getWidth(),
                        original.getHeight(), null);
            g.dispose();
            return resized;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    void initializeSprites() {
        final int w = getWidth();
        final int h = getHeight();
        final int cellW = (int) ((float) w / nW);
        final int cellH = (int) ((float) h / nH);
        monsterSprites = new BufferedImage[nEnemyTypes];
        for (int i = 0; i < nEnemyTypes; ++i)
            monsterSprites[i] = loadSprite(this.getClass().getResource("/game/sprites/monster" + (i + 1) +  ".png"), cellW, cellH);
        heroSprite = loadSprite(this.getClass().getResource("/game/sprites/hero.png"), cellW, cellH);
        background = loadSprite(this.getClass().getResource("/game/sprites/space.png"), w, h, false);
    }
    public void init() {
        initializeSprites();
    }
    public void updateContents() {
        repaint();
    }
    @Override
    public void paint(Graphics _g) {
        final Graphics2D g = (Graphics2D) _g.create();
        super.paintComponent(g);
        final int w = getWidth();
        final int h = getHeight();
        if (background != null)
            g.drawImage(background, 0, 0, this);
        final int cellW = (int) ((float) w / nW);
        final int cellH = (int) ((float) h / nH);
        for (int _j = 0, len = elements.size(); _j < len; ++_j) {
            final int x = w - (int) ((_j + 1) * (float) w / nW);
            final int [] column = elements.get(len - _j - 1);
            for (int i = 0; i < nH; ++i) {
                final int enemyId = column[i];
                if (enemyId != 0) {
                    final int y = (int) (i * (float) h / nH);
                    final Image sprite = monsterSprites[enemyId - 1];
                    g.drawImage(sprite, x, y, this);
                }
            }
        }
        {
            final int x = (int) (position.x * (float) w / nW);
            final int y = (int) (position.y * (float) h / nH);
            g.drawImage(heroSprite, x, y, this);
        }
        if (GAME_OVER) {
            final String text = "GAME OVER";
            final Font font = new Font("sansserif", Font.BOLD, (int) ((float) h / 10));
            final FontMetrics metrics = g.getFontMetrics(font);
            final int x = (w - metrics.stringWidth(text)) / 2;
            final int y = ((h - metrics.getHeight()) / 2) + metrics.getAscent();
            g.setColor(Color.RED);
            g.setFont(font);
            g.drawString(text, x, y);
        }
    }
    boolean GAME_OVER = false;
    public void gameOver() {
        this.GAME_OVER = true;
    }
}
