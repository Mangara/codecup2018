package codecup2018.evaluator;

import codecup2018.data.Board;
import java.util.Arrays;

public class MedianFree implements Evaluator {

    @Override
    public int evaluate(Board board) {
        // Find the value of all free spaces
        int[] free = board.getFreeSpots();

        for (int i = 0; i < free.length; i++) {
            free[i] = Board.setMoveEval(free[i], board.getHoleValue(Board.getMovePos(free[i])));
        }

        // Return the one that wouldnt be filled in if the players just took
        // turns filling in free spaces with 0
        Arrays.sort(free);
        return 10000 * Board.getMoveEval(free[(free.length - 1) / 2]);
    }

    @Override
    public void initialize(Board board) {
    }

    @Override
    public void block(byte pos) {
    }

    @Override
    public void applyMove(byte[] move) {
    }

    @Override
    public void undoMove(byte[] move) {
    }

}
