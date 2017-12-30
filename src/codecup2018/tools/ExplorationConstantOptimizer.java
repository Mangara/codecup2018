package codecup2018.tools;

import codecup2018.Pair;
import codecup2018.data.Board;
import codecup2018.data.BitBoard;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.MedianFree;
import codecup2018.evaluator.MixedEvaluator;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.BucketSortMaxMovesOneHole;
import codecup2018.movegenerator.MostFreeMax;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.SimpleMaxPlayer;
import codecup2018.player.TimedUCBPlayer;
import codecup2018.timecontrol.EqualTimeController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ExplorationConstantOptimizer {

    private static final int N_GAMES = 300;

    private static final int minInitialHeuristicWeight = 100;
    private static final int maxInitialHeuristicWeight = 600;
    private int initialHeuristicWeight = 400;

    private static final double minUcbParameter = 0;
    private static final double maxUcbParameter = 0.5;
    private double ucbParameter = 0.2;

    public void optimize() throws NoSuchFieldException, IllegalAccessException {
        double temperature = 1;

        while (temperature > 0.00001) {
            optimizeIHW(temperature);
            optimizeUCB(temperature);
            temperature *= 0.8;
        }
    }

    private void optimizeIHW(double temperature) {
        List<Integer> values = getValuesInIHWWindow(temperature);

        System.out.println("Testing IHW = " + values);

        double[][] scores = new double[values.size()][];

        for (int i = 0; i < values.size(); i++) {
            initialHeuristicWeight = values.get(i);
            scores[i] = test();
        }

        int bestI = findBestScoreIndex(scores);
        initialHeuristicWeight = values.get(bestI);

        System.out.println("Best: " + initialHeuristicWeight);
        System.out.println();
    }

    private void optimizeUCB(double temperature) {
        List<Double> values = getValuesInUCBWindow(temperature);

        System.out.println("Testing UCB = " + values);

        double[][] scores = new double[values.size()][];

        for (int i = 0; i < values.size(); i++) {
            ucbParameter = values.get(i);
            scores[i] = test();
        }

        int bestI = findBestScoreIndex(scores);
        ucbParameter = values.get(bestI);

        System.out.println("Best: " + ucbParameter);
        System.out.println();
    }

    private static final int N_SAMPLES = 5;

    private List<Integer> getValuesInIHWWindow(double temperature) {
        int windowSize = (int) Math.ceil(0.5 * temperature * (maxInitialHeuristicWeight - minInitialHeuristicWeight));
        List<Integer> values = new ArrayList<>();

        values.add(initialHeuristicWeight);
        for (int i = 0; i < N_SAMPLES; i++) {
            int offset = (int) Math.round(((i + 1) / (double) N_SAMPLES) * windowSize);

            if (initialHeuristicWeight - offset > minInitialHeuristicWeight) {
                values.add(initialHeuristicWeight - offset);
            }
            if (initialHeuristicWeight + offset < maxInitialHeuristicWeight) {
                values.add(initialHeuristicWeight + offset);
            }
        }

        Collections.sort(values);
        return values;
    }

    private List<Double> getValuesInUCBWindow(double temperature) {
        double windowSize = 0.5 * temperature * (maxUcbParameter - minUcbParameter);
        List<Double> values = new ArrayList<>();

        values.add(ucbParameter);
        for (int i = 0; i < N_SAMPLES; i++) {
            double offset = ((i + 1) / (double) N_SAMPLES) * windowSize;

            if (ucbParameter - offset > minUcbParameter) {
                values.add(ucbParameter - offset);
            }
            if (ucbParameter + offset < maxUcbParameter) {
                values.add(ucbParameter + offset);
            }
        }

        Collections.sort(values);
        return values;
    }

    private int findBestScoreIndex(double[][] scores) {
        // Compute maxima
        double[] max = new double[scores[0].length];

        for (int j = 0; j < scores[0].length; j++) {
            max[j] = 0;

            for (int i = 0; i < scores.length; i++) {
                max[j] = Math.max(max[j], Math.abs(scores[i][j]));
            }
        }

        // Normalize
        for (int j = 0; j < scores[0].length; j++) {
            if (max[j] != 0) {
                for (int i = 0; i < scores.length; i++) {
                    scores[i][j] /= max[j];
                }
            }
        }

        // Average
        double[] average = new double[scores.length];

        for (int i = 0; i < scores.length; i++) {
            average[i] = 0;

            for (int j = 0; j < scores[i].length; j++) {
                average[i] += scores[i][j];
            }

            average[i] /= scores[i].length;
        }

        // Find best
        int bestI = -1;
        double bestAverage = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < scores.length; i++) {
            if (average[i] > bestAverage) {
                bestAverage = average[i];
                bestI = i;
            }
        }

        return bestI;
    }

    private double[] test() {
        TimedUCBPlayer.INITIAL_HEURISTIC_WEIGHT = initialHeuristicWeight;
        TimedUCBPlayer.UCB_PARAMETER = ucbParameter;
        TimedUCBPlayer player = new TimedUCBPlayer(String.format("TUCB_Mix_BSM1_Eq1_%d_%f", initialHeuristicWeight, ucbParameter), new MixedEvaluator(), new BucketSortMaxMovesOneHole(), new EqualTimeController(1000));

        List<Player> opponents = Arrays.<Player>asList(
                new RandomPlayer("Rando", new AllMoves(), new Random(1567245267354L)),
                new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves()),
                new NegaMaxPlayer("NM_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)
        //new MultiAspirationTableCutoffPlayer("MAsTC_IEV_BSM1_4", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 4)
        );

        int[][] vsScores = new int[opponents.size()][(int) Math.ceil(N_GAMES / (double) opponents.size())];

        for (int i = 0; i < N_GAMES; i++) {
            Player opponent = opponents.get(i % opponents.size());

            Board board = GameHost.setUpBoard();
            player.initialize(new BitBoard(board));
            opponent.initialize(new BitBoard(board));

            int score;

            if (i % (2 * opponents.size()) < opponents.size()) {
                // Play a game
                for (int turn = 0; turn < 15; turn++) {
                    int p1Move = player.move();
                    opponent.processMove(p1Move, false);

                    int p2Move = opponent.move();
                    if (turn < 14) {
                        player.processMove(p2Move, false);
                    }
                }

                score = -opponent.getBoard().getFinalScore();
            } else {
                // Play a game
                for (int turn = 0; turn < 15; turn++) {
                    int p1Move = opponent.move();
                    player.processMove(p1Move, false);

                    int p2Move = player.move();
                    if (turn < 14) {
                        opponent.processMove(p2Move, false);
                    }
                }

                score = player.getBoard().getFinalScore();
            }

            vsScores[i % opponents.size()][i / opponents.size()] = score;
        }

        double[] averageScore = new double[opponents.size()];

        for (int i = 0; i < opponents.size(); i++) {
            double totalScore = 0;

            for (int j = 0; j < vsScores[i].length; j++) {
                totalScore += vsScores[i][j];
            }

            averageScore[i] = totalScore / vsScores[i].length;
        }

        System.out.println(player.getName() + " - " + Arrays.toString(averageScore));

        return averageScore;
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        new ExplorationConstantOptimizer().optimize();
    }
}
