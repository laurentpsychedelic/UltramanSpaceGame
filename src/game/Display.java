package game;

import game.Engine.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.Thread;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Display extends JPanel {
    int nW;
    int nH;
    GameState state;
    BufferedImage [] monsterSprites;
    BufferedImage heroSprite;
    BufferedImage deadHeroSprite;
    BufferedImage [] explosionSprites;
    BufferedImage [] missileSprites;
    BufferedImage background;
    public Display(int nW, int nH, GameState state) {
        this.nW = nW;
        this.nH = nH;
        this.state = state;
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
        monsterSprites = new BufferedImage[state.nEnemyTypes];
        for (int i = 0; i < state.nEnemyTypes; ++i)
            monsterSprites[i] = loadSprite(this.getClass().getResource("/game/sprites/monster" + (i + 1) +  ".png"), cellW, cellH);
        heroSprite = loadSprite(this.getClass().getResource("/game/sprites/hero.png"), cellW, cellH);
        deadHeroSprite = loadSprite(this.getClass().getResource("/game/sprites/hero_dead.png"), cellW, cellH);
        background = loadSprite(this.getClass().getResource("/game/sprites/space.png"), w, h, false);
        explosionSprites = new BufferedImage[lenExplosion];
        for (int i = 0; i < lenExplosion; ++i)
            explosionSprites[i] = loadSprite(this.getClass().getResource("/game/sprites/explosion" + (i + 1) +  ".png"), cellW, cellH);
        missileSprites = new BufferedImage[lenMissile];
        for (int i = 0; i < lenMissile; ++i)
            missileSprites[i] = loadSprite(this.getClass().getResource("/game/sprites/missile" + (i + 1) +  ".png"), cellW, cellH);
    }
    public void init() {
        initializeSprites();
    }
    public void updateContents() {
        repaint();
    }
    final int lenMissile = 4;
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
        for (int _j = 0, len = state.monsters.size(); _j < len; ++_j) {
            final int x = w - (int) ((_j + 1) * (float) w / nW);
            final int [] column = state.monsters.get(len - _j - 1);
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
            final int x = (int) (state.position.x * (float) w / nW);
            final int y = (int) (state.position.y * (float) h / nH);
            g.drawImage(GAME_OVER ? deadHeroSprite : heroSprite, x, y, this);
        }
        if (!state.missiles.isEmpty()) {
            for (Missile m : state.missiles) {
                int phaseMissile = m.phase;
                phaseMissile++;
                if (phaseMissile >= lenMissile)
                    phaseMissile = 0;
                m.phase = phaseMissile;
                final Point2D.Float p = m.p;
                final Image sprite = missileSprites[phaseMissile];
                final int x = (int) (p.x * (float) w / nW);
                final int y = (int) (p.y * (float) h / nH);
                g.drawImage(sprite, x, y, this);
            }
        }
        if (!explosions.isEmpty()) {
            for (Iterator<Explosion> iterator = explosions.iterator(); iterator.hasNext();) {
                final Explosion e = iterator.next();
                final Point2D.Float p = e.p;
                int phaseExplosion = e.phase;
                final Image sprite = explosionSprites[phaseExplosion];
                phaseExplosion++;
                if (phaseExplosion >= lenExplosion)
                    iterator.remove();
                else
                    e.phase = phaseExplosion;
                final int x = (int) (p.x * (float) w / nW);
                final int y = (int) (p.y * (float) h / nH);
                g.drawImage(sprite, x, y, this);
            }
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
        {
            final String text = "SCORE: " + state.score;
            final Font font = new Font("sansserif", Font.BOLD, (int) ((float) h / 25));
            final FontMetrics metrics = g.getFontMetrics(font);
            final int x = 10;
            final int y = 10 + metrics.getAscent();
            g.setColor(Color.YELLOW);
            g.setFont(font);
            g.drawString(text, x, y);
        }
        {
            final String text = "AMMO: " + state.nMissiles;
            final Font font = new Font("sansserif", Font.BOLD, (int) ((float) h / 25));
            final FontMetrics metrics = g.getFontMetrics(font);
            final int x = (w - metrics.stringWidth(text)) / 2;
            final int y = 10 + metrics.getAscent();
            g.setColor(Color.YELLOW);
            g.setFont(font);
            g.drawString(text, x, y);
        }
        {
            final String text = "LEVEL: " + (state.level);
            final Font font = new Font("sansserif", Font.BOLD, (int) ((float) h / 25));
            final FontMetrics metrics = g.getFontMetrics(font);
            final int x = w - metrics.stringWidth(text) - 10;
            final int y = 10 + metrics.getAscent();
            g.setColor(Color.YELLOW);
            g.setFont(font);
            g.drawString(text, x, y);
        }
    }
    boolean GAME_OVER = false;
    public void gameOver() {
        this.GAME_OVER = true;
        explosion(new Point2D.Float(state.position.x, state.position.y));
    }
    public class Explosion {
        Point2D.Float p;
        int phase;
        Explosion(Point2D.Float p, int phase) {
            this.p = p;
            this.phase = phase;
        }

    }
    ArrayList<Explosion> explosions = new ArrayList<>();
    final int lenExplosion = 12;
    final int dTExplosion = 100;
    public void explosion(final Point2D.Float p) {
        explosions.add(new Explosion(p, 0));
        playSound(this.getClass().getResource("/game/sounds/explosion.wav"));
    }
    void playSound(final URL file) {
        try {
            final DataLine.Info daInfo = new DataLine.Info(Clip.class, null);
            final AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
            final DataLine.Info info = new DataLine.Info(Clip.class, inputStream.getFormat());
            final Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(inputStream);
            clip.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
