package codecup2018;

import codecup2018.tools.RandomPositionGenerator;
import codecup2018.data.Board;
import codecup2018.data.BitBoard;
import codecup2018.evaluator.CountingEvaluator;
import codecup2018.evaluator.Evaluator;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.AllMoves;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.StandardPlayer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class RandomPositionPerformanceTest {

    private final List<StandardPlayer> players = Arrays.<StandardPlayer>asList(
            new NegaMaxPlayer("NM_IEV_AM_30", new IncrementalExpectedValue(), new AllMoves(), 30)
            //new AspirationPlayer("As_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
            //new AspirationTablePlayer("AsT_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
            //new MultiAspirationTablePlayer("MAsT_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
            //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_MI_6", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 6),
            //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_MIN_6", new IncrementalExpectedValue(), new MaxInfluenceMinMoves(), 6),
            //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_BSM_6", new IncrementalExpectedValue(), new BucketSortMaxMoves(), 6),
            //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_BSM1_6", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 6)
            //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_LM_2", new IncrementalExpectedValue(), new LikelyMoves(), 2),
            //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_LM_3", new IncrementalExpectedValue(), new LikelyMoves(), 3),
            //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_LM_4", new IncrementalExpectedValue(), new LikelyMoves(), 4),
            //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_LM_5", new IncrementalExpectedValue(), new LikelyMoves(), 5)
    );

    @Test
    public void runTest() throws NoSuchFieldException, IllegalAccessException {
        List<Board> testBoards = RandomPositionGenerator.generateRealisticTestBoards(2);

        for (StandardPlayer player : players) {
            CountingEvaluator count = makeEvaluatorCount(player);

            int[] evaluations = new int[testBoards.size()];
            long[] times = new long[testBoards.size()];
            long totalEvaluations = 0;
            long totalTimeMicroseconds = 0;

            for (int i = 0; i < testBoards.size(); i++) {
                Board board = testBoards.get(i);
                player.initialize(new BitBoard(board));

                long start = System.nanoTime();
                player.move();
                long time = System.nanoTime() - start;

                evaluations[i] = count.getnEvaluations();
                totalEvaluations += count.getnEvaluations();

                times[i] = time;
                totalTimeMicroseconds += time / 1000;
            }

            Arrays.sort(evaluations);
            Arrays.sort(times);

            System.out.printf("Player: %s%n"
                    + "Average evaluations: %d%n"
                    + "Quartiles: %d - %d - %d - %d - %d%n"
                    + "Average time (ms): %.3f%n"
                    + "Quartiles: %.3f - %.3f - %.3f - %.3f - %.3f%n%n",
                    player.getName(),
                    totalEvaluations / testBoards.size(),
                    evaluations[0],
                    evaluations[testBoards.size() / 4],
                    evaluations[testBoards.size() / 2],
                    evaluations[(3 * testBoards.size()) / 4],
                    evaluations[testBoards.size() - 1],
                    nsToMs(1000 * (totalTimeMicroseconds / testBoards.size())),
                    nsToMs(times[0]),
                    nsToMs(times[testBoards.size() / 4]),
                    nsToMs(times[testBoards.size() / 2]),
                    nsToMs(times[(3 * testBoards.size()) / 4]),
                    nsToMs(times[testBoards.size() - 1])
            );
        }
    }

    /**
     * Some dirty reflection tricks so that we can leave the field private
     * final.
     *
     * @param player
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private CountingEvaluator makeEvaluatorCount(StandardPlayer player) throws NoSuchFieldException, IllegalAccessException {
        Field evaluator = StandardPlayer.class.getDeclaredField("evaluator");
        evaluator.setAccessible(true);
        Evaluator oldEvaluator = (Evaluator) evaluator.get(player);
        CountingEvaluator count = new CountingEvaluator(oldEvaluator);
        evaluator.set(player, count);
        return count;
    }

    private static double nsToMs(long nanoseconds) {
        return nanoseconds / 1000000.0;
    }
}
