package codecup2018;

import codecup2018.evaluator.ExpectedValue;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.MostFreeMax;
import codecup2018.movegenerator.NoHolesMax;
import codecup2018.player.AlphaBetaPlayer;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.SimpleMaxPlayer;

public class TimingHarness {

    private static final long GAMES = 25;

    public static void main(String[] args) {
        //evaluateTiming(new RandomPlayer("Rando", new AllMoves()));
        //evaluateTiming(new RandomPlayer("RandMostFreeMax", new MostFreeMax()));
        //evaluateTiming(new AlphaBetaPlayer("AB_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~1s per game
        evaluateTiming(new NegaMaxPlayer("NM_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4)); // ~?s per game
        //evaluateTiming(new AlphaBetaPlayer("AB_MF_10", new ExpectedValue(), new MostFreeMax(), 10)); // ~1s per game
        //evaluateTiming(new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves()));
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
        
        System.out.println(player.getName());
        System.out.println();
        System.out.println("Average time per game versus:");
        System.out.printf("Rando - %.5f ms%n", vsRandom / (double) (GAMES * 1000000));
        System.out.printf("Expy  - %.5f ms%n", vsExpy / (double) (GAMES * 1000000));
    }

}
