package com.vaavdevelopers.tictactoe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements SettingsDialog.SettingsDialogListener {

    public static final String PREFS_NAME = "TicTacToeShared";

    Dialog howtoplay_dialog;
    private long backPressedTime = 0;
    private Toast backToast;
    //public boolean sound = false, music = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // play button
        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PlayerModeActivity.class));
            }
        });

        // play online
        findViewById(R.id.btn_play_online).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RoomActivity.class));
            }
        });

        //how to play button
        findViewById(R.id.btn_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HowtoPlayDialog();
            }
        });

        //about button
        findViewById(R.id.btn_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog diag = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("About")
                        .setMessage("Developed By VaavDevelopers 2020")
                        .create();

                diag.show();
            }
        });

        //Exit button
        findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage("Are you sure you want to exit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })

                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        //settings button
        findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsDialog();

            }
        });
    }

    public void openSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settingsDialog.setMusic(settings.getBoolean("music", false));
        settingsDialog.setSound(settings.getBoolean("sound", false));
        settingsDialog.show(getSupportFragmentManager(), "settings dialog");

    }

    @Override
    public void applySettings(boolean sound, boolean music) {
        Toast.makeText(this, "sound: "+sound+" music: "+music, Toast.LENGTH_SHORT).show();
        //this.sound = sound;
        //this.music = music;

        //save the music and sound setttings
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("sound", sound);
        editor.putBoolean("music", music);
        editor.commit();
    }

    @Override
    public void onBackPressed() {

        if(backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return ;
        } else {
            backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
        //super.onBackPressed();
    }

    public void HowtoPlayDialog() {

        howtoplay_dialog = new Dialog(MainActivity.this);

        howtoplay_dialog.setContentView(R.layout.howtoplaydialog);

        howtoplay_dialog.show();

    }

}
