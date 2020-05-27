package com.vaavdevelopers.tictactoe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {


    //Shared Preferences file
    public static final String PREFS_NAME = "TicTacToeShared";

    private Dialog howtoplay_dialog;

    private Button[][] buttons = new Button[3][3];

    LinearLayout gameGrid;
    private boolean player1Turn = true;

    private int roundCount;
    ArrayList<Integer> nextTurnChoices = new ArrayList<>();

    private int player1Points = 0;
    private int player2Points = 0;

    private TextView textViewPoints;
    private TextView textTurn;
    private Boolean isSinglePlayer;

    private SoundPool soundPool;
    private int sound_click, sound_win, sound_finished;
    MediaPlayer player;

    TextView Xwins, Owins, draw;

    private boolean isSoundEnabled, isMusicEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //check single player or 2 player
        isSinglePlayer = getIntent().getExtras().getBoolean("singlePlayer");

        //show how to play only once
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean dialogShown = settings.getBoolean("dialogShown", false);

        if (!dialogShown) {
            // AlertDialog code here
            HowtoPlayDialog();

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("dialogShown", true);
            editor.commit();
        }

        //check if sound and music are enabled
        isMusicEnabled = settings.getBoolean("music", true);
        isSoundEnabled = settings.getBoolean("sound", true);

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
                    if(isMusicEnabled)
                        player.start();
                }
            });
        }

        if(isMusicEnabled) {
            player.start();
        }

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

        for(int i=0;i<9;i++)
            nextTurnChoices.add(i);

        textTurn = findViewById(R.id.txt_turn);

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
            textTurn.setText("O, it's your turn");
            v.setBackground(getResources().getDrawable(R.drawable.gridbtn_p1));

        } else {
            ((Button) v).setText("O");
            textTurn.setText("X, it's your turn");
            v.setBackground(getResources().getDrawable(R.drawable.gridbtn_p2));

        }

        //take first turn
        String id_no = v.getResources().getResourceEntryName(v.getId()).split("_")[1];
        int id_row = id_no.charAt(0) - 48;
        int id_col = id_no.charAt(1) - 48;
        int used_cell = id_row*3 + id_col;

        System.err.println("setTExt: "+((Button)v).getText()+ " used cell: "+used_cell);

        //remove used cell from the choices list
        nextTurnChoices.remove(new Integer(used_cell));
        for(int i=0;i<nextTurnChoices.size();i++)
        {
            System.err.print(nextTurnChoices.get(i)+" ");
        }
        System.err.println();
        roundCount++;
        //remove item from nextTurnChoices
        //nextTurnChoices.remove();

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

            if(isSinglePlayer && !player1Turn) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Random random = new Random();
                int index = random.nextInt(nextTurnChoices.size());
                int random_box = nextTurnChoices.get(index);

                int random_row = random_box / 3, random_column = random_box % 3;
                String random_id = "btn_" + random_row + "" + random_column;

                int resID = getResources().getIdentifier(random_id, "id", getPackageName());
                Button random_btn = findViewById(resID);
                random_btn.performClick();
            }

        }
    }

    private void playDrawSound() {
        if(isSoundEnabled)
            soundPool.play(sound_finished, 1, 1, 0, 0, 1);

    }

    private void playWinSound() {
        if(isSoundEnabled)
            soundPool.play(sound_win, 1, 1, 0, 0, 1);
    }

    private void playClickSound() {
        if(isSoundEnabled)
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
        textTurn.setText("X, it's your turn");

        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++) {
                buttons[i][j].setBackground(getResources().getDrawable(R.drawable.gridbtn_empty));
            }
        }

        nextTurnChoices.clear();
        for(int i=0;i<9;i++) {
            nextTurnChoices.add(i);
        }

    }

    private void resetGame() {
        player1Points = 0;
        player2Points = 0;
        updatePointsText();
        resetBoard();
    }

    public void HowtoPlayDialog() {

        howtoplay_dialog = new Dialog(GameActivity.this);

        howtoplay_dialog.setContentView(R.layout.howtoplaydialog);

        howtoplay_dialog.show();

    }
}
