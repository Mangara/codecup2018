package codecup2018.data;

import java.util.ArrayList;
import java.util.List;

public class ArrayBoard implements Board {

    private final byte[][] grid = new byte[8][8];
    private final boolean[] myUsed = new boolean[15];
    private final boolean[] oppUsed = new boolean[15];
    private int nFree = 36;

    public ArrayBoard() {
    }

    public ArrayBoard(Board board) {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                set(a, b, board.get(a, b));
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
    public byte get(byte a, byte b) {
        return grid[a][b];
    }
    
    @Override
    public int getNFreeSpots() {
        return nFree;
    }

    @Override
    public boolean isFree(byte a, byte b) {
        return grid[a][b] == FREE;
    }
    
    @Override
    public List<byte[]> getFreeSpots() {
        List<byte[]> free = new ArrayList<>(nFree);
        
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (grid[a][b] == FREE) {
                    free.add(new byte[] {a, b, 0});
                }
            }
        }
        
        return free;
    }

    @Override
    public int getHoleValue(byte a, byte b) {
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
    public int getFreeSpotsAround(byte a, byte b) {
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
    public void block(byte a, byte b) {
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
    public void applyMove(byte[] move) {
        grid[move[0]][move[1]] = move[2];
        nFree--;

        if (move[2] > 0) {
            myUsed[move[2] - 1] = true;
        } else {
            oppUsed[-move[2] - 1] = true;
        }
    }

    @Override
    public void undoMove(byte[] move) {
        grid[move[0]][move[1]] = FREE;
        nFree++;

        if (move[2] > 0) {
            myUsed[move[2] - 1] = false;
        } else {
            oppUsed[-move[2] - 1] = false;
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
}
