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

    MyTools(Player myPlayer) {
        this.myPlayer = myPlayer;
        firstTurn = true;
        CENTER = Coordinates.get(4, 4);
        CORNERS = Coordinates.getCorners();
        CENTER_NEIGHBOURS = Coordinates.getNeighbors(CENTER);
    }

    // generates a move for a muscovite player
    Move getMove(TablutBoardState boardState, int playerColor) {

        Map<TablutMove, Double> moveValues1 = new HashMap<>();
        int opponentColor = 1 - playerColor;

        // bait greedy opponents
        if (playerColor == TablutBoardState.MUSCOVITE && firstTurn) {
            firstTurn = false;
            return new TablutMove(4, 1, 3, 1, myPlayer.getColor());
        }

        // go through all player's moves
        outerLoop:
        for (TablutMove playerMove1 : boardState.getAllLegalMoves()) {
            TablutBoardState boardState1 = (TablutBoardState) boardState.clone();
            boardState1.processMove(playerMove1);

            // check for win conditions
            if (boardState1.gameOver() && boardState1.getWinner() == playerColor) {
                return playerMove1;
            }

            // go through all opponents moves
            List<Double> moveValues2 = new ArrayList<>();
            for (TablutMove opponentMove : boardState1.getAllLegalMoves()) {
                TablutBoardState boardState2 = (TablutBoardState) boardState1.clone();
                boardState2.processMove(opponentMove);

                // check for opponent win condition
                if (boardState2.gameOver() && boardState2.getWinner() == opponentColor) {
                    continue outerLoop;
                }

                // go through all of player's move following opponent's move
                for (TablutMove playerMove2 : boardState2.getAllLegalMoves()) {
                    TablutBoardState boardState3 = (TablutBoardState) boardState2.clone();
                    boardState3.processMove(playerMove2);
                    moveValues2.add(evalBoard(boardState2, boardState3, playerMove2, playerColor));
                }
            }
            moveValues1.put(playerMove1, calculateAverageDouble(moveValues2));
        }

        Move myMove = getMaxMove(moveValues1);
        return myMove == null ? boardState.getRandomMove() : myMove;
    }

    private Double evalBoard(TablutBoardState initialBoardState,
                             TablutBoardState finalBoardState,
                             TablutMove playerMove,
                             int playerColor) {
        if (playerColor == TablutBoardState.MUSCOVITE) {
            return muscEvalBoard(initialBoardState, finalBoardState, playerMove);
        } else if (playerColor == TablutBoardState.SWEDE) {
            return swedeEvalBoard(initialBoardState, finalBoardState, playerMove);
        }
        return null;
    }

    // evaluates a board from a muscovite player point of view
    private Double muscEvalBoard(TablutBoardState initialBoardState,
                                 TablutBoardState finalBoardState,
                                 TablutMove muscoviteMove) {

        int pieceValue = 100;
        double cornerCaptureBonus = 200;
        double centerCaptureBonus = 500;
        double kingDistanceValue = 50;
        double vulnerablePiecePenalty = 0.9;
        int player_id = myPlayer.getColor();
        int opponent = 1 - player_id;
        double moveValue = 0;
        int numPlayerPieces = finalBoardState.getNumberPlayerPieces(player_id);
        int finalOpponentPieces = finalBoardState.getNumberPlayerPieces(opponent);
        int initialOpponentPieces = initialBoardState.getNumberPlayerPieces(opponent);
        Coord endCoord = muscoviteMove.getEndPosition();

        // check for win conditions
        if (finalBoardState.gameOver() && finalBoardState.getWinner() == TablutBoardState.MUSCOVITE) {
            moveValue += 5000;
        }

        // increase value for each piece owned, decrease for each piece owned by opponent
        moveValue += numPlayerPieces * pieceValue;
        moveValue -= finalOpponentPieces * pieceValue;

        // extra steps if we didn't capture a piece
        if (finalOpponentPieces == initialOpponentPieces) {
            // if we didn't capture a piece then putting one of ours in peril is costly...
            vulnerablePiecePenalty *= 5;

            // increase value if we moved our piece towards the king
            moveValue -= endCoord.distance(finalBoardState.getKingPosition()) * kingDistanceValue;
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

    // evaluates a board from a swede player's point of view
    private Double swedeEvalBoard(TablutBoardState initialBoardState,
                                  TablutBoardState finalBoardState,
                                  TablutMove swedeMove) {

        double pieceValue = 400;
        double kingDistanceValue = 5;
        int player_id = myPlayer.getColor();
        int opponent = 1 - player_id;
        double moveValue = 0;
        int numPlayerPieces = finalBoardState.getNumberPlayerPieces(player_id);
        int initialOpponentPieces = initialBoardState.getNumberPlayerPieces(opponent);
        int finalOpponentPieces = finalBoardState.getNumberPlayerPieces(opponent);
        int kingDistance = Coordinates.distanceToClosestCorner(finalBoardState.getKingPosition());

        // check for win conditions
        if (finalBoardState.gameOver() && finalBoardState.getWinner() == TablutBoardState.SWEDE) {
            moveValue += 5000;
        } else if (!initialBoardState.getKingPosition().equals(finalBoardState.getKingPosition())) {
            // check if we put our king in peril
            if (isPieceVulnerable(finalBoardState, swedeMove)) {
                moveValue -= 5000;
            }
        }


        // increase value for each piece owned, decrease for each piece owned by opponent
        moveValue += numPlayerPieces * pieceValue;
        moveValue -= finalOpponentPieces * pieceValue;

        // give incentive for moving king if we didn't capture any opponent pieces
        // if we're going for a capture, check to see if opponent will capture the piece we just moved
        if (finalOpponentPieces == initialOpponentPieces) {
            moveValue -= kingDistance * kingDistanceValue;
        } else if (isPieceVulnerable(finalBoardState, swedeMove)) {
            moveValue -= pieceValue;
        }


        return moveValue;
    }

    // returns true if the opponent is able to capture the piece on the following turn
    private boolean isPieceVulnerable(TablutBoardState boardState, TablutMove playerMove) {

        int player_id = myPlayer.getColor();
        Coord pieceCoord = playerMove.getEndPosition();
        List<Coord> neighbours = Coordinates.getNeighbors(pieceCoord);
        TablutBoardState.Piece playerPieceType;
        TablutBoardState.Piece opponentPieceType;

        if (player_id == TablutBoardState.MUSCOVITE) {
            playerPieceType = TablutBoardState.Piece.BLACK;
            opponentPieceType = TablutBoardState.Piece.WHITE;
        } else {
            playerPieceType = TablutBoardState.Piece.WHITE;
            opponentPieceType = TablutBoardState.Piece.BLACK;
        }

        // go through all neighbours of piece's coordinates
        for (Coord neighbour : neighbours) {
            TablutBoardState.Piece neighbourPiece = boardState.getPieceAt(neighbour);
            if (neighbourPiece == opponentPieceType) {
                Coord sandwichCoord = null;
                try {
                    sandwichCoord = Coordinates.getSandwichCoord(pieceCoord, neighbour);
                } catch (Exception ignored) {
                }
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

    private static double calculateAverageDouble(List<Double> marks) {
        Double sum = 0.0;
        if (!marks.isEmpty()) {
            for (Double mark : marks) {
                sum += mark;
            }
            return sum / marks.size();
        }
        return sum;
    }


}
