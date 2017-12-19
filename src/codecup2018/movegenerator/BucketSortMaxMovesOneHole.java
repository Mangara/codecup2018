package codecup2018.movegenerator;

import codecup2018.data.Board;

public class BucketSortMaxMovesOneHole implements MoveGenerator {

    private final int[][] freeSorted = new int[7][31];

    @Override
    public int[] generateMoves(final Board board, boolean player1) {
        byte max = getMaxValueLeft(board, player1);
        byte min = getMinValueLeft(board, player1);

        byte[] free = board.getFreeSpots();
        byte[] index = new byte[7];

        for (int i = 0; i < free.length; i++) {
            byte pos = free[i];
            int freeAround = board.getFreeSpotsAround(pos);
            freeSorted[freeAround][index[freeAround]++] = Board.buildMove(pos, max, 0);
        }

        int[] moves = new int[free.length - (index[0] > 1 ? index[0] - 1 : 0)];
        int movesIndex = 0;

        // Moves with many open squares should be processed first
        for (int i = 6; i > 0; i--) {
            System.arraycopy(freeSorted[i], 0, moves, movesIndex, index[i]);
            movesIndex += index[i];
        }

        // Only include the worst hole
        if (index[0] > 1) {
            int worstHoleValue = 76;
            int worstHole = 0;
            
            for (int i = 0; i < index[0]; i++) {
                int hole = freeSorted[0][i];
                int holeValue = board.getHoleValue(Board.getMovePos(hole));
                
                if (!player1) {
                    holeValue *= -1;
                }
                
                if (holeValue < worstHoleValue) {
                    worstHoleValue = holeValue;
                    worstHole = hole;
                }
            }
            
            moves[movesIndex] = Board.setMoveVal(worstHole, min);
        } else if (index[0] == 1) {
            moves[movesIndex] = Board.setMoveVal(freeSorted[0][0], min);
        }

        return moves;
    }

    private byte getMaxValueLeft(Board board, boolean player1) {
        for (byte v = 15; v > 0; v--) {
            if (player1) {
                if (!board.haveIUsed(v)) {
                    return v;
                }
            } else {
                if (!board.hasOppUsed(v)) {
                    return (byte) -v;
                }
            }
        }

        System.err.println("ERROR: No value left for active player.");
        Board.print(board);
        
        for (int i = 15; i >= 1; i--) {
            System.err.printf("%6d", i);
        }
        System.err.println();
        for (byte i = 15; i >= 1; i--) {
            System.err.printf("%6b", player1 ? board.haveIUsed(i) : board.hasOppUsed(i));
        }
        System.err.println();
        
        
        throw new IllegalArgumentException();
    }

    private byte getMinValueLeft(Board board, boolean player1) {
        for (byte v = 1; v <= 15; v++) {
            if (player1) {
                if (!board.haveIUsed(v)) {
                    return v;
                }
            } else {
                if (!board.hasOppUsed(v)) {
                    return (byte) -v;
                }
            }
        }

        throw new IllegalArgumentException();
    }

}
