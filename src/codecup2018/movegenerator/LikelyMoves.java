package codecup2018.movegenerator;

import codecup2018.data.Board;
import codecup2018.tools.RandomPositionGenerator;
import java.util.Arrays;
import java.util.List;

public class LikelyMoves implements MoveGenerator {

    @Override
    public int[] generateMoves(final Board board, boolean player1) {
        byte[] free = board.getFreeSpots();
        int[] positions = new int[free.length];

        // Moves with many open squares should be processed first
        // Within that, moves that remove more opponent influence
        for (int i = 0; i < free.length; i++) {
            byte pos = free[i];
            int freeSpots = board.getFreeSpotsAround(pos);
            int holeValue = board.getHoleValue(pos);
            int sortingScore = -1 * (10000 * freeSpots + (player1 ? -holeValue : holeValue));

            positions[i] = Board.buildMove(pos, (byte) 0, sortingScore);
        }

        Arrays.sort(positions);

        // Assign values (done now so that they do not affect sorting behaviour)
        byte[] values = getValues(board, player1);
        byte min = values[values.length - 1];

        int[] tempMoves = new int[positions.length * values.length];
        int moveIndex = 0;

        for (int i = 0; i < positions.length; i++) {
            int freeSpots = (Board.getMoveEval(positions[i]) - 75) / -10000;

            if (freeSpots == 0) { // Always fill holes with our lowest remaining tile
                tempMoves[moveIndex] = Board.setMoveVal(positions[i], min);
                moveIndex++;
            } else if (freeSpots > 2) { // Don't waste min on these
                for (int j = 0; j < values.length - 1; j++) {
                    tempMoves[moveIndex] = Board.setMoveVal(positions[i], values[j]);
                    moveIndex++;
                }
            } else {
                for (int j = 0; j < values.length; j++) {
                    tempMoves[moveIndex] = Board.setMoveVal(positions[i], values[j]);
                    moveIndex++;
                }
            }
        }

        // Only return actual moves
        int[] moves = new int[moveIndex];
        System.arraycopy(tempMoves, 0, moves, 0, moveIndex);
        return moves;
    }

    private byte[] getValues(Board board, boolean player1) {
        byte max = 0, min = 0, med = 0;

        if (player1) {
            for (byte v = 15; v > 0; v--) {
                if (!board.haveIUsed(v)) {
                    max = v;
                    break;
                }
            }

            for (byte v = 1; v <= 15; v++) {
                if (!board.haveIUsed(v)) {
                    min = v;
                    break;
                }
            }

            if (max > 8 && min < 8) {
                for (byte v = 8; v < max; v++) {
                    if (!board.haveIUsed(v)) {
                        med = v;
                        break;
                    }
                }
            }
        } else {
            for (byte v = 15; v > 0; v--) {
                if (!board.hasOppUsed(v)) {
                    max = (byte) -v;
                    break;
                }
            }

            for (byte v = 1; v <= 15; v++) {
                if (!board.hasOppUsed(v)) {
                    min = (byte) -v;
                    break;
                }
            }

            if (-max > 8 && -min < 8) {
                for (byte v = 8; v < -max; v++) {
                    if (!board.hasOppUsed(v)) {
                        med = (byte) -v;
                        break;
                    }
                }
            }
        }

        if (max == min) {
            return new byte[]{max};
        } else if (med == 0 || med == max || med == min) {
            return new byte[]{max, min};
        } else {
            if (player1) {
                return new byte[]{med, max, min};
            } else {
                return new byte[]{max, med, min};
            }
        }
    }

    public static void main(String[] args) {
        LikelyMoves lm = new LikelyMoves();
        List<Board> boards = RandomPositionGenerator.generateRealisticTestBoards(100);

        for (Board board : boards) {
            // Player 1
            Board.print(board);
            int[] moves = lm.generateMoves(board, true);

            System.err.print("Likely moves: [");
            for (int move : moves) {
                System.err.print(Board.moveToString(move) + ", ");
            }
            System.err.println("]");

            // Player 2
            board.applyMove(moves[0]);
            Board.print(board);
            moves = lm.generateMoves(board, false);

            System.err.print("Likely moves: [");
            for (int move : moves) {
                System.err.print(Board.moveToString(move) + ", ");
            }
            System.err.println("]");
        }
    }
}
