package com.vaavdevelopers.tictactoe.modular;

import java.util.ArrayList;
import java.util.Arrays;

public class XOBoard {

    public static final int RESULT_X_WIN = 1;
    public static final int RESULT_DRAW = 0;
    public static final int RESULT_O_WIN = -1;
    public static final int RESULT_NONE = -2;
    int n, winning_match;
    String board[][];
    int roundCount = 0;
    int player1Points = 0, player2Points = 0;
    ArrayList<String> temp_diagonal;

    XOBoard(int n, int x) {
        this.n = n;
        this.winning_match = x;
        board = new String[n][n];
        resetBoard();
    }

    void playMove(int move, boolean playerTurn) {
        int i = move/n;
        int j = move%n;
        if(playerTurn)
            board[i][j] = "X";
        else
            board[i][j] = "O";
        roundCount++;

    }

    public String checkForMatch() {
        int i, j;
        String first;

        //System.err.println(temp_diagonal);

        //checking for match
        for(i=0;i <= temp_diagonal.size() - winning_match;i++)
        {
            if(temp_diagonal.get(i).equals(""))
                continue; // empty i.e move not made

            first = temp_diagonal.get(i);

            for(j=i+1;j<i+winning_match;j++)
            {
                if(!temp_diagonal.get(j).equals(first))
                    break;
            }
            if(j>=i+winning_match) {
                //System.err.println(first);
                return first;
            }

        }
        return "";
    }


    public int gameResult() {

        int i, j, k;
        //boolean flag = false;
        temp_diagonal = new ArrayList<>(n);


        k = n;
        String flag = "";

        for(i=0; i<n; i++) {

            for(j=0;j<n;j++) { temp_diagonal.add(board[i][j]); }
            flag = checkForMatch();
            if(!flag.equals("")) { break; }
            temp_diagonal.clear();
        }


        for(i=0;i<n;i++) {

            for(j=0;j<n;j++) { temp_diagonal.add(board[j][i]); }
            flag = checkForMatch();
            if(!flag.equals("")) { break; }
            temp_diagonal.clear();
        }

        //diagonal
        while(k>=winning_match) {

            for(i=k-1, j=n-1; i>=0; i--, j--) temp_diagonal.add(board[i][j]);
            flag = checkForMatch();
            if(!flag.equals("")) { break; }

            temp_diagonal.clear();

            for(i=k-1, j=n-1; i>=0; i--, j--) temp_diagonal.add(board[j][i]);
            flag = checkForMatch();
            if(!flag.equals("")) { break; }

            temp_diagonal.clear();

            for(i=n-k, j=n-1; i<n; i++, j--)  temp_diagonal.add(board[i][j]);
            flag = checkForMatch();
            if(!flag.equals("")) { break; }

            temp_diagonal.clear();

            for(i=n-k, j=n-1; i<n; i++, j--)  temp_diagonal.add(board[j][i]);
            flag = checkForMatch();
            if(!flag.equals("")) { break; }

            temp_diagonal.clear();
            k--;

        }

        temp_diagonal.clear();

        if(flag.equals("X")) { player1Points++; return RESULT_X_WIN;}
        else if (flag.equals("O")) {player2Points++; return RESULT_O_WIN; }
        else if(roundCount == n*n) return RESULT_DRAW;
        else return RESULT_NONE;
    }


    public void resetBoard() {

        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                board[i][j] = "";

        roundCount = 0;
    }
}

