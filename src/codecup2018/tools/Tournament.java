package codecup2018.tools;

import codecup2018.Pair;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.BucketSortMaxMovesOneHole;
import codecup2018.player.IterativeDFSPlayer;
import codecup2018.player.KillerMultiAspirationTableCutoffPlayer;
import codecup2018.player.Player;
import codecup2018.timecontrol.EqualTimeController;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Tournament {

    private static final int GAMES = 100;

    public static void main(String[] args) throws IOException {
        runTournament(Arrays.<Player>asList(
                //new RandomPlayer("Rando", new AllMoves()),
                //new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()),
                //new NegaMaxPlayer("NM_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10),
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_MI_4", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 4),
                //new AspirationPlayer("As_EV_MI_6", new ExpectedValue(), new MaxInfluenceMoves(), 6),
                //new MultiAspirationTablePlayer("MAsT_EV_MI_6", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 6),
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_MI_3", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 3),
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5),
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_MI_6", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 6),
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_BSM_6", new IncrementalExpectedValue(), new BucketSortMaxMoves(), 6),
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_BSM1_4", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 4),
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_BSM1_6", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 6),
                new KillerMultiAspirationTableCutoffPlayer("KMAsTC_IEV_BSM1_6", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 6),
                //new KillerMultiAspirationTableCutoffPlayer("KMAsTC_IEV_BSM1_7", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 7) // (best so far)
                //new KillerMultiAspirationTableCutoffPlayer("KMAsTC_ME_BSM1_7", new MedianExpected(), new BucketSortMaxMovesOneHole(), 7),
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_MIN_6", new IncrementalExpectedValue(), new MaxInfluenceMinMoves(), 6),
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_LM_3", new IncrementalExpectedValue(), new LikelyMoves(), 3)
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_LM_4", new IncrementalExpectedValue(), new LikelyMoves(), 4),
                //new KillerMultiAspirationTableCutoffPlayer("KMAsTC_IEV_LM_4", new IncrementalExpectedValue(), new LikelyMoves(), 4),
                //new KillerMultiAspirationTableCutoffPlayer("KMAsTC_IEV_LM_5", new IncrementalExpectedValue(), new LikelyMoves(), 5),
                //new KillerMultiAspirationTableCutoffPlayer("KMAsTC_ME_LM_5", new MedianExpected(), new LikelyMoves(), 5),
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_LM_5", new IncrementalExpectedValue(), new LikelyMoves(), 5)
                //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_LM_6", new IncrementalExpectedValue(), new LikelyMoves(), 6)
                //new UpperConfidenceBoundsPlayer("UCB_ME_BSM1_500", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), 500),
                //new UpperConfidenceBoundsPlayer("UCB_ME_BSM1_5000", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), 5000),
                //new UpperConfidenceBoundsPlayer("UCB_Mix_BSM1_50000", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), 50000),
                //new UpperConfidenceBoundsPlayer("UCB_ME_BSM1_50000", new MedianExpected(), new BucketSortMaxMovesOneHole(), 50000)
                //new TimedUCBPlayer("TUCB_Mix_BSM1_Eq0.1s", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), new EqualTimeController(100)),
                //new TimedUCBPlayer("TUCB_Mix_BSM1_Eq0.5s", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), new EqualTimeController(500)),
                //new TimedUCBPlayer("TUCB_Mix_BSM1_Eq2.5s", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), new EqualTimeController(2500))
                //new TimedUCBPlayer("TUCB_Mix_BSM1_Pr0.1s", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), new ProportionalController(100))
                //new TimedUCBPlayer("TUCB_Mix_BSM1_Pr0.5s", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), new ProportionalController(500)),
                //new TimedUCBPlayer("TUCB_Mix_BSM1_Pr2.5s", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), new ProportionalController(2500))
                new IterativeDFSPlayer("ID_IEV_BSM1_6", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), new EqualTimeController(400))
        ));
    }

    public static void runTournament(List<Player> players) throws IOException {
        int n = players.size();
        int[][] wins = new int[n][n];
        double[][] avgScore = new double[n][n];
        double[][] stdDev = new double[n][n];

        long totalGames = (n * (n - 1) * GAMES) / 2;
        long currentGame = 0;
        System.out.println("Playing all the games:");

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int[] scores = new int[GAMES];

                // Run all games
                for (int k = 0; k < GAMES; k++) {
                    scores[k] = (k % 2 == 0
                            ? GameHost.runGame(players.get(i), players.get(j), false)
                            : -GameHost.runGame(players.get(j), players.get(i), false));

                    currentGame++;
                    if (currentGame % 10 == 0) {
                        System.out.printf("%10d/%d%n", currentGame, totalGames);
                    }
                }

                // Compute statistics
                int totalScore = 0;

                for (int k = 0; k < GAMES; k++) {
                    totalScore += scores[k];

                    if (scores[k] > 0) {
                        wins[i][j]++;
                    } else if (scores[k] < 0) {
                        wins[j][i]++;
                    }
                }

                avgScore[i][j] = totalScore / (double) GAMES;
                avgScore[j][i] = -avgScore[i][j];

                double squaredError = 0;

                for (int k = 0; k < GAMES; k++) {
                    double error = scores[k] - avgScore[i][j];
                    squaredError += error * error;
                }

                stdDev[i][j] = Math.sqrt(squaredError / (GAMES - 1));
                stdDev[j][i] = stdDev[i][j];
            }
        }

        report(players, wins, avgScore, stdDev);
    }

    private static void report(List<Player> players, int[][] wins, double[][] avgScore, double[][] stdDev) {
        // List players
        System.out.println("Players:");
        for (int i = 0; i < players.size(); i++) {
            System.out.printf("%d: %s%n", i, players.get(i).getName());
        }
        System.out.println();

        // Print wins
        int WIN_CELL_WIDTH = 5;

        System.out.println("Number of wins:");

        System.out.printf("%" + WIN_CELL_WIDTH + "s", "");
        for (int i = 0; i < players.size(); i++) {
            System.out.printf("%" + WIN_CELL_WIDTH + "d", i);
        }
        System.out.println();

        for (int i = 0; i < players.size(); i++) {
            System.out.printf("%" + WIN_CELL_WIDTH + "d", i);
            for (int j = 0; j < players.size(); j++) {
                System.out.printf("%" + WIN_CELL_WIDTH + "d", wins[i][j]);
            }
            System.out.println();
        }
        System.out.println();

        // Print averages
        int AVG_CELL_WIDTH = 6;
        DecimalFormat smallValueFormat = new DecimalFormat("0.000 ");

        System.out.println("Average score:");

        System.out.printf("%" + AVG_CELL_WIDTH + "s ", "");
        for (int i = 0; i < players.size(); i++) {
            System.out.printf("%" + AVG_CELL_WIDTH + "d ", i);
        }
        System.out.println();

        for (int i = 0; i < players.size(); i++) {
            System.out.printf("%" + AVG_CELL_WIDTH + "d ", i);
            for (int j = 0; j < players.size(); j++) {
                if (i == j) {
                    System.out.print("     0 ");
                } else if (Math.abs(avgScore[i][j]) < 0.1) {
                    if (avgScore[i][j] >= 0) {
                        System.out.print(" " + smallValueFormat.format(avgScore[i][j]));
                    } else {
                        System.out.print(smallValueFormat.format(avgScore[i][j]));
                    }
                } else {
                    System.out.printf("%" + AVG_CELL_WIDTH + "." + (AVG_CELL_WIDTH - 3) + "g ", avgScore[i][j]);
                }
            }
            System.out.println();
        }
        System.out.println();

        // Print comparisons
        final double[][] pValue = new double[players.size()][players.size()];
        List<Pair<Integer, Integer>> hypotheses = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                if (avgScore[i][j] > 0) {
                    hypotheses.add(new Pair<>(i, j));
                    pValue[i][j] = computeP(avgScore[i][j], stdDev[i][j]);
                } else if (avgScore[i][j] < 0) {
                    hypotheses.add(new Pair<>(j, i));
                    pValue[j][i] = computeP(avgScore[j][i], stdDev[j][i]);
                }
            }
        }

        Collections.sort(hypotheses, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                return Double.compare(pValue[o1.getFirst()][o1.getSecond()], pValue[o2.getFirst()][o2.getSecond()]);
            }
        });

        System.out.println("Comparisons:");
        for (Pair<Integer, Integer> hypothesis : hypotheses) {
            int p1 = hypothesis.getFirst();
            int p2 = hypothesis.getSecond();
            System.out.printf("    %s > %s with p = %f%n", players.get(p1).getName(), players.get(p2).getName(), pValue[p1][p2]);
        }
    }

    private static double computeP(double avg, double stdDev) {
        double t = avg * Math.sqrt(GAMES) / stdDev; // test value, GAMES - 1 DoF
        double tt2 = -t * t / 2;
        double erftt = (2 / Math.sqrt(Math.PI)) * Math.sqrt(-Math.expm1(tt2)) * (Math.sqrt(Math.PI) / 2 + 31 * Math.exp(tt2) / 200 - 341 * Math.exp(2 * tt2) / 8000);

        return 1 - 0.5 * (erftt + 1);
    }
}
