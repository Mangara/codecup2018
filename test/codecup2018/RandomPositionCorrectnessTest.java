package codecup2018;

import codecup2018.data.Board;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RandomPositionCorrectnessTest {

    private final Board board;
    private final Player unoptimized = new NegaMaxPlayer("NM_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5);
    private final Player player = new AspirationPlayer("As_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5);

    public RandomPositionCorrectnessTest(Board board) {
        this.board = board;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return RandomPostionGenerator.generateTestData(1000);
    }

    @Test
    public void runTest() {
        
    }
}
