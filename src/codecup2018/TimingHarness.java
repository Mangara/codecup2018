package codecup2018;

import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.SimpleMaxPlayer;
import java.util.Arrays;
import java.util.Random;

public class TimingHarness {

    private static final int GAMES = 100;

    public static void main(String[] args) {
        System.out.println("Player,Average Rando time (ms),Min Rando time (ms),1st Quartile Rando time (ms),Median Rando time (ms),3rd Quartile Rando time (ms),Max Rando time (ms),Average Expy time (ms),Min Expy time (ms),1st Quartile Expy time (ms),Median Expy time (ms),3rd Quartile Expy time (ms),Max Expy time (ms)");

        //evaluateTiming(new RandomPlayer("Rando", new AllMoves()));
        //evaluateTiming(new MaxComponentPlayer(new RandomPlayer("Rando", new AllMoves())));
        //evaluateTiming(new RandomPlayer("RandMostFreeMax", new MostFreeMax()));
        //evaluateTiming(new AlphaBetaPlayer("AB_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~1s per game
        //evaluateTiming(new NegaMaxPlayer("NM_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~1s per game
        //evaluateTiming(new AspirationPlayer("As_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~0.5s per game
        //evaluateTiming(new AspirationPlayer("As_EV_MI_5", new ExpectedValue(), new MaxInfluenceMoves(), 5)); // ~0.5s per game
        //evaluateTiming(new AspirationPlayer("As_EV_MI_6", new ExpectedValue(), new MaxInfluenceMoves(), 6)); // ~2s per game
        evaluateTiming(new AspirationPlayer("As_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5)); // ~0.4s per game
        //evaluateTiming(new MaxComponentPlayer(new NegaMaxPlayer("NM_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4))); // ~1s per game
        //evaluateTiming(new AlphaBetaPlayer("AB_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)); // ~1s per game
        //evaluateTiming(new NegaMaxPlayer("NM_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)); // ~1s per game
        //evaluateTiming(new AspirationPlayer("As_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)); // ~1s per game
        //evaluateTiming(new AspirationPlayer("As_MF_MFM_11", new MedianFree(), new MostFreeMax(), 11)); // ~2.5s per game
        //evaluateTiming(new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves()));

        /*/// Aspiration window search
        AspirationPlayer asp = new AspirationPlayer("As_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4);
        
        for (int i = 640000; i > 100; i /= 2) {
            AspirationPlayer.WINDOW_SIZE = i;
            System.out.print(Integer.toString(i) + ',');
            evaluateTiming(asp);
        }
        
        AspirationPlayer asp2 = new AspirationPlayer("As_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10);
        
        for (int i = 640000; i > 100; i /= 2) {
            AspirationPlayer.WINDOW_SIZE = i;
            System.out.print(Integer.toString(i) + ',');
            evaluateTiming(asp2);
        }
        //*/
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

        
        System.out.printf("%s,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f%n", 
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
