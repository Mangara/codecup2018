package codecup2018.movegenerator;

import codecup2018.data.Board;

public class MostFreeMax implements MoveGenerator {

    @Override
    public int[] generateMoves(Board board, boolean player1) {
        byte[] free = board.getFreeSpots();

        // Find the maximum number of free neighbours
        int[] freeSpots = new int[free.length];
        int maxFreeSpots = 0;
        int maxCount = 0;

        for (int i = 0; i < free.length; i++) {
            freeSpots[i] = board.getFreeSpotsAround(free[i]);

            if (freeSpots[i] > maxFreeSpots) {
                maxFreeSpots = freeSpots[i];
                maxCount = 1;
            } else if (freeSpots[i] == maxFreeSpots) {
                maxCount++;
            }
        }

        // Collect all moves with the maximum number of free spots
        byte v = getMaxValueLeft(board, player1);
        int[] moves = new int[maxCount];
        int moveIndex = 0;

        for (int i = 0; i < free.length; i++) {
            byte pos = free[i];

            if (freeSpots[i] == maxFreeSpots) {
                moves[moveIndex] = Board.buildMove(pos, v, 0);
                moveIndex++;
            }
        }

        return moves;
    }

    private byte getMaxValueLeft(Board board, boolean player1) {
        for (byte v = 15; v > 0; v--) {
            if ((player1 && !board.haveIUsed(v)) || (!player1 && !board.hasOppUsed(v))) {
                return (player1 ? v : (byte) -v);
            }
        }

        throw new IllegalArgumentException();
    }
}
