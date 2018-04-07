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

    // maybe: bonus for moving the a piece into a position where it can attach the king
    // maybe: act differently if king is in castle

    private boolean firstTurn;
    private final Player myPlayer;
    private final Coord CENTER;
    private final List<Coord> CORNERS;
    private final List<Coord> CENTER_NEIGHBOURS;

    // constructor
    MyTools(Player myPlayer) {
        this.myPlayer = myPlayer;
        firstTurn = true;
        CENTER = Coordinates.get(4, 4);
        CORNERS = Coordinates.getCorners();
        CENTER_NEIGHBOURS = Coordinates.getNeighbors(CENTER);
    }

    // generates a move
    Move getMove(TablutBoardState boardState) {

        Map<TablutMove, Double> moveValues1 = new HashMap<>();
        int opponentColor = 1 - myPlayer.getColor();

        // bait greedy opponents
        if (myPlayer.getColor() == TablutBoardState.MUSCOVITE && firstTurn) {
            firstTurn = false;
            return new TablutMove(4, 1, 3, 1, myPlayer.getColor());
        }

        // go through all player's moves
        outerLoop:
        for (TablutMove playerMove1 : boardState.getAllLegalMoves()) {
            TablutBoardState boardState1 = (TablutBoardState) boardState.clone();
            boardState1.processMove(playerMove1);

            // check for win conditions
            if (boardState1.gameOver() && boardState1.getWinner() == myPlayer.getColor()) {
                return playerMove1;
            }

            if (myPlayer.getColor() == TablutBoardState.MUSCOVITE) {
                // see if we can trap opponent king
                if (boardState.getLegalMovesForPosition(boardState.getKingPosition()).size() > 0) {
                    if (boardState1.getLegalMovesForPosition(boardState1.getKingPosition()).size() == 0) {
                        return playerMove1;
                    }
                }

                // check that we can have a piece on all edges
                if (boardState1.getNumberPlayerPieces(myPlayer.getColor()) > 5) {
                    if (!doesPlayerHavePieceOnAllEdges(boardState1)) {
                        continue;
                    }
                }
            }

            // go through all opponents moves
            for (TablutMove opponentMove : boardState1.getAllLegalMoves()) {
                TablutBoardState boardState2 = (TablutBoardState) boardState1.clone();
                boardState2.processMove(opponentMove);

                // check for opponent win condition
                if (boardState2.gameOver() && boardState2.getWinner() == opponentColor) {
                    continue outerLoop;
                }

                // check if opponent can trap the king
                if (myPlayer.getColor() == TablutBoardState.SWEDE) {
                    if (boardState2.getLegalMovesForPosition(boardState2.getKingPosition()).size() == 0) {
                        continue outerLoop;
                    }
                }

            }
            moveValues1.put(playerMove1, evalMove(boardState, boardState1, playerMove1));
        }

        Move myMove = getMaxMove(moveValues1);
        return myMove == null ? boardState.getRandomMove() : myMove;
    }

    // evaluates the value of a move using the appropriate heuristic
    private Double evalMove(TablutBoardState initialBoardState,
                            TablutBoardState finalBoardState,
                            TablutMove playerMove) {
        if (myPlayer.getColor() == TablutBoardState.MUSCOVITE) {
            return muscEvalMove(initialBoardState, finalBoardState, playerMove);
        } else if (myPlayer.getColor() == TablutBoardState.SWEDE) {
            return swedeEvalMove(initialBoardState, finalBoardState, playerMove);
        }
        return null;
    }

    // evaluates a muscovites player's move
    private Double muscEvalMove(TablutBoardState initialBoardState,
                                TablutBoardState finalBoardState,
                                TablutMove muscoviteMove) {

        int pieceValue = 4;
        double cornerCaptureBonus = 4;
        double centerCaptureBonus = 10;
        double kingDistanceValue = 1;
        double vulnerablePiecePenalty = 1;
        int player_id = myPlayer.getColor();
        int opponent = 1 - player_id;
        double moveValue = 0;
        int numPlayerPieces = finalBoardState.getNumberPlayerPieces(player_id);
        int finalOpponentPieces = finalBoardState.getNumberPlayerPieces(opponent);
        int initialOpponentPieces = initialBoardState.getNumberPlayerPieces(opponent);
        Coord endCoord = muscoviteMove.getEndPosition();

        // increase value for each piece owned, decrease for each piece owned by opponent
        moveValue += numPlayerPieces * pieceValue;
        moveValue -= 2 * (finalOpponentPieces - 1) * pieceValue;

        // extra steps if we didn't capture a piece
        if (finalOpponentPieces == initialOpponentPieces) {
            // if we didn't capture a piece then putting one of ours in peril is costly...
            vulnerablePiecePenalty *= 5;

            // increase value if we moved our piece towards the king
            moveValue -= endCoord.distance(finalBoardState.getKingPosition()) * kingDistanceValue;
        }

        // check if opponent will capture the moved piece on their next turn
        if (isPieceVulnerable(finalBoardState, muscoviteMove.getEndPosition())) {
            moveValue -= vulnerablePiecePenalty * pieceValue;
        }

        // check if opponent will capture neighbours of the moved piece
        for (Coord coord : Coordinates.getNeighbors(muscoviteMove.getStartPosition())) {
            if (finalBoardState.getPieceAt(coord) == TablutBoardState.Piece.BLACK) {
                if (isPieceVulnerable(finalBoardState, coord)) {
                    moveValue -= vulnerablePiecePenalty * pieceValue;
                }
            }
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
                        if (endCoord.equals(sandwichCoord)) {

                            moveValue += cornerCaptureBonus;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return moveValue;
    }

    // evaluates a swede player's move
    private Double swedeEvalMove(TablutBoardState initialBoardState,
                                 TablutBoardState finalBoardState,
                                 TablutMove swedeMove) {

        double pieceValue = 6;
        double kingDistanceValue = 4;
        int player_id = myPlayer.getColor();
        int opponent = 1 - player_id;
        double moveValue = 0;
        double vulnerablePiecePenalty = 1;
        int numPlayerPieces = finalBoardState.getNumberPlayerPieces(player_id);
        int initialOpponentPieces = initialBoardState.getNumberPlayerPieces(opponent);
        int finalOpponentPieces = finalBoardState.getNumberPlayerPieces(opponent);
        int kingDistance = Coordinates.distanceToClosestCorner(finalBoardState.getKingPosition());

        // increase value for each piece owned, decrease for each piece owned by opponent
        moveValue += 2 * (numPlayerPieces - 1) * pieceValue;
        moveValue -= finalOpponentPieces * pieceValue;

        // give incentive for moving king if we didn't capture any opponent pieces
        // if we're going for a capture, check to see if opponent will capture the piece we just moved
        if (finalOpponentPieces == initialOpponentPieces) {
            vulnerablePiecePenalty *= 3;
            moveValue -= kingDistance * kingDistanceValue;
        }

        // check to see if opponent will capture the piece we just moved
        if (isPieceVulnerable(finalBoardState, swedeMove.getEndPosition())) {
            moveValue -= vulnerablePiecePenalty * pieceValue;
        }

        // check if opponent will capture neighbours of the moved piece
        for (Coord coord : Coordinates.getNeighbors(swedeMove.getStartPosition())) {
            if (finalBoardState.getPieceAt(coord) == TablutBoardState.Piece.WHITE) {
                if (isPieceVulnerable(finalBoardState, coord)) {
                    moveValue -= vulnerablePiecePenalty * pieceValue;
                }
            }
        }

        return moveValue;
    }

    // returns true if the opponent is able to capture the piece on the following turn
    private boolean isPieceVulnerable(TablutBoardState boardState, Coord pieceCoord) {

        List<Coord> neighbours = Coordinates.getNeighbors(pieceCoord);

        // go through all neighbours of piece's coordinates
        for (Coord neighbour : neighbours) {
            TablutBoardState.Piece neighbourPiece = boardState.getPieceAt(neighbour);

            if (doesPlayerOwnPiece(neighbourPiece, myPlayer.getColor())) {
                continue;
            }

            Coord sandwichCoord = null;
            try {
                sandwichCoord = Coordinates.getSandwichCoord(pieceCoord, neighbour);
            } catch (Exception ignored) {
            }
            if (sandwichCoord == null) {
                continue;
            }
            if (!boardState.coordIsEmpty(sandwichCoord)) {
                continue;
            }

            // if sandwichCoord is on the same row
            if (sandwichCoord.x == pieceCoord.x) {
                // check lesser side
                for (int j = sandwichCoord.y; j >= 0; j--) {
                    TablutBoardState.Piece piece = boardState.getPieceAt(sandwichCoord.x, j);
                    if (piece == TablutBoardState.Piece.EMPTY) {
                        continue;
                    }
                    if (doesPlayerOwnPiece(piece, myPlayer.getColor())) {
                        break;
                    }
                    if (doesPlayerOwnPiece(piece, 1 - myPlayer.getColor())) {
                        return true;
                    }
                }
                for (int j = sandwichCoord.y; j < 9; j++) {
                    TablutBoardState.Piece piece = boardState.getPieceAt(sandwichCoord.x, j);
                    if (piece == TablutBoardState.Piece.EMPTY) {
                        continue;
                    }
                    if (doesPlayerOwnPiece(piece, myPlayer.getColor())) {
                        break;
                    }
                    if (doesPlayerOwnPiece(piece, 1 - myPlayer.getColor())) {
                        return true;
                    }
                }
                // if sandwichCoord is on the same column
            } else if (sandwichCoord.y == pieceCoord.y) {
                for (int i = sandwichCoord.x; i >= 0; i--) {
                    TablutBoardState.Piece piece = boardState.getPieceAt(i, sandwichCoord.y);
                    if (piece == TablutBoardState.Piece.EMPTY) {
                        continue;
                    }
                    if (doesPlayerOwnPiece(piece, myPlayer.getColor())) {
                        break;
                    }
                    if (doesPlayerOwnPiece(piece, 1 - myPlayer.getColor())) {
                        return true;
                    }
                }
                for (int i = sandwichCoord.x; i < 9; i++) {
                    TablutBoardState.Piece piece = boardState.getPieceAt(i, sandwichCoord.y);
                    if (piece == TablutBoardState.Piece.EMPTY) {
                        continue;
                    }
                    if (doesPlayerOwnPiece(piece, myPlayer.getColor())) {
                        break;
                    }
                    if (doesPlayerOwnPiece(piece, 1 - myPlayer.getColor())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean doesPlayerHavePieceOnAllEdges(TablutBoardState boardState) {
        boolean pieceFound = false;

        for (int i = 0; i < 9; i++) {
            if (doesPlayerOwnPiece(boardState.getPieceAt(i, 0), myPlayer.getColor())) {
                pieceFound = true;
            }
        }
        if (pieceFound) {
            pieceFound = false;
        } else {
            return false;
        }

        for (int i = 0; i < 9; i++) {
            if (doesPlayerOwnPiece(boardState.getPieceAt(i, 8), myPlayer.getColor())) {
                pieceFound = true;
            }
        }
        if (pieceFound) {
            pieceFound = false;
        } else {
            return false;
        }

        for (int j = 0; j < 9; j++) {
            if (doesPlayerOwnPiece(boardState.getPieceAt(0, j), myPlayer.getColor())) {
                pieceFound = true;
            }
        }
        if (pieceFound) {
            pieceFound = false;
        } else {
            return false;
        }

        for (int j = 0; j < 9; j++) {
            if (doesPlayerOwnPiece(boardState.getPieceAt(8, j), myPlayer.getColor())) {
                pieceFound = true;
            }
        }
        return pieceFound;

    }


    // returns true if the player with the corresponding color owns a piece
    private boolean doesPlayerOwnPiece(TablutBoardState.Piece piece, int color) {
        if (color == TablutBoardState.MUSCOVITE) {
            return (piece == TablutBoardState.Piece.BLACK);
        } else if (color == TablutBoardState.SWEDE) {
            return piece == TablutBoardState.Piece.WHITE || piece == TablutBoardState.Piece.KING;
        }
        return false;
    }

    // returns the move with the highest value
    // source : https://stackoverflow.com/a/5911199
    private Move getMaxMove(Map<TablutMove, Double> moveValues) {
        Map.Entry<TablutMove, Double> maxEntry = null;
        for (Map.Entry<TablutMove, Double> entry : moveValues.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        if (maxEntry != null) {
            List<TablutMove> bestMoves = new ArrayList<>();
            for (Map.Entry<TablutMove, Double> entry : moveValues.entrySet()) {
                if (entry.getValue().compareTo(maxEntry.getValue()) == 0) {
                    bestMoves.add(entry.getKey());
                }
            }
            int random = ThreadLocalRandom.current().nextInt(bestMoves.size());
            return bestMoves.get(random);
        }
        return null;
    }

    // for debug purposes
    // source : https://stackoverflow.com/a/10791597
    static double calculateAverageInteger(List<Integer> marks) {
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
