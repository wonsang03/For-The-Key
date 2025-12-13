package main;

import javax.swing.JFrame;
import common.Constants;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public MainFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setTitle(Constants.GAME_TITLE);
        
        GamePanel gamePanel = new GamePanel();
        this.add(gamePanel);
        
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
        gamePanel.requestFocusInWindow();
        gamePanel.requestFocus();
    }
}
