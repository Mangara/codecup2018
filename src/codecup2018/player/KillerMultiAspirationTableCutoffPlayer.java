package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.MedianFree;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;

public class KillerMultiAspirationTableCutoffPlayer extends StandardPlayer {

    public static boolean DEBUG_FINAL_VALUE = false;
    private static final boolean DEBUG_AB = false;
    private static final boolean DEBUG_BETA = false;
    private static final int DEBUG_TURN = -1;

    private final static int INITIAL_WINDOW_SIZE = 5000;
    private final static double WINDOW_FACTOR = 1.75;
    private final static int NUM_KILLERS = 2;

    private final byte maxDepth;
    private final Evaluator endgameEvaluator = new MedianFree();
    private final Player endgamePlayer = new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves());

    private final static int TABLE_SIZE_POWER = 20;
    private final static int TABLE_SIZE = 1 << TABLE_SIZE_POWER;
    private final static int TABLE_KEY_MASK = TABLE_SIZE - 1;
    private final TranspositionEntry[] transpositionTable = new TranspositionEntry[TABLE_SIZE];

    private int prevScore = 0;

    private final int[][] killerMoves;

    public KillerMultiAspirationTableCutoffPlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
        super(name, evaluator, generator);
        this.maxDepth = (byte) depth;
        killerMoves = new int[maxDepth + 2][NUM_KILLERS];
        for (int[] killers : killerMoves) {
            Arrays.fill(killers, Board.ILLEGAL_MOVE);
        }
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
        if (board.isGameInEndGame()) {
            endgamePlayer.initialize(board);
            return endgamePlayer.selectMove();

            ////DEBUG
            //return Board.setMoveEval(endgamePlayer.selectMove(), endgameEvaluator.evaluate(board));
        }

        int window = INITIAL_WINDOW_SIZE;
        int alpha = prevScore - window / 2, beta = prevScore + window / 2;
        int move, eval;
        boolean failLow = false;
        boolean failHigh = false;

        while (true) {
            if (DEBUG_AB || DEBUG_FINAL_VALUE || turn == DEBUG_TURN) {
                System.err.printf("Searching [%d, %d]", alpha, beta);
            }

            move = negamax((byte) 1, (byte) (maxDepth + 1), alpha, beta);
            eval = Board.getMoveEval(move);

            if (DEBUG_AB || DEBUG_FINAL_VALUE || turn == DEBUG_TURN) {
                System.err.printf(" => %d%n", eval);
            }

            if (eval <= alpha) { // Fail low
                failLow = true;
                alpha = eval - window;
            } else if (eval >= beta) { // Fail high
                failHigh = true;
                beta = eval + window;
            } else {
                break;
            }

            if (failLow && failHigh) {
                System.err.println("Search is unstable on turn " + turn);
                move = negamax((byte) 1, (byte) (maxDepth + 1), Board.MIN_EVAL, Board.MAX_EVAL);
                eval = Board.getMoveEval(move);
                break;
            }

            window *= WINDOW_FACTOR;
        }

        if (DEBUG_AB || DEBUG_FINAL_VALUE || turn == DEBUG_TURN) {
            System.err.println("Turn " + turn + " final: " + eval);
        }

        prevScore = eval;

        // Shift killer moves
        for (int i = killerMoves.length - 1; i >= 0; i--) {
            if (i > 1) {
                killerMoves[i] = killerMoves[i - 2];
            } else {
                killerMoves[i] = new int[NUM_KILLERS];
                Arrays.fill(killerMoves[i], Board.ILLEGAL_MOVE);
            }
        }

        return move;
    }

    private int negamax(byte player, byte depth, int alpha, int beta) {
        if (depth == 0 || board.isGameOver()) {
            return Board.buildMove((byte) 0, (byte) 0, player * evaluator.evaluate(board));
        }

        if (board.isGameInEndGame()) {
            return Board.buildMove((byte) 0, (byte) 0, player * (endgameEvaluator.evaluate(board) + evaluator.evaluate(board) / 100));
        }

        if (DEBUG_AB || turn == DEBUG_TURN) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sRunning negamax with %d plies left, interval=[%d, %d] and board state:%n", getName(), "", depth, alpha, beta);
            Board.print(board);
        }

        int bestMove = Board.MIN_EVAL_MOVE;
        int bestEval = Board.MIN_EVAL;
        int myAlpha = alpha;
        int myBeta = beta;

        // Check the transposition table
        TranspositionEntry entry = transpositionTable[board.getTranspositionTableKey() & TABLE_KEY_MASK];
        boolean tableMatch = entry != null && entry.hash == board.getHash() && board.isLegalMove(entry.bestMove);

        if (tableMatch) {
            int entryEval = Board.getMoveEval(entry.bestMove);

            // Return the stored evaluation if it matches what we're looking for
            if (entry.depthSearched >= depth) {
                switch (entry.type) {
                    case TranspositionEntry.EXACT:
                        if (DEBUG_AB || turn == DEBUG_TURN) {
                            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sExact transposition table result: %d%n", getName(), "", entryEval);
                        }
                        return entry.bestMove;
                    case TranspositionEntry.LOWER_BOUND:
                        if (entryEval >= beta) {
                            if (DEBUG_AB || turn == DEBUG_TURN) {
                                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sLower bound transposition table result: %d%n", getName(), "", entryEval);
                            }
                            return entry.bestMove;
                        }
                        if (DEBUG_AB || turn == DEBUG_TURN) {
                            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sInitializing alpha from transposition table result: %d%n", getName(), "", entryEval);
                        }
                        myAlpha = Math.max(myAlpha, entryEval);
                        break;
                    case TranspositionEntry.UPPER_BOUND:
                        if (entryEval <= alpha) {
                            if (DEBUG_AB || turn == DEBUG_TURN) {
                                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sUpper bound transposition table result: %d%n", getName(), "", entryEval);
                            }
                            return entry.bestMove;
                        }
                        if (DEBUG_AB || turn == DEBUG_TURN) {
                            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sInitializing beta from transposition table result: %d%n", getName(), "", entryEval);
                        }
                        myBeta = Math.min(myBeta, entryEval);
                        break;
                }
            }

            if (depth == 1) { // At depth 1, the move will return the same result as last time, regardless of alpha and beta
                bestMove = entry.bestMove;
                bestEval = entryEval;

                if (DEBUG_AB || turn == DEBUG_TURN) {
                    System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sInitializing best move from transposition table: %s => %d%n", getName(), "", Board.moveToString(bestMove), bestEval);
                }
                
                if (bestEval > myAlpha) {
                    myAlpha = bestEval;
                }
            } else {
                // Try the stored best move first
                bestMove = evaluateMove(entry.bestMove, player, depth, myAlpha, myBeta, " stored");
                /*///PROFILING
                int move = entry.bestMove;
                board.applyMove(move);
                evaluator.applyMove(move);

                bestMove = Board.setMoveEval(move, -Board.getMoveEval(negamax((byte) -player, (byte) (depth - 1), -myBeta, -myAlpha)));

                board.undoMove(move);
                evaluator.undoMove(move);
                //*/
                bestEval = Board.getMoveEval(bestMove);

                if (bestEval > myAlpha) {
                    myAlpha = bestEval;
                }
            }
        }

        // Try killer moves
        if (myAlpha < myBeta) {
            for (int move : killerMoves[depth]) {
                if (board.isLegalMove(move) && !(tableMatch && Board.equalMoves(move, entry.bestMove))) {
                    move = evaluateMove(move, player, depth, myAlpha, myBeta, " killer");

                    /*///PROFILING
                    board.applyMove(move);
                    evaluator.applyMove(move);

                    move = Board.setMoveEval(move, -Board.getMoveEval(negamax((byte) -player, (byte) (depth - 1), -myBeta, -myAlpha)));

                    board.undoMove(move);
                    evaluator.undoMove(move);
                    //*/
                    if (DEBUG_AB || turn == DEBUG_TURN || DEBUG_BETA) {
                        //System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBefore processing killer move: bestMove=%s bestEval=%d myAlpha=%d myBeta=%d%n", getName(), "", Board.moveToString(bestMove), bestEval, myAlpha, myBeta);
                    }

                    if (move > bestMove) {
                        int eval = Board.getMoveEval(move);

                        if (eval > bestEval) { // Only overwrite when strictly better
                            bestMove = move;
                            bestEval = eval;

                            if (eval > myAlpha) {
                                myAlpha = eval;

                                if (myBeta <= myAlpha) {
                                    // Beta-cutoff
                                    if (DEBUG_AB || turn == DEBUG_TURN || DEBUG_BETA) {
                                        System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBeta-cutoff from killer move %s%n", getName(), "", Board.moveToString(bestMove));
                                    }
                                    updateKillerMoves(depth, bestMove);
                                    break;
                                }
                            }
                        }
                    }

                    if (DEBUG_AB || turn == DEBUG_TURN || DEBUG_BETA) {
                        //System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sAfter processing killer move: bestMove=%s bestEval=%d myAlpha=%d myBeta=%d%n", getName(), "", Board.moveToString(bestMove), bestEval, myAlpha, myBeta);
                    }
                }
            }
        } else {
            if (DEBUG_AB || turn == DEBUG_TURN || DEBUG_BETA) {
                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBeta-cutoff from stored move %s%n", getName(), "", Board.moveToString(bestMove));
            }
            updateKillerMoves(depth, bestMove);
        }

        if (myAlpha < myBeta) { // No cut-off yet
            int[] moves = generator.generateMoves(board, player > 0);

            for (int move : moves) {
                if ((tableMatch && Board.equalMoves(move, entry.bestMove)) || matchesKiller(move, depth)) {
                    continue; // We already tried this one
                }

                move = evaluateMove(move, player, depth, myAlpha, myBeta, "");
                /*///PROFILING
                board.applyMove(move);
                evaluator.applyMove(move);

                move = Board.setMoveEval(move, -Board.getMoveEval(negamax((byte) -player, (byte) (depth - 1), -myBeta, -myAlpha)));

                board.undoMove(move);
                evaluator.undoMove(move);
                //*/

                if (DEBUG_AB || turn == DEBUG_TURN || DEBUG_BETA) {
                    //System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBefore processing move: bestMove=%s bestEval=%d myAlpha=%d myBeta=%d%n", getName(), "", Board.moveToString(bestMove), bestEval, myAlpha, myBeta);
                }

                if (move > bestMove) {
                    int eval = Board.getMoveEval(move);

                    if (eval > bestEval) { // Only overwrite when strictly better
                        bestMove = move;
                        bestEval = eval;

                        if (eval > myAlpha) {
                            myAlpha = eval;

                            if (myBeta <= myAlpha) {
                                if (DEBUG_AB || turn == DEBUG_TURN || DEBUG_BETA) {
                                    System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBeta cut-off from move %s%n", getName(), "", Board.moveToString(bestMove));
                                }
                                updateKillerMoves(depth, bestMove);
                                break;
                            }
                        }
                    }
                }

                if (DEBUG_AB || turn == DEBUG_TURN || DEBUG_BETA) {
                    //System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sAfter processing move: bestMove=%s bestEval=%d myAlpha=%d myBeta=%d%n", getName(), "", Board.moveToString(bestMove), bestEval, myAlpha, myBeta);
                }
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
            entry.turn = (byte) turn;
        } else if (tableMatch && entry.depthSearched == depth) {
            // Update results
            entry.bestMove = bestMove;
            entry.type = (bestEval > alpha ? (bestEval < beta ? TranspositionEntry.EXACT : TranspositionEntry.LOWER_BOUND) : TranspositionEntry.UPPER_BOUND);
        }

        return bestMove;
    }

    private int evaluateMove(int move, byte player, byte depth, int alpha, int beta, String type) {
        if (DEBUG_AB || turn == DEBUG_TURN) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sEvaluating%s move %s", getName(), "", type, Board.moveToString(move));

            if (depth > 1) {
                System.err.println();
            }
        }

        board.applyMove(move);
        evaluator.applyMove(move);

        int result = Board.setMoveEval(move, -Board.getMoveEval(negamax((byte) -player, (byte) (depth - 1), -beta, -alpha)));

        board.undoMove(move);
        evaluator.undoMove(move);

        if (DEBUG_AB || turn == DEBUG_TURN) {
            if (depth > 1) {
                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sGot back a score of %d%n", getName(), "", Board.getMoveEval(result));
            } else {
                System.err.printf(" => %d%n", Board.getMoveEval(result));
            }
        }

        return result;
    }

    private void updateKillerMoves(byte depth, int move) {
        if (DEBUG_AB || turn == DEBUG_TURN) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sUpdating killer moves with new move %s. Old: %s", getName(), "", Board.moveToString(move), killersToString());
        }

        int[] killers = killerMoves[depth];

        for (int i = 0; i < NUM_KILLERS; i++) {
            if (Board.equalMoves(killers[0], move)) {
                break; // return
            }

            // Make this move the current killer and shift others down
            int temp = killers[i];
            killers[i] = move;
            move = temp;
        }

        if (DEBUG_AB || turn == DEBUG_TURN) {
            System.err.printf(" New: %s%n", killersToString());
        }
    }

    private String killersToString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < killerMoves.length; i++) {
            sb.append(' ').append(Integer.toString(i)).append(": [");
            for (int j = 0; j < killerMoves[0].length; j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(Board.moveToString(killerMoves[i][j]));
            }
            sb.append(']');
        }

        return sb.toString();
    }

    private boolean matchesKiller(int move, byte depth) {
        for (int i = 0; i < NUM_KILLERS; i++) {
            if (Board.equalMoves(move, killerMoves[depth][i])) {
                return true;
            }
        }

        return false;
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
