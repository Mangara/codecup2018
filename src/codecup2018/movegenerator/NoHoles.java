package codecup2018.movegenerator;

import codecup2018.data.Board;
import java.util.ArrayList;
import java.util.List;

public class NoHoles implements MoveGenerator {

    @Override
    public int[] generateMoves(Board board, boolean player1) {
        byte[] free = board.getFreeSpots();
        
        // Count non-holes
        int[] freeNeighbours = new int[free.length];
        int nonHoles = 0;

        for (int i = 0; i < free.length; i++) {
            byte pos = free[i];
            freeNeighbours[i] = board.getFreeSpotsAround(pos);

            if (freeNeighbours[i] > 0) {
                nonHoles++;
            }
        }

        // Either play all non-holes or all holes
        List<Byte> freeValues = getFreeValues(board, player1);
        int[] moves = new int[freeValues.size() * (nonHoles > 0 ? nonHoles : free.length)];
        int moveIndex = 0;

        for (int i = 0; i < free.length; i++) {
            byte pos = free[i];

            if (nonHoles == 0 || freeNeighbours[i] > 0) {
                for (Byte v : freeValues) {
                    moves[moveIndex] = Board.buildMove(pos, v, 0);
                    moveIndex++;
                }
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
