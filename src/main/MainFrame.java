package main;

import javax.swing.JFrame;
import common.Constants;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

	public MainFrame() {
        // 1. 닫기 버튼 설정
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setTitle(Constants.GAME_TITLE);
        
        // 2. GamePanel 생성 및 추가
        GamePanel gamePanel = new GamePanel();
        this.add(gamePanel);
        
        // 3. 창 크기 자동 조절
        this.pack();
        
        // 4. 화면 중앙 배치
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
        // 5. 포커스를 패널로 넘겨야 키가 인식됨
        gamePanel.requestFocusInWindow();
        gamePanel.requestFocus();
        
        // 주의: 게임 스레드는 GamePanel 생성자에서 이미 시작됨 (setupGame() -> startGameThread())
    }
}
