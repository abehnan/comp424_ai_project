package student_player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import javafx.scene.control.Tab;
import tablut.TablutBoardState;
import tablut.TablutMove;

class MyTools {

    // todo: add values for vulnerable positions. e.g. lose 1 point for every vulnerable piece
    // todo: bonus for moving the a piece into a position where it can attach the king
    // todo: make sure bonus for king distance gives value based on distance improvement on swede turn
    // todo: decrease value if we're sacrificing a piece
    // todo: discard move if we put our king in peril
    // todo: seperate king move nad non king move for swedes
    private final int player_id;
    private final Coord CENTER = Coordinates.get(4, 4);
    private final List<Coord> CORNERS = Coordinates.getCorners();
    private final List<Coord> CENTER_NEIGHBOURS = Coordinates.getNeighbors(CENTER);


    MyTools(int myPlayer) {
        this.player_id = myPlayer;
    }

    private int muscoviteEvalBoard(TablutBoardState initialBoardState,
                                   TablutBoardState finalBoardState,
                                   TablutMove muscoviteMove) {

        int PIECE_VALUE = 100;
        int CORNER_CAPTURE_BONUS = 200;
        int CENTER_CAPTURE_BONUS = 500;
        int opponent = 1 - player_id;
        int moveValue = 0;
        int numPlayerPieces = finalBoardState.getNumberPlayerPieces(player_id);
        int numOpponentPieces = finalBoardState.getNumberPlayerPieces(opponent);
        Coord endPosition = muscoviteMove.getEndPosition();

        // increase value for each piece owned, decrease for each piece owned by opponent
        moveValue += numPlayerPieces * PIECE_VALUE;
        moveValue -= numOpponentPieces * PIECE_VALUE;

        // give additional incentive for captures at vulnerable positions
        // encourages AI to be more aggressive
        for (Coord centerNeighbor : CENTER_NEIGHBOURS) {
            if (!initialBoardState.getKingPosition().equals(centerNeighbor)) {
                if (initialBoardState.isOpponentPieceAt(centerNeighbor)) {
                    try {
                        Coord sandwichCoord = Coordinates.getSandwichCoord(CENTER, centerNeighbor);
                        if (endPosition.equals(sandwichCoord)) {
                            moveValue += CENTER_CAPTURE_BONUS;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        for (Coord corner : CORNERS) {
            List<Coord> cornerNeighbours = Coordinates.getNeighbors(corner);
            for (Coord cornerNeighbour : cornerNeighbours) {
                if (initialBoardState.isOpponentPieceAt(cornerNeighbour)) {
                    try {
                        Coord sandwichCoord = Coordinates.getSandwichCoord(corner, cornerNeighbour);
                        if (endPosition.equals(sandwichCoord)) {

                            moveValue += CORNER_CAPTURE_BONUS;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return moveValue;
    }


    private int swedeEvalBoard(TablutBoardState boardState) {

        int pieceValue = 200;
        int kingDistanceValue = 5;
        int opponent = 1 - player_id;
        int moveValue = 0;
        int numPlayerPieces = boardState.getNumberPlayerPieces(player_id);
        int numOpponentPieces = boardState.getNumberPlayerPieces(opponent);

        // increase value for each piece owned, decrease for each piece owned by opponent
        moveValue += numPlayerPieces * pieceValue;
        moveValue -= numOpponentPieces * pieceValue;

        int kingSafetyDistance = Coordinates.distanceToClosestCorner(boardState.getKingPosition());

        moveValue -= kingSafetyDistance * kingDistanceValue;
        return moveValue;
    }

    Move generateMuscoviteMove(TablutBoardState boardState) {
        // bait greedy opponents
        if (boardState.getTurnNumber() == 0) {
            return new TablutMove(4, 1, 3, 1, player_id);
        }

        Map<TablutMove, Integer> moveValues = new HashMap<>();

        // go through player's legal moves
        for (TablutMove playerMove : boardState.getAllLegalMoves()) {
            int moveValue = 0;
            TablutBoardState clonedBoardState = (TablutBoardState) boardState.clone();
            clonedBoardState.processMove(playerMove);

            // check for win conditions
            if (clonedBoardState.gameOver()) {
                if (clonedBoardState.getWinner() == TablutBoardState.MUSCOVITE) {
                    moveValue = Integer.MAX_VALUE;
                } else if (clonedBoardState.getWinner() == TablutBoardState.SWEDE) {
                    moveValue = Integer.MIN_VALUE;
                }

                moveValues.put(playerMove, moveValue);
            } else {
                // go through opponent's moves on following turn
                for (TablutMove opponentMove : clonedBoardState.getAllLegalMoves()) {
                    TablutBoardState nextBoardState = (TablutBoardState) clonedBoardState.clone();
                    nextBoardState.processMove(opponentMove);

                    // check for opponent win condition
                    if (nextBoardState.gameOver()) {
                        if (nextBoardState.getWinner() == TablutBoardState.SWEDE) {
                            moveValue = Integer.MIN_VALUE + 1;
                            break;
                        }
                    }
                }
                if (moveValue == Integer.MIN_VALUE + 1) {
                    moveValues.put(playerMove, moveValue);
                } else {
                    moveValue = muscoviteEvalBoard(boardState, clonedBoardState, playerMove);
                    moveValues.put(playerMove, moveValue);
                }
            }
        }
        Move myMove = getMaxMove(moveValues);
        return myMove == null ? boardState.getRandomMove() : myMove;
    }

    Move generateSwedeMove(TablutBoardState boardState) {

        Map<TablutMove, Integer> moveValues = new HashMap<>();

        // go through player's legal moves
        for (TablutMove playerMove : boardState.getAllLegalMoves()) {
            int moveValue = 0;
            TablutBoardState clonedBoardState = (TablutBoardState) boardState.clone();
            clonedBoardState.processMove(playerMove);

            // check for win conditions
            if (clonedBoardState.gameOver()) {
                if (clonedBoardState.getWinner() == TablutBoardState.SWEDE) {
                    moveValue = Integer.MAX_VALUE;
                } else if (clonedBoardState.getWinner() == TablutBoardState.MUSCOVITE) {
                    moveValue = Integer.MIN_VALUE;
                }

                moveValues.put(playerMove, moveValue);
            } else {
                // go through opponent's moves on following turn
                for (TablutMove opponentMove : clonedBoardState.getAllLegalMoves()) {
                    TablutBoardState nextBoardState = (TablutBoardState) clonedBoardState.clone();
                    nextBoardState.processMove(opponentMove);

                    // check for opponent win condition
                    if (nextBoardState.gameOver()) {
                        if (nextBoardState.getWinner() == TablutBoardState.MUSCOVITE) {
                            moveValue = Integer.MIN_VALUE + 1;
                            break;
                        }
                    }
                }
                if (moveValue == Integer.MIN_VALUE + 1) {
                    moveValues.put(playerMove, moveValue);
                } else {
                    moveValue = swedeEvalBoard(clonedBoardState);
                    moveValues.put(playerMove, moveValue);
                }
            }
        }
        Move myMove = getMaxMove(moveValues);
        return myMove == null ? boardState.getRandomMove() : myMove;
    }

    private Move getMaxMove(Map<TablutMove, Integer> moveValues) {
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
}
