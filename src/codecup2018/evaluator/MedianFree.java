package codecup2018.evaluator;

import codecup2018.data.Board;
import java.util.Arrays;
import java.util.List;

public class MedianFree implements Evaluator {

    @Override
    public int evaluate(Board board) {
        // Find the value of all free spaces
        List<byte[]> free = board.getFreeSpots();
        int[] holeValues = new int[free.size()];

        for (int i = 0; i < free.size(); i++) {
            byte[] spot = free.get(i);
            holeValues[i] = board.getHoleValue(Board.getPos(spot[0], spot[1]));
        }

        // Return the one that wouldnt be filled in if the players just took
        // turns filling in free spaces with 0
        Arrays.sort(holeValues);
        return 10000 * holeValues[(holeValues.length - 1) / 2];
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
