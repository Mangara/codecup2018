package codecup2018.data;

import java.util.Random;

public abstract class Board {

    public static final byte BLOCKED = 16;
    public static final byte FREE = 0;

    protected static final int[] KEY_POSITION_NUMBERS = new int[64 * 32];
    protected static final long[] HASH_POSITION_NUMBERS = new long[64 * 32];

    static {
        Random rand = new Random(611382272);

        for (int i = 0; i < 64 * 32; i++) {
            KEY_POSITION_NUMBERS[i] = rand.nextInt();
            HASH_POSITION_NUMBERS[i] = rand.nextLong();
        }
    }

    /*
    The board is a 8 by 8 array, laid out linearly.
    A1 corresponds to (0, 0) = 0 * 8 + 0 = 0.
    F2 corresponds to (5, 1) = 5 * 8 + 1 = 41.
     */
    // Core methods
    public abstract byte get(byte pos);

    public abstract void block(byte pos);

    public abstract void applyMove(int move);

    public abstract void undoMove(int move);

    public abstract boolean haveIUsed(byte value);

    public abstract boolean hasOppUsed(byte value);

    // Helpful methods
    public abstract boolean isFree(byte pos);

    public abstract int getNFreeSpots();

    public abstract int getHoleValue(byte pos);

    public abstract int getFreeSpotsAround(byte pos);

    public abstract byte[] getFreeSpots();

    public abstract boolean isGameOver();

    public abstract boolean isGameInEndGame();

    public abstract boolean isLegalMove(int move);

    public abstract int getTranspositionTableKey();

    public abstract long getHash();

    // Move-related utility methods
    public static final int MOVE_POS_MASK = (1 << 6) - 1;           // 00000000000000000000000000111111
    public static final int MOVE_VAL_MASK = ((1 << 5) - 1) << 6;    // 00000000000000000000011111000000
    public static final int MOVE_EVAL_MASK = ((1 << 21) - 1) << 11; // 11111111111111111111100000000000
    public static final int MIN_EVAL = -750001;
    public static final int MAX_EVAL = 750001;
    public static final int MIN_EVAL_MOVE = buildMove((byte) 0, (byte) 0, MIN_EVAL);
    public static final int MAX_EVAL_MOVE = buildMove((byte) 0, (byte) 0, MAX_EVAL);

    public static final byte getMovePos(int move) {
        return (byte) (move & MOVE_POS_MASK);
    }

    public static final byte getMoveVal(int move) {
        return (byte) (((move & MOVE_VAL_MASK) >> 6) - 15);
    }

    public static final int getMoveEval(int move) {
        return move >> 11;
    }

    public static final int setMovePos(int move, byte pos) {
        return (move & ~MOVE_POS_MASK) | pos;
    }

    public static final int setMoveVal(int move, byte val) {
        return (move & ~MOVE_VAL_MASK) | (val + 15 << 6);
    }

    public static final int setMoveEval(int move, int eval) {
        return (move & ~MOVE_EVAL_MASK) | (eval << 11);
    }

    public static final int negateEval(int move) {
        return (move & ~MOVE_EVAL_MASK) | ((-(move >> 11) << 11) & MOVE_EVAL_MASK);
    }

    public static final int buildMove(byte pos, byte val, int eval) {
        return (eval << 11) | (val + 15 << 6) | pos;
    }

    // Position-related utility methods
    public static final byte getPos(byte a, byte b) {
        return (byte) (8 * a + b);
    }

    public static final byte[] getCoordinates(byte pos) {
        return new byte[]{(byte) (pos / 8), (byte) (pos % 8)};
    }

    // String conversions
    public static final byte parsePos(String location) {
        return (byte) (8 * (location.charAt(0) - 'A') + location.charAt(1) - '1');
    }

    public static final String posToString(byte pos) {
        return Character.toString((char) ('A' + pos / 8)) + Character.toString((char) ('1' + pos % 8));
    }

    public static final String coordinatesToString(byte a, byte b) {
        return Character.toString((char) ('A' + a)) + Character.toString((char) ('1' + b));
    }

    public static final int parseMove(String move) {
        return buildMove((byte) (8 * (move.charAt(0) - 'A') + move.charAt(1) - '1'), (byte) Integer.parseInt(move.substring(3)), 0);
    }

    public static final String moveToString(int move) {
        return posToString(getMovePos(move)) + '=' + getMoveVal(move);
    }

    public static final void print(Board board) {
        for (byte h = 0; h < 8; h++) {
            System.err.printf("%" + (2 * (7 - h) + 1) + "s", "");
            for (byte i = 0; i <= h; i++) {
                if (i > 0) {
                    System.err.print(' ');
                }
                byte value = board.get(getPos((byte) (h - i), i));
                System.err.print(value == Board.BLOCKED ? "  X" : String.format("%3d", value));
            }
            System.err.printf("%" + (2 * (7 - h) + 1) + "s%n", "");
        }
        System.err.print("nFree: " + board.getNFreeSpots());

        if (board instanceof BitBoard) {
            System.err.println(" freeConnections: " + ((BitBoard) board).freeEdgeCount);
        } else {
            System.err.println();
        }
    }
}
