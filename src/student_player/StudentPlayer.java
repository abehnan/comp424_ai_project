package student_player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {

    // todo: add values for vulnerable positions. e.g. lose 1 point for every vulnerable piece
    // todo: bonus for moving the a piece into a position where it can attach the king
    // todo: make sure bonus for king distance gives value based on distance improvement on swede turn
    // todo: decrease value if we're sacrificing a piece
    // todo: discard move if we put our king in peril

    private final int CAPTURE_VALUE = 10;
    private final int PIECE_VALUE = 1;
    private final int KING_DISTANCE_VALUE = 1;
    private final int VULNERABLE_PIECE_PENALTY = 1;
    private final int VULNERABLE_KING_PENALTY = 150;
    private final int CENTER_CAPTURE_BONUS = 40;
    private final int CORNER_CAPTURE_BONUS = 20;
    private final Coord CENTER = Coordinates.get(4,4);
    private final List<Coord> CORNERS = Coordinates.getCorners();
    private final List<Coord> CENTER_NEIGHBOURS = Coordinates.getNeighbors(CENTER);

    public StudentPlayer() {
        super("260639146");
    }

    private Move generateMuscoviteMove(TablutBoardState boardState) {
        if (boardState.getTurnNumber() == 0) {
            return new TablutMove(4, 1, 3, 1, player_id);
        }

        int opponent = boardState.getOpponent();
        List<TablutMove> legalMoves = boardState.getAllLegalMoves();
        Map<TablutMove, Integer> moveValues = new HashMap<>();

        for (TablutMove legalMove : legalMoves) {
            int moveValue = 0;
            TablutBoardState clonedBoardState = (TablutBoardState)boardState.clone();
            clonedBoardState.processMove(legalMove);

            // check for win conditions
            // value remains 0 on draw
            if (clonedBoardState.gameOver()) {
                if (clonedBoardState.getWinner() == TablutBoardState.MUSCOVITE) {
                    moveValue = Integer.MAX_VALUE;
                }
                else if (clonedBoardState.getWinner() == TablutBoardState.SWEDE) {
                    moveValue = Integer.MIN_VALUE;
                }
            }
            else {
                // increase value for each piece owned, decrease for each piece owned by opponent
                int numPlayerPieces = clonedBoardState.getNumberPlayerPieces(player_id);
                int numOpponentPieces = clonedBoardState.getNumberPlayerPieces(opponent);
                moveValue += numPlayerPieces * PIECE_VALUE;
                moveValue -= numOpponentPieces * PIECE_VALUE;

                // increase value for capturing opponent pieces
                int numCaptures = numOpponentPieces - boardState.getNumberPlayerPieces(opponent);
                moveValue += numCaptures * CAPTURE_VALUE;

                // give additional incentive for captures at vulnerable positions
                // encourages AI to be more aggressive
                Coord endPosition = legalMove.getEndPosition();
                for (Coord centerNeighbor : CENTER_NEIGHBOURS) {
                    if (boardState.isOpponentPieceAt(centerNeighbor)) {
                        Coord sandwichCoord;
                        try {
                            sandwichCoord = Coordinates.getSandwichCoord(CENTER, centerNeighbor);
                            if (endPosition.equals(sandwichCoord)) {
                                moveValue += CENTER_CAPTURE_BONUS;
                            }
                        } catch (Exception ignored) {}
                    }
                }
                for (Coord corner : CORNERS) {
                    List<Coord> cornerNeighbours = Coordinates.getNeighbors(corner);
                    for (Coord cornerNeighbour : cornerNeighbours) {
                        if (boardState.isOpponentPieceAt(cornerNeighbour)) {
                            try {
                                Coord sandwichCoord = Coordinates.getSandwichCoord(corner, cornerNeighbour);
                                if (endPosition.equals(sandwichCoord)) {
                                    moveValue += CORNER_CAPTURE_BONUS;
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
            moveValues.put(legalMove, moveValue);
        }
        Move myMove = getBestMove(moveValues);
        return myMove == null ? boardState.getRandomMove() : myMove;
    }

    // TODO
    private Move generateSwedeMove(TablutBoardState boardState) {
        return boardState.getRandomMove();
    }

    // source: https://stackoverflow.com/a/5911199
    private Move getBestMove(Map<TablutMove, Integer> moveValues) {
        Map.Entry<TablutMove, Integer> maxEntry = null;
        for (Map.Entry<TablutMove, Integer> entry : moveValues.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        if (maxEntry != null) {
            List<TablutMove> bestMoves = new ArrayList<>();
            for (Map.Entry<TablutMove, Integer> entry : moveValues.entrySet()) {
                if (entry.getValue().compareTo(maxEntry.getValue()) == 0) {
                    bestMoves.add(entry.getKey());
                }
            }
            int random = ThreadLocalRandom.current().nextInt(bestMoves.size());
            return bestMoves.get(random);
        }
        return null;
    }

    public Move chooseMove(TablutBoardState boardState) {
        if (player_id == TablutBoardState.MUSCOVITE) {
            return generateMuscoviteMove(boardState);
        } else {
            return generateSwedeMove(boardState);
        }
    }
}