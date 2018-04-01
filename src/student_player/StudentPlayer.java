package student_player;

import java.util.*;

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
    private final int CAPTURE_VALUE = 15;
    private final int PIECE_VALUE = 3;
    private final int KING_DISTANCE_VALUE = 3;
    private final int VULNERABLE_PIECE_PENALTY = 1;
    private final int VULNERABLE_KING_PENALTY = 150;
    private final int VULNERABLE_CAPTURE_BONUS = 75;
    private final Coord CENTER = Coordinates.get(4,4);
    private final List<Coord> CORNERS = Coordinates.getCorners();
    private final List<Coord> CENTER_NEIGHBOURS = Coordinates.getNeighbors(CENTER);
//    private final List<Coord> CORNER_NEIGHBOURS = Arrays.asList(
//            Coordinates.get(0,1),
//            Coordinates.get(1,0),
//            Coordinates.get(8,7),
//            Coordinates.get(7,8),
//            Coordinates.get(0,7),
//            Coordinates.get(7,0),
//            Coordinates.get(1,8),
//            Coordinates.get(8,1)
//    );

    public StudentPlayer() {
        super("260639146");
    }

    // TODO
    private Move generateMuscoviteMove(TablutBoardState boardState) {
        if (boardState.getTurnNumber() == 0) {
            return new TablutMove(4, 1, 3, 1, player_id);
        }

        int opponent = boardState.getOpponent();
        List<TablutMove> legalMoves = boardState.getAllLegalMoves();
        Map<TablutMove, Integer> moveValues = new HashMap<>();

        for (TablutMove legalMove : legalMoves) {
            int totalValue = 0, numCaptures;
            TablutBoardState clonedBoardState = (TablutBoardState)boardState.clone();
            clonedBoardState.processMove(legalMove);

            // check for win condition
            if (clonedBoardState.getWinner() == TablutBoardState.MUSCOVITE) {
                return legalMove;
            }

            // increase value for each piece owned, decrease for each piece owned by opponent
            totalValue += clonedBoardState.getNumberPlayerPieces(player_id) * PIECE_VALUE;
            totalValue -= clonedBoardState.getNumberPlayerPieces(opponent) * PIECE_VALUE;

            // increase value for capturing opponent pieces
            numCaptures = clonedBoardState.getNumberPlayerPieces(opponent) - boardState.getNumberPlayerPieces(opponent);
            totalValue += numCaptures * CAPTURE_VALUE;

            // give additional incentive for captures at vulnerable positions
            Coord endPosition = legalMove.getEndPosition();
            for (Coord centerNeighbor : CENTER_NEIGHBOURS) {
                if (boardState.isOpponentPieceAt(centerNeighbor)) {
                    Coord sandwichCoord;
                    try {
                        sandwichCoord = Coordinates.getSandwichCoord(CENTER, centerNeighbor);
                        if (endPosition.equals(sandwichCoord)) {
                            totalValue += VULNERABLE_CAPTURE_BONUS;
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
                                totalValue += VULNERABLE_CAPTURE_BONUS;
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

            moveValues.put(legalMove, totalValue);
        }
        return getBestMove(moveValues);
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
        return maxEntry != null ? maxEntry.getKey() : null;
    }

    public Move chooseMove(TablutBoardState boardState) {
        if (player_id == TablutBoardState.MUSCOVITE) {
            return generateMuscoviteMove(boardState);
        } else {
            return generateSwedeMove(boardState);
        }

    }
}