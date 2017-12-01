package codecup2018.movegenerator;

import codecup2018.data.Board;
import java.util.ArrayList;
import java.util.List;

public class AllMoves implements MoveGenerator {

    @Override
    public int[] generateMoves(Board board, boolean player1) {
        byte[] free = board.getFreeSpots();
        List<Byte> freeValues = getFreeValues(board, player1);

        int[] moves = new int[free.length * freeValues.size()];
        int i = 0;

        for (byte pos : free) {
            for (byte v : freeValues) {
                moves[i] = Board.buildMove(pos, v, 0);
                i++;
            }
        }

        return moves;
    }

    private List<Byte> getFreeValues(Board board, boolean player1) {
        List<Byte> freeValues = new ArrayList<>();

        for (byte v = 1; v <= 15; v++) {
            if ((player1 && board.haveIUsed(v)) || (!player1 && board.hasOppUsed(v))) {
                continue;
            }

            freeValues.add(player1 ? v : (byte) -v);
        }

        return freeValues;
    }
}
