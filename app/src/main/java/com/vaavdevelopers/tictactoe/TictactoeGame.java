package com.vaavdevelopers.tictactoe;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class TictactoeGame {

    Button[][] board;
    int roundCount;
    private Context context;
    boolean X_turn;
    int player1Points, player2Points;

    public TictactoeGame(Context context) {
        board = new Button[3][3];
        this.context = context;
        roundCount = 0;
        X_turn = true;
        player1Points = player2Points = 0;
    }

    public boolean checkForWin() {
        String[][] field = new String[3][3];

        for(int i=0;i<3;i++) {
            for(int j=0;j<3;j++) {
                field[i][j] = board[i][j].getText().toString();
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

    public void player1Wins() {
        player1Points++;
        Toast.makeText(context, "X wins!", Toast.LENGTH_SHORT).show();

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        //title of game conclusion
        TextView Xwins = new TextView(context);
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


    public void player2Wins() {
        Toast.makeText(context, "O wins!", Toast.LENGTH_SHORT).show();
        player2Points++;

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        TextView Owins = new TextView(context);
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

    public void draw() {
        Toast.makeText(context, "Draw!", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        TextView draw = new TextView(context);
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

    public void resetBoard() {

        for(int i=0;i<3;i++) {
            for(int j=0;j<3;j++) {
                board[i][j].setText("");
            }
        }
        roundCount = 0;

        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++) {
                board[i][j].setBackground(context.getResources().getDrawable(R.drawable.gridbtn_empty));
            }
        }
    }

    public void setBoardPlayable() {
        for(int i=0;i<3;i++) {
            for(int j=0;j<3;j++) {
                board[i][j].setEnabled(true);
            }
        }
    }

    public void setBoardNotPlayable() {
        for(int i=0;i<3;i++) {
            for(int j=0;j<3;j++) {
                board[i][j].setEnabled(false);
            }
        }
    }


    public void onDestroy() {
        if(context != null) {
            context = null;
        }
    }
}
