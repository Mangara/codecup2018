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

    private static final int N_GAMES = 6;

    private static final int minInitialHeuristicWeight = 1;
    private static final int maxInitialHeuristicWeight = 1000;
    private int initialHeuristicWeight = 500;

    private static final double minUcbParameter = 0;
    private static final double maxUcbParameter = 5;
    private double ucbParameter = 1;

    public void optimize() throws NoSuchFieldException, IllegalAccessException {
        double temperature = 1;

        while (temperature > 0.00001) {
            optimizeIHW(temperature);
            optimizeUCB(temperature);
            temperature *= 0.98;
        }
    }

    private void optimizeIHW(double temperature) {
        List<Integer> values = getValuesInIHWWindow(temperature);

        int bestValue = -1;
        double bestQuality = Double.NEGATIVE_INFINITY;

        for (Integer value : values) {
            initialHeuristicWeight = value;
            double quality = test();

            if (quality > bestQuality) {
                bestQuality = quality;
                bestValue = value;
            }
        }

        initialHeuristicWeight = bestValue;
    }
    
    private void optimizeUCB(double temperature) {
        List<Double> values = getValuesInUCBWindow(temperature);

        double bestValue = -1;
        double bestQuality = Double.NEGATIVE_INFINITY;

        for (Double value : values) {
            ucbParameter = value;
            double quality = test();

            if (quality > bestQuality) {
                bestQuality = quality;
                bestValue = value;
            }
        }

        ucbParameter = bestValue;
    }
    
    private static final int N_SAMPLES = 5;
    
    private List<Integer> getValuesInIHWWindow(double temperature) {
        int windowSize = Math.min((int) Math.ceil(0.5 * temperature * (maxInitialHeuristicWeight - minInitialHeuristicWeight)), 
                Math.min(initialHeuristicWeight - minInitialHeuristicWeight, maxInitialHeuristicWeight - initialHeuristicWeight));
        List<Integer> values = new ArrayList<>();
        
        values.add(initialHeuristicWeight);
        for (int i = 0; i < N_SAMPLES; i++) {
            int offset = (int) Math.round(((i + 1) / (double) N_SAMPLES) * windowSize);
            values.add(initialHeuristicWeight - offset);
            values.add(initialHeuristicWeight + offset);
        }
        
        Collections.sort(values);
        return values;
    }
    
    private List<Double> getValuesInUCBWindow(double temperature) {
        double windowSize = Math.min(0.5 * temperature * (maxUcbParameter - minUcbParameter),
                Math.min(ucbParameter - minUcbParameter, maxUcbParameter - ucbParameter));
        List<Double> values = new ArrayList<>();
        
        values.add(ucbParameter);
        for (int i = 0; i < N_SAMPLES; i++) {
            double offset = ((i + 1) / (double) N_SAMPLES) * windowSize;
            values.add(ucbParameter - offset);
            values.add(ucbParameter + offset);
        }
        
        Collections.sort(values);
        return values;
    }

    private double[] test() {
        TimedUCBPlayer.INITIAL_HEURISTIC_WEIGHT = initialHeuristicWeight;
        TimedUCBPlayer.UCB_PARAMETER = ucbParameter;
        TimedUCBPlayer player = new TimedUCBPlayer(String.format("TUCB_Mix_BSM1_Eq0.1_%d_%f", initialHeuristicWeight, ucbParameter), new MixedEvaluator(), new BucketSortMaxMovesOneHole(), new EqualTimeController(100));

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
            double max = maxVsScore[i];
            double totalNormalizedScore = 0;
            
            for (int j = 0; j < vsScores[i].length; j++) {
                totalNormalizedScore += vsScores[i][j] / max;
            }
            
            averageNormalizedScores += totalNormalizedScore / vsScores[i].length;
        }
        
        averageNormalizedScores /= opponents.size();

        System.out.println(player.getName() + " - " + averageNormalizedScores);
        
        return averageScore;
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        new ExplorationConstantOptimizer().optimize();
    }
}
