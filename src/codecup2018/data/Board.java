package codecup2018.data;

import codecup2018.Util;
import java.util.List;
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

    public abstract void applyMove(byte[] move);

    public abstract void undoMove(byte[] move);

    public abstract boolean haveIUsed(byte value);

    public abstract boolean hasOppUsed(byte value);

    // Helpful methods
    public abstract boolean isFree(byte pos);

    public abstract int getNFreeSpots();

    public abstract int getHoleValue(byte pos);

    public abstract int getFreeSpotsAround(byte pos);

    public abstract List<byte[]> getFreeSpots();

    public abstract boolean isGameOver();

    public abstract boolean isLegalMove(byte[] move);

    public abstract int getTranspositionTableKey();

    public abstract long getHash();

    // Utility methods
    public static byte getPos(byte a, byte b) {
        return (byte) (8 * a + b);
    }

    public static byte[] getCoordinates(byte pos) {
        return new byte[]{(byte) (pos / 8), (byte) (pos % 8)};
    }

    public static String posToString(byte pos) {
        return Character.toString((char) ('A' + pos / 8)) + Character.toString((char) ('1' + pos % 8));
    }
    
    public static String coordinatesToString(byte a, byte b) {
        return Character.toString((char) ('A' + a)) + Character.toString((char) ('1' + b));
    }

    public static byte[] parseMove(String move) {
        return new byte[]{(byte) (move.charAt(0) - 'A'), (byte) (move.charAt(1) - '1'), (byte) (Integer.parseInt(move.substring(3)))};
    }

    public static byte getPos(String location) {
        return (byte) (8 * (location.charAt(0) - 'A') + location.charAt(1) - '1');
    }

    public static byte[] getCoordinates(String location) {
        return new byte[]{(byte) (location.charAt(0) - 'A'), (byte) (location.charAt(1) - '1')};
    }

    public static void print(Board board) {
        for (byte h = 0; h < 8; h++) {
            System.err.printf("%" + 2 * (7 - h) + "s", "");
            for (byte i = 0; i <= h; i++) {
                if (i > 0) {
                    System.err.print(' ');
                }
                byte value = board.get(getPos((byte) (h - i), i));
                System.err.print(value == Board.BLOCKED ? "  X" : String.format("%3d", value));
            }
            System.err.printf("%" + 2 * (7 - h) + "s%n", "");
        }
        System.err.println("nFree: " + board.getNFreeSpots());
    }

    public static void main(String[] args) {
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                System.out.print(pos + " ");
            }
        }
        System.out.println();
        for (int a = 0; a < 8; a++) {
            for (int b = 0; b < 8 - a; b++) {
                System.out.print(getPos((byte) a, (byte) b) + " ");
            }
        }
        System.out.println();
    }
}
