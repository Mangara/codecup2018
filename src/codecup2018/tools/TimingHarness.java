package codecup2018.tools;

import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.BucketSortMaxMovesOneHole;
import codecup2018.player.IterativeDFSPlayer;
import codecup2018.player.KillerMultiAspirationTableCutoffPlayer;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.SimpleMaxPlayer;
import codecup2018.timecontrol.ProportionalController;
import java.util.Arrays;
import java.util.Random;

public class TimingHarness {

    private static final boolean condensedOutput = false;
    private static final int GAMES = 100;

    public static void main(String[] args) {
        if (condensedOutput) {
            System.out.println("Player,Average Rando time (ms),Min Rando time (ms),1st Quartile Rando time (ms),Median Rando time (ms),3rd Quartile Rando time (ms),Max Rando time (ms),Average Expy time (ms),Min Expy time (ms),1st Quartile Expy time (ms),Median Expy time (ms),3rd Quartile Expy time (ms),Max Expy time (ms)");
        }

        //evaluateTiming(new RandomPlayer("Rando", new AllMoves()));
        //evaluateTiming(new MaxComponentPlayer(new RandomPlayer("Rando", new AllMoves())));
        //evaluateTiming(new RandomPlayer("RandMostFreeMax", new MostFreeMax()));
        //evaluateTiming(new AlphaBetaPlayer("AB_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~1s per game
        //evaluateTiming(new NegaMaxPlayer("NM_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~1s per game
        //evaluateTiming(new AspirationPlayer("As_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~0.5s per game
        //evaluateTiming(new AspirationPlayer("As_EV_MI_5", new ExpectedValue(), new MaxInfluenceMoves(), 5)); // ~0.5s per game
        //evaluateTiming(new AspirationPlayer("As_EV_MI_6", new ExpectedValue(), new MaxInfluenceMoves(), 6)); // ~2s per game
        //evaluateTiming(new AspirationPlayer("As_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5)); // ~0.1s per game
        //evaluateTiming(new MaxComponentPlayer(new NegaMaxPlayer("NM_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4))); // ~1s per game
        //evaluateTiming(new AlphaBetaPlayer("AB_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)); // ~1s per game
        //evaluateTiming(new NegaMaxPlayer("NM_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)); // ~1s per game
        //evaluateTiming(new AspirationPlayer("As_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)); // ~1s per game
        //evaluateTiming(new AspirationPlayer("As_MF_MFM_11", new MedianFree(), new MostFreeMax(), 11)); // ~2.5s per game
        //evaluateTiming(new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves()));
        //evaluateTiming(new MultiAspirationTablePlayer("MAsT_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5)); // ~0.1s per game
        //evaluateTiming(new MultiAspirationTablePlayer("MAsT_IEV_MI_6", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 6)); // ~0.5-0.7s per game
        //evaluateTiming(new MultiAspirationTablePlayer("MAsT_IEV_MI_7", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 7)); // ~2.3-3.3s per game
        //evaluateTiming(new KillerMultiAspirationTableCutoffPlayer("KMAsTC_IEV_BSM1_7", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 7)); // ~0.7-0.9s per game
        evaluateTiming(new IterativeDFSPlayer("ID_IEV_BSM1_LD1s", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), new ProportionalController(1000, ProportionalController.LINEAR_DECAY))); // ~0.9-1s per game
    }

    private static void evaluateTiming(Player player) {
        Player random = new RandomPlayer("Rando", new AllMoves(), new Random(44923260));
        long[] randoTimes = new long[GAMES];

        //System.out.println("Playing versus Rando ...");
        for (int i = 0; i < GAMES; i++) {
            long start = System.nanoTime();
            if (i % 2 == 0) {
                GameHost.runGame(random, player, false);
            } else {
                GameHost.runGame(player, random, false);
            }
            randoTimes[i] = System.nanoTime() - start;

            if (i % 5 == 0) {
                //System.out.printf("%10d/%d%n", i, GAMES);
            }
        }

        Player expy = new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves());
        long[] expyTimes = new long[GAMES];

        //System.out.println("Playing versus Expy ...");
        for (int i = 0; i < GAMES; i++) {
            long start = System.nanoTime();
            if (i % 2 == 0) {
                GameHost.runGame(expy, player, false);
            } else {
                GameHost.runGame(player, expy, false);
            }
            expyTimes[i] = System.nanoTime() - start;

            if (i % 5 == 0) {
                //System.out.printf("%10d/%d%n", i, GAMES);
            }
        }

        Arrays.sort(randoTimes);
        Arrays.sort(expyTimes);

        long totalRandoTime = 0;
        long totalExpyTime = 0;

        for (int i = 0; i < GAMES; i++) {
            totalRandoTime += randoTimes[i];
            totalExpyTime += expyTimes[i];
        }

        System.out.printf(
                (condensedOutput
                        ? "%s,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f%n"
                        : "Player: %s%n"
                        + "Average Rando time (ms): %.3f%n"
                        + "Quartiles: %.3f - %.3f - %.3f - %.3f - %.3f%n"
                        + "Average Expy time (ms): %.3f%n"
                        + "Quartiles: %.3f - %.3f - %.3f - %.3f - %.3f%n%n"),
                player.getName(),
                nsToMs(totalRandoTime / GAMES),
                nsToMs(randoTimes[0]),
                nsToMs(randoTimes[GAMES / 4]),
                nsToMs(randoTimes[GAMES / 2]),
                nsToMs(randoTimes[(3 * GAMES) / 4]),
                nsToMs(randoTimes[GAMES - 1]),
                nsToMs(totalExpyTime / GAMES),
                nsToMs(expyTimes[0]),
                nsToMs(expyTimes[GAMES / 4]),
                nsToMs(expyTimes[GAMES / 2]),
                nsToMs(expyTimes[(3 * GAMES) / 4]),
                nsToMs(expyTimes[GAMES - 1])
        );
    }

    private static double nsToMs(long nanoseconds) {
        return nanoseconds / 1000000.0;
    }

}
