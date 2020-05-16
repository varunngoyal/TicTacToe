package com.vaavdevelopers.tictactoe;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

public class GameAudio {

    private SoundPool soundPool;
    private int sound_click, sound_win, sound_finished;
    MediaPlayer player;
    private Context context;

    GameAudio(Context context) {
        this.context = context;
        initMusic();
        initSound();
    }

    public void playDrawSound() {
        soundPool.play(sound_finished, 1, 1, 0, 0, 1);

    }

    public void playWinSound() {
        soundPool.play(sound_win, 1, 1, 0, 0, 1);
    }

    public void playClickSound() {
        soundPool.play(sound_click, 1, 1, 0, 0, 1);
    }

    public void initSound() {
        //get sounds
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();

        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        sound_win = soundPool.load(context, R.raw.success,1);

        sound_click = soundPool.load(context, R.raw.softhit2, 1);

        sound_finished = soundPool.load(context, R.raw.finished, 1);
    }

    public void initMusic() {
        if (player == null) {
            player = MediaPlayer.create(context, R.raw.music_loop);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.start();
                }
            });
        }
    }

    public void playMusic() {
        player.start();
    }

    public void releaseMusic() {
        if(player != null) {
            player.release();
            player = null;
        }
    }

    public void releaseSound() {
        soundPool.release();
        soundPool = null;
    }

    //At end, don't forgot to relase memory
    public void onDestroy() {
        if(context != null) {
            context = null;
        }
    }
}
