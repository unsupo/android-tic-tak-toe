package arndt.com.ticktacktoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import arndt.com.ticktacktoe.objects.Node;
import arndt.com.ticktacktoe.objects.Pair;

/**
 * Created by jarndt on 1/29/18.
 */

public class TickTakToe {
    public static int X = 1, O = 2, NOTHING = 0;
    private int[][] board;
    int lastPlayer = O;
    public TickTakToe(){
        init();
    }
    private void init(){
        board = new int[3][3];
    }

    public static List<Pair<Integer,Integer>> getAvailableMoves(int[][] board){
        List<Pair<Integer,Integer>> moves = new ArrayList<>();
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board[i].length; j++)
                if(board[i][j]==NOTHING)
                    moves.add(new Pair<>(i,j));
        return moves;
    }

    public static int getBoardScore(int[][] board){
        int[] x = new int[board.length];
        int score = 0, z = 0, zz = 0;
        for (int i = 0; i < board.length; i++) {
            int y = 0;
            for (int j = 0; j < board[i].length; j++) {
                if(i==j)
                    if(board[i][j]==X)
                        z+=1;
                    else if(board[i][j]==O)
                        z-=1;
                if(board[i][j]==X) {
                    y += 1;
                    x[j] += 1;
                }else if(board[i][j]==O) {
                    y -= 1;
                    x[j] -= 1;
                }
            }
            if(Math.abs(y)==board[i].length)
                score+=y;
        }
        for (int i = 0; i < x.length; i++)
            if(Math.abs(x[i])==board.length)
                score+=x[i];
        if(Math.abs(z)==board.length)
            score+=z;
        for (int i = 0; i < board.length; i++)
            if(board[i][board.length-1-i]==X)
                zz+=1;
            else if(board[i][board.length-1-i]==O)
                zz-=1;
        if(Math.abs(zz)==board.length)
            score+=zz;
        return score;
    }

    public void makeMove(int x, int y) {
        int piece = lastPlayer == X ? O : X;
        board[x][y]=piece;
        lastPlayer=piece;
    }
    public void makeMove(int piece, int x, int y) {
        board[x][y]=piece;
        lastPlayer=piece;
    }

    public int[][] getBoard() {
        return board;
    }

    public Pair<Integer,Integer> getComputerMove(){
        int piece = lastPlayer == X ? O : X;
        Node<Integer,Integer> parent = new Node<>(board.hashCode(),getBoardScore(board));
        parent.getValues().put("board",board);
        double v = minimax(parent,piece,board,-Integer.MAX_VALUE,Integer.MAX_VALUE,0);
//        double v = alphabeta(parent,0,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,piece==X?colorX:colorO);
        List<Node<Integer,Integer>> moves = new ArrayList<>();
        for(Node<Integer,Integer> s : parent.getChildren())
            if(s.getValues().get("score")!=null && s.getValues().get("score").equals(v))
                moves.add(s);
        if(moves.isEmpty())
            return null;
        return (Pair<Integer, Integer>) moves.get(randBetween(0,moves.size()-1)).getValues().get("pair");
    }

    private double alphabeta(Node<Integer,Integer> node, int depth, double alpha, double beta, int player){
        int[][] pb = ((int[][])node.getValues().get("board"));
        List<Pair<Integer, Integer>> moves = getAvailableMoves(pb);
        int bs = getBoardScore(board);
        if (moves.isEmpty() || Math.abs(bs) >= board.length)//leaf node
            return bs;//+color*depth/10.;
        if(player == X){
            for(Pair<Integer, Integer> s : moves){
                int[][] b = clone(pb);
                b[s.getFirst()][s.getSecond()]=player;
                Node<Integer, Integer> child = new Node<>(bs,b.hashCode());
                child.getValues().put("board",b);
                node.getChildren().add(child);
                child.setParent(node);
                beta = Math.max(beta,alphabeta(child,depth+1,alpha,beta,-player));
//                if(beta<=alpha)
//                    break;
            }
            return alpha;
        }else {
            for(Pair<Integer, Integer> s : moves){
                int[][] b = clone(pb);
                b[s.getFirst()][s.getSecond()]=player;
                Node<Integer, Integer> child = new Node<>(bs,b.hashCode());
                child.getValues().put("board",b);
                node.getChildren().add(child);
                child.setParent(node);
                alpha = Math.min(alpha,alphabeta(child,depth+1,alpha,beta,-player));
//                if(beta<=alpha)
//                    break;
            }
            return beta;
        }
    }

    int colorX = 1, colorO = -1;
    private double pvs(Node<Integer,Integer> node, int depth, double alpha, double beta, int color){
        int[][] pb = ((int[][])node.getValues().get("board"));
        List<Pair<Integer, Integer>> moves = getAvailableMoves(pb);
        int bs = getBoardScore(board);
        if (moves.isEmpty() || Math.abs(bs) >= board.length)//leaf node
            return bs;//+color*depth/10.;
        int i = 0;
        double score;
        for(Pair<Integer, Integer> s : moves){
            int[][] b = clone(pb);
            b[s.getFirst()][s.getSecond()]=color==colorX?X:O;
            Node<Integer, Integer> child = new Node<>(bs,b.hashCode());
            child.getValues().put("board",b);
            node.getChildren().add(child);
            child.setParent(node);
            if(i++==0)
                score = -pvs(child,depth-1,-beta,-alpha,-color);
            else {
                score = -pvs(child,depth-1,-alpha-1,-alpha,-color);
                if(alpha<score && score<beta)
                    score = -pvs(child,depth-1,-beta,-score,-color);
            }
            alpha = Math.max(alpha,score);
            if (alpha>=beta)
                break;
        }
        return alpha;
    }


    private static double minimax(Node<Integer,Integer> parent, int player, final int[][] board, double alpha, double beta, int depth) {
        List<Pair<Integer, Integer>> moves = getAvailableMoves(board);
        int bs = getBoardScore(board);
        if (moves.isEmpty() || Math.abs(bs) == board.length)//leaf node
            return bs+(player==X?-1:1)*depth/10.;
        double bestVal = player == X ? -Integer.MAX_VALUE : Integer.MAX_VALUE;
        for(Pair<Integer, Integer> s : moves){
            int[][] b = clone(board);
            b[s.getFirst()][s.getSecond()]=player;
            Node<Integer, Integer> n = new Node<>(bs,b.hashCode());
            parent.getChildren().add(n);
            n.setParent(parent);
//            map.put(b.hashCode(),b);
            double score = minimax(n,player==O?X:O,b,alpha,beta, depth+1);
            n.getValues().put("score",score);
            n.getValues().put("pair",s);
            if(player == X) {
                bestVal = Math.max(bestVal, score);
                alpha = Math.max(alpha,bestVal);
            } else {
                bestVal = Math.min(bestVal, score);
                beta = Math.min(beta,bestVal);
            }
            if(beta<= alpha)
                break;
        }
        return bestVal;
    }
    private static int[][] clone(int[][] b) {
        int[][] tmp = new int[b.length][b[0].length];
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b[i].length; j++) {
                tmp[i][j]=b[i][j];
            }
        }
        return tmp;
    }

    private static Random r;    // pseudo-random number generator
    private static long seed;        // pseudo-random number generator seed

    // static initializer
    static {
        // this is how the seed was set in Java 1.4
        seed = System.currentTimeMillis();
        r = new Random(seed);
    }
    public static int randBetween(int min, int max){
        return r.nextInt(max - min + 1) + min;
    }

    public List<Pair<Integer,Integer>> getVictory() {
        if(Math.abs(getBoardScore(board))<board.length)
            return null;//then no victory yet
        int[] x = new int[board.length];
        List<Pair<Integer,Integer>>[] xVictory = new List[board.length];
        for (int i = 0; i < board.length; i++)
            xVictory[i] = new ArrayList<>();
        List<Pair<Integer,Integer>> victory = new ArrayList<>(),
                zxVictory = new ArrayList<>(),
                zyVictory = new ArrayList<>();
        int zx = 0, zy = 0;
        for (int i = 0; i < board.length; i++) {
            int y = 0;
            List<Pair<Integer,Integer>> yVictory = new ArrayList<>();
            for (int j = 0; j < board[i].length; j++) {
                yVictory.add(new Pair<>(i,j));
                if(board[i][j]==X) {
                    y++;
                    x[j]++;
                    xVictory[j].add(new Pair<>(i,j));
                }else if(board[i][j]==O) {
                    y--;
                    x[j]--;
                    xVictory[j].add(new Pair<>(i,j));
                }if(i==j){
                    zxVictory.add(new Pair<>(i,j));
                    if(board[i][j]==X)
                        zx++;
                    else if(board[i][j]==O)
                        zx--;
                }if(i==board.length-1-j){
                    zyVictory.add(new Pair<>(i,j));
                    if(board[i][j]==X)
                        zy++;
                    else if(board[i][j]==O)
                        zy--;
                }
            }
            if(Math.abs(y)==board.length)
                victory.addAll(yVictory);
        }
        if(Math.abs(zx) == board.length)
            victory.addAll(zxVictory);
        if(Math.abs(zy) == board.length)
            victory.addAll(zyVictory);
        for (int i = 0; i < x.length; i++)
            if(Math.abs(x[i])==board.length)
                victory.addAll(xVictory[i]);
        return victory;
    }
}
