package student_player;

import boardgame.Move;
import boardgame.Player;
import tablut.*;

import java.util.ArrayList;
import java.util.List;

public class MyTestSuite {
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("No argument specified. Requires 's' or 'm' as a command-line argument.");
        } else if (args[0].equals("m")) {
            muscoviteTest();
        } else if (args[0].equals("s")) {
            swedeTest();
        } else {
            throw new IllegalArgumentException("Illegal argument specified. Requires 's' or 'm' as a command-line argument.");
        }
    }

    private static void muscoviteTest() {
        int numGames = 150;
        int randomWins = 0;
        int greedyWins = 0;
        int studentWins = 0;
        int totalWins = 0;
        int randomDraws = 0;
        int greedyDraws = 0;
        int studentDraws = 0;
        int totalDraws = 0;
        int randomLosses = 0;
        int greedyLosses = 0;
        int studentLosses = 0;
        int totalLosses = 0;
        List<Integer> turnCounts = new ArrayList<>();

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("            STARTING MUSCOVITE TEST");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numGames; i++) {

            TablutBoardState b = new TablutBoardState();

            Player muscovite = new StudentPlayer();
            muscovite.setColor(TablutBoardState.MUSCOVITE);

            Player swede;
            if (i < numGames / 3) {
                swede = new RandomTablutPlayer("RandomSwede");
//                continue;
            } else if (i < 2 * numGames / 3) {
                swede = new GreedyTablutPlayer("GreedySwede");
//                continue;
            } else {
                swede = new StudentPlayer();
//                continue;
            }
            swede.setColor(TablutBoardState.SWEDE);
            Player player = muscovite;

            System.out.print("Game: " + i + "...");
            while (!b.gameOver()) {
                Move m = player.chooseMove(b);
                b.processMove((TablutMove) m);
                player = (player == muscovite) ? swede : muscovite;
            }

            turnCounts.add(b.getTurnNumber());
            if (b.getWinner() == TablutBoardState.SWEDE) {
                if (i < numGames / 3) {
                    randomLosses++;
                } else if (i < 2 * numGames / 3) {
                    greedyLosses++;
                } else {
                    studentLosses++;
                }
                totalLosses++;
                System.out.println("LOSS in " + b.getTurnNumber() + " turns.");
            } else if (b.getWinner() == TablutBoardState.MUSCOVITE) {
                if (i < numGames / 3) {
                    randomWins++;
                } else if (i < 2 * numGames / 3) {
                    greedyWins++;
                } else {
                    studentWins++;
                }
                totalWins++;
                System.out.println("WIN in " + b.getTurnNumber() + " turns.");
            } else {
                if (i < numGames / 3) {
                    randomDraws++;
                } else if (i < 2 * numGames / 3) {
                    greedyDraws++;
                } else {
                    studentDraws++;
                }
                totalDraws++;
                System.out.println("DRAW in " + b.getTurnNumber() + " turns.");
            }
        }
        long totalTime = System.currentTimeMillis() - startTime;

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("            MUSCOVITE TEST COMPLETE");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("average number of turns: " + MyTools.calculateAverageInteger(turnCounts));
        System.out.println(numGames + " games");
        System.out.println("format: WINS/DRAWS/LOSSES");
        System.out.println("total score: " + totalWins + "/" + totalDraws + "/" + totalLosses);
        System.out.println(randomWins + "/" + randomDraws + "/" + randomLosses +  " vs. random player (" +
                (double)randomWins/(numGames/3)*100 + "% win rate)");
        System.out.println(greedyWins + "/" + greedyDraws + "/" + greedyLosses + " vs. greedy player (" +
                (double)greedyWins/(numGames/3)*100 + "% win rate)");
        System.out.println(studentWins + "/" + studentDraws + "/" + studentLosses + " vs. student player (" +
                (double)studentWins/(numGames/3)*100 + "% win rate)");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("\n\n time taken: " + totalTime);
    }


    private static void swedeTest() {
        int numGames = 600;
        int randomWins = 0;
        int greedyWins = 0;
        int studentWins = 0;
        int totalWins = 0;
        int randomDraws = 0;
        int greedyDraws = 0;
        int studentDraws = 0;
        int totalDraws = 0;
        int randomLosses = 0;
        int greedyLosses = 0;
        int studentLosses = 0;
        int totalLosses = 0;
        List<Integer> turnCounts = new ArrayList<>();

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("                    STARTING SWEDE TEST");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numGames; i++) {

            TablutBoardState b = new TablutBoardState();

            Player swede = new StudentPlayer();
            swede.setColor(TablutBoardState.SWEDE);

            Player muscovite;
            if (i < numGames / 3) {
                muscovite = new RandomTablutPlayer("RandomMuscovite");
//                continue;
            } else if (i < 2 * numGames / 3) {
                muscovite = new GreedyTablutPlayer("GreedyMuscovite");
//                continue;
            } else {
                muscovite = new StudentPlayer();
//                continue;
            }
            muscovite.setColor(TablutBoardState.MUSCOVITE);
            Player player = muscovite;

            System.out.print("Game: " + i + "...");
            while (!b.gameOver()) {
                Move m = player.chooseMove(b);
                b.processMove((TablutMove) m);
                player = (player == muscovite) ? swede : muscovite;
            }

            turnCounts.add(b.getTurnNumber());
            if (b.getWinner() == TablutBoardState.MUSCOVITE) {
                if (i < numGames / 3) {
                    randomLosses++;
                } else if (i < 2 * numGames / 3) {
                    greedyLosses++;
                } else {
                    studentLosses++;
                }
                totalLosses++;
                System.out.println("LOSS in " + b.getTurnNumber() + " turns.");
            } else if (b.getWinner() == TablutBoardState.SWEDE) {
                if (i < numGames / 3) {
                    randomWins++;
                } else if (i < 2 * numGames / 3) {
                    greedyWins++;
                } else {
                    studentWins++;
                }
                totalWins++;
                System.out.println("WIN in " + b.getTurnNumber() + " turns.");
            } else {
                if (i < numGames / 3) {
                    randomDraws++;
                } else if (i < 2 * numGames / 3) {
                    greedyDraws++;
                } else {
                    studentDraws++;
                }
                totalDraws++;
                System.out.println("DRAW in " + b.getTurnNumber() + " turns.");
            }
        }
        long totalTime = System.currentTimeMillis() - startTime;


        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("                    SWEDE TEST COMPLETE");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("average number of turns: " + MyTools.calculateAverageInteger(turnCounts));
        System.out.println(numGames + " games");
        System.out.println("format: WINS/DRAWS/LOSSES");
        System.out.println("total score: " + totalWins + "/" + totalDraws + "/" + totalLosses);
        System.out.println(randomWins + "/" + randomDraws + "/" + randomLosses +  " vs. random player (" +
                (double)randomWins/(numGames/3)*100 + "% win rate)");
        System.out.println(greedyWins + "/" + greedyDraws + "/" + greedyLosses + " vs. greedy player (" +
                (double)greedyWins/(numGames/3)*100 + "% win rate)");
        System.out.println(studentWins + "/" + studentDraws + "/" + studentLosses + " vs. student player (" +
                (double)studentWins/(numGames/3)*100 + "% win rate)");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("\n\n time taken: " + totalTime);
    }



}
