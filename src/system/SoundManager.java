package system;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import common.Constants;

public class SoundManager {

    Clip clip;
    URL soundURL[] = new URL[40]; // 소리 파일 40개까지 저장 가능

    public SoundManager() {
    	// [무기] (0 ~ 5)
        soundURL[0] = getSoundURL("/sound/dagger_swing.wav");  // 1. 단검
        soundURL[1] = getSoundURL("/sound/longsword.wav");     // 2. 롱소드
        soundURL[2] = getSoundURL("/sound/greatsword.wav");    // 3. 대검
        soundURL[3] = getSoundURL("/sound/pistol_shot.wav");   // 4. 권총
        soundURL[4] = getSoundURL("/sound/shotgun.wav");       // 5. 샷건
        soundURL[5] = getSoundURL("/sound/sniper.wav");        // 6. 스나이퍼

        // [스테이지/BGM] (6 ~ 11)
        soundURL[6] = getSoundURL("/sound/bgm_forest.wav");    // 7. 숲
        soundURL[7] = getSoundURL("/sound/bgm_swamp.wav");     // 8. 늪지대
        soundURL[8] = getSoundURL("/sound/bgm_ice.wav");       // 9. 얼음
        soundURL[9] = getSoundURL("/sound/bgm_hell.wav");      // 10. 지옥
        soundURL[10] = getSoundURL("/sound/bgm_throne.wav");   // 11. 알현실
        soundURL[11] = getSoundURL("/sound/stage_clear.wav");  // 12. 스테이지 클리어

        // [아이템] (12 ~ 17)
        soundURL[12] = getSoundURL("/sound/key_get.wav");      // 13. 열쇠 획득
        soundURL[13] = getSoundURL("/sound/key_drop.wav");     // 14. 열쇠 드랍
        soundURL[14] = getSoundURL("/sound/key_use.wav");      // 15. 열쇠 사용
        soundURL[15] = getSoundURL("/sound/item_get.wav");     // 16. 아이템 획득
        soundURL[16] = getSoundURL("/sound/item_use.wav");     // 17. 아이템 사용
        soundURL[17] = getSoundURL("/sound/chest_open.wav");   // 18. 상자 열림

        // [방] (18)
        soundURL[18] = getSoundURL("/sound/door_open.wav");    // 19. 문 열림

        // [플레이어] (19 ~ 21)
        soundURL[19] = getSoundURL("/sound/player_move.wav");  // 20. 움직임
        soundURL[20] = getSoundURL("/sound/player_hit.wav");   // 21. 공격 당함
        soundURL[21] = getSoundURL("/sound/player_die.wav");   // 22. 죽음

        // [적/몬스터] (22 ~ 28)
        soundURL[22] = getSoundURL("/sound/enemy_swing1.wav"); // 23. 팔 휘두름
        soundURL[23] = getSoundURL("/sound/enemy_swing2.wav"); // 24. 무기 휘두름
        soundURL[24] = getSoundURL("/sound/enemy_throw.wav");  // 25. 던짐
        soundURL[25] = getSoundURL("/sound/slime_walk.wav");   // 26. 슬라임 걷기
        soundURL[26] = getSoundURL("/sound/bite.wav");         // 27. 물기
        soundURL[27] = getSoundURL("/sound/ice_shatter.wav");  // 28. 얼음 깨짐
        soundURL[28] = getSoundURL("/sound/enemy_die.wav");    // 29. 죽음
    }
    
    // [수정] 프로젝트 루트 기준 경로로 URL 생성
    private URL getSoundURL(String path) {
        try {
            // /sound/... 경로를 res/sound/...로 변환
            String resourcePath = path.startsWith("/") ? path.substring(1) : path;
            File soundFile = Constants.getResourceFile("res/" + resourcePath);
            if (soundFile.exists()) {
                return soundFile.toURI().toURL();
            }
        } catch (Exception e) {
            // 파일이 없으면 null 반환
        }
        return null;
    }

    public void setFile(int i) {
        try {
            if (soundURL[i] == null) {
                System.err.println("사운드 파일을 찾을 수 없습니다: 인덱스 " + i);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
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
}
