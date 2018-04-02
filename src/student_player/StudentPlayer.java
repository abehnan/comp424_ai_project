package student_player;

import java.util.ArrayList;
import java.util.List;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;
import tablut.GreedyTablutPlayer;
import tablut.RandomTablutPlayer;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/**
 * A player file submitted by a student.
 */
public class StudentPlayer extends TablutPlayer {

    private MyTools myTools;
    private boolean setup = true;

    public StudentPlayer() {
        super("260639146");
    }

    public Move chooseMove(TablutBoardState boardState) {
        if (setup) {
            setup = false;
            myTools = new MyTools(this);
        }
        if (player_id == TablutBoardState.MUSCOVITE) {
            return myTools.generateMuscoviteMove(boardState);
        } else {
            return myTools.generateSwedeMove(boardState);
        }
    }

    // For Debugging purposes only.
    public static void main(String[] args) {


        int numGames = 100;
        int numDraws = 0;
        int swedeWins = 0;
        int muscoviteWins = 0;
        List<Integer> turnCounts = new ArrayList<>();

        for (int i = 1; i <= numGames; i++) {
            TablutBoardState b = new TablutBoardState();

            Player swede = new StudentPlayer();
//            Player swede = new RandomTablutPlayer("RandomSwede");
//            Player swede = new GreedyTablutPlayer("GreedySwede");
            swede.setColor(TablutBoardState.SWEDE);


//            Player muscovite = new GreedyTablutPlayer("GreedyMuscovite");
//            Player muscovite = new RandomTablutPlayer("RandomMuscovite");
            Player muscovite = new StudentPlayer();
            muscovite.setColor(TablutBoardState.MUSCOVITE);

            Player player = muscovite;
            while (!b.gameOver()) {
                Move m = player.chooseMove(b);
                b.processMove((TablutMove) m);
                player = (player == muscovite) ? swede : muscovite;
            }
            turnCounts.add(b.getTurnNumber());
            System.out.println(
                    "Game: " + i + " winner: " +
                            TablutMove.getPlayerName(b.getWinner()) + " in " + b.getTurnNumber() + " turns.");
            if (b.getWinner() == swede.getColor()) {
                swedeWins++;
            }
            if (b.getWinner() == muscovite.getColor()) {
                muscoviteWins++;
            }
            if (b.getTurnNumber() == Board.NOBODY) {
                numDraws++;
            }
        }
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(numGames + " games");
        System.out.println(swedeWins + " swede wins");
        System.out.println(muscoviteWins + " muscovite wins");
        System.out.println(numDraws + " draws");
        double swedeWinRate = (double) swedeWins / numGames * 100;
        double muscoviteWinRate = (double) muscoviteWins / numGames * 100;
        System.out.println("average turn number: " + StudentPlayer.calculateAverage(turnCounts));
        System.out.println("swede win rate: " + swedeWinRate + "%");
        System.out.println("muscovite win rate: " + muscoviteWinRate + "%");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

    }

    // for debug purposes
    private static double calculateAverage(List<Integer> marks) {
        Integer sum = 0;
        if (!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }
}