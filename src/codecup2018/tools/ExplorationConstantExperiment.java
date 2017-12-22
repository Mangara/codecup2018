package codecup2018.tools;

import codecup2018.data.Board;
import codecup2018.data.BitBoard;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.evaluator.MedianFree;
import codecup2018.evaluator.MixedEvaluator;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.BucketSortMaxMovesOneHole;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.movegenerator.MostFreeMax;
import codecup2018.movegenerator.NoHoles;
import codecup2018.player.MultiAspirationTableCutoffPlayer;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.RankSelectPlayer;
import codecup2018.player.SimpleMaxPlayer;
import codecup2018.player.UpperConfidenceBoundsPlayer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ExplorationConstantExperiment {

    private static final int N_GAMES = 250;

    private static List<Integer> initialHeuristicWeight = Arrays.asList(
            //1, 2, 3, 
            //4, 5, 6, 7, 8, 9, 10, 15, 20, 30, 
            40, 50, 100, 200, 500, 1000
            //, 2000, 5000
    );

    private static List<Double> ucbParameter = Arrays.asList(
            0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9,
            1.0, 1.1, 1.2, 1.5, 1.75, 2.0,
            2.25, 2.5, 2.75, 3.0, 3.5, 4.0,
            4.5, 5.0
    );

    public void runTest() throws NoSuchFieldException, IllegalAccessException {
        System.out.println("Player,"
                + "Average time (ms),Min time (ms),1st Quartile time (ms),Median time (ms),3rd Quartile time (ms),Max time (ms),"
                + "Average score,Min score,1st Quartile score,Median score,3rd Quartile score,Max score,"
                + "Average score vs 1,Min score vs 1,1st Quartile score vs 1,Median score vs 1,3rd Quartile score vs 1,Max score vs 1,"
                + "Average score vs 2,Min score vs 2,1st Quartile score vs 2,Median score vs 2,3rd Quartile score vs 2,Max score vs 2,"
                + "Average score vs 3,Min score vs 3,1st Quartile score vs 3,Median score vs 3,3rd Quartile score vs 3,Max score vs 3,"
                + "Average score vs 4,Min score vs 4,1st Quartile score vs 4,Median score vs 4,3rd Quartile score vs 4,Max score vs 4");
        for (Integer weight : initialHeuristicWeight) {
            for (Double ucb : ucbParameter) {
                UpperConfidenceBoundsPlayer.INITIAL_HEURISTIC_WEIGHT = weight;
                UpperConfidenceBoundsPlayer.UCB_PARAMETER = ucb;
                UpperConfidenceBoundsPlayer player = new UpperConfidenceBoundsPlayer(String.format("UCB_ME_BSM1_20000_%d_%f", weight, ucb), new MixedEvaluator(), new BucketSortMaxMovesOneHole(), 20000);

                List<Player> opponents = Arrays.<Player>asList(
                        /*new RankSelectPlayer("Ranky_MI_0.8", new MaxInfluenceMoves(), 0.8, new Random(256745367354L)),
                        new RandomPlayer("Rando", new AllMoves(), new Random(1567245267354L)),
                        new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()),
                        new MultiAspirationTableCutoffPlayer("MAsTC_IEV_BSM1_4", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 4)*/
                        new NegaMaxPlayer("NM_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)
                );

                int[] scores = new int[N_GAMES];
                int[][] vsScores = new int[opponents.size()][(int) Math.ceil(N_GAMES / (double) opponents.size())];
                long totalScore = 0;
                long[] times = new long[N_GAMES];
                long totalTimeMicroseconds = 0;

                for (int i = 0; i < N_GAMES; i++) {
                    Player opponent = opponents.get(i % opponents.size());

                    Board board = GameHost.setUpBoard();
                    player.initialize(new BitBoard(board));
                    opponent.initialize(new BitBoard(board));

                    long start, time = 0;

                    if (i % (2 * opponents.size()) < opponents.size()) {
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

                        scores[i] = -opponent.getBoard().getFinalScore();
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

                        scores[i] = player.getBoard().getFinalScore();
                    }

                    totalScore += scores[i];
                    vsScores[i % opponents.size()][i / opponents.size()] = scores[i];

                    times[i] = time;
                    totalTimeMicroseconds += time / 1000;
                }

                Arrays.sort(times);
                Arrays.sort(scores);
                for (int i = 0; i < opponents.size(); i++) {
                    Arrays.sort(vsScores[i]);
                }

                System.out.print(player.getName() + ',');
                System.out.printf("%f,%f,%f,%f,%f,%f,",
                        nsToMs(1000 * (totalTimeMicroseconds / N_GAMES)),
                        nsToMs(times[0]),
                        nsToMs(times[N_GAMES / 4]),
                        nsToMs(times[N_GAMES / 2]),
                        nsToMs(times[(3 * N_GAMES) / 4]),
                        nsToMs(times[N_GAMES - 1]));
                System.out.printf("%f,%d,%d,%d,%d,%d,",
                        totalScore / (double) N_GAMES,
                        scores[0],
                        scores[N_GAMES / 4],
                        scores[N_GAMES / 2],
                        scores[(3 * N_GAMES) / 4],
                        scores[N_GAMES - 1]);

                for (int i = 0; i < opponents.size(); i++) {
                    long totalVsScore = 0;
                    for (int j = 0; j < vsScores[i].length; j++) {
                        totalVsScore += vsScores[i][j];
                    }

                    System.out.printf("%f,%d,%d,%d,%d,%d",
                            totalVsScore / (double) vsScores[i].length,
                            vsScores[i][0],
                            vsScores[i][vsScores[i].length / 4],
                            vsScores[i][vsScores[i].length / 2],
                            vsScores[i][(3 * vsScores[i].length) / 4],
                            vsScores[i][vsScores[i].length - 1]);

                    if (i < opponents.size() - 1) {
                        System.out.print(",");
                    } else {
                        System.out.println();
                    }
                }
            }
        }
    }

    private static double nsToMs(long nanoseconds) {
        return nanoseconds / 1000000.0;
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        new ExplorationConstantExperiment().runTest();
    }
}
