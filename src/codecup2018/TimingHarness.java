package codecup2018;

import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.MedianFree;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.MostFreeMax;
import codecup2018.movegenerator.NoHolesMax;
import codecup2018.player.AlphaBetaPlayer;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.MaxComponentPlayer;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.SimpleMaxPlayer;

public class TimingHarness {

    private static final long GAMES = 400;

    public static void main(String[] args) {
        System.out.println("Player,Rando time (ms),Expy time (ms)");
        
        //evaluateTiming(new RandomPlayer("Rando", new AllMoves()));
        //evaluateTiming(new MaxComponentPlayer(new RandomPlayer("Rando", new AllMoves())));
        //evaluateTiming(new RandomPlayer("RandMostFreeMax", new MostFreeMax()));
        //evaluateTiming(new AlphaBetaPlayer("AB_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~1s per game
        //evaluateTiming(new NegaMaxPlayer("NM_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~1s per game
        //evaluateTiming(new AspirationPlayer("As_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~?s per game
        //evaluateTiming(new MaxComponentPlayer(new NegaMaxPlayer("NM_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4))); // ~1s per game
        //evaluateTiming(new AlphaBetaPlayer("AB_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)); // ~1s per game
        //evaluateTiming(new NegaMaxPlayer("NM_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)); // ~1s per game
        //evaluateTiming(new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves()));

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
    }

    private static void evaluateTiming(Player player) {
        Player random = new RandomPlayer("Rando", new AllMoves());
        Player expy = new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves());

        // Play vs Random and Expy
        long start = System.nanoTime();
        for (int i = 0; i < GAMES; i++) {
            if (i % 2 == 0) {
                GameHost.runGame(random, player, false);
            } else {
                GameHost.runGame(player, random, false);
            }
        }
        long vsRandom = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < GAMES; i++) {
            if (i % 2 == 0) {
                GameHost.runGame(expy, player, false);
            } else {
                GameHost.runGame(player, expy, false);
            }
        }
        long vsExpy = System.nanoTime() - start;

        System.out.printf("%s,%.3f,%.3f%n", player.getName(), vsRandom / (double) (GAMES * 1000000), vsExpy / (double) (GAMES * 1000000));
    }

}
