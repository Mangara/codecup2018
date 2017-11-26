package codecup2018.movegenerator;

import codecup2018.data.ArrayBoard;
import codecup2018.data.Board;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MaxInfluenceMoves implements MoveGenerator {

    @Override
    public List<byte[]> generateMoves(final Board board, boolean player1) {
        byte v = getMaxValueLeft(board, player1);

        List<byte[]> moves = new ArrayList<>();

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (!board.isFree(a, b)) {
                    continue;
                }

                moves.add(new byte[]{a, b, (player1 ? v : (byte) -v)});
            }
        }
        
        // Moves with many open squares should be processed first
        Collections.sort(moves, new Comparator<byte[]>() {
            @Override
            public int compare(byte[] m1, byte[] m2) {
                return -Integer.compare(board.getFreeSpotsAround(m1[0], m1[1]), board.getFreeSpotsAround(m2[0], m2[1]));
            }
        });

        return moves;
    }

    private byte getMaxValueLeft(Board board, boolean player1) {
        for (byte v = 15; v > 0; v--) {
            if ((player1 && !board.haveIUsed(v)) || (!player1 && !board.hasOppUsed(v))) {
                return v;
            }
        }

        throw new IllegalArgumentException();
    }

}
