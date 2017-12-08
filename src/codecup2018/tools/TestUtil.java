package codecup2018.tools;

import codecup2018.data.ArrayBoard;
import codecup2018.data.Board;

public class TestUtil {
    
    public static Board parseBoard(String board) {
        String[] tiles = board.split("\\s+");
        
        byte[][] grid = new byte[8][8];
        
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                grid[a][b] = parseTile(tiles[getTileNumber(a, b)]);
            }
        }
        
        return new ArrayBoard(grid);
    }

    private static int getTileNumber(int a, int b) {
        int tile = 1;
        int counter = 1;
        
        for (int i = 0; i < a; i++) {
            tile += counter;
            counter++;
        }
        
        counter++;
        
        for (int i = 0; i < b; i++) {
            tile += counter;
            counter++;
        }
        
        return tile;
    }

    private static byte parseTile(String tile) {
        switch (tile) {
            case "0":
                return Board.FREE;
            case "X":
                return Board.BLOCKED;
            case "":
                throw new NullPointerException();
            default:
                return Byte.parseByte(tile);
        }
    }
    
    public static void main(String[] args) {
        String board1 = 
                  "                 3               \n"
                + "               1  -4             \n"
                + "             0 -13   8           \n"
                + "           X  11   X   X         \n"
                + "         2  -3 -12   5  15       \n"
                + "       6 -14  14   7 -15  -2     \n"
                + "    -9   0  -7   X   0   9  -8   \n"
                + "  13  -6 -11  12  -1 -10   0   X ";
        
        Board b = parseBoard(board1);
        Board.print(b);
    }
}
