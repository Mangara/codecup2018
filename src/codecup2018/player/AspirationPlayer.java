package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.Util;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;
import java.util.List;

public class AspirationPlayer extends StandardPlayer {

    public static boolean DEBUG_FINAL_VALUE = true;
    
    private static final boolean DEBUG_AB = false;
    private static final boolean DEBUG_COUNT_LEAVES = true;

    private static final byte[] FAIL_HIGH = new byte[0];
    private static final byte[] FAIL_LOW = new byte[0];

    public static int WINDOW_SIZE = 10000;

    private final int depth;
    private int prevScore = 0;

    private int nLeaves = 0; // For evaluating different heuristics

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
        if (DEBUG_COUNT_LEAVES) {
            nLeaves = 0;
        }

        byte[] move = topLevelSearch(prevScore - WINDOW_SIZE, prevScore + WINDOW_SIZE);

        if (move == FAIL_HIGH) {
            move = topLevelSearch(prevScore + WINDOW_SIZE, Integer.MAX_VALUE);
        } else if (move == FAIL_LOW) {
            move = topLevelSearch(Integer.MIN_VALUE + 1, prevScore - WINDOW_SIZE);
        }

        if (DEBUG_COUNT_LEAVES) {
            System.err.println("Evals: " + nLeaves);
        }

        if (move == FAIL_HIGH || move == FAIL_LOW) {
            throw new InternalError("Search is unstable: failed " + (move == FAIL_HIGH ? "high" : "low") + " after first failing " + (move == FAIL_HIGH ? "low" : "high"));
        }

        return move;
    }

    private byte[] topLevelSearch(int alpha, int beta) {
        if (DEBUG_AB || DEBUG_FINAL_VALUE) {
            System.err.printf("Starting search in [%d, %d]%n", alpha, beta);
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
                        return FAIL_HIGH; // TODO: double-check if this should really be <= or <
                    }
                }
            }
        }

        if (bestValue < alpha) {
            return FAIL_LOW; // TODO: double-check if this should really be <= or <
        } else {
            if (DEBUG_FINAL_VALUE) {
                System.err.println("Final: " + bestValue);
            }
            
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
            if (DEBUG_COUNT_LEAVES) {
                nLeaves++;
            }
            
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
