package com.vaavdevelopers.tictactoe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class RoomActivity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 0;
    Button button, createRoom, joinRoom;

    String playerName = "";

    FirebaseDatabase database;
    DatabaseReference playerRef, roomRefP1, roomRefP2;

    List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );


        createRoom = findViewById(R.id.createRoom);
        joinRoom = findViewById(R.id.joinRoom);

        database = FirebaseDatabase.getInstance();


        /*SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("PlayerName", "" );*/

        showSignInOptions();

        //check if the player exists and get reference
        if(!playerName.equals("")) {
            playerRef = database.getReference("players/"+playerName);
            addEventListener();
            playerRef.setValue("");

        }

        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRoomDialog();
            }
        });

        joinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinRoomDialog();
            }
        });

    }

    public void joinRoomDialog() {

        final AlertDialog.Builder alert = new AlertDialog.Builder(RoomActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.layout_create_room_dialog, null);

        alert.setView(mView);

        final EditText edit_room_name = mView.findViewById(R.id.roomName);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String roomName = edit_room_name.getText().toString();

                if(!roomName.equals("")) {
                    //create room
                    roomRefP2 = database.getReference("rooms/"+roomName+"/player2");
                    roomRefP2.setValue(playerName);

                    //enter room
                    Intent intent = new Intent(RoomActivity.this, OnlineGameActivity.class);
                    intent.putExtra("host", false);
                    intent.putExtra("roomName", roomName);
                    startActivity(intent);
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void createRoomDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(RoomActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.layout_create_room_dialog, null);

        alert.setView(mView);

        final EditText edit_room_name = mView.findViewById(R.id.roomName);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String roomName = edit_room_name.getText().toString();

                if(!roomName.equals("")) {
                    //create room
                    roomRefP1 = database.getReference("rooms/"+roomName+"/player1");
                    roomRefP1.setValue(playerName);

                    //enter room
                    Intent intent = new Intent(RoomActivity.this, OnlineGameActivity.class);
                    intent.putExtra("host", true);
                    intent.putExtra("roomName", roomName);
                    startActivity(intent);
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }



    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.MyTheme)
                        .build(), MY_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if(resultCode == RESULT_OK)
            {
                //Get User
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //Show Email on Toast
                Toast.makeText(this, ""+user.getEmail(),Toast.LENGTH_SHORT).show();

                //extract name from email
                playerName = extractNameFromEmail(user.getEmail());
                playerRef = database.getReference("players/"+playerName);
                addEventListener();
                playerRef.setValue("");


            } else {
                Toast.makeText(this, ""+response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String extractNameFromEmail(String email)
    {
        return email.split("@")[0];

    }

    public void addEventListener() {
        //read from database
        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //success =  continue to the next screen after saving the player's name
                if(!playerName.equals("")) {
                    SharedPreferences preferences = getSharedPreferences("PREFS", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("playerName", playerName);
                    editor.apply();

                    //startActivity(new Intent(getApplicationContext(), Main2Activity.class));
                    //finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //error
                button.setText("LOG IN");
                button.setEnabled(true);
                Toast.makeText(RoomActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
