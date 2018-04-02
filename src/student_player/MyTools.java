package student_player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import boardgame.Move;
import boardgame.Player;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;

class MyTools {

    // todo: bonus for moving the a piece into a position where it can attach the king
    // maybe: act differently if king is in castle

    private boolean firstTurn;
    private final Player myPlayer;
    private final Coord CENTER;
    private final List<Coord> CORNERS;
    private final List<Coord> CENTER_NEIGHBOURS ;

    MyTools(Player myPlayer) {
//        this.player_id = myPlayer.getColor();
        this.myPlayer = myPlayer;
        firstTurn = true;
        CENTER = Coordinates.get(4, 4);
        CORNERS = Coordinates.getCorners();
        CENTER_NEIGHBOURS = Coordinates.getNeighbors(CENTER);
    }

    // evalutates a board from a muscovite player point of view
    private int muscoviteEvalBoard(TablutBoardState initialBoardState,
                                   TablutBoardState finalBoardState,
                                   TablutMove muscoviteMove) {

        int pieceValue = 100;
        int cornerCaptureBonus = 200;
        int centerCaptureBonus = 500;
        double vulnerablePiecePenalty = 0.6;
        int player_id = myPlayer.getColor();
        int opponent = 1 - player_id;
        int moveValue = 0;
        int numPlayerPieces = finalBoardState.getNumberPlayerPieces(player_id);
        int finalOpponentPieces = finalBoardState.getNumberPlayerPieces(opponent);
        int initialOpponentPieces = initialBoardState.getNumberPlayerPieces(opponent);
        Coord endCoord = muscoviteMove.getEndPosition();

        // increase value for each piece owned, decrease for each piece owned by opponent
        moveValue += numPlayerPieces * pieceValue;
        moveValue -= finalOpponentPieces * pieceValue;

        // if we didn't capture a piece then putting one of ours in peril is costly...
        if (finalOpponentPieces == initialOpponentPieces) {
            vulnerablePiecePenalty *= 2;
        }

        // check if opponent will capture the moved piece on their next turn
        if (isPieceVulnerable(finalBoardState, muscoviteMove)) {
            moveValue -= vulnerablePiecePenalty * pieceValue;
        }

        // give additional incentive for captures at vulnerable positions
        // encourages AI to be more aggressive
        for (Coord centerNeighbor : CENTER_NEIGHBOURS) {
            if (!initialBoardState.getKingPosition().equals(centerNeighbor)) {
                if (initialBoardState.isOpponentPieceAt(centerNeighbor)) {
                    try {
                        Coord sandwichCoord = Coordinates.getSandwichCoord(CENTER, centerNeighbor);
                        if (endCoord.equals(sandwichCoord)) {
                            moveValue += centerCaptureBonus;
                        }
                    } catch (Exception ignored) { }
                }
            }
        }
        for (Coord corner : CORNERS) {
            List<Coord> cornerNeighbours = Coordinates.getNeighbors(corner);
            for (Coord cornerNeighbour : cornerNeighbours) {
                if (initialBoardState.isOpponentPieceAt(cornerNeighbour)) {
                    try {
                        Coord sandwichCoord = Coordinates.getSandwichCoord(corner, cornerNeighbour);
                        if (endCoord.equals(sandwichCoord)) {

                            moveValue += cornerCaptureBonus;
                        }
                    } catch (Exception ignored) { }
                }
            }
        }
        return moveValue;
    }

    // evaluates a board from a swede player's point of view
    private int swedeEvalBoard(TablutBoardState initialBoardState,
                               TablutBoardState finalBoardState,
                               TablutMove swedeMove) {

        int pieceValue = 400;
        int kingDistanceValue = 20;
        int player_id = myPlayer.getColor();
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
        // if we're going for a capture, check to see if opponent will capture the piece we just moved
        if (finalOpponentPieces ==  initialOpponentPieces) {
            moveValue -= kingDistance * kingDistanceValue;
        } else if (isPieceVulnerable(finalBoardState, swedeMove)) {
            moveValue -=  0.8 * pieceValue;
        }

        return moveValue;
    }

    // generates a move for a muscovite player
    Move generateMuscoviteMove(TablutBoardState boardState) {

        int player_id = myPlayer.getColor();
        Map<TablutMove, Integer> moveValues = new HashMap<>();

        // bait greedy opponents
        if (firstTurn) {
            firstTurn = false;
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

    // generates a move for a swede player
    Move generateSwedeMove(TablutBoardState boardState) {

        int player_id = myPlayer.getColor();
        Map<TablutMove, Integer> moveValues = new HashMap<>();

        // bait greedy opponents
        if (firstTurn) {
            firstTurn = false;
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

            int moveValue = swedeEvalBoard(boardState, clonedBoardState, playerMove);
            moveValues.put(playerMove, moveValue);
        }

        Move myMove = getMaxMove(moveValues);
        return myMove == null ? boardState.getRandomMove() : myMove;
    }

    // returns true if the opponent is able to capture the piece on the following turn
    private boolean isPieceVulnerable(TablutBoardState boardState, TablutMove playerMove) {

        int player_id = myPlayer.getColor();
        Coord pieceCoord = playerMove.getEndPosition();
        List<Coord> neighbours = Coordinates.getNeighbors(pieceCoord);
        TablutBoardState.Piece playerPieceType;
        TablutBoardState.Piece opponentPieceType;

        if (player_id == 0) {
            playerPieceType = TablutBoardState.Piece.BLACK;
            opponentPieceType = TablutBoardState.Piece.WHITE;
        } else {
            playerPieceType = TablutBoardState.Piece.WHITE;
            opponentPieceType = TablutBoardState.Piece.BLACK;
        }

        // go through all neighbours of piece's coordinates
        for (Coord neighbour : neighbours) {
            if (boardState.isOpponentPieceAt(neighbour)) {
                Coord sandwichCoord = null;
                try {
                    sandwichCoord = Coordinates.getSandwichCoord(pieceCoord, neighbour);
                } catch (Exception ignored) { }
                if (sandwichCoord != null) {
                    // if sandwichCoord is on the same row
                    if (neighbour.x == pieceCoord.x) {
                        if (neighbour.y < pieceCoord.y) {
                            for (int j = sandwichCoord.y; j < 9; j++) {
                                TablutBoardState.Piece piece = boardState.getPieceAt(sandwichCoord.x, j);
                                if (piece == opponentPieceType) {
                                    return true;
                                } else if (piece == playerPieceType) {
                                    return false;
                                }
                            }
                        }
                        if (neighbour.y > pieceCoord.y) {
                            for (int j = sandwichCoord.y; j >= 0; j--) {
                                TablutBoardState.Piece piece = boardState.getPieceAt(sandwichCoord.x, j);
                                if (piece == opponentPieceType) {
                                    return true;
                                } else if (piece == playerPieceType) {
                                    return false;
                                }
                            }
                        }
                    // if sandwichCoord is on the same column
                    } else if (neighbour.y == pieceCoord.y) {
                        if (neighbour.x < pieceCoord.x) {
                            for (int i = sandwichCoord.y; i < 9; i++) {
                                TablutBoardState.Piece piece = boardState.getPieceAt(i, sandwichCoord.y);
                                if (piece == opponentPieceType) {
                                    return true;
                                } else if (piece == playerPieceType) {
                                    return false;
                                }
                            }
                        }
                        if (neighbour.x > pieceCoord.x) {
                            for (int i = sandwichCoord.y; i >= 0; i--) {
                                TablutBoardState.Piece piece = boardState.getPieceAt(i, sandwichCoord.y);
                                if (piece == opponentPieceType) {
                                    return true;
                                } else if (piece == playerPieceType) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // returns the move with the highest value
    // source : https://stackoverflow.com/a/5911199
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
