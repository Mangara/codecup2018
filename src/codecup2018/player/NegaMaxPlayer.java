package codecup2018.player;

import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;
import java.util.List;

public class NegaMaxPlayer extends Player {

    private static final boolean DEBUG_AB = false;

    private final Evaluator evaluator;
    private final MoveGenerator generator;
    private int depth;

    public NegaMaxPlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
        super(name);
        this.evaluator = evaluator;
        this.generator = generator;
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
        int bestValue = Integer.MIN_VALUE + 1;
        byte[] bestMove = null;

        List<byte[]> moves = generator.generateMoves(board, true);

        for (byte[] move : moves) {
            if (DEBUG_AB) {
                System.err.println(getName() + ": Evaluating my move " + Arrays.toString(move));
            }

            board.applyMove(move);
            int value = -negamax(-1, depth, Integer.MIN_VALUE + 1, -bestValue);
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

    private int negamax(int player, int depth, int alpha, int beta) {
        if (DEBUG_AB) {
            System.err.printf("%s:  Running negamax with %d turns left, interval=[%d, %d] and board state:%n", getName(), depth, alpha, beta);
            board.print();
        }

        if (depth == 0 || board.isGameOver()) {
            return player * evaluator.evaluate(board);
        }

        int bestValue = Integer.MIN_VALUE + 1;
        List<byte[]> moves = generator.generateMoves(board, player > 0);

        for (byte[] move : moves) {
            if (DEBUG_AB) {
                System.err.printf("%s:   Evaluating move %s%n", getName(), Arrays.toString(move));
            }

            board.applyMove(move);
            int value = -negamax(-player, depth - 1, -beta, -alpha);
            board.undoMove(move);

            if (DEBUG_AB) {
                System.err.printf("%s:   Got back a score of %d%n", getName(), value);
            }

            if (value > bestValue) {
                bestValue = value;

                if (value > alpha) {
                    alpha = value;

                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }

        return bestValue;
    }

}
