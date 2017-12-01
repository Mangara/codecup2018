package codecup2018.movegenerator;

import codecup2018.data.Board;

public class NoHolesMax implements MoveGenerator {

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
        byte v = getExtremeValueLeft(board, player1, nonHoles > 0);
        int[] moves = new int[nonHoles > 0 ? nonHoles : free.length];
        int moveIndex = 0;

        for (int i = 0; i < free.length; i++) {
            byte pos = free[i];

            if (nonHoles == 0 || freeNeighbours[i] > 0) {
                moves[moveIndex] = Board.buildMove(pos, v, 0);
                moveIndex++;
            }
        }

        return moves;
    }

    private byte getExtremeValueLeft(Board board, boolean player1, boolean max) {
        for (int i = 0; i < 15; i++) {
            byte v = (byte) (max ? 15 - i : i + 1);

            if ((player1 && !board.haveIUsed(v)) || (!player1 && !board.hasOppUsed(v))) {
                return (player1 ? v : (byte) -v);
            }
        }

        throw new IllegalArgumentException();
    }
}
