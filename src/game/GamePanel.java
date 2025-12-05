package game; 

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

public class GamePanel extends JPanel {

    public GamePanel() {
        // 1. 패널 크기 설정
        this.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        
        // 2. 배경색 검은색
        this.setBackground(Color.BLACK);
        
        // 3. 성능 설정
        this.setDoubleBuffered(true);
        this.setFocusable(true);
    }
}