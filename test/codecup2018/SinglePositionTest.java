package codecup2018;

import codecup2018.evaluator.ExpectedValue;
import codecup2018.movegenerator.NoHolesMax;
import codecup2018.player.AlphaBetaPlayer;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import java.util.Arrays;
import org.junit.Test;

public class SinglePositionTest {

    private Player player1 = new AlphaBetaPlayer("AB_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4);
    private Player player2 = new NegaMaxPlayer("NM_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4);
    
    @Test
    public void runTest() {
        byte[] values = new byte[] {
            Board.BLOCKED,
            Board.BLOCKED, Board.BLOCKED,
            Board.BLOCKED, Board.BLOCKED, Board.BLOCKED,
            1, -1, 2, -2,
            3, -3, 4, -4, 5,
            -5, 6, -6, 7, -7, 8,
            Board.FREE, Board.FREE, Board.FREE, Board.FREE, Board.FREE, Board.FREE, Board.FREE, 
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
        
        Board board1 = new Board(grid);
        Board board2 = new Board(grid);
        
        player1.initialize(board1);
        byte[] move1 = player1.move();
        
        player2.initialize(board2);
        byte[] move2 = player2.move();
        
        System.out.println();
        System.out.println("==========================");
        System.out.println("Player 1 move: " + Arrays.toString(move1));
        System.out.println("Player 2 move: " + Arrays.toString(move2));
    }
}
