package codecup2018.evaluator;

import codecup2018.data.Board;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianFree implements Evaluator {

    @Override
    public int evaluate(Board board) {
        // Find the value of all free spaces
        List<Integer> holeValues = new ArrayList<>();

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (board.isFree(a, b)) {
                    holeValues.add(board.getHoleValue(a, b));
                }
            }
        }

        // Return the one that wouldnt be filled in if the players just took
        // turns filling in free spaces with 0
        Collections.sort(holeValues);
        return 10000 * holeValues.get((holeValues.size() - 1) / 2);
    }

    @Override
    public void initialize(Board board) {
    }

    @Override
    public void block(byte a, byte b) {
    }

    @Override
    public void applyMove(byte[] move) {
    }

    @Override
    public void undoMove(byte[] move) {
    }

}
