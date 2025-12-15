package player;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import main.GamePanel;

public class KeyHandler implements KeyListener {

    // [김민정님 코드] 플레이어 이동 상태 변수
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    
    // [김민정님 코드] 키 입력 감지
    public boolean onePressed, twoPressed;  // 민정 수정 : 아이템 슬롯 창이 3개에서 2개로 변경함
    public boolean qPressed, ePressed;
    public boolean kPressed;
    public boolean fPressed; // [민정 추가] : 열쇠 사용 키
    public boolean gPressed; // [민정 추가] 무기 줍기용 G키 변수 추가
    
    GamePanel gp;
    
    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
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
        
        if (code == KeyEvent.VK_1) {
            onePressed = true;
        }
        if (code == KeyEvent.VK_2) {
            twoPressed = true;
        }
//        if (code == KeyEvent.VK_3) {
//            threePressed = true;
//        }  // [민정 수정] 아이템 슬롯 창을 3개에서 2개로 변경함
        // [민정 수정]
        if (code == KeyEvent.VK_Q) {
            qPressed = true;
            // [민정 추가] 키를 누르는 순간 무기 교체 실행
            gp.player.swapWeapon();
        }
        if (code == KeyEvent.VK_E) {
            ePressed = true;
        }
        if (code == KeyEvent.VK_K) {
            kPressed = true;
        }
        // [추가] TAB 키를 누르면 정보창 상태(보임/안보임)를 반대로 뒤집음
        if (code == KeyEvent.VK_TAB) {
            if (gp.gameState == gp.playState) { // 플레이 중에만 작동하도록 설정
                gp.ui.showStatusDetail = !gp.ui.showStatusDetail;
            }
        } // 민정 추가
        if (code == KeyEvent.VK_F) { // [민정 추가] : 열쇠 사용 키
            fPressed = true;
        }
        if (code == KeyEvent.VK_G) { // [민정 추가] : 무기 줍기
            gPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
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
//        if (code == KeyEvent.VK_3) {
//            threePressed = false;
//        }  // [민정 수정] 아이템 슬롯 창이 3개에서 2개로 변경함
        if (code == KeyEvent.VK_Q) {
            qPressed = false;
        }
        if (code == KeyEvent.VK_E) {
            ePressed = false;
        }
        if (code == KeyEvent.VK_K) {
            kPressed = false;
        }
        if (code == KeyEvent.VK_F) { // [민정 추가] : 열쇠 사용
            fPressed = false;
        }
        if (code == KeyEvent.VK_G) { // [민정 추가] : 무기 줍기
            gPressed = false;
        }
    }
}