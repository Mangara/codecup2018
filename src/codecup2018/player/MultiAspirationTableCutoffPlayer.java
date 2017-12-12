package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.MedianFree;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;

public class MultiAspirationTableCutoffPlayer extends StandardPlayer {

    public static boolean DEBUG_FINAL_VALUE = false;
    private static final boolean DEBUG_AB = false;
    private static final int DEBUG_TURN = -1;

    private final static int INITIAL_WINDOW_SIZE = 5000;
    private final static double WINDOW_FACTOR = 1.75;

    private final byte maxDepth;
    private final Evaluator endgameEvaluator = new MedianFree();
    private final Player endgamePlayer = new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves());

    private final static int TABLE_SIZE_POWER = 20;
    private final static int TABLE_SIZE = 1 << TABLE_SIZE_POWER;
    private final static int TABLE_KEY_MASK = TABLE_SIZE - 1;
    private final TranspositionEntry[] transpositionTable = new TranspositionEntry[TABLE_SIZE];

    private int prevScore = 0;

    public MultiAspirationTableCutoffPlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
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
            if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                System.err.printf("Searching [%d, %d]", alpha, beta);
            }

            move = negamax((byte) 1, (byte) (maxDepth + 1), alpha, beta);
            eval = Board.getMoveEval(move);

            if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                System.err.printf(" => %d%n", eval);
            }

            if (eval <= alpha) { // Fail low
                failLow = true;

                beta = eval + 1;
                alpha = beta - window;
            } else if (eval >= beta) { // Fail high
                failHigh = true;

                alpha = eval - 1;
                beta = alpha + window;
            } else {
                break;
            }

            if (failLow && failHigh) {
                System.err.println("Search is unstable");
                move = negamax((byte) 1, (byte) (maxDepth + 1), Board.MIN_EVAL, Board.MAX_EVAL);
                eval = Board.getMoveEval(move);
                break;
            }

            window *= WINDOW_FACTOR;
        }

        if (DEBUG_AB || DEBUG_FINAL_VALUE) {
            System.err.println("Turn " + turn + " final: " + eval);
        }

        prevScore = eval;
        turn++;

        return move;
    }

    private int negamax(byte player, byte depth, int alpha, int beta) {
        if (depth == 0 || board.isGameOver()) {
            return Board.buildMove((byte) 0, (byte) 0, player * evaluator.evaluate(board));
        }

        if (board.isGameInEndGame()) {
            return Board.buildMove((byte) 0, (byte) 0, player * endgameEvaluator.evaluate(board));
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
            // Return the stored evaluation if it matches what we're looking for
            int entryEval = Board.getMoveEval(entry.bestMove);

            if (entry.depthSearched >= depth) {
                switch (entry.type) {
                    case TranspositionEntry.EXACT:
                        if (DEBUG_AB || turn == DEBUG_TURN) {
                            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1)) + "sExact transposition table result: %d%n", getName(), "", entryEval);
                        }
                        return entry.bestMove;
                    case TranspositionEntry.LOWER_BOUND:
                        if (entryEval >= beta) {
                            if (DEBUG_AB || turn == DEBUG_TURN) {
                                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1)) + "sLower bound transposition table result: %d%n", getName(), "", entryEval);
                            }
                            return entry.bestMove;
                        }
                        myAlpha = Math.max(myAlpha, entryEval);
                        break;
                    case TranspositionEntry.UPPER_BOUND:
                        if (entryEval <= alpha) {
                            if (DEBUG_AB || turn == DEBUG_TURN) {
                                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1)) + "sUpper bound transposition table result: %d%n", getName(), "", entryEval);
                            }
                            return entry.bestMove;
                        }
                        myBeta = Math.min(myBeta, entryEval);
                        break;
                }
            }

            // Try the stored best move first
            bestMove = evaluateMove(entry.bestMove, player, depth, myAlpha, myBeta);

            /*///PROFILING
            int move = entry.bestMove;
            board.applyMove(move);
            evaluator.applyMove(move);

            bestMove = Board.setMoveEval(move, -Board.getMoveEval(negamax((byte) -player, (byte) (depth - 1), -beta, -myAlpha)));

            board.undoMove(move);
            evaluator.undoMove(move);
            //*/
            bestEval = Board.getMoveEval(bestMove);

            if (bestEval > myAlpha) {
                myAlpha = bestEval;
            }
        }

        if (myAlpha < myBeta) { // No cut-off yet
            int[] moves = generator.generateMoves(board, player > 0);

            for (int move : moves) {
                if (tableMatch && Board.getMovePos(move) == Board.getMovePos(entry.bestMove) && Board.getMoveVal(move) == Board.getMoveVal(entry.bestMove)) {
                    continue; // We already tried this one
                }

                move = evaluateMove(move, player, depth, myAlpha, myBeta);

                /*///PROFILING
                board.applyMove(move);
                evaluator.applyMove(move);

                move = Board.setMoveEval(move, -Board.getMoveEval(negamax((byte) -player, (byte) (depth - 1), -beta, -myAlpha)));

                board.undoMove(move);
                evaluator.undoMove(move);
                //*/
                if (move > bestMove) {
                    int eval = Board.getMoveEval(move);

                    if (eval > bestEval) { // Only overwrite when strictly better
                        bestMove = move;
                        bestEval = eval;

                        if (eval > myAlpha) {
                            myAlpha = eval;

                            if (myBeta <= myAlpha) {
                                if (DEBUG_AB || turn == DEBUG_TURN) {
                                    System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBeta cut-off%n", getName(), "");
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            if (DEBUG_AB || turn == DEBUG_TURN) {
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
            entry.turn = (byte) turn;
        }

        return bestMove;
    }

    private int evaluateMove(int move, byte player, byte depth, int alpha, int beta) {
        if (DEBUG_AB || turn == DEBUG_TURN) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sEvaluating move %s%n", getName(), "", Board.moveToString(move));
        }

        board.applyMove(move);
        evaluator.applyMove(move);

        int result = Board.setMoveEval(move, -Board.getMoveEval(negamax((byte) -player, (byte) (depth - 1), -beta, -alpha)));

        board.undoMove(move);
        evaluator.undoMove(move);

        if (DEBUG_AB || turn == DEBUG_TURN) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sGot back a score of %d%n", getName(), "", Board.getMoveEval(move));
        }

        return result;
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
