package system;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundManager {

    Clip clip;
    URL soundURL[] = new URL[40]; // 소리 파일 40개까지 저장 가능

    public SoundManager() {
        // [무기] (0 ~ 5)
        soundURL[0] = getClass().getResource("/sounds/dagger_swing.wav");  // 1. 단검
        soundURL[1] = getClass().getResource("/sounds/longsword.wav");     // 2. 롱소드
        soundURL[2] = getClass().getResource("/sounds/greatsword.wav");    // 3. 대검
        soundURL[3] = getClass().getResource("/sounds/pistol_shot.wav");   // 4. 권총
        soundURL[4] = getClass().getResource("/sounds/shotgun.wav");       // 5. 샷건
        soundURL[5] = getClass().getResource("/sounds/sniper.wav");        // 6. 스나이퍼

        // [스테이지/BGM] (6 ~ 11)
        soundURL[6] = getClass().getResource("/sounds/bgm_forest.wav");    // 7. 숲
        soundURL[7] = getClass().getResource("/sounds/bgm_swamp.wav");     // 8. 늪지대
        soundURL[8] = getClass().getResource("/sounds/bgm_ice.wav");       // 9. 얼음
        soundURL[9] = getClass().getResource("/sounds/bgm_hell.wav");      // 10. 지옥
        soundURL[10] = getClass().getResource("/sounds/bgm_throne.wav");   // 11. 알현실
        soundURL[11] = getClass().getResource("/sounds/stage_clear.wav");  // 12. 스테이지 클리어

        // [아이템] (12 ~ 17)
        soundURL[12] = getClass().getResource("/sounds/key_get.wav");      // 13. 열쇠 획득
        soundURL[13] = getClass().getResource("/sounds/key_drop.wav");     // 14. 열쇠 드랍
        soundURL[14] = getClass().getResource("/sounds/key_use.wav");      // 15. 열쇠 사용
        soundURL[15] = getClass().getResource("/sounds/item_get.wav");     // 16. 아이템 획득
        soundURL[16] = getClass().getResource("/sounds/item_use.wav");     // 17. 아이템 사용
        soundURL[17] = getClass().getResource("/sounds/chest_open.wav");   // 18. 상자 열림

        // [방] (18)
        soundURL[18] = getClass().getResource("/sounds/door_open.wav");    // 19. 문 열림

        // [플레이어] (19 ~ 21)
        soundURL[19] = getClass().getResource("/sounds/player_move.wav");  // 20. 움직임
        soundURL[20] = getClass().getResource("/sounds/player_hit.wav");   // 21. 공격 당함
        soundURL[21] = getClass().getResource("/sounds/player_die.wav");   // 22. 죽음

        // [적/몬스터] (22 ~ 28)
        soundURL[22] = getClass().getResource("/sounds/enemy_swing1.wav"); // 23. 팔 휘두름
        soundURL[23] = getClass().getResource("/sounds/enemy_swing2.wav"); // 24. 무기 휘두름
        soundURL[24] = getClass().getResource("/sounds/enemy_throw.wav");  // 25. 던짐
        soundURL[25] = getClass().getResource("/sounds/slime_walk.wav");   // 26. 슬라임 걷기
        soundURL[26] = getClass().getResource("/sounds/bite.wav");         // 27. 물기
        soundURL[27] = getClass().getResource("/sounds/ice_shatter.wav");  // 28. 얼음 깨짐
        soundURL[28] = getClass().getResource("/sounds/enemy_die.wav");    // 29. 죽음
        
        // [추가] (29 ~ )
        soundURL[29] = getClass().getResource("/sounds/bgm_title.wav");    // 30. 타이틀 BGM
    }

    public void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
            System.out.println("소리 파일을 로드하는 중 오류 발생! 인덱스: " + i);
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            clip.start();
        }
    }

    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

    // 배경음악 재생 (기존 음악 끄고 -> 새 음악 세팅 -> 재생 -> 반복)
    public void playMusic(int i) {
        stop();
        setFile(i);
        play();
        loop();
    }
    
    // 효과음 재생용 (반복 없음)
    public void playSE(int i) {
        setFile(i);
        play();
    }
}