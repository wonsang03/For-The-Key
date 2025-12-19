package system;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundManager {

    Clip clip;
    File soundFiles[] = new File[40];

    public SoundManager() {
        // 무기 사운드 (0 ~ 5)
        soundFiles[0] = new File("res/sounds/dagger_swing.wav");
        soundFiles[1] = new File("res/sounds/longsword.wav");
        soundFiles[2] = new File("res/sounds/greatsword.wav");
        soundFiles[3] = new File("res/sounds/pistol_shot.wav");
        soundFiles[4] = new File("res/sounds/shotgun.wav");
        soundFiles[5] = new File("res/sounds/sniper.wav");

        // 스테이지/BGM (6 ~ 11)
        soundFiles[6] = new File("res/sounds/bgm_forest.wav");
        soundFiles[7] = new File("res/sounds/bgm_swamp.wav");
        soundFiles[8] = new File("res/sounds/bgm_ice.wav");
        soundFiles[9] = new File("res/sounds/bgm_hell.wav");
        soundFiles[10] = new File("res/sounds/bgm_throne.wav");
        soundFiles[11] = new File("res/sounds/stageclear.wav");

        // 아이템 (12 ~ 17)
        soundFiles[12] = new File("res/sounds/key_get.wav");
        soundFiles[13] = new File("res/sounds/key_drop.wav");
        soundFiles[14] = new File("res/sounds/key_use.wav");
        soundFiles[15] = new File("res/sounds/item_get.wav");
        soundFiles[16] = new File("res/sounds/item_use.wav");
        soundFiles[17] = new File("res/sounds/chest_open.wav");

        // 방 (18)
        soundFiles[18] = new File("res/sounds/door_open.wav");

        // 플레이어 (19 ~ 21)
        soundFiles[19] = new File("res/sounds/player_move.wav");
        soundFiles[20] = new File("res/sounds/player_hit.wav");
        soundFiles[21] = new File("res/sounds/player_die.wav");

        // 적/몬스터 (22 ~ 28)
        soundFiles[22] = new File("res/sounds/enemy_swing1.wav");
        soundFiles[23] = new File("res/sounds/enemy_swing2.wav");
        soundFiles[24] = new File("res/sounds/enemy_throw.wav");
        soundFiles[25] = new File("res/sounds/slimewalk.wav");
        soundFiles[26] = new File("res/sounds/bite.wav");
        soundFiles[27] = new File("res/sounds/ice_shatter.wav");
        soundFiles[28] = new File("res/sounds/enemy_die.wav");
        
        // 추가 (29 ~ )
        soundFiles[29] = new File("res/sounds/bgm_title.wav");
    }

    public void setFile(int i) {
        try {
            if (i < 0 || i >= soundFiles.length || soundFiles[i] == null || !soundFiles[i].exists()) {
                clip = null;
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundFiles[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e) {
            System.out.println("사운드 파일 로드 실패 (인덱스 " + i + "): " + e.getMessage());
            clip = null;
        }
    }

    public void play() {
        if (clip != null) {
            try {
                clip.start();
            } catch (Exception e) {
                System.out.println("사운드 재생 실패: " + e.getMessage());
            }
        }
    }

    public void loop() {
        if (clip != null) {
            try {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (Exception e) {
                System.out.println("사운드 반복 재생 실패: " + e.getMessage());
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
            System.out.println("배경음악 재생 실패 (인덱스 " + i + "): " + e.getMessage());
        }
    }
    
    public void playSE(int i) {
        try {
            setFile(i);
            if (clip != null) {
                play();
            }
        } catch (Exception e) {
            System.out.println("효과음 재생 실패 (인덱스 " + i + "): " + e.getMessage());
        }
    }
    
    public void playWeaponSound(int soundIndex) {
        if (soundIndex >= 0 && soundIndex < soundFiles.length && soundFiles[soundIndex] != null && soundFiles[soundIndex].exists()) {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(soundFiles[soundIndex]);
                Clip weaponClip = AudioSystem.getClip();
                weaponClip.open(ais);
                weaponClip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void playEnemySound(int soundIndex) {
        if (soundIndex >= 0 && soundIndex < soundFiles.length && soundFiles[soundIndex] != null && soundFiles[soundIndex].exists()) {
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(soundFiles[soundIndex]);
                Clip enemyClip = AudioSystem.getClip();
                enemyClip.open(ais);
                enemyClip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
