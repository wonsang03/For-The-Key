package game;

import javax.swing.JFrame;

public class MainFrame extends JFrame {

    public MainFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setTitle(Constants.GAME_TITLE);

        GamePanel gamePanel = new GamePanel();
        this.add(gamePanel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        // ✅ 반드시 포커스를 패널로 넘겨야 키가 인식됨
        gamePanel.requestFocusInWindow();
        gamePanel.requestFocus();

        // 게임 스레드 시작
        gamePanel.startGameThread();
    }
}
