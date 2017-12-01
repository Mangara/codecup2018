package codecup2018.evaluator;

import codecup2018.data.Board;

public class ExpectedValue implements Evaluator {

    @Override
    public int evaluate(Board board) {
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
        
        // Average the expected value of each hole
        double totalExpectedHoleValue = 0;
        int nHoles = 0;
        
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                if (board.isFree(pos)) {
                    totalExpectedHoleValue += board.getHoleValue(pos) + board.getFreeSpotsAround(pos) * expectedFree;
                    nHoles++;
                }
            }
        }
        
        return (int) (10000 * totalExpectedHoleValue / nHoles);
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
