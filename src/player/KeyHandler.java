package player;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;

import main.GamePanel;

public class KeyHandler implements KeyListener {
	
	GamePanel gp;

    // 플레이어 이동 상태 변수 (Player 클래스에서 사용)
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    
    // 키 입력 감지 (아이템, 무기 교체 등)
    public boolean onePressed, twoPressed, threePressed;
    public boolean qPressed, ePressed;
    
    // 테스트용 키 (K: 피격 테스트)
    public boolean kPressed;
    
    // 생성자 추가: GamePanel을 받아와서 연결합니다.
    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // 사용하지 않음
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        // 플레이어 이동 (WASD)
        if (code == KeyEvent.VK_W) {
            upPressed = true;
        }
        if (code == KeyEvent.VK_S) {
            downPressed = true;
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = true;
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = true;
        }
        
        // 아이템 및 기능 키
        if (code == KeyEvent.VK_1) {
            onePressed = true;
        }
        if (code == KeyEvent.VK_2) {
            twoPressed = true;
        }
        if (code == KeyEvent.VK_3) {
            threePressed = true;
        }
        if (code == KeyEvent.VK_Q) {
            qPressed = true;
        }
        if (code == KeyEvent.VK_E) {
            ePressed = true;
        }
        
        // 피격 테스트용 키 (GamePanel에서 이 상태를 체크하거나 이벤트를 받을 수 있음)
        if (code == KeyEvent.VK_K) {
            kPressed = true;
        }
        
        if (code == KeyEvent.VK_TAB) {
            if (gp.gameState == gp.playState) {
                gp.ui.showStatusDetail = !gp.ui.showStatusDetail;
            }
        }
    }
    
    public void playSound(String soundName) {
        try {
            // 프로젝트 내 sounds 폴더 경로 참조
            File file = new File("sounds/" + soundName);
            
            if(file.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
            } else {
                System.out.println("파일을 찾을 수 없음: " + soundName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        
        // 키를 뗐을 때 상태 false로 변경
        if (code == KeyEvent.VK_W) {
            upPressed = false;
        }
        if (code == KeyEvent.VK_S) {
            downPressed = false;
        }
        if (code == KeyEvent.VK_A) {
            leftPressed = false;
        }
        if (code == KeyEvent.VK_D) {
            rightPressed = false;
        }
        
        if (code == KeyEvent.VK_1) {
            onePressed = false;
        }
        if (code == KeyEvent.VK_2) {
            twoPressed = false;
        }
        if (code == KeyEvent.VK_3) {
            threePressed = false;
        }
        if (code == KeyEvent.VK_Q) {
            qPressed = false;
        }
        if (code == KeyEvent.VK_E) {
            ePressed = false;
        }
        
        // 테스트 키 해제
        if (code == KeyEvent.VK_K) {
            kPressed = false;
        }
    }    
}