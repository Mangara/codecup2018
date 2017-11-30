package codecup2018.evaluator;

import codecup2018.data.Board;

public class IncrementalExpectedValue implements Evaluator {

    private Board board;
    private int nFree = 0;
    private int nUnused = 0;
    private int totalUnused = 0;
    private int totalHoleValue = 0;
    private int totalFreeDegree = 0;
    
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
    public void applyMove(byte[] move) {
        int holeValue = board.getHoleValue(Board.getPos(move[0], move[1]));
        int freeDegree = board.getFreeSpotsAround(Board.getPos(move[0], move[1]));

        nFree--;
        totalHoleValue = totalHoleValue - holeValue + freeDegree * move[2];
        totalFreeDegree -= 2 * freeDegree;

        nUnused--;
        totalUnused -= move[2];
    }

    @Override
    public void undoMove(byte[] move) {
        int holeValue = board.getHoleValue(Board.getPos(move[0], move[1]));
        int freeDegree = board.getFreeSpotsAround(Board.getPos(move[0], move[1]));

        nFree++;
        totalHoleValue = totalHoleValue + holeValue - freeDegree * move[2];
        totalFreeDegree += 2 * freeDegree;

        nUnused++;
        totalUnused += move[2];
    }

    @Override
    public int evaluate(Board board) {
        if (board != this.board) {
            throw new InternalError();
        }

        double expectedFree = (nUnused == 0 ? 0 : totalUnused / (double) nUnused);
        double totalExpectedHoleValue = totalHoleValue + expectedFree * totalFreeDegree;
        return (int) (10000 * totalExpectedHoleValue / nFree);
    }

}
