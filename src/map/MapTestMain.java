package map;

import common.Constants;

import javax.swing.JFrame;

/**
 * 맵 시스템 테스트용 메인 클래스
 * 독립적으로 실행 가능 (Main.java와 별개)
 *
 * 실행 방법:
 * javac -encoding UTF-8 -d bin -sourcepath src src/map/MapTestMain.java
 * java -cp bin map.MapTestMain
 */
public class MapTestMain {

    public static void main(String[] args) {
        System.out.println("=== Map Test Started ===");

        JFrame frame = new JFrame(Constants.GAME_TITLE + " - Map Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // 맵 테스트 패널 추가
        MapTestPanel panel = new MapTestPanel();
        frame.add(panel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // 게임 루프 시작
        panel.startGameThread();
    }
}
