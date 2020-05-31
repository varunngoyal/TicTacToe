package com.vaavdevelopers.tictactoe.modular;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vaavdevelopers.tictactoe.R;

public class OnlineActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PREFS_NAME = "TicTacToeShared";
    LinearLayout linearLayout;
    TableRow.LayoutParams params;
    private int n = 3, matchToWin = 3;
    boolean player1Turn = true;
    XOBoard board;
    Button[][] buttons = new Button[n][n];
    TextView textViewPoints, textTurn;

    String roomName, playerName, message;
    Boolean host = false;
    FirebaseDatabase database;
    DatabaseReference messageRef, player2Ref, player1Ref, gameRef;
    boolean copyRoundStateFlag = false;

    //audio variables
    SoundPool soundPool;
    int sound_win, sound_click, sound_finished;
    MediaPlayer player;
    private boolean isSoundEnabled, isMusicEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        linearLayout = findViewById(R.id.LL);
        textViewPoints = findViewById(R.id.text_view_points);
        textTurn = findViewById(R.id.txt_turn);
        board = new XOBoard(n, matchToWin);
        initGameBoard();

        findViewById(R.id.button_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        //******************************audio code **************************************************
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
        //check if sound and music are enabled
        isMusicEnabled = preferences.getBoolean("music", true);
        isSoundEnabled = preferences.getBoolean("sound", true);


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
            player = new MediaPlayer();
            try { player.setDataSource(this, Uri.parse("android.resource://" + this.getPackageName() + "/raw/music_loop")); }
            catch (Exception e) {}
            try { player.prepare(); } catch (Exception e) {}
            //player.start();
            //player = MediaPlayer.create(this, R.raw.music_loop);
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

        //*******************************************************************************************

        playerName = preferences.getString("playerName", "");

        roomName = getIntent().getStringExtra("roomName");
        host = getIntent().getBooleanExtra("host", false);

        database = FirebaseDatabase.getInstance();
        messageRef = database.getReference("rooms/" + roomName + "/message" );
        player2Ref = database.getReference("rooms/"+roomName + "/player2");
        gameRef = database.getReference("rooms/"+roomName );
        player1Ref = database.getReference("rooms/"+roomName+"/player1");

        if(host) {
            player2Ref.setValue("");
            addPlayer2EventListener();
            messageRef.setValue("");

        } else {
            //player1Ref = database.getReference("rooms/"+roomName+"/player1");
            Toast.makeText(this, "Welcome to the online mode! Player1 has already joined the game!",
                    Toast.LENGTH_LONG).show();
            player2Ref.setValue(playerName);
            addPlayer1EventListener();
        }

        setBoardPlayable(false);

        addRoomEventListener();


    }

    private void addRoomEventListener() {
        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //message received

                if(host && dataSnapshot.getValue(String.class).contains("O:") ||
                        !host && dataSnapshot.getValue(String.class).contains("X:")) {

                    copyRoundState(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void copyRoundState(DataSnapshot dataSnapshot) {

        Toast.makeText(OnlineActivity.this, "" +
                dataSnapshot.getValue(String.class), Toast.LENGTH_SHORT).show();

        setBoardPlayable(true);

        String[] temp = dataSnapshot.getValue(String.class).split(":");
        if(temp.length > 1) {
            int used_cell = Integer.parseInt(temp[1]);
            int used_row = used_cell / 3;
            int used_col = used_cell % 3;

            copyRoundStateFlag = true;
            buttons[used_row][used_col].performClick();
            copyRoundStateFlag = false;
        }
    }

    private void addPlayer2EventListener() {
        player2Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //player 2 joins room
                String player2_name = dataSnapshot.getValue(String.class);

                if(!player2_name.equals("")) {
                    Toast.makeText(OnlineActivity.this,
                            ""+player2_name+" has joined the room!",
                            Toast.LENGTH_SHORT).show();
                    message = "O:";
                    messageRef.setValue(message);

                } else {
                    Toast.makeText(OnlineActivity.this,
                            "Welcome to online mode! Waiting for player 2 to join..",
                            Toast.LENGTH_SHORT).show();
                    board.resetBoard();
                    setBoardPlayable(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addPlayer1EventListener() {
        player2Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //player 2 joins room
                String player_name = dataSnapshot.getValue(String.class);

                if(!player_name.equals("")) {
                    Toast.makeText(OnlineActivity.this,
                            ""+player_name+" has joined the room!",
                            Toast.LENGTH_SHORT).show();
                    message = "O:";
                    messageRef.setValue(message);

                } else {
                    Toast.makeText(OnlineActivity.this,
                            player_name+" has left the room",
                            Toast.LENGTH_SHORT).show();
                    board.resetBoard();
                    setBoardPlayable(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                buttons[i][j].setBackgroundResource(R.drawable.gridbtn_custom_empty);
                //button.setText(i+""+j);
                buttons[i][j].setText("");
                buttons[i][j].setLayoutParams(params);

                buttons[i][j].setTextColor(Color.WHITE);

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

        if(!copyRoundStateFlag)
            setBoardPlayable(false);

        if (player1Turn) {
            ((Button) v).setText("X");
            v.setBackground(getResources().getDrawable(R.drawable.gridbtn_custom_p1));
            message = "X:";

        } else {
            ((Button) v).setText("O");
            v.setBackground(getResources().getDrawable(R.drawable.gridbtn_custom_p2));
            message = "O:";

        }
        int button_no = extractButtonNoFromTag((Button)v);
        board.playMove(button_no, player1Turn);


        int result = board.gameResult();
        //System.err.println(result);
        gameResultListener(result);

        messageRef.setValue(message + button_no);

    }

    private void gameResultListener(int result) {

        switch (result) {

            case XOBoard.RESULT_X_WIN:

                showWinAlert("X wins!");
                textViewPoints.setText(" "+board.player1Points+":"+board.player2Points+" ");

                //Toast.makeText(this, "X wins!", Toast.LENGTH_SHORT).show();
                break;

            case XOBoard.RESULT_O_WIN:

                showWinAlert("O wins!");
                textViewPoints.setText(" "+board.player1Points+":"+board.player2Points+" ");

                //Toast.makeText(this, "O wins!", Toast.LENGTH_SHORT).show();
                break;

            case XOBoard.RESULT_DRAW:

                showWinAlert("Draw!");
                //Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();
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


    }

    private void showWinAlert(String winStr) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if(winStr.equals("Draw!")) {
            if(isSoundEnabled)
                soundPool.play(sound_finished, 1, 1, 0, 0, 1);
            //play draw sound
        } else {
            if(isSoundEnabled)
                soundPool.play(sound_win, 1, 1, 0, 0, 1);
            //play win sound
        }

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

    private void resetBoard() {
        board.resetBoard();

        for(int i=0;i<n;i++) {
            for(int j=0;j<n;j++) {
                buttons[i][j].setText("");
                buttons[i][j].setBackgroundResource(R.drawable.gridbtn_custom_empty);
            }
        }
        player1Turn = true;
        textTurn.setText("X, it's your turn");
        if(host)
            setBoardPlayable(true);
        else
            setBoardPlayable(false);
    }

    private void resetGame() {
        board.player1Points = board.player2Points = 0;
        textViewPoints.setText(" 0:0 ");
    }

    private void setBoardPlayable(boolean toggle) {

        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                buttons[i][j].setEnabled(toggle);
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
