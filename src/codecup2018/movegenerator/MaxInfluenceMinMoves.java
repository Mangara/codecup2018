package codecup2018.movegenerator;

import codecup2018.data.Board;
import java.util.Arrays;

public class MaxInfluenceMinMoves implements MoveGenerator {

    @Override
    public int[] generateMoves(final Board board, boolean player1) {
        byte max = getMaxValueLeft(board, player1);
        byte min = getMinValueLeft(board, player1);
        
        byte[] free = board.getFreeSpots();
        int[] moves = new int[free.length];
        
        // Moves with many open squares should be processed first
        for (int i = 0; i < free.length; i++) {
            byte pos = free[i];
            int freeAround = board.getFreeSpotsAround(pos);
            byte v = (freeAround == 0 ? min : max);
            
            moves[i] = Board.buildMove(pos, v, -freeAround);
        }
        
        Arrays.sort(moves);
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
