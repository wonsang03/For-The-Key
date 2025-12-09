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
        soundURL[0] = getClass().getResource("/sound/dagger_swing.wav");  // 1. 단검
        soundURL[1] = getClass().getResource("/sound/longsword.wav");     // 2. 롱소드
        soundURL[2] = getClass().getResource("/sound/greatsword.wav");    // 3. 대검
        soundURL[3] = getClass().getResource("/sound/pistol_shot.wav");   // 4. 권총
        soundURL[4] = getClass().getResource("/sound/shotgun.wav");       // 5. 샷건
        soundURL[5] = getClass().getResource("/sound/sniper.wav");        // 6. 스나이퍼

        // [스테이지/BGM] (6 ~ 11)
        soundURL[6] = getClass().getResource("/sound/bgm_forest.wav");    // 7. 숲
        soundURL[7] = getClass().getResource("/sound/bgm_swamp.wav");     // 8. 늪지대
        soundURL[8] = getClass().getResource("/sound/bgm_ice.wav");       // 9. 얼음
        soundURL[9] = getClass().getResource("/sound/bgm_hell.wav");      // 10. 지옥
        soundURL[10] = getClass().getResource("/sound/bgm_throne.wav");   // 11. 알현실
        soundURL[11] = getClass().getResource("/sound/stage_clear.wav");  // 12. 스테이지 클리어

        // [아이템] (12 ~ 17)
        soundURL[12] = getClass().getResource("/sound/key_get.wav");      // 13. 열쇠 획득
        soundURL[13] = getClass().getResource("/sound/key_drop.wav");     // 14. 열쇠 드랍
        soundURL[14] = getClass().getResource("/sound/key_use.wav");      // 15. 열쇠 사용
        soundURL[15] = getClass().getResource("/sound/item_get.wav");     // 16. 아이템 획득
        soundURL[16] = getClass().getResource("/sound/item_use.wav");     // 17. 아이템 사용
        soundURL[17] = getClass().getResource("/sound/chest_open.wav");   // 18. 상자 열림

        // [방] (18)
        soundURL[18] = getClass().getResource("/sound/door_open.wav");    // 19. 문 열림

        // [플레이어] (19 ~ 21)
        soundURL[19] = getClass().getResource("/sound/player_move.wav");  // 20. 움직임
        soundURL[20] = getClass().getResource("/sound/player_hit.wav");   // 21. 공격 당함
        soundURL[21] = getClass().getResource("/sound/player_die.wav");   // 22. 죽음

        // [적/몬스터] (22 ~ 28)
        soundURL[22] = getClass().getResource("/sound/enemy_swing1.wav"); // 23. 팔 휘두름
        soundURL[23] = getClass().getResource("/sound/enemy_swing2.wav"); // 24. 무기 휘두름
        soundURL[24] = getClass().getResource("/sound/enemy_throw.wav");  // 25. 던짐
        soundURL[25] = getClass().getResource("/sound/slime_walk.wav");   // 26. 슬라임 걷기
        soundURL[26] = getClass().getResource("/sound/bite.wav");         // 27. 물기
        soundURL[27] = getClass().getResource("/sound/ice_shatter.wav");  // 28. 얼음 깨짐
        soundURL[28] = getClass().getResource("/sound/enemy_die.wav");    // 29. 죽음
    }

    public void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        clip.start();
    }

    public void loop() {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        clip.stop();
    }
}