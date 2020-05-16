package com.vaavdevelopers.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PlayerModeActivity extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_mode);

        intent = new Intent(PlayerModeActivity.this, GameActivity.class);


        findViewById(R.id.btn_single_player).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("singlePlayer", true);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_2player).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("singlePlayer", false);
                startActivity(intent);
            }
        });
    }
}
