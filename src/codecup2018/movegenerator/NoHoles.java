package codecup2018.movegenerator;

import codecup2018.data.Board;
import java.util.ArrayList;
import java.util.List;

public class NoHoles implements MoveGenerator {

    @Override
    public List<byte[]> generateMoves(Board board, boolean player1) {
        List<byte[]> moves = new ArrayList<>();
        boolean anyNonHole = false;

        for (byte a = 0; a < 8 && !anyNonHole; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8 && !anyNonHole; pos++) {
                if (board.isFree(pos) && board.getFreeSpotsAround(pos) > 0) {
                    anyNonHole = true;
                }
            }
        }
        
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                if (!board.isFree(pos) || (anyNonHole && board.getFreeSpotsAround(pos) == 0)) {
                    continue;
                }

                for (byte v = 1; v <= 15; v++) {
                    if ((player1 && board.haveIUsed(v)) || (!player1 && board.hasOppUsed(v))) {
                        continue;
                    }

                    moves.add(new byte[]{a, (byte) (pos % 8), (player1 ? v : (byte) -v)});
                }
            }
        }

        return moves;
    }

}
