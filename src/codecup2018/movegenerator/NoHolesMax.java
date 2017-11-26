package codecup2018.movegenerator;

import codecup2018.data.ArrayBoard;
import codecup2018.data.Board;
import java.util.ArrayList;
import java.util.List;

public class NoHolesMax implements MoveGenerator {

    @Override
    public List<byte[]> generateMoves(Board board, boolean player1) {
        List<byte[]> moves = new ArrayList<>();
        boolean anyNonHole = false;

        for (byte a = 0; a < 8 && !anyNonHole; a++) {
            for (byte b = 0; b < 8 - a && !anyNonHole; b++) {
                if (board.isFree(a, b) && board.getFreeSpotsAround(a, b) > 0) {
                    anyNonHole = true;
                }
            }
        }

        byte v = getExtremeValueLeft(board, player1, anyNonHole);

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (!board.isFree(a, b) || (anyNonHole && board.getFreeSpotsAround(a, b) == 0)) {
                    continue;
                }

                moves.add(new byte[]{a, b, (player1 ? v : (byte) -v)});
            }
        }

        return moves;
    }

    private byte getExtremeValueLeft(Board board, boolean player1, boolean max) {
        for (int i = 0; i < 15; i++) {
            byte v = (byte) (max ? 15 - i : i + 1);

            if ((player1 && !board.haveIUsed(v)) || (!player1 && !board.hasOppUsed(v))) {
                return v;
            }
        }

        throw new IllegalArgumentException();
    }
}
