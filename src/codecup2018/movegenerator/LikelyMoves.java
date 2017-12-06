package codecup2018.movegenerator;

import codecup2018.data.Board;
import codecup2018.tools.RandomPositionGenerator;
import java.util.Arrays;
import java.util.List;

public class LikelyMoves implements MoveGenerator {

    @Override
    public int[] generateMoves(final Board board, boolean player1) {
        byte[] values = getValues(board, player1);
        byte min = values[values.length - 1];

        byte[] free = board.getFreeSpots();

        int nHoles = countHoles(board, free);
        
        int[] moves = new int[(free.length - nHoles) * values.length + nHoles];

        // Moves with many open squares should be processed first
        int moveIndex = 0;
        for (int i = 0; i < free.length; i++) {
            byte pos = free[i];
            int freeSpots = board.getFreeSpotsAround(pos);
            int holeValue = board.getHoleValue(pos);
            int sortingScore = -10000 * freeSpots + (player1 ? holeValue : -holeValue);

            if (freeSpots == 0) { // Always fill holes with our lowest remaining tile
                moves[moveIndex] = Board.buildMove(pos, min, sortingScore);
                moveIndex++;
            } else {
                for (int j = 0; j < values.length; j++) {
                    moves[moveIndex] = Board.buildMove(pos, (byte) 0, sortingScore);
                    moveIndex++;
                }
            }
        }

        Arrays.sort(moves);

        // Assign values in the proper order (so that they do not affect sorting behaviour)
        int valueIndex = 0;
        for (int i = 0; i < moves.length && Board.getMoveEval(moves[i]) <= -9000; i++) {
            moves[i] = Board.setMoveVal(moves[i], values[valueIndex]);

            valueIndex++;
            if (valueIndex == values.length) {
                valueIndex = 0;
            }
        }

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
    
    private int countHoles(Board board, byte[] free) {
        int holes = 0;
        
        for (byte pos : free) {
            if (board.getFreeSpotsAround(pos) == 0) {
                holes++;
            }
        }
        
        return holes;
    }
    
    public static void main(String[] args) {
        LikelyMoves lm = new LikelyMoves();
        List<Board> boards = RandomPositionGenerator.generateAllTestBoards(3);
        
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
