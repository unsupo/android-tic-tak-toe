package arndt.com.ticktacktoe;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import arndt.com.ticktacktoe.objects.Pair;

import static arndt.com.ticktacktoe.TickTakToe.O;
import static arndt.com.ticktacktoe.TickTakToe.X;

public class MainActivity extends AppCompatActivity {
    TickTakToe tickTakToe;
    private int wins = 0, loses = 0, ties = 0;
    private int columns = 3, rows = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tickTakToe = new TickTakToe();
        setClickListeners();
        findViewById(R.id.bReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }

    private void reset() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                getButton(i,j).setText("");
            }
        }
        tickTakToe = new TickTakToe();
        ((TextView) findViewById(R.id.tvInfo)).setText("");
        if(player == X) {
            makeComputerMove();
            player = O;
        }else
            player = X;
    }

    private void setClickListeners() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final int m = i,n = j;
                final Button b = getButton(i,j);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getDelayed())
                            return;
                        if(b.getText() != null && (b.getText().equals("X") || b.getText().equals("O")))
                            return;
                        b.setText(tickTakToe.lastPlayer==O?"X":"O");
                        tickTakToe.makeMove(m,n);
                        makeComputerMove();
                    }
                });
            }
        }
    }

    private void makeComputerMove() {
        Pair<Integer, Integer> computerMove = tickTakToe.getComputerMove();
        int score = TickTakToe.getBoardScore(tickTakToe.getBoard());
        if(computerMove == null) {
            endGame(score);
        }else {
            int lastPlayer = tickTakToe.lastPlayer;
            getButton(computerMove.getFirst(), computerMove.getSecond()).setText(lastPlayer==O?"X":"O");
            tickTakToe.makeMove(computerMove.getFirst(), computerMove.getSecond());
            computerMove = tickTakToe.getComputerMove();
            score = TickTakToe.getBoardScore(tickTakToe.getBoard());
            if(computerMove == null)
                endGame(score);
        }
    }

    private void endGame(int score) {
        String win = "You Win", lose = "You Lose";
        ((TextView) findViewById(R.id.tvInfo)).setText(
                (score == 0 ? "SCRATCH, TIE" : score < 0 ?
                        "O's win "+(this.player==O?win:lose) :
                        "X's win "+(this.player==X?win:lose))
        );
        List<Pair<Integer, Integer>> victory = tickTakToe.getVictory();
        if(victory != null) {
            for (Pair<Integer, Integer> s : victory) {
                Button b = getButton(s.getFirst(), s.getSecond());
                b.setText("--" + b.getText() + "--");
            }
        }
        if(score == 0)
            ties++;
        else if(this.player == X){
            if(score < 0)
                loses++;
            else
                wins++;
        }else {
            if (score > 0)
                loses++;
            else
                wins++;
        }
        ((TextView)findViewById(R.id.history)).setText("Wins: "+wins+", Loses: "+loses+", Ties: "+ties);
        setDelayed(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reset();
                setDelayed(false);
            }
        }, 1000);
    }

    private int player = X;
    private boolean delayed = false;
    private synchronized boolean getDelayed(){
        return delayed;
    } private synchronized void setDelayed(boolean delayed){
        this.delayed = delayed;
    }

    private Button getButton(int row, int column){
        return findViewById(getResources().getIdentifier("b"+row+column,"id",this.getPackageName()));
    }
}
