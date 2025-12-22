package system;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundManager {

    Clip clip;
    String soundPaths[] = new String[40];

    public SoundManager() {
        // 무기 사운드 (0 ~ 5)
        soundPaths[0] = "/res/sounds/dagger_swing.wav";
        soundPaths[1] = "/res/sounds/longsword.wav";
        soundPaths[2] = "/res/sounds/greatsword.wav";
        soundPaths[3] = "/res/sounds/pistol_shot.wav";
        soundPaths[4] = "/res/sounds/shotgun.wav";
        soundPaths[5] = "/res/sounds/sniper.wav";

        // 스테이지/BGM (6 ~ 11)
        soundPaths[6] = "/res/sounds/bgm_forest.wav";
        soundPaths[7] = "/res/sounds/bgm_swamp.wav";
        soundPaths[8] = "/res/sounds/bgm_ice.wav";
        soundPaths[9] = "/res/sounds/bgm_hell.wav";
        soundPaths[10] = "/res/sounds/bgm_throne.wav";
        soundPaths[11] = "/res/sounds/stageclear.wav";

        // 아이템 (12 ~ 17)
        soundPaths[12] = "/res/sounds/key_get.wav";
        soundPaths[13] = "/res/sounds/key_drop.wav";
        soundPaths[14] = "/res/sounds/key_use.wav";
        soundPaths[15] = "/res/sounds/item_get.wav";
        soundPaths[16] = "/res/sounds/item_use.wav";
        soundPaths[17] = "/res/sounds/chest_open.wav";

        // 방 (18)
        soundPaths[18] = "/res/sounds/door_open.wav";

        // 플레이어 (19 ~ 21)
        soundPaths[19] = "/res/sounds/player_move.wav";
        soundPaths[20] = "/res/sounds/player_hit.wav";
        soundPaths[21] = "/res/sounds/player_die.wav";

        // 적/몬스터 (22 ~ 28)
        soundPaths[22] = "/res/sounds/enemy_swing1.wav";
        soundPaths[23] = "/res/sounds/enemy_swing2.wav";
        soundPaths[24] = "/res/sounds/enemy_throw.wav";
        soundPaths[25] = "/res/sounds/slimewalk.wav";
        soundPaths[26] = "/res/sounds/bite.wav";
        soundPaths[27] = "/res/sounds/ice_shatter.wav";
        soundPaths[28] = "/res/sounds/enemy_die.wav";

        // 추가 (29 ~ )
        soundPaths[29] = "/res/sounds/bgm_title.wav";
    }

    public void setFile(int i) {
        try {
            if (i < 0 || i >= soundPaths.length || soundPaths[i] == null) {
                clip = null;
                return;
            }
            java.io.InputStream is = getClass().getResourceAsStream(soundPaths[i]);
            if (is == null) {
                clip = null;
                return;
            }
            java.io.BufferedInputStream bis = new java.io.BufferedInputStream(is);
            AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
            clip = null;
        }
    }

    public void play() {
        if (clip != null) {
            try {
                clip.start();
            } catch (Exception e) {
                // 사운드 재생 실패 시 조용히 처리
            }
        }
    }

    public void loop() {
        if (clip != null) {
            try {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (Exception e) {
                // 사운드 반복 재생 실패 시 조용히 처리
            }
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

    public void playMusic(int i) {
        try {
            stop();
            setFile(i);
            if (clip != null) {
                play();
                loop();
            }
        } catch (Exception e) {
            // 배경음악 재생 실패 시 조용히 처리
        }
    }
    
    public void playSE(int i) {
        try {
            setFile(i);
            if (clip != null) {
                play();
            }
        } catch (Exception e) {
            // 효과음 재생 실패 시 조용히 처리
        }
    }
    
    public void playWeaponSound(int soundIndex) {
        if (soundIndex >= 0 && soundIndex < soundPaths.length && soundPaths[soundIndex] != null) {
            try {
                java.io.InputStream is = getClass().getResourceAsStream(soundPaths[soundIndex]);
                if (is == null) return;
                java.io.BufferedInputStream bis = new java.io.BufferedInputStream(is);
                AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
                Clip weaponClip = AudioSystem.getClip();
                weaponClip.open(ais);
                weaponClip.start();
            } catch (Exception e) {
                // 무기 사운드 재생 실패 시 조용히 처리
            }
        }
    }
    
    public void playEnemySound(int soundIndex) {
        if (soundIndex >= 0 && soundIndex < soundPaths.length && soundPaths[soundIndex] != null) {
            try {
                java.io.InputStream is = getClass().getResourceAsStream(soundPaths[soundIndex]);
                if (is == null) return;
                java.io.BufferedInputStream bis = new java.io.BufferedInputStream(is);
                AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
                Clip enemyClip = AudioSystem.getClip();
                enemyClip.open(ais);
                enemyClip.start();
            } catch (Exception e) {
                // 적 사운드 재생 실패 시 조용히 처리
            }
        }
    }
}
