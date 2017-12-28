package codecup2018.evaluator;

import codecup2018.data.Board;
import java.util.Arrays;

public class MedianExpected implements Evaluator {

    @Override
    public int evaluate(Board board) {
        // Find out how many moves I have left (this varies depending on if I'm the true player1 or player2)
        int movesLeft = 0;
        
        for (byte v = 1; v <= 15; v++) {
            if (!board.haveIUsed(v)) {
                movesLeft++;
            }
        }
        
        // Compute the expected value of a free space =
        // Average value of all pieces left for both players
        int totalUnused = 0;
        int nUnused = 0;
        
        for (byte i = 1; i <= 15; i++) {
            if (!board.haveIUsed(i)) {
                totalUnused += i;
                nUnused++;
            }
            if (!board.hasOppUsed(i)) {
                totalUnused -= i;
                nUnused++;
            }
        }
        
        double expectedFree = (nUnused == 0 ? 0 : totalUnused / (double) nUnused);
        
        // Find the value of all free spaces
        byte[] free = board.getFreeSpots();
        double[] holes = new double[free.length];

        for (int i = 0; i < free.length; i++) {
            byte pos = free[i];
            holes[i] = board.getHoleValue(pos) + expectedFree * board.getFreeSpotsAround(pos);
        }

        // Return the one that wouldn't be filled in if the players just took
        // turns optimally filling in free spaces with 0
        Arrays.sort(holes);
        return (int) (10000 * holes[movesLeft]);
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
