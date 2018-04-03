package student_player;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutPlayer;

/**
 * A player file submitted by a student.
 */
public class StudentPlayer extends TablutPlayer {

    private MyTools myTools = new MyTools(this);

    public StudentPlayer() {
        super("260639146");
    }

    public Move chooseMove(TablutBoardState boardState) {
        return myTools.getMove(boardState, player_id);
    }
}