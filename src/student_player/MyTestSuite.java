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
        int numGames = 300;
        int randomWins = 0;
        int greedyWins = 0;
        int studentWins = 0;
        int totalWins = 0;
        List<Integer> turnCounts = new ArrayList<>();

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("\t\t\t\tSTARTING MUSCOVITE TEST");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        for (int i = 0; i < numGames; i++) {
            TablutBoardState b = new TablutBoardState();

            Player muscovite = new StudentPlayer();
            muscovite.setColor(TablutBoardState.MUSCOVITE);

            Player swede;
            if (i < numGames / 3) {
                swede = new RandomTablutPlayer("RandomSwede");
            } else if (i < 2 * numGames / 3) {
                swede = new GreedyTablutPlayer("GreedySwede");
            } else {
                swede = new StudentPlayer();
            }
            swede.setColor(TablutBoardState.SWEDE);
            Player player = muscovite;

            while (!b.gameOver()) {
                Move m = player.chooseMove(b);
                b.processMove((TablutMove) m);
                player = (player == muscovite) ? swede : muscovite;
            }

            turnCounts.add(b.getTurnNumber());
            if (b.getWinner() == TablutBoardState.MUSCOVITE) {
                if (i < numGames / 3) {
                    randomWins++;
                } else if (i < 2 * numGames / 3) {
                    greedyWins++;
                } else {
                    studentWins++;
                }
                totalWins++;
                System.out.println("Game: " + i + " WIN in " + b.getTurnNumber() + " turns.");
            } else if (b.getWinner() == TablutBoardState.SWEDE) {
                System.out.println("Game: " + i + " LOSS in " + b.getTurnNumber() + " turns.");
            } else {
                System.out.println("Game: " + i + " DRAW in " + b.getTurnNumber() + " turns.");
            }
        }

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(numGames + " games");
        System.out.println(totalWins + " total muscovite wins");
        System.out.println(randomWins + " wins vs. random player");
        System.out.println(greedyWins + " wins vs. greedy player");
        System.out.println(studentWins + " wins vs student player");
        double swedeWinRate = (double) totalWins / numGames * 100;
        double randomWinRate = (double) randomWins / (numGames / 3) * 100;
        double greedyWinRate = (double) greedyWins / (numGames / 3) * 100;
        double studentWinRate = (double) studentWins / (numGames / 3) * 100;
        System.out.println("average number of turns: " + calculateAverage(turnCounts));
        System.out.println("total win rate: " + swedeWinRate + "%");
        System.out.println("win rate vs. random player: " + randomWinRate + "%");
        System.out.println("win rate vs. greedy player: " + greedyWinRate + "%");
        System.out.println("win rate vs. student player: " + studentWinRate + "%");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }


    private static void swedeTest() {
        int numGames = 300;
        int randomWins = 0;
        int greedyWins = 0;
        int studentWins = 0;
        int totalWins = 0;
        List<Integer> turnCounts = new ArrayList<>();

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("\t\t\t\t\tSTARTING SWEDE TEST");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        for (int i = 0; i < numGames; i++) {
            TablutBoardState b = new TablutBoardState();

            Player swede = new StudentPlayer();
            swede.setColor(TablutBoardState.SWEDE);

            Player muscovite;
            if (i < numGames / 3) {
                muscovite = new RandomTablutPlayer("RandomMuscovite");
            } else if (i < 2 * numGames / 3) {
                muscovite = new GreedyTablutPlayer("GreedyMuscovite");
            } else {
                muscovite = new StudentPlayer();
            }
            muscovite.setColor(TablutBoardState.MUSCOVITE);
            Player player = muscovite;

            while (!b.gameOver()) {
                Move m = player.chooseMove(b);
                b.processMove((TablutMove) m);
                player = (player == muscovite) ? swede : muscovite;
            }

            turnCounts.add(b.getTurnNumber());
            if (b.getWinner() == TablutBoardState.SWEDE) {
                if (i < numGames / 3) {
                    randomWins++;
                } else if (i < 2 * numGames / 3) {
                    greedyWins++;
                } else {
                    studentWins++;
                }
                totalWins++;
                System.out.println("Game: " + i + " WIN in " + b.getTurnNumber() + " turns.");
            } else if (b.getWinner() == TablutBoardState.MUSCOVITE) {
                System.out.println("Game: " + i + " LOSS in " + b.getTurnNumber() + " turns.");
            } else {
                System.out.println("Game: " + i + " DRAW in " + b.getTurnNumber() + " turns.");
            }
        }

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(numGames + " games");
        System.out.println(totalWins + " total swede wins");
        System.out.println(randomWins + " wins vs. random player");
        System.out.println(greedyWins + " wins vs. greedy player");
        System.out.println(studentWins + " wins vs student player");
        double swedeWinRate = (double) totalWins / numGames * 100;
        double randomWinRate = (double) randomWins / (numGames / 3) * 100;
        double greedyWinRate = (double) greedyWins / (numGames / 3) * 100;
        double studentWinRate = (double) studentWins / (numGames / 3) * 100;
        System.out.println("average number of turns: " + calculateAverage(turnCounts));
        System.out.println("total win rate: " + swedeWinRate + "%");
        System.out.println("win rate vs. random player: " + randomWinRate + "%");
        System.out.println("win rate vs. greedy player: " + greedyWinRate + "%");
        System.out.println("win rate vs. student player: " + studentWinRate + "%");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

    // for debug purposes
    // source : https://stackoverflow.com/a/10791597
    public static double calculateAverage(List<Integer> marks) {
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
