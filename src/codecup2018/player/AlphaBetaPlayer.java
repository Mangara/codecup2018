package codecup2018.player;

import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;
import java.util.List;

public class AlphaBetaPlayer extends Player {

    private final Evaluator evaluator;
    private final MoveGenerator generator;
    private int depth;

    public AlphaBetaPlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
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
        double bestValue = Double.NEGATIVE_INFINITY;
        byte[] bestMove = null;

        List<byte[]> moves = generator.generateMoves(board, true);

        for (byte[] move : moves) {
            //System.err.println(getName() + ": Evaluating my move " + Arrays.toString(move));
            board.applyMove(move);
            double value = alphaBeta(false, depth, bestValue, Double.POSITIVE_INFINITY);
            board.undoMove(move);
            //System.err.println(getName() + ": Value of my move " + Arrays.toString(move) + " is " + value);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private double alphaBeta(boolean player1, int depth, double alpha, double beta) {
        //System.err.printf("%s:  Running alpha-beta with %d turns left, interval=[%f, %f] and board state:%n", getName(), depth, alpha, beta);
        //board.print();

        if (depth == 0 || board.isGameOver()) {
            return evaluator.evaluate(board);
        }

        double bestValue = (player1 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
        List<byte[]> moves = generator.generateMoves(board, player1);

        for (byte[] move : moves) {
            //System.err.printf("%s:   Evaluating move %s%n", getName(), Arrays.toString(move));
            board.applyMove(move);
            double value = alphaBeta(!player1, depth - 1, alpha, beta);
            board.undoMove(move);
            //System.err.printf("%s:   Got back a score of %f%n", getName(), value);

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
