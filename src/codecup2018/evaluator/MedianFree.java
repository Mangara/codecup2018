package codecup2018.evaluator;

import codecup2018.data.Board;
import java.util.Arrays;

public class MedianFree implements Evaluator {

    @Override
    public int evaluate(Board board) {
        // Find the value of all free spaces
        byte[] free = board.getFreeSpots();

        for (int i = 0; i < free.length; i++) {
            free[i] = (byte) board.getHoleValue(free[i]); // In the range [-75, 75], so fits in a byte
        }

        // Return the one that wouldnt be filled in if the players just took
        // turns filling in free spaces with 0
        Arrays.sort(free);
        return 10000 * free[(free.length - 1) / 2];
    }

    @Override
    public void initialize(Board board) {
    }

    @Override
    public void block(byte pos) {
    }

    @Override
    public void applyMove(int move) {
    }

    @Override
    public void undoMove(int move) {
    }

}
