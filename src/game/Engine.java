package game;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.Timer;

public class Engine {
    int nW;
    int nH;
    int dT;
    Timer timer;
    Timer timerAction;
    Display display;
    public class GameState {
        ArrayList<int []> monsters = new ArrayList<>();
        ArrayList<Missile> missiles = new ArrayList<>();
        int nEnemyTypes = 3;
        Point position;
        int score = 0;
        int level = 1;
        int nMissiles = 10;
    }
    public class Missile {
        Point2D.Float p;
        Point2D.Float v;
        int phase;
        public Missile(Point2D.Float p, Point2D.Float v, int phase) {
            this.p = p;
            this.v = v;
            this.phase = phase;
        }

    }

    int difficulty = 100;
    GameState state = new GameState();
    public Engine(int nW, int nH, int dT) {
        this.nW = nW;
        this.nH = nH;
        state.position = new Point(nW / 3, nH / 2);
        this.dT = dT;
        this.display = new Display(nW, nH, state);
        timer = new Timer(dT, tickListenerIncrement);
        timerAction = new Timer(100, tickListener);
    }
    public Display getDisplay() { return display; }
    ActionListener tickListenerIncrement = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            incrementAction();
        }
    };
    ActionListener tickListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            display.updateContents();
            incrementMissiles();
            if (!GAME_OVER)
                detectCollision();
        }
    };

    public void init() {
        display.init();
    }
    public void start() {
        timer.start();
        timerAction.start();
    }
    public void restart() {
        stop();
        timer = new Timer(dT, tickListenerIncrement);
        start();
    }
    void stop() {
        timer.stop();
    }

    Random random = new Random();
    int [] newColumn() {
        final int [] column = new int[nH];
        for (int i = 0; i < nH; ++i) {
            if (random.nextInt(1000) > (1000 - difficulty))
                column[i] = random.nextInt(state.nEnemyTypes) + 1;
        }
        return column;
    }
    int countMonsters(int [] column) {
        int n = 0;
        for (int i = 0, len = column.length; i < len; ++i) {
            if (column[i] > 0)
                n++;
        }
        return n;
    }
    final int difficultyStep = 5;
    final float speedUpStep = 1.05f;
    int difficultyFloor = 0;
    final int scoreStepDifficulty = 10;
    final int nMissilesStep = 5;
    int bonusMissilesFloor = 0;
    final int scoreStepBonusMissiles = 50;
    void incrementScore(int scoreInc) {
        state.score += scoreInc;
        final int newDifficultyFloor = state.score / scoreStepDifficulty;
        if (difficultyFloor != newDifficultyFloor) {
            final int nSteps = newDifficultyFloor - difficultyFloor;
            for (int i = 0; i < nSteps; ++i) {
                difficulty += difficultyStep;
                state.level++;
                dT = (int) (1.0f / speedUpStep * dT);
            }
            difficultyFloor = newDifficultyFloor;
            restart();
        }
        final int newBonusMissilesFloor = state.score / scoreStepBonusMissiles;
        if (bonusMissilesFloor != newBonusMissilesFloor) {
            final int nSteps = newBonusMissilesFloor - bonusMissilesFloor;
            for (int i = 0; i < nSteps; ++i) {
                state.nMissiles += nMissilesStep;
            }
            bonusMissilesFloor = newBonusMissilesFloor;
        }
    }
    void incrementAction() {
        state.monsters.add(newColumn());
        if (state.monsters.size() > nW) {
            final int [] column = state.monsters.get(0);
            incrementScore(countMonsters(column));
            state.monsters.remove(0);
        }
        detectCollision();
    }
    void incrementMissiles() {
        if (!state.missiles.isEmpty()) {
            for (Iterator<Missile> iterator = state.missiles.iterator(); iterator.hasNext();) {
                final Missile m = iterator.next();
                final Point2D.Float p = m.p;
                final Point2D.Float v = m.v;
                p.x += v.x;
                p.y += v.y;
                if (p.x < 0 || p.x > nW || p.y < 0 || p.y > nH)
                    iterator.remove();
            }
        }
    }
    void detectCollision() {
        // Collision missile monster
        if (!state.missiles.isEmpty()) {
            for (Missile m : state.missiles) {
                final Point2D.Float p = m.p;
                final int _x = (int) Math.round(p.x - (nW - state.monsters.size()));
                final int _y = (int) Math.round(p.y);
                if (_x < 0 || _x >= state.monsters.size() || _y < 0 || _y >= nH)
                    return;
                final int [] column = state.monsters.get(_x);
                if (column[_y] > 0) {
                    column[_y] = 0; // Kill monster
                    incrementScore(1);
                    display.explosion(new Point2D.Float(p.x, p.y));
                }
            }
        }
        // Collision hero - monster
        final int _x = state.position.x - (nW - state.monsters.size());
        if (_x < 0 || _x >= state.monsters.size())
            return;
        final int [] column = state.monsters.get(_x);
        if (column[state.position.y] > 0)
            gameOver();
    }
    boolean GAME_OVER = false;
    void gameOver() {
        GAME_OVER = true;
        stop();
        display.gameOver();
    }
    void launchMissile(Point2D.Float v) {
        if (state.nMissiles <= 0)
            return;
        state.nMissiles--;
        state.missiles.add(new Missile(new Point2D.Float(state.position.x, state.position.y), new Point2D.Float(v.x, v.y), 0));
    }

    public void keyTyped(KeyEvent ke) {
        keyImpl(ke);
    }

    public void keyPressed(KeyEvent ke) {
        keyImpl(ke);
    }

    public void keyImpl(KeyEvent ke) {
        final int keyCode = ke.getKeyCode();
        if (GAME_OVER) {
            if (keyCode == KeyEvent.VK_ENTER)
                System.exit(0);
            return;
        }
        int x = state.position.x;
        int y = state.position.y;
        switch ( keyCode ) {
        case KeyEvent.VK_UP:
            y--;
            break;
        case KeyEvent.VK_DOWN:
            y++;
            break;
        case KeyEvent.VK_LEFT:
            x--;
            break;
        case KeyEvent.VK_RIGHT :
            x++;
            break;
        case KeyEvent.VK_SPACE:
            launchMissile(new Point2D.Float(0.50f, 0.00f));
            if (ke.isShiftDown()) {
                launchMissile(new Point2D.Float(0.35f, -0.35f));
                launchMissile(new Point2D.Float(0.35f,  0.35f));
            }
            break;
        }
        if (x < 0)
            x = 0;
        if (x >= nW)
            x = nW - 1;
        if (y < 0)
            y = 0;
        if (y >= nH)
            y = nH - 1;
        state.position.x = x;
        state.position.y = y;
        display.updateContents();
        detectCollision();
    }

    public void keyReleased(KeyEvent ke) {
        /* NOTHING */
    }
}
