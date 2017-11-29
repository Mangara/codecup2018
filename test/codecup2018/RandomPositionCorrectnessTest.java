package codecup2018;

import codecup2018.data.BitBoard;
import codecup2018.data.Board;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.player.AspirationTablePlayer;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RandomPositionCorrectnessTest {

    private final Board board;
    private final List<Player> players = Arrays.<Player>asList(
            new NegaMaxPlayer("NM_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
            new AspirationTablePlayer("AsT_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5)
    );

    public RandomPositionCorrectnessTest(Board board) {
        this.board = board;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return RandomPostionGenerator.generateTestData(1000);
    }

    @Test
    public void runTest() {
        List<byte[]> moves = new ArrayList<>();

        for (Player player : players) {
            player.initialize(new BitBoard(board));
            moves.add(player.move());
        }

        byte[] first = moves.get(0);
        
        for (byte[] move : moves) {
            if (!Arrays.equals(move, first)) {
                Util.print(board);
                for (int i = 0; i < players.size(); i++) {
                    Player p = players.get(i);
                    byte[] m = moves.get(i);
                    
                    System.err.println(p + ": " + Arrays.toString(m));
                }
                System.err.println();
                
                fail("Returned different move.");
            }
        }
    }
}
