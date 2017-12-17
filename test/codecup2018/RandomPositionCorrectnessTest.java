package codecup2018;

import codecup2018.tools.RandomPositionGenerator;
import codecup2018.data.Board;
import codecup2018.data.BitBoard;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.player.MultiAspirationTableCutoffPlayer;
import codecup2018.player.MultiAspirationTablePlayer;
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
            //new NegaMaxPlayer("NM_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
            //new AspirationTablePlayer("AsT_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5)
            
            //new AlphaBetaPlayer("AB_IEV_AM_3", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
            new NegaMaxPlayer("NM_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
            //new AspirationPlayer("As_IEV_MFM_3", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
            //new AspirationTablePlayer("AsT_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
            new MultiAspirationTablePlayer("MAsT_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
            new MultiAspirationTableCutoffPlayer("MAsTC_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5)
    );

    public RandomPositionCorrectnessTest(Board board) {
        this.board = board;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return RandomPositionGenerator.generateRealisticTestData(1000); // 1000
    }

    @Test
    public void runTest() {
        List<Integer> moves = new ArrayList<>();

        for (Player player : players) {
            player.initialize(new BitBoard(board));
            moves.add(player.move());
        }

        int first = moves.get(0);
        
        for (int move : moves) {
            if (move != first) {
                Board.print(board);
                for (int i = 0; i < players.size(); i++) {
                    Player p = players.get(i);
                    int m = moves.get(i);
                    
                    System.err.println(p.getName() + ": " + Board.moveToString(m) + " eval: " + Board.getMoveEval(m));
                }
                System.err.println();
                
                fail("Returned different move.");
            }
        }
    }
}
