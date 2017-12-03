package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;

public class AspirationTablePlayer extends StandardPlayer {

    public static boolean DEBUG_FINAL_VALUE = false;
    private static final boolean DEBUG_AB = false;

    public static int WINDOW_SIZE = 10001;

    private final byte maxDepth;

    private final static int TABLE_SIZE_POWER = 20;
    private final static int TABLE_SIZE = 1 << TABLE_SIZE_POWER;
    private final static int TABLE_KEY_MASK = TABLE_SIZE - 1;
    private final TranspositionEntry[] transpositionTable = new TranspositionEntry[TABLE_SIZE];

    private int prevScore = 0;
    private byte turn = 1;

    public AspirationTablePlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
        super(name, evaluator, generator);
        this.maxDepth = (byte) depth;
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        prevScore = evaluator.evaluate(board);
        turn = 1;
        Arrays.fill(transpositionTable, null);
    }

    @Override
    protected int selectMove() {
        int alpha = prevScore - WINDOW_SIZE, beta = prevScore + WINDOW_SIZE;

        if (DEBUG_AB || DEBUG_FINAL_VALUE) {
            System.err.printf("Searching [%d, %d]", alpha, beta);
        }

        int move = negamax((byte) 1, (byte) (maxDepth + 1), alpha, beta);
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

            move = negamax((byte) 1, (byte) (maxDepth + 1), alpha, beta);
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

            move = negamax((byte) 1, (byte) (maxDepth + 1), alpha, beta);
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
            move = negamax((byte) 1, (byte) (maxDepth + 1), Board.MIN_EVAL, Board.MAX_EVAL);
            eval = Board.getMoveEval(move);
        }

        prevScore = eval;
        turn++;

        return move;
    }

    private int negamax(byte player, byte depth, int alpha, int beta) {
        if (depth == 0 || board.isGameOver()) {
            return Board.buildMove((byte) 0, (byte) 0, player * evaluator.evaluate(board));
        }

        if (DEBUG_AB) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sRunning negamax with %d plies left, interval=[%d, %d] and board state:%n", getName(), "", depth, alpha, beta);
            Board.print(board);
        }

        int bestMove = Board.MIN_EVAL_MOVE;
        int bestEval = Board.MIN_EVAL;
        int myAlpha = alpha;

        // Check the transposition table
        TranspositionEntry entry = transpositionTable[board.getTranspositionTableKey() & TABLE_KEY_MASK];
        boolean tableMatch = entry != null && entry.hash == board.getHash() && board.isLegalMove(entry.bestMove);

        if (tableMatch) {
            // Return the stored evaluation if it matches what we're looking for
            int entryEval = Board.getMoveEval(entry.bestMove);

            if (entry.depthSearched >= depth && (entry.type == TranspositionEntry.EXACT
                    || (entry.type == TranspositionEntry.UPPER_BOUND && entryEval <= alpha)
                    || (entry.type == TranspositionEntry.LOWER_BOUND && entryEval >= beta))) {
                if (DEBUG_AB) {
                    System.err.printf("%s:%" + (2 * (maxDepth - depth + 1)) + "s%s transposition table result: %d%n", getName(), "", (entry.type == TranspositionEntry.EXACT ? "Exact" : (entry.type == TranspositionEntry.LOWER_BOUND ? "Lower bound" : "Upper bound")), entryEval);
                }
                return entry.bestMove;
            }

            // Try the stored best move first
            // Store the move out of an abundance of caution; if entry.bestMove was changed due to
            // a table cell collision, undoing the new move could really mess up the search.
            bestMove = evaluateMove(entry.bestMove, player, depth, myAlpha, beta);
            bestEval = Board.getMoveEval(bestMove);

            if (bestEval > myAlpha) {
                myAlpha = bestEval;
            }
        }

        if (myAlpha < beta) { // No cut-off yet
            int[] moves = generator.generateMoves(board, player > 0);

            for (int move : moves) {
                if (tableMatch && Board.getMovePos(move) == Board.getMovePos(entry.bestMove) && Board.getMoveVal(move) == Board.getMoveVal(entry.bestMove)) {
                    continue; // We already tried this one
                }

                move = evaluateMove(move, player, depth, myAlpha, beta);

                if (move > bestMove) {
                    int eval = Board.getMoveEval(move);
                    
                    if (eval > bestEval) { // Only overwrite when strictly better
                        bestMove = move;
                        bestEval = eval;

                        if (eval > myAlpha) {
                            myAlpha = eval;

                            if (beta <= myAlpha) {
                                if (DEBUG_AB) {
                                    System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBeta cut-off%n", getName(), "");
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            if (DEBUG_AB) {
                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBeta-cutoff from stored move%n", getName(), "");
            }
        }

        // Add to the transposition table
        if (entry == null) {
            entry = new TranspositionEntry();
            transpositionTable[board.getTranspositionTableKey() & TABLE_KEY_MASK] = entry;
        }

        if (entry.turn < turn || entry.depthSearched < depth) {
            entry.hash = board.getHash();
            entry.bestMove = bestMove;
            entry.depthSearched = depth;
            entry.type = (bestEval > alpha ? (bestEval < beta ? TranspositionEntry.EXACT : TranspositionEntry.LOWER_BOUND) : TranspositionEntry.UPPER_BOUND);
            entry.turn = turn;
        }

        return bestMove;
    }

    private int evaluateMove(int move, byte player, byte depth, int alpha, int beta) {
        if (DEBUG_AB) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sEvaluating move %s%n", getName(), "", Board.moveToString(move));
        }

        board.applyMove(move);
        evaluator.applyMove(move);

        move = Board.setMoveEval(move, -Board.getMoveEval(negamax((byte) -player, (byte) (depth - 1), -beta, -alpha)));

        board.undoMove(move);
        evaluator.undoMove(move);

        if (DEBUG_AB) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sGot back a score of %d%n", getName(), "", Board.getMoveEval(move));
        }

        return move;
    }

    private class TranspositionEntry {

        static final byte EXACT = 3, LOWER_BOUND = 4, UPPER_BOUND = 5;

        long hash; // Position hashcode for identification
        int bestMove; // Best move found from this position, with evaluation
        byte depthSearched; // The depth this position was searched to
        byte type; // Whether the value was exact (between alpha and beta) or an upper bound (<= alpha) or a lower bound (>= beta)
        byte turn; // In which turn this entry was stored
    }
}
