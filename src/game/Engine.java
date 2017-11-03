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
    ArrayList<int []> elements = new ArrayList<>();
    Point position;
    final int nEnemyTypes = 3;
    public Engine(int nW, int nH, int dT) {
        this.nW = nW;
        this.nH = nH;
        this.position = new Point(nW / 3, nH / 2);
        this.dT = dT;
        this.display = new Display(nW, nH, elements, position, nEnemyTypes);
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
    final int difficulty = 10;
    int [] newColumn() {
        final int [] column = new int[nH];
        for (int i = 0; i < nH; ++i) {
            if (random.nextInt(100) < difficulty)
                column[i] = random.nextInt(nEnemyTypes) + 1;
        }
        return column;
    }
    void incrementAction() {
        elements.add(newColumn());
        if (elements.size() > nW)
            elements.remove(0);
        display.updateContents();
        detectCollision();
    }
    void detectCollision() {
        final int _x = position.x - (nW - elements.size());
        if (_x < 0 || _x >= elements.size())
            return;
        final int [] column = elements.get(_x);
        if (column[position.y] > 0)
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
        int x = position.x;
        int y = position.y;
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
        position.x = x;
        position.y = y;
        display.updateContents();
        detectCollision();
    }
    
    public void keyReleased(KeyEvent ke) {
        /* NOTHING */
    }
}
