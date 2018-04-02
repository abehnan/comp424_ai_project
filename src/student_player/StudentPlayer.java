package student_player;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {

    private final MyTools myTools = new MyTools(player_id);

    public StudentPlayer() {
        super("260639146");
    }

    public Move chooseMove(TablutBoardState boardState) {
        if (player_id == TablutBoardState.MUSCOVITE) {
            return myTools.generateMuscoviteMove(boardState);
        } else {
            return myTools.generateSwedeMove(boardState);
        }
    }
}