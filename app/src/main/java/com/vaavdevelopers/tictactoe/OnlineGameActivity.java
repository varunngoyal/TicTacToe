package com.vaavdevelopers.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vaavdevelopers.tictactoe.GameAudio;
import com.vaavdevelopers.tictactoe.R;
import com.vaavdevelopers.tictactoe.TictactoeGame;

public class OnlineGameActivity extends AppCompatActivity implements View.OnClickListener{

    String roomName, playerName, message;
    Boolean host = false;

    FirebaseDatabase database;
    DatabaseReference messageRef, player2Ref, player1Ref, gameRef;

    TictactoeGame game;
    GameAudio audio;
    TextView textViewPoints;
    Button btnReset;

    //private Button[][] buttons = new Button[3][3];
    //int roundCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        game = new TictactoeGame(this);
        audio = new GameAudio(this);
        textViewPoints = findViewById(R.id.text_view_points);

        //initialize the buttons
        for(int i=0;i<3;i++) {

            for(int j=0;j<3;j++) {
                String button_id = "btn_"+i+""+j;
                System.err.println(button_id);
                int resID = getResources().getIdentifier(button_id, "id", getPackageName());

                game.board[i][j] = findViewById(resID);
                game.board[i][j].setOnClickListener(this);

            }

        }
        audio.playMusic();

        btnReset = findViewById(R.id.button_reset);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("playerName", "");

        roomName = getIntent().getStringExtra("roomName");
        host = getIntent().getBooleanExtra("host", false);


        /*if(roomName.equals(playerName))
            host = true;
        else
            host = false;*/

        // listen for incoming messages
        database = FirebaseDatabase.getInstance();
        messageRef = database.getReference("rooms/" + roomName + "/message" );
        player2Ref = database.getReference("rooms/"+roomName + "/player2");
        gameRef = database.getReference("rooms/"+roomName );

        if(host) {
            player2Ref.setValue("");
            addPlayer2EventListener();
            messageRef.setValue("");

        } else {
            //player1Ref = database.getReference("rooms/"+roomName+"/player1");
            Toast.makeText(this, "Welcome to the online mode! Player1 has already joined the game!", Toast.LENGTH_SHORT).show();
        }

        //dummy O's turn before beginning the game so that X can start the game i.e. host
        //message = "O:";

        game.setBoardNotPlayable();

        addRoomEventListener(); 
        //addGameCloseListener();
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

    private void addPlayer2EventListener() {
        player2Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //player 2 joins room
                String player2_name = dataSnapshot.getValue(String.class);

                if(!player2_name.equals("")) {
                    Toast.makeText(OnlineGameActivity.this, ""+player2_name+" has joined the room!", Toast.LENGTH_SHORT).show();
                    message = "O:";
                    messageRef.setValue(message);

                } else {
                    Toast.makeText(OnlineGameActivity.this, "Welcome to online mode! Waiting for player 2 to join..", Toast.LENGTH_SHORT).show();
                    game.resetBoard();
                    game.setBoardNotPlayable();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        String id_no = v.getResources().getResourceEntryName(v.getId()).split("_")[1];
        int id_row = id_no.charAt(0) - 48;
        int id_col = id_no.charAt(1) - 48;
        int used_cell = id_row * 3 + id_col;
        String message = "";

        if (!((Button) v).getText().toString().equals("")) {
            return;
        }

        //disableButtons();
        game.setBoardNotPlayable();

        if(host == true) {
            ((Button) v).setText("X");
            message = "X:";
            v.setBackground(getResources().getDrawable(R.drawable.gridbtn_p1));

        } else {
            ((Button) v).setText("O");
            message = "O:";
            v.setBackground(getResources().getDrawable(R.drawable.gridbtn_p2));
        }
        messageRef.setValue(message + used_cell);

        evaluateRound(host);
    }

    public void evaluateRound(boolean host) {
        audio.playClickSound();

        game.roundCount++;
        //remove item from nextTurnChoices
        //nextTurnChoices.remove();

        if (game.checkForWin()) {


            if (host) {
                game.player1Wins();
                audio.playWinSound();
            } else {
                game.player2Wins();
                audio.playWinSound();
            }
            updatePointsText();

        }
        else if(game.roundCount == 9) {
            game.draw();
            audio.playDrawSound();
        }
    }



    public void copyRoundState(DataSnapshot dataSnapshot) {
        game.setBoardPlayable();

        Toast.makeText(OnlineGameActivity.this, "" +
                dataSnapshot.getValue(String.class), Toast.LENGTH_SHORT).show();
        String[] temp = dataSnapshot.getValue(String.class).split(":");
        if(temp.length > 1) {
            int used_cell = Integer.parseInt(temp[1]);
            int used_row = used_cell / 3;
            int used_col = used_cell % 3;


            game.board[used_row][used_col].setText(temp[0]);
            if(host) {
                game.board[used_row][used_col].setBackground(getResources().getDrawable(R.drawable.gridbtn_p2));
            } else {
                game.board[used_row][used_col].setBackground(getResources().getDrawable(R.drawable.gridbtn_p1));

            }
            evaluateRound(!host);
        }
    }

    private void updatePointsText() {

        textViewPoints.setText(game.player1Points+":"+game.player2Points);
    }

    private void resetGame() {
        game.player1Points = 0;
        game.player2Points = 0;
        updatePointsText();
        //game.resetBoard();
    }

    @Override
    protected void onDestroy() {
        audio.releaseMusic();
        audio.releaseSound();
        audio.onDestroy();
        game.onDestroy();
        super.onDestroy();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isFinishing()) {
            if(!host) {
                // give notification to p1 and make player2 string empty
                player2Ref.setValue("");
                Toast.makeText(this, "You left the room!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
