package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.Util;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;
import java.util.List;

public class AspirationPlayer extends StandardPlayer {

    public static boolean DEBUG_FINAL_VALUE = false;
    private static final boolean DEBUG_AB = false;

    private static final int FAIL_HIGH = 617245752;
    private static final int FAIL_LOW = 617245753;

    public static int WINDOW_SIZE = 10001;

    private final int maxDepth;
    private int prevScore = 0;

    public AspirationPlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
        super(name, evaluator, generator);
        this.maxDepth = depth;
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        prevScore = evaluator.evaluate(board);
    }

    @Override
    protected int selectMove() {
        int alpha = prevScore - WINDOW_SIZE, beta = prevScore + WINDOW_SIZE;
        
        if (DEBUG_AB || DEBUG_FINAL_VALUE) {
            System.err.printf("Searching [%d, %d]%n", alpha, beta);
        }

        int move = topLevelSearch(alpha, beta);

        if (move == FAIL_HIGH) {
            alpha = beta - 1;
            beta = Board.MAX_EVAL;
            
            if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                System.err.printf("Searching [%d, %d]%n", alpha, beta);
            }

            move = topLevelSearch(alpha, beta);
        } else if (move == FAIL_LOW) {
            beta = alpha + 1;
            alpha = Board.MIN_EVAL;
            
            if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                System.err.printf("Searching [%d, %d]%n", alpha, beta);
            }

            move = topLevelSearch(alpha, beta);
        }

        if (move == FAIL_HIGH || move == FAIL_LOW) {
            System.err.println("Search is unstable: failed " + (move == FAIL_HIGH ? "high" : "low") + " after first failing " + (move == FAIL_HIGH ? "low" : "high"));
            return topLevelSearch(Integer.MIN_VALUE + 1, Integer.MAX_VALUE);
        }

        return move;
    }

    private int topLevelSearch(int alpha, int beta) {
        if (DEBUG_AB || DEBUG_FINAL_VALUE) {
            System.err.printf("Searching [%d, %d]%n", alpha, beta);
        }

        // Top-level alpha-beta
        int bestValue = Integer.MIN_VALUE + 1;
        int bestMove = Board.MIN_EVAL_MOVE;

        int[] moves = generator.generateMoves(board, true);

        for (int move : moves) {
            if (DEBUG_AB) {
                System.err.println(getName() + ": Evaluating my move " + Board.moveToString(move));
            }

            board.applyMove(move);
            evaluator.applyMove(move);
            int value = -negamax(-1, maxDepth, -beta, -Math.max(alpha, bestValue));
            board.undoMove(move);
            evaluator.undoMove(move);

            if (DEBUG_AB) {
                System.err.println(getName() + ": Value of my move " + Board.moveToString(move) + " is " + value);
            }

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;

                if (bestValue >= beta) {
                    return FAIL_HIGH;
                }
            }
        }

        if (bestValue <= alpha) {
            return FAIL_LOW;
        } else {
            if (DEBUG_FINAL_VALUE) {
                System.err.println("Final: " + bestValue);
            }

            prevScore = bestValue;
            return bestMove;
        }
    }

    private int negamax(int player, int depth, int alpha, int beta) {
        if (depth == 0 || board.isGameOver()) {
            return Board.buildMove((byte) 0, (byte) 0, player * evaluator.evaluate(board));
        }
        
        if (DEBUG_AB) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sRunning negamax with %d turns left, interval=[%d, %d] and board state:%n", getName(), "", depth, Board.getMoveEval(alpha), Board.getMoveEval(beta));
            Board.print(board);
        }

        int bestMove = Board.MIN_EVAL_MOVE;
        int[] moves = generator.generateMoves(board, player > 0);

        for (int move : moves) {
            move = evaluateMove(move, player, depth, alpha, beta);

            if (move > bestMove && Board.getMoveEval(move) > Board.getMoveEval(bestMove)) { // Only overwrite when strictly better
                bestMove = move;

                if (bestMove > alpha) {
                    alpha = bestMove;

                    if (beta <= alpha) {
                        System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBeta cut-off%n", getName(), "");
                        break;
                    }
                }
            }
        }

        return bestMove;
    }

    private int evaluateMove(int move, int player, int depth, int alpha, int beta) {
        if (DEBUG_AB) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sEvaluating move %s%n", getName(), "", Board.moveToString(move));
        }

        board.applyMove(move);
        evaluator.applyMove(move);

        move = Board.setMoveEval(move, -Board.getMoveEval(negamax(-player, depth - 1, Board.negateEval(beta), Board.negateEval(alpha))));

        board.undoMove(move);
        evaluator.undoMove(move);

        if (DEBUG_AB) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sGot back a score of %d%n", getName(), "", Board.getMoveEval(move));
        }

        return move;
    }

}
