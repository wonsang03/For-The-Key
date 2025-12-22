package system;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class SoundManager {

    Clip clip;
    String soundPaths[] = new String[40];

    public SoundManager() {
        // 무기 사운드 (0 ~ 5)
        soundPaths[0] = "/system/sounds/dagger_swing.wav";
        soundPaths[1] = "/system/sounds/longsword.wav";
        soundPaths[2] = "/system/sounds/greatsword.wav";
        soundPaths[3] = "/system/sounds/pistol_shot.wav";
        soundPaths[4] = "/system/sounds/shotgun.wav";
        soundPaths[5] = "/system/sounds/sniper.wav";

        // 스테이지/BGM (6 ~ 11)
        soundPaths[6] = "/system/sounds/bgm_forest.wav";
        soundPaths[7] = "/system/sounds/bgm_swamp.wav";
        soundPaths[8] = "/system/sounds/bgm_ice.wav";
        soundPaths[9] = "/system/sounds/bgm_hell.wav";
        soundPaths[10] = "/system/sounds/bgm_throne.wav";
        soundPaths[11] = "/system/sounds/stageclear.wav";

        // 아이템 (12 ~ 17)
        soundPaths[12] = "/system/sounds/key_get.wav";
        soundPaths[13] = "/system/sounds/key_drop.wav";
        soundPaths[14] = "/system/sounds/key_use.wav";
        soundPaths[15] = "/system/sounds/item_get.wav";
        soundPaths[16] = "/system/sounds/item_use.wav";
        soundPaths[17] = "/system/sounds/chest_open.wav";

        // 방 (18)
        soundPaths[18] = "/system/sounds/door_open.wav";

        // 플레이어 (19 ~ 21)
        soundPaths[19] = "/system/sounds/player_move.wav";
        soundPaths[20] = "/system/sounds/player_hit.wav";
        soundPaths[21] = "/system/sounds/player_die.wav";

        // 적/몬스터 (22 ~ 28)
        soundPaths[22] = "/system/sounds/enemy_swing1.wav";
        soundPaths[23] = "/system/sounds/enemy_swing2.wav";
        soundPaths[24] = "/system/sounds/enemy_throw.wav";
        soundPaths[25] = "/system/sounds/slimewalk.wav";
        soundPaths[26] = "/system/sounds/bite.wav";
        soundPaths[27] = "/system/sounds/ice_shatter.wav";
        soundPaths[28] = "/system/sounds/enemy_die.wav";

        // 추가 (29 ~ )
        soundPaths[29] = "/system/sounds/bgm_title.wav";
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
        if (i < 0 || i >= soundPaths.length || soundPaths[i] == null) {
            return;
        }
        try {
            java.io.InputStream is = getClass().getResourceAsStream(soundPaths[i]);
            if (is == null) return;
            java.io.BufferedInputStream bis = new java.io.BufferedInputStream(is);
            AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
            Clip seClip = AudioSystem.getClip();
            seClip.open(ais);
            
            // 재생 완료 시 자동으로 Clip을 닫아 메모리 누수 방지
            seClip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        Clip clip = (Clip) event.getSource();
                        clip.close();
                    }
                }
            });
            
            seClip.start();
        } catch (Exception e) {
            // 효과음 재생 실패 시 조용히 처리
        }
    }
    
    public void playWeaponSound(int soundIndex) {
        // playSE()로 통일하여 일관된 방식으로 처리
        playSE(soundIndex);
    }
    
    public void playEnemySound(int soundIndex) {
        // playSE()로 통일하여 일관된 방식으로 처리
        playSE(soundIndex);
    }
}
