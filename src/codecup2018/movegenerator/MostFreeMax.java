package codecup2018.movegenerator;

import codecup2018.data.Board;
import java.util.ArrayList;
import java.util.List;

public class MostFreeMax implements MoveGenerator {

    @Override
    public List<byte[]> generateMoves(Board board, boolean player1) {
        List<byte[]> moves = new ArrayList<>();
        byte v = getMaxValueLeft(board, player1);
        int mostFree = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (!board.isFree(a, b)) {
                    continue;
                }
                
                int free = board.getFreeSpotsAround(a, b);
                
                if (free < mostFree) {
                    continue;
                } else if (free > mostFree) {
                    mostFree = free;
                    moves.clear();
                }

                moves.add(new byte[]{a, b, (player1 ? v : (byte) -v)});
            }
        }

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
