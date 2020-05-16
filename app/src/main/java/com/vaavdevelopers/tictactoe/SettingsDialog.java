package com.vaavdevelopers.tictactoe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class SettingsDialog extends AppCompatDialogFragment {

    public static final String PREFS_NAME = "TicTacToeShared";


    private Switch switchSound, switchMusic;
    private SettingsDialogListener listener;
    public boolean sound = false, music = false;

    public void setMusic(boolean music) {
        this.music = music;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.settingsdialog, null);


        builder.setView(view)
                .setTitle("Settings")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        boolean sound = switchSound.isChecked();
                        boolean music = switchMusic.isChecked();

                        listener.applySettings(sound, music);
                    }
                });

        switchMusic = view.findViewById(R.id.switch_music);
        switchSound = view.findViewById(R.id.switch_sound);

        //get sound and music default from shared prefs


        switchMusic.setChecked(music);
        switchSound.setChecked(sound);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (SettingsDialogListener) context;
        } catch(ClassCastException e ) {
            throw new ClassCastException(context.toString() + " must implement SettingsDialogListener");
        }

    }

    public interface SettingsDialogListener {

        void applySettings(boolean sound, boolean music);
    }
}
