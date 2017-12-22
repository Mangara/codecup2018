package codecup2018.data;

public class BitBoard extends Board {

    private static final long BOARD = 0b0000000100000011000001110000111100011111001111110111111111111111L;
    private static final long EXCLUDE_ONE = ~0b1L;
    private static final long EXCLUDE_EIGHT = ~0b10000000L;
    private static final long EXCLUDE_NINE = ~0b100000000L;
    private static final long[] NEIGHBOURS = new long[64];

    static {
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                long posMask = posMask(pos);

                NEIGHBOURS[pos]
                        = (((posMask << 1) & EXCLUDE_NINE)
                        | ((posMask >>> 1) & EXCLUDE_EIGHT)
                        | ((posMask << 7) & EXCLUDE_EIGHT)
                        | ((posMask >>> 7) & EXCLUDE_ONE)
                        | (posMask << 8)
                        | (posMask >>> 8))
                        & BOARD;
            }
        }
    }

    private long free = 0;
    private long myTiles = 0;
    private long oppTiles = 0;
    private long myValues = 0;
    private long oppValues = 0;
    private short myUsed = 0;
    private short oppUsed = 0;
    public int freeEdgeCount = 0; // The number of edges in the subgraph induced by the free tiles

    // Transposition table cached values
    private int key;
    private long hash;

    public BitBoard() {
        free = BOARD;
        freeEdgeCount = (3 * 2 + 18 * 4 + 15 * 6)/2; // Corners, edge, center
        initializeTranspositionTableValues();
    }

    public BitBoard(Board board) {
        int myValIndex = 0;
        int oppValIndex = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                byte val = board.get(pos);
                long posMask = posMask(pos);

                if (val == FREE) {
                    free |= posMask;
                    freeEdgeCount += getFreeSpotsAround(pos);
                } else if (val == BLOCKED) {
                    // Nothing
                } else if (val > 0) {
                    myTiles |= posMask;
                    myValues |= ((long) val) << (4 * myValIndex);
                    myUsed |= (1 << val);
                    myValIndex++;
                } else if (val < 0) {
                    oppTiles |= posMask;
                    oppValues |= ((long) -val) << (4 * oppValIndex);
                    oppUsed |= (1 << -val);
                    oppValIndex++;
                }
            }
        }

        initializeTranspositionTableValues();
    }

    private static long posMask(byte pos) {
        return 1L << pos;
    }

    @Override
    public byte get(byte pos) {
        long posMask = posMask(pos);

        if ((free & posMask) != 0) {
            return FREE;
        }

        if ((myTiles & posMask) != 0) {
            return getValue(posMask, myTiles, myValues);
        }

        if ((oppTiles & posMask) != 0) {
            return (byte) -getValue(posMask, oppTiles, oppValues);
        }

        return BLOCKED;
    }

    @Override
    public boolean isFree(byte pos) {
        return (free & posMask(pos)) != 0;
    }

    @Override
    public int getNFreeSpots() {
        return Long.bitCount(free);
    }

    @Override
    public byte[] getFreeSpots() {
        byte[] result = new byte[getNFreeSpots()];
        long tempFree = free;

        for (int i = 0; tempFree != 0; i++) {
            int pos = Long.numberOfTrailingZeros(tempFree);
            result[i] = (byte) pos;
            tempFree &= (tempFree - 1); // Clear lowest bit
        }

        return result;
    }

    @Override
    public int getFreeSpotsAround(byte pos) {
        return Long.bitCount(free & NEIGHBOURS[pos]);
    }

    @Override
    public int getHoleValue(byte pos) {
        long neighbours = NEIGHBOURS[pos];
        long myNeighbours = myTiles & neighbours;
        long oppNeighbours = oppTiles & neighbours;

        int total = 0;

        while (myNeighbours != 0) {
            long posMask = Long.lowestOneBit(myNeighbours);
            total += getValue(posMask, myTiles, myValues);
            myNeighbours ^= posMask;
        }

        while (oppNeighbours != 0) {
            long posMask = Long.lowestOneBit(oppNeighbours);
            total -= getValue(posMask, oppTiles, oppValues);
            oppNeighbours ^= posMask;
        }

        return total;
    }

    @Override
    public void block(byte pos) {
        free &= ~posMask(pos);
        freeEdgeCount -= getFreeSpotsAround(pos);
        
        // Update hash values
        int index = 32 * pos + FREE + 15;
        int newIndex = index - FREE + BLOCKED;
        key ^= KEY_POSITION_NUMBERS[index] ^ KEY_POSITION_NUMBERS[newIndex];
        hash ^= HASH_POSITION_NUMBERS[index] ^ HASH_POSITION_NUMBERS[newIndex];
    }

    @Override
    public void applyMove(int move) {
        byte pos = getMovePos(move);
        long posMask = posMask(pos);
        byte value = getMoveVal(move);

        /*///DEBUG
        System.err.println("Board before move " + moveToString(move));
        Board.print(this);
        //*/
        
        free &= ~posMask;
        freeEdgeCount -= getFreeSpotsAround(pos);

        if (value > 0) {
            myTiles |= posMask;
            myUsed |= (1 << value);
            myValues = insertValue(posMask, myTiles, myValues, value);
        } else if (value < 0) {
            oppTiles |= posMask;
            oppUsed |= (1 << -value);
            oppValues = insertValue(posMask, oppTiles, oppValues, -value);
        }
        
        // Update hash values
        int index = 32 * pos + FREE + 15;
        int newIndex = index - FREE + value;
        key ^= KEY_POSITION_NUMBERS[index] ^ KEY_POSITION_NUMBERS[newIndex];
        hash ^= HASH_POSITION_NUMBERS[index] ^ HASH_POSITION_NUMBERS[newIndex];
        
        /*///DEBUG
        System.err.println("Board after move " + moveToString(move));
        Board.print(this);
        //*/
    }

    @Override
    public void undoMove(int move) {
        byte pos = getMovePos(move);
        long posMask = posMask(pos);
        byte value = getMoveVal(move);
        
        free |= posMask;
        freeEdgeCount += getFreeSpotsAround(pos);

        if (value > 0) {
            myTiles &= ~posMask;
            myUsed &= ~(1 << value);
            myValues = removeValue(posMask, myTiles, myValues);
        } else if (value < 0) {
            oppTiles &= ~posMask;
            oppUsed &= ~(1 << -value);
            oppValues = removeValue(posMask, oppTiles, oppValues);
        }
        
        // Update hash values
        int index = 32 * pos + value + 15;
        int newIndex = index - value + FREE;
        key ^= KEY_POSITION_NUMBERS[index] ^ KEY_POSITION_NUMBERS[newIndex];
        hash ^= HASH_POSITION_NUMBERS[index] ^ HASH_POSITION_NUMBERS[newIndex];
    }

    private int getValueIndex(long posMask, long tiles) {
        return 4 * Long.bitCount(tiles & (posMask - 1));
    }

    private byte getValue(long posMask, long tiles, long values) {
        return (byte) ((values >>> getValueIndex(posMask, tiles)) & 0b1111);
    }

    private long insertValue(long posMask, long tiles, long values, int value) {
        int index = getValueIndex(posMask, tiles);
        return (values & ((1L << index) - 1)) // values that should be kept
                | ((values << 4) & ~((1L << (index + 4)) - 1)) // values that should be shifted
                | ((long) value) << index; // new value
    }

    private long removeValue(long posMask, long tiles, long values) {
        int index = getValueIndex(posMask, tiles);
        long preIndexMask = (1L << index) - 1;
        return (values & preIndexMask) // values that should be kept
                | ((values >> 4) & ~preIndexMask); // values that should be shifted
    }

    @Override
    public boolean haveIUsed(byte value) {
        return (myUsed & (1 << value)) != 0;
    }

    @Override
    public boolean hasOppUsed(byte value) {
        return (oppUsed & (1 << value)) != 0;
    }

    @Override
    public boolean isGameOver() {
        return getNFreeSpots() == 1;
    }

    @Override
    public boolean isGameInEndGame() {
        return freeEdgeCount == 0;
    }

    private void initializeTranspositionTableValues() {
        key = 0;
        hash = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                int index = 32 * pos + get(pos) + 15;
                key ^= KEY_POSITION_NUMBERS[index];
                hash ^= HASH_POSITION_NUMBERS[index];
            }
        }
    }

    @Override
    public int getTranspositionTableKey() {
        return key;
    }

    @Override
    public long getHash() {
        return hash;
    }
}
