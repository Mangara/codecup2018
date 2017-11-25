package codecup2018;

public class BitBoard implements Board {

    private static final long BOARD = 0b0000000100000011000001110000111100011111001111110111111111111111L;

    private long free = BOARD;
    private long myTiles = 0;
    private long oppTiles = 0;
    private long myValues = 0;
    private long oppValues = 0;
    private short myUsed = 0;
    private short oppUsed = 0;

    public BitBoard() {
    }

    public BitBoard(Board board) {
        int myValIndex = 0;
        int oppValIndex = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                byte val = board.get(a, b);
                int pos = getPos(a, b);

                if (val == FREE) {
                    // Nothing
                } else if (val == BLOCKED) {
                    free -= (1L << pos);
                } else if (val > 0) {
                    free -= (1L << pos);
                    myTiles |= (1L << pos);
                    myValues |= ((long) val) << (4 * myValIndex);
                    myUsed |= (1 << val);
                    myValIndex++;
                } else if (val < 0) {
                    free -= (1L << pos);
                    oppTiles |= (1L << pos);
                    oppValues |= ((long) -val) << (4 * oppValIndex);
                    oppUsed |= (1 << -val);
                    oppValIndex++;
                }
            }
        }
    }

    private int getPos(byte a, byte b) {
        return 8 * a + b;
    }

    @Override
    public byte get(byte a, byte b) {
        long posMask = 1L << getPos(a, b);

        if ((free & posMask) > 0) {
            return FREE;
        }

        if ((myTiles & posMask) > 0) {
            return getValue(posMask, myTiles, myValues);
        }

        if ((oppTiles & posMask) > 0) {
            return (byte) -getValue(posMask, oppTiles, oppValues);
        }

        return BLOCKED;
    }

    public boolean isFree(byte a, byte b) {
        return (free & (1L << getPos(a, b))) > 0;
    }

    @Override
    public int getFreeSpots() {
        return Long.bitCount(free);
    }

    private static final long EXCLUDE_ONE = ~0b1L;
    private static final long EXCLUDE_EIGHT = ~0b10000000L;
    private static final long EXCLUDE_NINE = ~0b100000000L;

    private long neighbours(int pos) {
        long posMask = 1L << pos;

        return (((posMask << 1) & EXCLUDE_NINE)
                | ((posMask >>> 1) & EXCLUDE_EIGHT)
                | ((posMask << 7) & EXCLUDE_EIGHT)
                | ((posMask >>> 7) & EXCLUDE_ONE)
                | (posMask << 8)
                | (posMask >>> 8))
                & BOARD;
    }

    @Override
    public int getFreeSpotsAround(byte a, byte b) {
        return Long.bitCount(free & neighbours(getPos(a, b)));
    }

    @Override
    public int getHoleValue(byte a, byte b) {
        long neighbours = neighbours(getPos(a, b));
        long myNeighbours = myTiles & neighbours;
        long oppNeighbours = oppTiles & neighbours;

        int total = 0;

        while (myNeighbours > 0) {
            long posMask = Long.lowestOneBit(myNeighbours);
            total += getValue(posMask, myTiles, myValues);
            myNeighbours ^= posMask;
        }

        while (oppNeighbours > 0) {
            long posMask = Long.lowestOneBit(oppNeighbours);
            total -= getValue(posMask, oppTiles, oppValues);
            oppNeighbours ^= posMask;
        }

        return total;
    }

    public void block(byte a, byte b) {
        free &= ~(1L << getPos(a, b));
    }

    @Override
    public void applyMove(byte[] move) {
        long posMask = (1L << getPos(move[0], move[1]));
        free &= ~posMask;

        byte value = move[2];

        if (value > 0) {
            myTiles |= posMask;
            myUsed |= (1 << value);
            myValues = insertValue(posMask, myTiles, myValues, value);
        } else if (value < 0) {
            oppTiles |= posMask;
            oppUsed |= (1 << -value);
            oppValues = insertValue(posMask, oppTiles, oppValues, -value);
        }
    }

    @Override
    public void undoMove(byte[] move) {
        long posMask = (1L << getPos(move[0], move[1]));
        free |= posMask;

        byte value = move[2];

        if (value > 0) {
            myTiles &= ~posMask;
            myUsed &= ~(1 << value);
            myValues = removeValue(posMask, myTiles, myValues);
        } else if (value < 0) {
            oppTiles &= ~posMask;
            oppUsed &= ~(1 << -value);
            oppValues = removeValue(posMask, oppTiles, oppValues);
        }
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
        return (myUsed & (1 << value)) > 0;
    }

    @Override
    public boolean hasOppUsed(byte value) {
        return (oppUsed & (1 << value)) > 0;
    }

    @Override
    public boolean isGameOver() {
        return getFreeSpots() == 1;
    }
}