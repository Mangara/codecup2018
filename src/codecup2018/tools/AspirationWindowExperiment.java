package codecup2018.tools;

import codecup2018.data.Board;
import codecup2018.data.BitBoard;
import codecup2018.evaluator.CountingEvaluator;
import codecup2018.evaluator.Evaluator;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.movegenerator.NoHoles;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.AspirationTablePlayer;
import codecup2018.player.MultiAspirationTablePlayer;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.RankSelectPlayer;
import codecup2018.player.SimpleMaxPlayer;
import codecup2018.player.StandardPlayer;
import codecup2018.tools.GameHost;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AspirationWindowExperiment {

    private static final int N_GAMES = 1000;

    private static List<Integer> windowSize = Arrays.asList(
            //1,
            2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000
    );

    private static List<Double> windowFactor = Arrays.asList(
            //0.1, 0.2, 0.5, 0.8, 0.9, 
            1.0, 1.1, 1.2, 1.5, 1.75, 2.0, 2.25, 2.5, 2.75, 3.0, 3.5, 4.0, 4.5, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1000.0
    );

    public void runTest() throws NoSuchFieldException, IllegalAccessException {
        for (Integer window : windowSize) {
            for (Double factor : windowFactor) {
                MultiAspirationTablePlayer.INITIAL_WINDOW_SIZE = window;
                MultiAspirationTablePlayer.WINDOW_FACTOR = factor;
                MultiAspirationTablePlayer player = new MultiAspirationTablePlayer("MAsT_IEV_MI_5_" + window + "_" + factor, new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5);
                
                List<Player> opponents = Arrays.<Player>asList(
                        new RankSelectPlayer("Ranky_MI_0.8", new MaxInfluenceMoves(), 0.8, new Random(256745367354L)),
                        new RandomPlayer("Rando", new AllMoves(), new Random(1567245267354L)),
                        new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles())
                );

                CountingEvaluator count = makeEvaluatorCount(player);

                int[] evaluations = new int[N_GAMES];
                long[] times = new long[N_GAMES];
                long totalEvaluations = 0;
                long totalTimeMicroseconds = 0;

                for (int i = 0; i < N_GAMES; i++) {
                    Player opponent = opponents.get(i % opponents.size());

                    Board board = GameHost.setUpBoard();
                    player.initialize(new BitBoard(board));
                    opponent.initialize(new BitBoard(board));

                    long start, time = 0;

                    if (i % 2 == 0) {
                        // Play a game
                        for (int turn = 0; turn < 15; turn++) {
                            start = System.nanoTime();
                            int p1Move = player.move();
                            time += System.nanoTime() - start;

                            opponent.processMove(p1Move, false);

                            int p2Move = opponent.move();
                            if (turn < 14) {
                                start = System.nanoTime();
                                player.processMove(p2Move, false);
                                time += System.nanoTime() - start;
                            }
                        }
                    } else {
                        // Play a game
                        for (int turn = 0; turn < 15; turn++) {
                            int p1Move = opponent.move();

                            start = System.nanoTime();
                            player.processMove(p1Move, false);
                            time += System.nanoTime() - start;

                            start = System.nanoTime();
                            int p2Move = player.move();
                            time += System.nanoTime() - start;

                            if (turn < 14) {
                                opponent.processMove(p2Move, false);
                            }
                        }
                    }

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
                        totalEvaluations / N_GAMES,
                        evaluations[0],
                        evaluations[N_GAMES / 4],
                        evaluations[N_GAMES / 2],
                        evaluations[(3 * N_GAMES) / 4],
                        evaluations[N_GAMES - 1],
                        nsToMs(1000 * (totalTimeMicroseconds / N_GAMES)),
                        nsToMs(times[0]),
                        nsToMs(times[N_GAMES / 4]),
                        nsToMs(times[N_GAMES / 2]),
                        nsToMs(times[(3 * N_GAMES) / 4]),
                        nsToMs(times[N_GAMES - 1])
                );
            }
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

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        new AspirationWindowExperiment().runTest();
    }
}
