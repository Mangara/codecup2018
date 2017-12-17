package codecup2018;

import codecup2018.tools.RandomPositionGenerator;
import codecup2018.data.Board;
import codecup2018.data.BitBoard;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.player.AspirationTablePlayer;
import codecup2018.player.StandardPlayer;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class RandomPositionCollisionTest {

    private final List<StandardPlayer> players = Arrays.<StandardPlayer>asList(
            new AspirationTablePlayer("AT_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5)
    );

    @Test
    public void runTest() throws NoSuchFieldException, IllegalAccessException {
        List<Board> testBoards = RandomPositionGenerator.generateRealisticTestBoards(1000);

        for (StandardPlayer player : players) {
            int[] collisions = new int[testBoards.size()];
            long totalCollisions = 0;

            for (int i = 0; i < testBoards.size(); i++) {
                Board board = testBoards.get(i);
                player.initialize(new BitBoard(board));
                player.move();

                collisions[i] = 0;//((AspirationTablePlayer) player).collisions;
                totalCollisions += collisions[i];
            }

            Arrays.sort(collisions);

            System.out.printf("Player: %s%n"
                    + "Average collisions: %d%n"
                    + "Quartiles: %d - %d - %d - %d - %d%n",
                    player.getName(),
                    totalCollisions / testBoards.size(),
                    collisions[0],
                    collisions[testBoards.size() / 4],
                    collisions[testBoards.size() / 2],
                    collisions[(3 * testBoards.size()) / 4],
                    collisions[testBoards.size() - 1]
            );
        }
    }
}
