package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;
import java.util.List;

public class AspirationTablePlayer extends StandardPlayer {

    public static boolean DEBUG_FINAL_VALUE = true;
    private static final boolean DEBUG_AB = false;

    private static final byte[] FAIL_HIGH = new byte[0];
    private static final byte[] FAIL_LOW = new byte[0];

    public static int WINDOW_SIZE = 10001;

    private final byte maxDepth;

    private final static int TABLE_SIZE_POWER = 20;
    private final static int TABLE_SIZE = 1 << TABLE_SIZE_POWER;
    private final static int TABLE_KEY_MASK = TABLE_SIZE - 1;
    private final TranspositionEntry[] transpositionTable = new TranspositionEntry[TABLE_SIZE];

    private int prevScore = 0;
    private byte turn = 0;

    public AspirationTablePlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
        super(name, evaluator, generator);
        this.maxDepth = (byte) depth;
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        prevScore = evaluator.evaluate(board);
        turn = 0;
        Arrays.fill(transpositionTable, null);
    }

    @Override
    protected byte[] selectMove() {
        byte[] move = topLevelSearch(prevScore - WINDOW_SIZE, prevScore + WINDOW_SIZE);

        if (move == FAIL_HIGH) {
            move = topLevelSearch(prevScore + WINDOW_SIZE - 1, Integer.MAX_VALUE);
        } else if (move == FAIL_LOW) {
            move = topLevelSearch(Integer.MIN_VALUE + 1, prevScore - WINDOW_SIZE + 1);
        }

        if (move == FAIL_HIGH || move == FAIL_LOW) {
            System.err.println("Search is unstable: failed " + (move == FAIL_HIGH ? "high" : "low") + " after first failing " + (move == FAIL_HIGH ? "low" : "high"));
            return topLevelSearch(Integer.MIN_VALUE + 1, Integer.MAX_VALUE);
        }

        turn++;

        return move;
    }

    private byte[] topLevelSearch(int alpha, int beta) {
        if (DEBUG_AB || DEBUG_FINAL_VALUE) {
            System.err.printf("Searching [%d, %d]%n", alpha, beta);
        }

        // Top-level alpha-beta
        int bestValue = Integer.MIN_VALUE + 1;
        byte[] bestMove = null;
        int myAlpha = alpha;

        List<byte[]> moves = generator.generateMoves(board, true);

        for (byte[] move : moves) {
            if (DEBUG_AB) {
                System.err.println(getName() + ": Evaluating my move " + Arrays.toString(move));
            }

            board.applyMove(move);
            evaluator.applyMove(move);
            int value = -negamax((byte) -1, maxDepth, -beta, -myAlpha);
            board.undoMove(move);
            evaluator.undoMove(move);

            if (DEBUG_AB) {
                System.err.println(getName() + ": Value of my move " + Arrays.toString(move) + " is " + value);
            }

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;

                if (value > myAlpha) {
                    myAlpha = value;

                    if (myAlpha >= beta) {
                        if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                            System.err.println("FAIL HIGH");
                        }

                        return FAIL_HIGH;
                    }
                }
            }
        }

        if (bestValue <= alpha) {
            if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                System.err.println("FAIL LOW");
            }

            return FAIL_LOW;
        } else {
            if (DEBUG_AB || DEBUG_FINAL_VALUE) {
                System.err.println("Final: " + bestValue);
            }

            prevScore = bestValue;
            return bestMove;
        }
    }

    private int negamax(byte player, byte depth, int alpha, int beta) {
        if (depth == 0 || board.isGameOver()) {
            return player * evaluator.evaluate(board);
        }
        
        if (DEBUG_AB) {
            System.err.printf("%s:%" + (2 * (maxDepth - depth + 1)) + "sRunning negamax with %d plies left, interval=[%d, %d] and board state:%n", getName(), "", depth, alpha, beta);
            Board.print(board);
        }

        int bestValue = Integer.MIN_VALUE + 1;
        byte[] bestMove = null;
        int myAlpha = alpha;

        // Check the transposition table
        TranspositionEntry entry = transpositionTable[board.getTranspositionTableKey() & TABLE_KEY_MASK];
        boolean tableMatch = entry != null && entry.hash == board.getHash() && board.isLegalMove(entry.bestMove);
        
        if (tableMatch) {
            // Return the stored evaluation if it matches what we're looking for
            if (entry.depthSearched >= depth && (entry.type == TranspositionEntry.EXACT
                    || (entry.type == TranspositionEntry.UPPER_BOUND && entry.value <= alpha)
                    || (entry.type == TranspositionEntry.LOWER_BOUND && entry.value >= beta))) {
                if (DEBUG_AB) {
                    System.err.printf("%s:%" + (2 * (maxDepth - depth + 1)) + "s%s transposition table result: %d%n", getName(), "", (entry.type == TranspositionEntry.EXACT ? "Exact" : (entry.type == TranspositionEntry.LOWER_BOUND ? "Lower bound" : "Upper bound")), entry.value);
                }
                return entry.value;
            }

            // Try the stored best move first
            /*
            Out of an abundance of caution; if entry.bestMove was changed due to
            a table cell collision, undoing the new move could really mess up the search.
             */
            bestMove = entry.bestMove;

            if (DEBUG_AB) {
                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sEvaluating move %s%n", getName(), "", Arrays.toString(bestMove));
            }

            board.applyMove(bestMove);
            evaluator.applyMove(bestMove);
            bestValue = -negamax((byte) -player, (byte) (depth - 1), -beta, -myAlpha);
            board.undoMove(bestMove);
            evaluator.undoMove(bestMove);

            if (DEBUG_AB) {
                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sGot back a score of %d%n", getName(), "", bestValue);
            }

            if (bestValue > myAlpha) {
                myAlpha = bestValue;
            }
        }

        if (beta > myAlpha) { // First move did not cause a cutoff
            List<byte[]> moves = generator.generateMoves(board, player > 0);

            for (byte[] move : moves) {
                if (tableMatch && Arrays.equals(move, entry.bestMove)) {
                    continue; // We already tried this one
                }
                
                if (DEBUG_AB) {
                    System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sEvaluating move %s%n", getName(), "", Arrays.toString(move));
                }

                board.applyMove(move);
                evaluator.applyMove(move);
                int value = -negamax((byte) -player, (byte) (depth - 1), -beta, -myAlpha);
                board.undoMove(move);
                evaluator.undoMove(move);

                if (DEBUG_AB) {
                    System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sGot back a score of %d%n", getName(), "", value);
                }

                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;

                    if (value > myAlpha) {
                        myAlpha = value;

                        if (beta <= myAlpha) {
                            if (DEBUG_AB) {
                                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBeta-cutoff%n", getName(), "", value);
                            }

                            break;
                        }
                    }
                }
            }
        } else {
            if (DEBUG_AB) {
                System.err.printf("%s:%" + (2 * (maxDepth - depth + 1) + 1) + "sBeta-cutoff%n", getName(), "", bestValue);
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
            entry.value = bestValue;
            entry.type = (bestValue > alpha ? (bestValue < beta ? TranspositionEntry.EXACT : TranspositionEntry.LOWER_BOUND) : TranspositionEntry.UPPER_BOUND);
            entry.turn = turn;
        }

        return bestValue;
    }

    private class TranspositionEntry {

        static final byte EXACT = 3, LOWER_BOUND = 4, UPPER_BOUND = 5;

        long hash; // Position hashcode for identification
        byte[] bestMove; // Best move found from this position
        byte depthSearched; // The depth this position was searched to
        int value; // The value returned for this position
        byte type; // Whether the value was exact (between alpha and beta) or an upper bound (<= alpha) or a lower bound (>= beta)
        byte turn; // In which turn this entry was stored
    }
}
