package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.Util;
import codecup2018.evaluator.Evaluator;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;
import java.util.List;

public class AspirationPlayer extends StandardPlayer {

    private static final boolean DEBUG_AB = false;

    private static final byte[] FAIL_HIGH = new byte[0];
    private static final byte[] FAIL_LOW = new byte[0];

    public static int WINDOW_SIZE = 10000;

    private final int depth;
    private int prevScore = 0;

    public AspirationPlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
        super(name, evaluator, generator);
        this.depth = depth;
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        prevScore = 0;
    }

    @Override
    protected byte[] selectMove() {
        byte[] move = topLevelSearch(prevScore - WINDOW_SIZE, prevScore + WINDOW_SIZE);

        if (move == FAIL_HIGH) {
            return topLevelSearch(prevScore + WINDOW_SIZE, Integer.MAX_VALUE);
        } else if (move == FAIL_LOW) {
            return topLevelSearch(Integer.MIN_VALUE + 1, prevScore - WINDOW_SIZE);
        } else {
            return move;
        }
    }

    private byte[] topLevelSearch(int alpha, int beta) {
        if (DEBUG_AB) {
            System.err.printf(getName() + ": Starting search with interval=[%d, %d]%n", alpha, beta);
        }

        // Top-level alpha-beta
        int bestValue = Integer.MIN_VALUE + 1;
        byte[] bestMove = null;

        List<byte[]> moves = generator.generateMoves(board, true);

        for (byte[] move : moves) {
            if (DEBUG_AB) {
                System.err.println(getName() + ": Evaluating my move " + Arrays.toString(move));
            }

            board.applyMove(move);
            evaluator.applyMove(move);
            int value = -negamax(-1, depth, -beta, -alpha);
            board.undoMove(move);
            evaluator.undoMove(move);

            if (DEBUG_AB) {
                System.err.println(getName() + ": Value of my move " + Arrays.toString(move) + " is " + value);
            }

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;

                if (value > alpha) {
                    alpha = value;

                    if (beta <= alpha) {
                        return FAIL_HIGH;
                    }
                }
            }
        }

        if (bestValue < alpha) {
            return FAIL_LOW;
        } else {
            //System.err.println("Final score: " + bestValue);
            prevScore = bestValue;
            return bestMove;
        }
    }

    private int negamax(int player, int depth, int alpha, int beta) {
        if (DEBUG_AB) {
            System.err.printf("%s:  Running negamax with %d turns left, interval=[%d, %d] and board state:%n", getName(), depth, alpha, beta);
            Util.print(board);
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
            evaluator.applyMove(move);
            int value = -negamax(-player, depth - 1, -beta, -alpha);
            board.undoMove(move);
            evaluator.undoMove(move);

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
