package codecup2018.data;

import java.util.List;
import java.util.Random;

public abstract class Board {

    public static final byte BLOCKED = 120;
    public static final byte FREE = 0;

    protected static final int[] KEY_POSITION_NUMBERS = new int[64 * 32];
    protected static final long[] HASH_POSITION_NUMBERS = new long[64 * 32];

    static {
        Random rand = new Random(611382272);

        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 32; j++) {
                KEY_POSITION_NUMBERS[32 * i + j] = rand.nextInt();
                HASH_POSITION_NUMBERS[32 * i + j] = rand.nextLong();
            }
        }
    }

    // Core methods
    public abstract byte get(byte a, byte b);

    public abstract void block(byte a, byte b);

    public abstract void applyMove(byte[] move);

    public abstract void undoMove(byte[] move);

    public abstract boolean haveIUsed(byte value);

    public abstract boolean hasOppUsed(byte value);

    // Helpful methods
    public abstract boolean isFree(byte a, byte b);

    public abstract int getNFreeSpots();

    public abstract int getHoleValue(byte a, byte b);

    public abstract int getFreeSpotsAround(byte a, byte b);

    public abstract List<byte[]> getFreeSpots();

    public abstract boolean isGameOver();

    public abstract int getTranspositionTableKey();

    public abstract long getHash();
}
