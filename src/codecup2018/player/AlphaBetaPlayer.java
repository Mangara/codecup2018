package codecup2018.player;

import codecup2018.Util;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;
import java.util.List;

public class AlphaBetaPlayer extends StandardPlayer {

    private static final boolean DEBUG_AB = false;

    private int depth;

    public AlphaBetaPlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
        super(name, evaluator, generator);
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
    
    @Override
    protected byte[] selectMove() {
        // Top-level alpha-beta
        int bestValue = Integer.MIN_VALUE;
        byte[] bestMove = null;

        List<byte[]> moves = generator.generateMoves(board, true);

        for (byte[] move : moves) {
            if (DEBUG_AB) {
                System.err.println(getName() + ": Evaluating my move " + Arrays.toString(move));
            }

            board.applyMove(move);
            int value = alphaBeta(false, depth, bestValue, Integer.MAX_VALUE);
            board.undoMove(move);

            if (DEBUG_AB) {
                System.err.println(getName() + ": Value of my move " + Arrays.toString(move) + " is " + value);
            }

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int alphaBeta(boolean player1, int depth, int alpha, int beta) {
        if (DEBUG_AB) {
            System.err.printf("%s:  Running alpha-beta with %d turns left, interval=[%d, %d] and board state:%n", getName(), depth, alpha, beta);
            Util.print(board);
        }

        if (depth == 0 || board.isGameOver()) {
            return evaluator.evaluate(board);
        }

        int bestValue = (player1 ? Integer.MIN_VALUE : Integer.MAX_VALUE);
        List<byte[]> moves = generator.generateMoves(board, player1);

        for (byte[] move : moves) {
            if (DEBUG_AB) {
                System.err.printf("%s:   Evaluating move %s%n", getName(), Arrays.toString(move));
            }

            board.applyMove(move);
            evaluator.applyMove(move);
            int value = alphaBeta(!player1, depth - 1, alpha, beta);
            board.undoMove(move);
            evaluator.undoMove(move);

            if (DEBUG_AB) {
                System.err.printf("%s:   Got back a score of %d%n", getName(), value);
            }

            if (player1) {
                // Maximize
                if (value > bestValue) {
                    bestValue = value;
                }

                if (value > alpha) {
                    alpha = value;
                }
            } else {
                // Minimize
                if (value < bestValue) {
                    bestValue = value;
                }

                if (value < beta) {
                    beta = value;
                }
            }

            if (beta <= alpha) {
                break;
            }
        }

        return bestValue;
    }

}
