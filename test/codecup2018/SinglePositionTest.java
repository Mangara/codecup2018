package codecup2018;

import codecup2018.data.ArrayBoard;
import codecup2018.data.Board;
import codecup2018.data.BitBoard;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.BucketSortMaxMovesOneHole;
import codecup2018.movegenerator.NoHolesMax;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.IterativeDFSPlayer;
import codecup2018.player.Player;
import codecup2018.player.SimpleMaxPlayer;
import codecup2018.timecontrol.ProportionalController;
import codecup2018.tools.TestUtil;
import codecup2018.tools.TimeControlGA;
import java.util.Arrays;
import org.junit.Test;

public class SinglePositionTest {

    //private Player player = new AspirationPlayer("NM_EV_NHM_2", new ExpectedValue(), new NoHolesMax(), 2);
    //private Player player = new NegaMaxPlayer("NM_EV_NHM_2", new ExpectedValue(), new NoHolesMax(), 2);
    //private Player player = new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves());
    private Player player = new IterativeDFSPlayer("ID_IEV_BSM1", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), new ProportionalController(100, TimeControlGA.toArray(TimeControlGA.equalTime())));

    @Test
    public void runTest() {
        /*byte[] values = new byte[] {
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
        
        Board board = new BitBoard(new ArrayBoard(grid));*/
        Board board = TestUtil.parseBoard(
                  "                -7               \n"
                + "               0  11             \n"
                + "             8 -15  -5           \n"
                + "           0  -9  12   0         \n"
                + "         X  13   0 -14   0       \n"
                + "       X   0 -13   7  15 -11     \n"
                + "     2 -12  14   1 -10   0   9   \n"
                + "  -6  10   0  -8   X   X   X   0 ");

        Player.DEBUG = true;
        player.initialize(board);
        int move = player.move();

        System.err.println();
        System.err.println("==========================");
        System.err.println("Move: " + Board.moveToString(move));
    }
}
