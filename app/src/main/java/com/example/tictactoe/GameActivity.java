package com.example.tictactoe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Point;

import static java.lang.Thread.sleep;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private Button[][] buttons = new Button[3][3];

    LinearLayout gameGrid;
    private boolean player1Turn = true;

    private int roundCount;

    private int player1Points = 0;
    private int player2Points = 0;

    private TextView textViewPoints;

    private SoundPool soundPool;
    private int sound_click, sound_win, sound_finished;
    MediaPlayer player;

    TextView Xwins, Owins, draw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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
        sound_win = soundPool.load(this, R.raw.success,1);

        sound_click = soundPool.load(this, R.raw.softhit2, 1);

        sound_finished = soundPool.load(this, R.raw.finished, 1);

        //play music
        if(player == null) {
            player = MediaPlayer.create(this, R.raw.music_loop);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.start();
                }
            });
        }

        player.start();




        //get screen size
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);


        textViewPoints = findViewById(R.id.text_view_points);

        gameGrid = findViewById(R.id.grid);
        System.err.println(point.y+" width "+point.x+" height");
        gameGrid.setLayoutParams(new LinearLayout.LayoutParams(point.x, point.x));

        for(int i=0;i<3;i++) {

            for(int j=0;j<3;j++) {
                String button_id = "btn_"+i+""+j;
                System.err.println(button_id);
                int resID = getResources().getIdentifier(button_id, "id", getPackageName());

                buttons[i][j] = findViewById(resID);
                buttons[i][j].setOnClickListener(this);

            }

        }

        Button buttonReset = findViewById(R.id.button_reset);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
    }

    @Override
    public void onClick(final View v) {
        playClickSound();
        if (!((Button) v).getText().toString().equals("")) {
            return;
        }

        if (player1Turn) {
            ((Button) v).setText("X");
            v.setBackground(getResources().getDrawable(R.drawable.gridbtn_p1));
        } else {
            ((Button) v).setText("O");
            v.setBackground(getResources().getDrawable(R.drawable.gridbtn_p2));

        }

        System.err.println("setTExt: "+((Button)v).getText());

        roundCount++;

        if (checkForWin()) {
            if (player1Turn) {

                player1Wins();
                playWinSound();
            } else {
                player2Wins();
                playWinSound();
            }
        }
        else if(roundCount == 9) {
            draw();
            playDrawSound();
        } else {
            player1Turn = !player1Turn;
        }
    }

    private void playDrawSound() {
        soundPool.play(sound_finished, 1, 1, 0, 0, 1);

    }

    private void playWinSound() {
        soundPool.play(sound_win, 1, 1, 0, 0, 1);
    }

    private void playClickSound() {

        soundPool.play(sound_click, 1, 1, 0, 0, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
        if(player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(player != null) {
            player.release();
            player = null;
        }
    }

    private boolean checkForWin() {
        String[][] field = new String[3][3];

        for(int i=0;i<3;i++) {
            for(int j=0;j<3;j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        //check if rows match
        for(int i=0;i<3;i++) {
            if(field[i][0].equals(field[i][1])
                    && field[i][0].equals(field[i][2])
                    && !field[i][0].equals("")) {
                return true;
            }

            if(field[0][i].equals(field[1][i])
                    && field[0][i].equals(field[2][i])
                    && !field[0][i].equals("")) {
                return true;
            }


        }

        if(field[0][0].equals(field[1][1]) &&
                field[0][0].equals(field[2][2])&&
                !field[0][0].equals("")){
            return true;
        }

        if(field[2][0].equals(field[1][1]) &&
                field[2][0].equals(field[0][2]) &&
                !field[2][0].equals(""))
            return true;

        return false;


    }

    private void player1Wins() {
        player1Points++;
        Toast.makeText(this, "X wins!", Toast.LENGTH_SHORT).show();
        updatePointsText();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //title of game conclusion
        Xwins = new TextView(this);
        Xwins.setPadding(0, 40, 0, 0);
        Xwins.setTextSize(30);
        Xwins.setTextColor(Color.BLACK);
        Xwins.setTypeface(null, Typeface.BOLD);
        Xwins.setGravity(Gravity.CENTER_HORIZONTAL);
        Xwins.setText("X Wins!");
        alert.setView(Xwins);

        //alert.setTitle("X wins!");
        alert.setCancelable(false);
        alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetBoard();
            }
        });
        alert.create().show();


        //resetBoard();
    }


    private void player2Wins() {
        player2Points++;
        Toast.makeText(this, "O wins!", Toast.LENGTH_SHORT).show();
        updatePointsText();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        Owins = new TextView(this);
        Owins.setPadding(0, 40, 0, 0);
        Owins.setTextSize(30);
        Owins.setTextColor(Color.BLACK);
        Owins.setTypeface(null, Typeface.BOLD);
        Owins.setGravity(Gravity.CENTER_HORIZONTAL);
        Owins.setText("O Wins!");
        //alert.setTitle("O wins!");
        alert.setView(Owins);
        alert.setCancelable(false);
        alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetBoard();
            }
        });
        alert.create().show();


        //resetBoard();
    }

    private void draw() {
        Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        draw = new TextView(this);
        draw.setPadding(0, 40, 0, 0);
        draw.setTextSize(30);
        draw.setTextColor(Color.BLACK);
        draw.setTypeface(null, Typeface.BOLD);
        draw.setGravity(Gravity.CENTER_HORIZONTAL);
        draw.setText("Draw!");
        //alert.setTitle("Draw!");
        alert.setView(draw);

        alert.setCancelable(false);
        alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetBoard();
            }
        });
        alert.create().show();



        //resetBoard();
    }

    private void updatePointsText() {

        textViewPoints.setText(player1Points+":"+player2Points);
    }
    private void resetBoard() {

        for(int i=0;i<3;i++) {
            for(int j=0;j<3;j++) {
                buttons[i][j].setText("");
            }
        }
        roundCount = 0;
        player1Turn = true;

        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++) {
                buttons[i][j].setBackground(getResources().getDrawable(R.drawable.gridbtn_empty));
            }
        }
    }

    private void resetGame() {
        player1Points = 0;
        player2Points = 0;
        updatePointsText();
        resetBoard();
    }
}
