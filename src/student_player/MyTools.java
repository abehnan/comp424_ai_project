package student_player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;

class MyTools {

    // todo: add values for vulnerable positions. e.g. lose 1 point for every vulnerable piece
    // todo: bonus for moving the a piece into a position where it can attach the king
    // todo: decrease value if we're sacrificing a piece
    // todo: separate king move nad non king move for swedes
    // todo: act differently if king is in castle
    // todo: lose occassionalyl against greedy M


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

        int pieceValue = 100;
        int cornerCaptureBonus = 200;
        int centerCaptureBonus = 500;
        int opponent = 1 - player_id;
        int moveValue = 0;
        int numPlayerPieces = finalBoardState.getNumberPlayerPieces(player_id);
        int numOpponentPieces = finalBoardState.getNumberPlayerPieces(opponent);
        Coord endPosition = muscoviteMove.getEndPosition();

        // increase value for each piece owned, decrease for each piece owned by opponent
        moveValue += numPlayerPieces * pieceValue;
        moveValue -= numOpponentPieces * pieceValue;

        // give additional incentive for captures at vulnerable positions
        // encourages AI to be more aggressive
        for (Coord centerNeighbor : CENTER_NEIGHBOURS) {
            if (!initialBoardState.getKingPosition().equals(centerNeighbor)) {
                if (initialBoardState.isOpponentPieceAt(centerNeighbor)) {
                    try {
                        Coord sandwichCoord = Coordinates.getSandwichCoord(CENTER, centerNeighbor);
                        if (endPosition.equals(sandwichCoord)) {
                            moveValue += centerCaptureBonus;
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

                            moveValue += cornerCaptureBonus;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return moveValue;
    }


    private int swedeEvalBoard(TablutBoardState initialBoardState, TablutBoardState finalBoardState) {

        int pieceValue = 100;
        int kingDistanceValue = 10;
        int opponent = 1 - player_id;
        int moveValue = 0;
        int numPlayerPieces = finalBoardState.getNumberPlayerPieces(player_id);
        int initialOpponentPieces = initialBoardState.getNumberPlayerPieces(opponent);
        int finalOpponentPieces = finalBoardState.getNumberPlayerPieces(opponent);
        int kingDistance = Coordinates.distanceToClosestCorner(finalBoardState.getKingPosition());

        // increase value for each piece owned, decrease for each piece owned by opponent
        moveValue += numPlayerPieces * pieceValue;
        moveValue -= finalOpponentPieces * pieceValue;

        // give incentive for moving king if we didn't capture any opponent pieces
        if (finalOpponentPieces ==  initialOpponentPieces) {
            moveValue -= kingDistance * kingDistanceValue;
        }

        return moveValue;
    }

    Move generateMuscoviteMove(TablutBoardState boardState) {

        Map<TablutMove, Integer> moveValues = new HashMap<>();

        // bait greedy opponents
        if (boardState.getTurnNumber() == 1) {
            return new TablutMove(4, 1, 3, 1, player_id);
        }

        // go through player's legal moves
        outerLoop:
        for (TablutMove playerMove : boardState.getAllLegalMoves()) {
            TablutBoardState clonedBoardState = (TablutBoardState) boardState.clone();
            clonedBoardState.processMove(playerMove);

            // check for win conditions
            if (clonedBoardState.gameOver() && clonedBoardState.getWinner() == TablutBoardState.MUSCOVITE) {
                return playerMove;
            }

            // go through opponent's moves on following turn
            for (TablutMove opponentMove : clonedBoardState.getAllLegalMoves()) {
                TablutBoardState nextBoardState = (TablutBoardState) clonedBoardState.clone();
                nextBoardState.processMove(opponentMove);

                // check for opponent win condition
                if (nextBoardState.gameOver() && nextBoardState.getWinner() == TablutBoardState.SWEDE) {
                    continue outerLoop;
                }
            }

            int moveValue = muscoviteEvalBoard(boardState, clonedBoardState, playerMove);
            moveValues.put(playerMove, moveValue);
        }

        Move myMove = getMaxMove(moveValues);
        return myMove == null ? boardState.getRandomMove() : myMove;
    }

    Move generateSwedeMove(TablutBoardState boardState) {

        Map<TablutMove, Integer> moveValues = new HashMap<>();

        // bait greedy opponents
        if (boardState.getTurnNumber() == 1) {
            return new TablutMove(4, 5, 5, 5, player_id);
        }

        // go through player's legal moves
        outerLoop:
        for (TablutMove playerMove : boardState.getAllLegalMoves()) {
            TablutBoardState clonedBoardState = (TablutBoardState) boardState.clone();
            clonedBoardState.processMove(playerMove);

            // check for win conditions
            if (clonedBoardState.gameOver() && clonedBoardState.getWinner() == TablutBoardState.SWEDE) {
                return playerMove;
            }

            // go through opponent's moves on following turn
            for (TablutMove opponentMove : clonedBoardState.getAllLegalMoves()) {
                TablutBoardState nextBoardState = (TablutBoardState) clonedBoardState.clone();
                nextBoardState.processMove(opponentMove);

                // check for opponent win condition
                if (nextBoardState.gameOver() && nextBoardState.getWinner() == TablutBoardState.MUSCOVITE) {
                    continue outerLoop;
                }
            }

            int moveValue = swedeEvalBoard(boardState, clonedBoardState);
            moveValues.put(playerMove, moveValue);
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
