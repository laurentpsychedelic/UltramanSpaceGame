package game;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Game {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                final Engine engine = new Engine(20, 15, 1000);
                final Window window = new Window(engine);
                final Display display = engine.getDisplay();
                display.setOpaque(true);
                display.setBackground(Color.BLACK);
                display.setBounds(0, 0, 800, 600);
                window.getContentPane().add(display);
                window.setLocation(0, 0);
                window.setSize(800, 600);
                window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                window.setResizable(false);
                window.setVisible(true);
                engine.start();
            }
        });
    }
    
}

    
