package codecup2018.data;

public class CachingBoard extends Board {

    private final byte[] values = new byte[64]; // The values on the board
    private final byte[] holeValues = new byte[73]; // The sum of neighbouring tiles
    private final byte[] freeNeighbours = new byte[73]; // The number of free neighbouring spots

    // Bit-vectors encoding the values used by each player
    private short myUsed = 0;
    private short oppUsed = 0;

    private int freeSpots; // The total number of free spots
    private int freeEdgeCount; // The number of edges in the subgraph induced by the free spots

    // Transposition table cached values
    private int key;
    private long hash;

    public CachingBoard() {
        freeSpots = 36;
        freeEdgeCount = (3 * 2 + 18 * 4 + 15 * 6) / 2; // Corners, edge, center

        initializeFreeNeighbours();
        initializeTranspositionTableValues();
    }

    public CachingBoard(Board board) {
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                byte val = board.get(pos);
                values[pos] = val;

                if (val == FREE || val == BLOCKED) {
                    continue;
                }
                
                if (val > 0) {
                    myUsed |= (1 << val);
                    updateCacheValue(holeValues, pos, val);
                } else if (val < 0) {
                    oppUsed |= (1 << -val);
                    updateCacheValue(holeValues, pos, val);
                }
            }
        }

        initializeFreeNeighbours();
        initializeTranspositionTableValues();

        freeSpots = 0;
        freeEdgeCount = 0;
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                if (values[pos] == FREE) {
                    freeSpots++;
                    freeEdgeCount += freeNeighbours[pos + 8];
                }
            }
        }
        freeEdgeCount /= 2;
    }

    @Override
    public byte get(byte pos) {
        return values[pos];
    }

    private void set(byte pos, byte val) {
        byte oldVal = values[pos];
        values[pos] = val;

        // Update hash values
        int base = 32 * pos + 15;
        int index = base + oldVal;
        int newIndex = base + val;
        key ^= KEY_POSITION_NUMBERS[index] ^ KEY_POSITION_NUMBERS[newIndex];
        hash ^= HASH_POSITION_NUMBERS[index] ^ HASH_POSITION_NUMBERS[newIndex];
    }

    private void updateCacheValue(byte[] cache, byte pos, int val) {
        cache[pos] += val;
        if (pos != 8) {
            cache[pos + 7] += val;
        }
        if (pos != 7) {
            cache[pos + 1] += val;
            cache[pos + 9] += val;
        }
        if (pos != 0) {
            cache[pos + 15] += val;
        }
        cache[pos + 16] += val;
    }

    @Override
    public void block(byte pos) {
        set(pos, BLOCKED);
        freeSpots--;
        freeEdgeCount -= freeNeighbours[pos + 8];
        
        updateCacheValue(freeNeighbours, pos, -1);
    }

    @Override
    public void applyMove(int move) {
        byte pos = Board.getMovePos(move);
        byte val = Board.getMoveVal(move);

        set(pos, val);
        freeSpots--;
        freeEdgeCount -= freeNeighbours[pos + 8];
        
        if (val > 0) {
            myUsed |= (1 << val);
        } else {
            oppUsed |= (1 << -val);
        }
        
        updateCacheValue(holeValues, pos, val);
        updateCacheValue(freeNeighbours, pos, -1);
    }

    @Override
    public void undoMove(int move) {
        byte pos = Board.getMovePos(move);
        byte val = Board.getMoveVal(move);

        set(pos, FREE);
        freeSpots++;
        freeEdgeCount += freeNeighbours[pos + 8];
        
        if (val > 0) {
            myUsed ^= (1 << val);
        } else {
            oppUsed ^= (1 << -val);
        }
        
        updateCacheValue(holeValues, pos, -val);
        updateCacheValue(freeNeighbours, pos, +1);
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
    public boolean isFree(byte pos) {
        return values[pos] == FREE;
    }

    @Override
    public int getNFreeSpots() {
        return freeSpots;
    }

    @Override
    public int getHoleValue(byte pos) {
        return holeValues[pos + 8];
    }

    @Override
    public int getFreeSpotsAround(byte pos) {
        return freeNeighbours[pos + 8];
    }

    @Override
    public byte[] getFreeSpots() {
        byte[] free = new byte[freeSpots];
        int i = 0;

        for (int a = 0; a < 8; a++) {
            for (int pos = 8 * a; pos < 7 * a + 8; pos++) {
                if (values[pos] == FREE) {
                    free[i] = (byte) pos;
                    i++;
                }
            }
        }

        return free;
    }

    @Override
    public boolean isGameOver() {
        return freeSpots == 1;
    }

    @Override
    public boolean isGameInEndGame() {
        return freeEdgeCount == 0;
    }

    @Override
    public int getTranspositionTableKey() {
        return key;
    }

    @Override
    public long getHash() {
        return hash;
    }

    private void initializeFreeNeighbours() {
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                freeNeighbours[pos + 8] += (isValidPos(pos - 8) && values[pos - 8] == FREE ? 1 : 0);
                freeNeighbours[pos + 8] += (isValidPos(pos - 7) && values[pos - 7] == FREE && pos != 7 ? 1 : 0);
                freeNeighbours[pos + 8] += (isValidPos(pos - 1) && values[pos - 1] == FREE && pos != 8 ? 1 : 0);
                freeNeighbours[pos + 8] += (isValidPos(pos + 1) && values[pos + 1] == FREE && pos != 7 ? 1 : 0);
                freeNeighbours[pos + 8] += (isValidPos(pos + 7) && values[pos + 7] == FREE && pos != 0 ? 1 : 0);
                freeNeighbours[pos + 8] += (isValidPos(pos + 8) && values[pos + 8] == FREE ? 1 : 0);
            }
        }
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
}
