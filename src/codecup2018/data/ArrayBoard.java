package codecup2018.data;

public class ArrayBoard extends Board {

    private final byte[][] grid = new byte[8][8];
    private final boolean[] myUsed = new boolean[15];
    private final boolean[] oppUsed = new boolean[15];
    private int nFree = 36;

    public ArrayBoard() {
    }

    public ArrayBoard(Board board) {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                set(a, b, board.get(getPos(a, b)));
            }
        }
    }

    public ArrayBoard(byte[][] board) {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                set(a, b, board[a][b]);
            }
        }
    }

    @Override
    public byte get(byte pos) {
        int a = pos / 8, b = pos % 8;
        return grid[a][b];
    }

    @Override
    public int getNFreeSpots() {
        return nFree;
    }

    @Override
    public boolean isFree(byte pos) {
        int a = pos / 8, b = pos % 8;
        return grid[a][b] == FREE;
    }

    @Override
    public byte[] getFreeSpots() {
        byte[] free = new byte[nFree];
        int i = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (grid[a][b] == FREE) {
                    free[i] = getPos(a, b);
                    i++;
                }
            }
        }

        return free;
    }

    @Override
    public int getHoleValue(byte pos) {
        int a = pos / 8, b = pos % 8;
        int total = 0;

        if (a > 0) {
            total += value(grid[a - 1][b]);
        }

        if (a < 7 - b) {
            total += value(grid[a + 1][b]);
        }

        if (b > 0) {
            total += value(grid[a][b - 1]);
        }

        if (b < 7 - a) {
            total += value(grid[a][b + 1]);
        }

        if (a > 0 && b < 8 - a) {
            total += value(grid[a - 1][b + 1]);
        }

        if (a < 8 - b && b > 0) {
            total += value(grid[a + 1][b - 1]);
        }

        return total;
    }

    private int value(byte val) {
        return (val > -16 && val < 16 ? val : 0);
    }

    @Override
    public int getFreeSpotsAround(byte pos) {
        int a = pos / 8, b = pos % 8;
        int total = 0;

        if (a > 0 && grid[a - 1][b] == FREE) {
            total++;
        }

        if (a < 7 - b && grid[a + 1][b] == FREE) {
            total++;
        }

        if (b > 0 && grid[a][b - 1] == FREE) {
            total++;
        }

        if (b < 7 - a && grid[a][b + 1] == FREE) {
            total++;
        }

        if (a > 0 && b < 8 - a && grid[a - 1][b + 1] == FREE) {
            total++;
        }

        if (a < 8 - b && b > 0 && grid[a + 1][b - 1] == FREE) {
            total++;
        }

        return total;
    }

    @Override
    public void block(byte pos) {
        byte a = (byte) (pos / 8), b = (byte) (pos % 8);
        set(a, b, BLOCKED);
    }

    public void set(byte a, byte b, byte value) {
        if (grid[a][b] == FREE && value != FREE) {
            nFree--;
        } else if (grid[a][b] != FREE && value == FREE) {
            nFree++;
        }

        grid[a][b] = value;

        if (value > 0 && value < 16) {
            myUsed[value - 1] = true;
        } else if (value < 0 && value > -16) {
            oppUsed[-value - 1] = true;
        }
    }

    @Override
    public void applyMove(int move) {
        byte pos = getMovePos(move);
        byte val = getMoveVal(move);
        byte a = (byte) (pos / 8), b = (byte) (pos % 8);
        
        grid[a][b] = val;
        nFree--;

        if (val > 0) {
            myUsed[val - 1] = true;
        } else {
            oppUsed[-val - 1] = true;
        }
    }

    @Override
    public void undoMove(int move) {
        byte pos = getMovePos(move);
        byte val = getMoveVal(move);
        byte a = (byte) (pos / 8), b = (byte) (pos % 8);
        
        grid[a][b] = FREE;
        nFree++;

        if (val > 0) {
            myUsed[val - 1] = false;
        } else {
            oppUsed[-val - 1] = false;
        }
    }

    @Override
    public boolean haveIUsed(byte value) {
        return myUsed[value - 1];
    }

    @Override
    public boolean hasOppUsed(byte value) {
        return oppUsed[value - 1];
    }

    @Override
    public boolean isGameOver() {
        return nFree == 1;
    }

    @Override
    public boolean isGameInEndGame() {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (isFree(Board.getPos(a, b)) && getFreeSpotsAround(Board.getPos(a, b)) > 0) {
                    return false;
                }
            }
        }
        
        return true;
    }

    @Override
    public boolean isLegalMove(int move) {
        byte val = getMoveVal(move);
        return isFree(getMovePos(move)) && ((val > 0 && !haveIUsed(val)) || (val < 0 && !hasOppUsed((byte) -val)));
    }

    @Override
    public int getTranspositionTableKey() {
        int key = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                key ^= KEY_POSITION_NUMBERS[32 * (8 * a + b) + get(getPos(a, b)) + 15];
            }
        }

        return key;
    }

    @Override
    public long getHash() {
        long hash = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                hash ^= HASH_POSITION_NUMBERS[32 * (8 * a + b) + get(getPos(a, b)) + 15];
            }
        }

        return hash;
    }
}
