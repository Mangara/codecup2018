package codecup2018;

public class Board {

    public static final int ME = 0, OPP = 1;
    public static final byte BLOCKED = 120, FREE = 0;

    private final byte[][] grid = new byte[8][8];
    private final boolean[] myUsed = new boolean[15];
    private final boolean[] oppUsed = new boolean[15];
    private int nFree = 36;

    public Board() {
    }

    public Board(Board board) {
        this(board.grid);
    }

    public Board(byte[][] board) {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                grid[a][b] = board[a][b];

                if (grid[a][b] != FREE) {
                    nFree--;

                    if (grid[a][b] != BLOCKED) {
                        if (grid[a][b] > 0) {
                            myUsed[grid[a][b] - 1] = true;
                        } else if (grid[a][b] < 0) {
                            oppUsed[-grid[a][b] - 1] = true;
                        }
                    }
                }
            }
        }
    }

    public byte get(byte a, byte b) {
        return grid[a][b];
    }

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
        return (val == BLOCKED ? 0 : val);
    }

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

    public void set(byte a, byte b, byte value) {
        grid[a][b] = value;
        nFree--;

        if (value > 0 && value != BLOCKED) {
            myUsed[value - 1] = true;
        } else if (value < 0) {
            oppUsed[-value - 1] = true;
        }
    }

    public void set(String location, byte value) {
        byte[] loc = getCoordinates(location);
        set(loc[0], loc[1], value);
    }

    public void applyMove(byte[] move) {
        grid[move[0]][move[1]] = move[2];
        nFree--;

        if (move[2] > 0) {
            myUsed[move[2] - 1] = true;
        } else {
            oppUsed[-move[2] - 1] = true;
        }
    }

    public void undoMove(byte[] move) {
        grid[move[0]][move[1]] = FREE;
        nFree++;

        if (move[2] > 0) {
            myUsed[move[2] - 1] = false;
        } else {
            oppUsed[-move[2] - 1] = false;
        }
    }

    public boolean haveIUsed(byte value) {
        return myUsed[value - 1];
    }

    public boolean hasOppUsed(byte value) {
        return oppUsed[value - 1];
    }

    public boolean isGameOver() {
        return nFree == 1;
    }

    public static byte[] getCoordinates(String location) {
        return new byte[]{(byte) (location.charAt(0) - 'A'), (byte) (location.charAt(1) - '1')};
    }

    public static String coordinatesToString(byte a, byte b) {
        return Character.toString((char) ('A' + a)) + Character.toString((char) ('1' + b));
    }

    public static byte[] parseMove(String move) {
        return new byte[]{(byte) (move.charAt(0) - 'A'), (byte) (move.charAt(1) - '1'), (byte) (move.charAt(3) - '1')};
    }

    public void print() {
        for (int h = 0; h < 8; h++) {
            System.err.print(spaces(7 - h));
            for (int i = 0; i <= h; i++) {
                if (i > 0) {
                    System.err.print(' ');
                }

                System.err.print(grid[h - i][i] == BLOCKED ? "  X" : String.format("%3d", grid[h - i][i]));
            }
            System.err.println(spaces(7 - h));
        }
    }

    private String spaces(int n) {
        switch (n) {
            case 0:
                return "";
            case 1:
                return "  ";
            case 2:
                return "    ";
            case 3:
                return "      ";
            case 4:
                return "        ";
            case 5:
                return "          ";
            case 6:
                return "            ";
            case 7:
                return "              ";
            default:
                throw new IllegalArgumentException();
        }
    }
}
