package com.vaavdevelopers.tictactoe.modular;

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
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.vaavdevelopers.tictactoe.GameActivity;
import com.vaavdevelopers.tictactoe.R;

import java.util.ArrayList;
import java.util.Random;

public class OfflineGameActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PREFS_NAME = "TicTacToeShared";
    LinearLayout linearLayout;
    TableRow.LayoutParams params;
    private int n = 3, matchToWin = 3;
    boolean player1Turn = true;
    XOBoard board;
    Button[][] buttons = new Button[n][n];
    TextView textViewPoints, textTurn;
    SoundPool soundPool;
    int sound_win, sound_click, sound_finished;
    MediaPlayer player;
    private Dialog howtoplay_dialog;
    private boolean isSoundEnabled, isMusicEnabled;
    boolean isSinglePlayer;
    ArrayList<Integer> nextTurnChoices = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_game);

        linearLayout = findViewById(R.id.LL);
        textViewPoints = findViewById(R.id.text_view_points);
        textTurn = findViewById(R.id.txt_turn);

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

        for(int i=0;i<n*n;i++)
            nextTurnChoices.add(i);

        board = new XOBoard(n, matchToWin);
        initGameBoard();

        findViewById(R.id.button_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
    }

    private void initGameBoard() {
        TableLayout mTableLayout = new TableLayout(this);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        mTableLayout.setLayoutParams(new TableLayout.LayoutParams(
                displaymetrics.widthPixels, displaymetrics.widthPixels
        ));

        params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, displaymetrics.widthPixels/n, 1f
        );

        //mTableLayout.setStretchAllColumns(true);

        for(int i=0;i<n;i++) {
            TableRow row = new TableRow(this);

            row.setLayoutParams(params);

            for(int j=0;j<n;j++) {
                buttons[i][j] = new Button(this);
                buttons[i][j].setPadding(0,0,0,0);
                buttons[i][j].setBackgroundResource(R.drawable.gridbtn_empty);
                //button.setText(i+""+j);
                buttons[i][j].setText("");
                buttons[i][j].setLayoutParams(params);

                buttons[i][j].setTag("btn_"+(i*n+j));
                buttons[i][j].setOnClickListener(this);
                //System.err.println(buttons[i][j].getTag());
                row.addView(buttons[i][j]);
            }
            mTableLayout.addView(row);

        }
        linearLayout.addView(mTableLayout);
    }

    private int extractButtonNoFromTag(Button button){
        String tag = (String)button.getTag();
        String number = tag.split("_")[1];
        return Integer.parseInt(number);
    }

    @Override
    public void onClick(View v) {

        //move already played
        if(!((Button)v).getText().equals("")) {
            return ;
        }

        if(isSoundEnabled)
            soundPool.play(sound_click, 1, 1, 0, 0, 1);


        if (player1Turn) {
            ((Button) v).setText("X");
            v.setBackground(getResources().getDrawable(R.drawable.gridbtn_p1));

        } else {
            ((Button) v).setText("O");
            v.setBackground(getResources().getDrawable(R.drawable.gridbtn_p2));

        }
        int button_no = extractButtonNoFromTag((Button)v);
        board.playMove(button_no, player1Turn);
        //single player code
        nextTurnChoices.remove(new Integer(button_no));
        System.err.println("next turn choices:"+nextTurnChoices);

        int result = board.gameResult();
        System.err.println(result);
        gameResultListener(result);

        if(result == XOBoard.RESULT_NONE) {

            if(isSinglePlayer && !player1Turn) {

                Random random = new Random();
                int index = random.nextInt(nextTurnChoices.size());
                int random_box = nextTurnChoices.get(index);

                buttons[random_box/n][random_box%n].performClick();
            }
        }

    }

    private void gameResultListener(int result) {

        switch (result) {

            case XOBoard.RESULT_X_WIN:

                if(isSoundEnabled)
                    soundPool.play(sound_win, 1, 1, 0, 0, 1);

                showWinAlert("X wins!");
                textViewPoints.setText(" "+board.player1Points+":"+board.player2Points+" ");

                Toast.makeText(this, "X wins!", Toast.LENGTH_SHORT).show();
                break;

            case XOBoard.RESULT_O_WIN:

                if(isSoundEnabled)
                    soundPool.play(sound_win, 1, 1, 0, 0, 1);

                showWinAlert("O wins!");
                textViewPoints.setText(" "+board.player1Points+":"+board.player2Points+" ");

                Toast.makeText(this, "O wins!", Toast.LENGTH_SHORT).show();
                break;

            case XOBoard.RESULT_DRAW:

                if(isSoundEnabled)
                    soundPool.play(sound_finished, 1, 1, 0, 0, 1);

                showWinAlert("Draw!");
                Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();
                break;

            case XOBoard.RESULT_NONE:
                //Toast.makeText(this, "Continue turn..", Toast.LENGTH_SHORT).show();
                player1Turn = !player1Turn;
                if(player1Turn)
                    textTurn.setText("X, it's your turn");
                else
                    textTurn.setText("O, it's your turn");
                break;

        }

        /*if (result != XOBoard.RESULT_NONE) {
            resetBoard();
        }*/
    }

    private void resetBoard() {
        board.resetBoard();

        for(int i=0;i<n;i++) {
            for(int j=0;j<n;j++) {
                buttons[i][j].setText("");
                buttons[i][j].setBackgroundResource(R.drawable.gridbtn_empty);
            }
        }
        player1Turn = true;

        nextTurnChoices.clear();
        for(int i=0;i<n*n;i++)
            nextTurnChoices.add(i);
    }

    private void resetGame() {
        board.player1Points = board.player2Points = 0;
        textViewPoints.setText(" 0:0 ");
    }

    private void showWinAlert(String winStr) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //title of game conclusion
        TextView Xwins = new TextView(this);
        Xwins.setPadding(0, 40, 0, 0);
        Xwins.setTextSize(30);
        Xwins.setTextColor(Color.BLACK);
        Xwins.setTypeface(null, Typeface.BOLD);
        Xwins.setGravity(Gravity.CENTER_HORIZONTAL);
        Xwins.setText(winStr);
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


    }

    public void HowtoPlayDialog() {

        howtoplay_dialog = new Dialog(OfflineGameActivity.this);

        howtoplay_dialog.setContentView(R.layout.howtoplaydialog);

        howtoplay_dialog.show();

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
}
