package codecup2018.movegenerator;

import codecup2018.data.Board;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MaxInfluenceMoves implements MoveGenerator {

    @Override
    public List<byte[]> generateMoves(final Board board, boolean player1) {
        List<byte[]> moves = board.getFreeSpots();
        
        // Moves with many open squares should be processed first
        for (byte[] move : moves) {
            // Temporarily store these sorting values in the move
            move[2] = (byte) board.getFreeSpotsAround(move[0], move[1]);
        }
        
        Collections.sort(moves, new Comparator<byte[]>() {
            @Override
            public int compare(byte[] m1, byte[] m2) {
                return -Byte.compare(m1[2], m2[2]);
            }
        });
        
        byte v = getMaxValueLeft(board, player1);
        
        for (byte[] move : moves) {
            move[2] = v;
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

        throw new IllegalArgumentException();
    }

}
