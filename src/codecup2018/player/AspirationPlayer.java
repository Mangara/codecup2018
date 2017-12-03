package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;

public class AspirationPlayer extends StandardPlayer {

    public static boolean DEBUG_FINAL_VALUE = false;
    private static final boolean DEBUG_AB = false;

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
            System.err.printf("Searching [%d, %d]", alpha, beta);
        }

        int move = negamax(1, maxDepth + 1, alpha, beta);
        int eval = Board.getMoveEval(move);

        if (DEBUG_AB || DEBUG_FINAL_VALUE) {
            System.err.printf(" => %d%n", eval);
        }

        if (eval >= beta) { // Fail high
            alpha = beta - 1;
            beta = Board.MAX_EVAL;

            if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                System.err.printf("Searching [%d, %d]", alpha, beta);
            }

            move = negamax(1, maxDepth + 1, alpha, beta);
            eval = Board.getMoveEval(move);

            if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                System.err.printf(" => %d%n", eval);
            }
        } else if (eval <= alpha) { // Fail low
            beta = alpha + 1;
            alpha = Board.MIN_EVAL;

            if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                System.err.printf("Searching [%d, %d]", alpha, beta);
            }

            move = negamax(1, maxDepth + 1, alpha, beta);
            eval = Board.getMoveEval(move);

            if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                System.err.printf(" => %d%n", eval);
            }
        }

        if (DEBUG_AB || DEBUG_FINAL_VALUE) {
            System.err.println("Final: " + eval);
        }

        if (eval >= beta || eval <= alpha) {
            System.err.println("Search is unstable: failed " + (eval >= beta ? "high" : "low") + " after first failing " + (eval >= beta ? "low" : "high"));
            move = negamax(1, maxDepth + 1, Board.MIN_EVAL, Board.MAX_EVAL);
            eval = Board.getMoveEval(move);
        }
        
        prevScore = eval;

        return move;
    }

    private int negamax(int player, int depth, int alpha, int beta) {
        if (depth == 0 || board.isGameOver()) {
            return Board.buildMove((byte) 0, (byte) 0, player * evaluator.evaluate(board));
        }

        if (DEBUG_AB) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sRunning negamax with %d turns left, interval=[%d, %d] and board state:%n", getName(), "", depth, alpha, beta);
            Board.print(board);
        }

        int bestMove = Board.MIN_EVAL_MOVE;
        int[] moves = generator.generateMoves(board, player > 0);

        for (int move : moves) {
            move = evaluateMove(move, player, depth, alpha, beta);

            if (move > bestMove) {
                int eval = Board.getMoveEval(move);

                if (eval > Board.getMoveEval(bestMove)) { // Only overwrite when strictly better
                    bestMove = move;

                    if (eval > alpha) {
                        alpha = eval;

                        if (beta <= alpha) {
                            if (DEBUG_AB) {
                                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBeta cut-off%n", getName(), "");
                            }
                            break;
                        }
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

        move = Board.setMoveEval(move, -Board.getMoveEval(negamax(-player, depth - 1, -beta, -alpha)));

        board.undoMove(move);
        evaluator.undoMove(move);

        if (DEBUG_AB) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sGot back a score of %d%n", getName(), "", Board.getMoveEval(move));
        }

        return move;
    }
}
