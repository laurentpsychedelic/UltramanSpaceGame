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
        ArrayList<int []> elements = new ArrayList<>();
        ArrayList<Point2D.Float> missiles = new ArrayList<>();
        int nEnemyTypes = 3;
        Point position;
        int score = 0;
        int level = 1;
        int nMissiles = 10;
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
        state.elements.add(newColumn());
        if (state.elements.size() > nW) {
            final int [] column = state.elements.get(0);
            incrementScore(countMonsters(column));
            state.elements.remove(0);
        }
        detectCollision();
    }
    void incrementMissiles() {
        if (!state.missiles.isEmpty()) {
            for (Iterator<Point2D.Float> iterator = state.missiles.iterator(); iterator.hasNext();) {
                Point2D.Float p = iterator.next();
                p.x += 0.5f;
                if (p.x > nW)
                    iterator.remove();
            }
        }
    }
    void detectCollision() {
        // Collision missile monster
        if (!state.missiles.isEmpty()) {
            for (Point2D.Float p : state.missiles) {
                final int _x = (int) p.x - (nW - state.elements.size());
                final int _y = (int) p.y;
                if (_x < 0 || _x >= state.elements.size())
                    return;
                final int [] column = state.elements.get(_x);
                if (column[_y] > 0) {
                    column[_y] = 0; // Kill monster
                    incrementScore(1);
                    display.explosion(new Point((int) p.x, (int) p.y));
                }
            }
        }
        // Collision hero - monster
        final int _x = state.position.x - (nW - state.elements.size());
        if (_x < 0 || _x >= state.elements.size())
            return;
        final int [] column = state.elements.get(_x);
        if (column[state.position.y] > 0)
            gameOver();
    }
    boolean GAME_OVER = false;
    void gameOver() {
        GAME_OVER = true;
        stop();
        display.gameOver();
    }
    void launchMissile() {
        if (state.nMissiles <= 0)
            return;
        state.nMissiles--;
        state.missiles.add(new Point2D.Float(state.position.x, state.position.y));
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
            launchMissile();
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
