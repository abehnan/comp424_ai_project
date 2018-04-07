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
        long start = System.currentTimeMillis();
        Move myMove = myTools.getMove(boardState);
        long end = System.currentTimeMillis() - start;
        if (end >= 1500) {
            System.out.println("WARNING time: " + end + "ms");
        }
        return myMove;
    }
}