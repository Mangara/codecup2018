package codecup2018.tools;

import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.BucketSortMaxMovesOneHole;
import codecup2018.player.IterativeDFSPlayer;
import codecup2018.player.Player;
import codecup2018.player.SimpleMaxPlayer;
import codecup2018.timecontrol.ProportionalController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import mga.GeneticAlgorithm;
import mga.Pair;
import mga.crossover.Crossover;
import mga.crossover.OnePointCrossover;
import mga.mutation.Mutation;
import mga.quality.QualityFunction;
import mga.selection.RouletteSelection;

public class TimeControlGA {

    private static final int TIME_PER_GAME = 400;
    private static final int POPULATION_SIZE = 20;
    private static final int ITERATIONS = 100;
    private static final int GAMES_PER_EVALUATION = 25;
    private static final List<Player> OPPONENTS = Arrays.<Player>asList(
            //new RandomPlayer("Rando", new AllMoves()),
            new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves())
    );
    private static final List<Double> OPPONENT_WEIGHT = Arrays.asList(1.0);

    public static void main(String[] args) {
        estimateTime();

        GeneticAlgorithm<List<Double>> ga = new GeneticAlgorithm<>(new DuelQuality(), new TimeCrossover(), new TimeMutation(), new RouletteSelection());
        ga.setElitistFraction(0.001); // Carry over the best individual each generation
        //ga.setDebugLevel(GeneticAlgorithm.DebugLevel.EVERYTHING);
        ga.setParallelEvaluation(false);
        ga.initialize(createInitialPopulation());
        Pair<List<Double>, Double> best = ga.getBestAfter(ITERATIONS);

        System.out.println("Best: " + best);
    }

    private static List<List<Double>> createInitialPopulation() {
        List<List<Double>> population = new ArrayList<>(POPULATION_SIZE);

        population.add(equalTime());

        for (int i = 1; i < POPULATION_SIZE; i++) {
            population.add(randomTime());
        }

        return population;
    }

    public static List<Double> equalTime() {
        List<Double> equalTime = new ArrayList<>(15);

        for (int i = 0; i < 15; i++) {
            equalTime.add(1.0 / (15 - i));
        }

        return equalTime;
    }

    public static List<Double> randomTime() {
        List<Double> randomTime = new ArrayList<>(15);

        for (int i = 0; i < 15; i++) {
            randomTime.add(Math.random());
        }

        return randomTime;
    }

    public static double[] toArray(List<Double> individual) {
        final double[] times = new double[individual.size()];
        for (int i = 0; i < times.length; i++) {
            times[i] = individual.get(i);
        }
        return times;
    }

    private static void estimateTime() {
        long milliseconds = ITERATIONS * POPULATION_SIZE * OPPONENTS.size() * GAMES_PER_EVALUATION * TIME_PER_GAME;
        String duration;

        if (milliseconds < 1000) {
            duration = String.format("%d ms", milliseconds);
        } else if (milliseconds < 60 * 1000) {
            duration = String.format("%.1f s", milliseconds / 1000.0);
        } else if (milliseconds < 60 * 60 * 1000) {
            duration = String.format("%.1f m", milliseconds / (1000.0 * 60));
        } else {
            duration = String.format("%.1f h", milliseconds / (1000.0 * 60 * 60));
        }

        System.out.println("Estimated duration: " + duration);
    }

    private static class DuelQuality implements QualityFunction<List<Double>> {

        @Override
        public double computeQuality(List<Double> individual) {
            //Player p = new TimedUCBPlayer("TUCB", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), new ProportionalController(TIME_PER_GAME, times));
            Player p = new IterativeDFSPlayer("ID_IEV_BSM1", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), new ProportionalController(TIME_PER_GAME, toArray(individual)));
            double weightedScore = 0;

            for (int i = 0; i < OPPONENTS.size(); i++) {
                Player opp = OPPONENTS.get(i);
                double oppWeight = OPPONENT_WEIGHT.get(i);

                long totalScore = 0;

                for (int k = 0; k < GAMES_PER_EVALUATION; k++) {
                    totalScore += (k % 2 == 0
                            ? GameHost.runGame(p, opp, false)
                            : -GameHost.runGame(opp, p, false));
                }

                weightedScore += oppWeight * (totalScore / (double) GAMES_PER_EVALUATION);
            }

            return weightedScore;
        }

    }

    private static class TimeMutation implements Mutation<List<Double>> {

        @Override
        public List<Double> mutate(List<Double> individual) {
            List<Double> result = new ArrayList<>(individual);

            if (ThreadLocalRandom.current().nextDouble() < 0.1) {
                // Big mutation
                for (int i = 0; i < result.size(); i++) {
                    double newVal = result.get(i) + 0.5 * (ThreadLocalRandom.current().nextDouble() - 0.5);
                    result.set(i, Math.min(Math.max(newVal, 0), 1));
                }
            } else {
                // Small mutation
                int i = ThreadLocalRandom.current().nextInt(result.size());
                double newVal = result.get(i) + 0.2 * (ThreadLocalRandom.current().nextDouble() - 0.5);
                result.set(i, Math.min(Math.max(newVal, 0), 1));
            }

            return result;
        }

    }

    private static class TimeCrossover implements Crossover<List<Double>> {

        private final Crossover<List<Double>> onePoint = new OnePointCrossover<>();

        @Override
        public Pair<List<Double>, List<Double>> crossover(List<Double> parent1, List<Double> parent2) {
            if (ThreadLocalRandom.current().nextDouble() < 0.7) {
                // Weighted average
                int chromosomeLength = parent1.size();
                double weight1 = ThreadLocalRandom.current().nextDouble();
                double weight2 = 1 - weight1;

                List<Double> child1 = new ArrayList<>(chromosomeLength);
                List<Double> child2 = new ArrayList<>(chromosomeLength);

                for (int i = 0; i < chromosomeLength; i++) {
                    child1.add(weight1 * parent1.get(i) + weight2 * parent2.get(i));
                    child2.add(weight2 * parent1.get(i) + weight1 * parent2.get(i));
                }

                return new Pair<>(child1, child2);
            } else {
                // 1-point
                return onePoint.crossover(parent1, parent2);
            }
        }

    }
}
