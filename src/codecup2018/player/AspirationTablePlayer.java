package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.Util;
import codecup2018.data.BitBoard;
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

    private final int depth;

    private final static int TABLE_SIZE_POWER = 20;
    private final static int TABLE_SIZE = 1 << TABLE_SIZE_POWER;
    private final static int TABLE_KEY_MASK = TABLE_SIZE - 1;
    private final TranspositionEntry[] transpositionTable = new TranspositionEntry[TABLE_SIZE];

    private int prevScore = 0;
    private byte turn = 0;

    public AspirationTablePlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
        super(name, evaluator, generator);
        this.depth = depth;
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        prevScore = 0;
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

        System.err.println("Stats:");
        System.err.println(" New positions stored: " + store);
        System.err.println(" Old positions overwritten: " + overwrite);
        System.err.println(" Same positions same turn: " + samePosSameTurn);
        System.err.println(" Same positions previous turn: " + samePosPrevTurn);
        System.err.println(" Different positions same turn: " + diffPosSameTurn);
        System.err.println(" Different positions previous turn: " + diffPosPrevTurn);

        return move;
    }

    private byte[] topLevelSearch(int alpha, int beta) {
        if (DEBUG_AB || DEBUG_FINAL_VALUE) {
            System.err.printf("Searching [%d, %d]%n", alpha, beta);
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
            int value = -negamax(-1, depth, -beta, -Math.max(alpha, bestValue));
            board.undoMove(move);
            evaluator.undoMove(move);

            if (DEBUG_AB) {
                System.err.println(getName() + ": Value of my move " + Arrays.toString(move) + " is " + value);
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

    private int store = 0;
    private int overwrite = 0;
    private int samePosPrevTurn = 0;
    private int samePosSameTurn = 0;
    private int diffPosPrevTurn = 0;
    private int diffPosSameTurn = 0;

    private int negamax(int player, int depth, int alpha, int beta) {
        if (DEBUG_AB) {
            System.err.printf("%s:  Running negamax with %d turns left, interval=[%d, %d] and board state:%n", getName(), depth, alpha, beta);
            Util.print(board);
        }

        ////DEBUG
        checkTable();
        //*/

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

    private class TranspositionEntry {

        static final byte EXACT = 3, LOWER_BOUND = 4, UPPER_BOUND = 5;

        long hash; // Position hashcode for identification
        byte[] bestMove; // Best move found from this position
        byte depthSearched; // The depth this position was searched to
        int value; // The value returned for this position
        byte type; // Whether the value was exact (between alpha and beta) or an upper bound (= alpha) or a lower bound (= beta)
        byte turn; // In which turn this entry was stored
        
        ////DEBUG
        Board b;
    }
    
    void checkTable() {
        int key = board.getTranspositionTableKey() & TABLE_KEY_MASK;
        TranspositionEntry e = transpositionTable[key];

        if (e == null || e.turn < turn) {
            TranspositionEntry entry = new TranspositionEntry();
            entry.hash = board.getHash();
            entry.turn = turn;
            entry.b = new BitBoard(board);
            transpositionTable[key] = entry;

            if (e == null) {
                store++;
            } else {
                overwrite++;

                if (e.hash == entry.hash) {
                    if (e.b )
                    samePosPrevTurn++;
                } else {
                    diffPosPrevTurn++;
                }
            }
        } else {
            if (e.hash == board.getHash()) {
                samePosSameTurn++;
            } else {
                diffPosSameTurn++;
            }
        }
    }
}
