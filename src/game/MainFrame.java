package game;

import javax.swing.JFrame;

public class MainFrame extends JFrame {

    public MainFrame() {
        // 1. 닫기 버튼 설정
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setTitle(Constants.GAME_TITLE);
        
        // 2. GamePanel 생성 및 추가 (같은 폴더니까 import 안 해도 됨)
        GamePanel gamePanel = new GamePanel();
        this.add(gamePanel);
        
        // 3. 창 크기 자동 조절
        this.pack();
        
        // 4. 화면 중앙 배치
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}