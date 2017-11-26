package codecup2018;

import codecup2018.data.ArrayBoard;
import codecup2018.data.Board;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.movegenerator.NoHolesMax;
import codecup2018.player.AlphaBetaPlayer;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.MaxComponentPlayer;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import java.util.Arrays;
import org.junit.Test;

public class SinglePositionTest {

    private Player player = new AspirationPlayer("NM_EV_NHM_2", new ExpectedValue(), new NoHolesMax(), 2);
    //private Player player = new NegaMaxPlayer("NM_EV_NHM_2", new ExpectedValue(), new NoHolesMax(), 2);
    
    @Test
    public void runTest() {
        byte[] values = new byte[] {
            Board.BLOCKED,
            Board.BLOCKED, Board.BLOCKED,
            Board.BLOCKED, Board.BLOCKED, Board.BLOCKED,
            1, -1, 2, -2,
            3, -3, 4, -4, 5,
            -5, 6, -6, 7, -7, 8,
            Board.FREE, Board.FREE, Board.BLOCKED, Board.FREE, Board.FREE, Board.FREE, Board.FREE, 
            -8, 9, -9, 10, -10, 11, -11, 12
        };
        byte[][] grid = new byte[8][8];
        int i = 0;
        
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                grid[a][b] = values[i];
                i++;
            }
        }
        
        ArrayBoard board = new ArrayBoard(grid);
        
        player.initialize(board);
        byte[] move = player.move();
        
        System.out.println();
        System.out.println("==========================");
        System.out.println("Move: " + Arrays.toString(move));
    }
}
