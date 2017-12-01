package codecup2018.movegenerator;

import codecup2018.data.Board;
import java.util.Arrays;

public class MaxInfluenceMoves implements MoveGenerator {

    @Override
    public int[] generateMoves(final Board board, boolean player1) {
        byte v = getMaxValueLeft(board, player1);
        byte[] free = board.getFreeSpots();
        int[] moves = new int[free.length];
        
        // Moves with many open squares should be processed first
        for (int i = 0; i < free.length; i++) {
            byte pos = free[i];
            moves[i] = Board.buildMove(pos, v, -board.getFreeSpotsAround(pos));
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

}
