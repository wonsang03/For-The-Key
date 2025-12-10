package common;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Entity {

    // 1. 위치와 속도 (모든 캐릭터 공통)
    public int x, y;
    public int speed;

    // 2. 이미지와 애니메이션
    // 나중에 몬스터나 NPC도 이 변수들을 써서 움직입니다.
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public String direction; // 현재 바라보는 방향

    public int spriteCounter = 0; // 애니메이션 속도 조절용
    public int spriteNum = 1;     // 1번 발, 2번 발 구분용

    // 3. 충돌 영역 (나중에 벽에 부딪히는 거 구현할 때 씀)
    public Rectangle solidArea;
    public boolean collisionOn = false;

    // 핵심: 빈 메서드 만들기
    // Player가 이 메서드들을 '재정의(Override)'해서 쓸 수 있도록
    // 부모 쪽에서 껍데기를 만들어 주는 것입니다.
    
    public void update() {
        // 자식 클래스(Player, Monster 등)에서 코드를 채울 예정
    }

    public void draw(Graphics2D g2) {
        // 자식 클래스에서 코드를 채울 예정
    }
}