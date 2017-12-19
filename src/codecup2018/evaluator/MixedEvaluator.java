package codecup2018.evaluator;

import codecup2018.data.Board;

public class MixedEvaluator implements Evaluator {

    private Board board;
    private int nFree = 0;
    private int nUnused = 0;
    private int totalUnused = 0;
    private int totalHoleValue = 0;
    private int totalFreeDegree = 0;
    
    private final Evaluator endgameEvaluator = new MedianFree();

    @Override
    public void initialize(Board board) {
        this.board = board;

        nFree = 0;
        nUnused = 0;
        totalUnused = 0;
        totalHoleValue = 0;
        totalFreeDegree = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                byte v = board.get(pos);

                if (v == Board.FREE) {
                    nFree++;
                    totalHoleValue += board.getHoleValue(pos);
                    totalFreeDegree += board.getFreeSpotsAround(pos);
                }
            }
        }

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
    }

    @Override
    public void block(byte pos) {
        int holeValue = board.getHoleValue(pos);
        int freeDegree = board.getFreeSpotsAround(pos);

        nFree--;
        totalHoleValue -= holeValue;
        totalFreeDegree -= 2 * freeDegree;
    }

    @Override
    public void applyMove(int move) {
        byte pos = Board.getMovePos(move);
        byte val = Board.getMoveVal(move);

        int holeValue = board.getHoleValue(pos);
        int freeDegree = board.getFreeSpotsAround(pos);

        nFree--;
        totalHoleValue = totalHoleValue - holeValue + freeDegree * val;
        totalFreeDegree -= 2 * freeDegree;

        nUnused--;
        totalUnused -= val;
    }

    @Override
    public void undoMove(int move) {
        byte pos = Board.getMovePos(move);
        byte val = Board.getMoveVal(move);

        int holeValue = board.getHoleValue(pos);
        int freeDegree = board.getFreeSpotsAround(pos);

        nFree++;
        totalHoleValue = totalHoleValue + holeValue - freeDegree * val;
        totalFreeDegree += 2 * freeDegree;

        nUnused++;
        totalUnused += val;
    }

    @Override
    public int evaluate(Board board) {
        if (board != this.board) {
            throw new InternalError();
        }

        if (totalFreeDegree == 0) {
            // Endgame
            return endgameEvaluator.evaluate(board) + (100 * totalHoleValue) / nFree;
        } else {
            double expectedFree = (nUnused == 0 ? 0 : totalUnused / (double) nUnused);
            double totalExpectedHoleValue = totalHoleValue + expectedFree * totalFreeDegree;
            return (int) (10000 * totalExpectedHoleValue / nFree);
        }
    }

}
