package game;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.Timer;

public class Engine {
    int nW;
    int nH;
    int dT;
    Timer timer;
    Display display;
    public class GameState {
        ArrayList<int []> elements = new ArrayList<>();
        int nEnemyTypes = 3;
        Point position;
        int score = 0;
        int difficulty = 10;
    }
    GameState state = new GameState();
    public Engine(int nW, int nH, int dT) {
        this.nW = nW;
        this.nH = nH;
        state.position = new Point(nW / 3, nH / 2);
        this.dT = dT;
        this.display = new Display(nW, nH, state);
        timer = new Timer(dT, tickListener);
    }
    public Display getDisplay() { return display; }
    ActionListener tickListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            incrementAction();
        }
    };
    public void start() {
        display.init();
        timer.start();
    }
    Random random = new Random();
    int [] newColumn() {
        final int [] column = new int[nH];
        for (int i = 0; i < nH; ++i) {
            if (random.nextInt(100) < state.difficulty)
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
    void incrementAction() {
        state.elements.add(newColumn());
        if (state.elements.size() > nW) {
            final int [] column = state.elements.get(0);
            state.score += countMonsters(column);
            state.elements.remove(0);
        }
        display.updateContents();
        detectCollision();
    }
    void detectCollision() {
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
    void stop() {
        timer.stop();
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
