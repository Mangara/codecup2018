package codecup2018.evaluator;

import codecup2018.Board;

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
            for (byte b = 0; b < 8 - a; b++) {
                if (board.isFree(a, b)) {
                    totalExpectedHoleValue += board.getHoleValue(a, b) + board.getFreeSpotsAround(a, b) * expectedFree;
                    nHoles++;
                }
            }
        }
        
        return (int) (10000 * totalExpectedHoleValue / nHoles);
    }
    
}
