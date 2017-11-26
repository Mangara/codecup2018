package codecup2018.tools;

import codecup2018.Pair;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.MedianFree;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.movegenerator.MostFreeMax;
import codecup2018.movegenerator.NoHoles;
import codecup2018.movegenerator.NoHolesMax;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.SimpleMaxPlayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Tournament {

    private static final int GAMES = 1000;

    public static void main(String[] args) {
        runTournament(Arrays.<Player>asList(
                new RandomPlayer("Rando", new AllMoves()),
                new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()),
                new AspirationPlayer("As_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4),
                new AspirationPlayer("As_EV_MI_6", new ExpectedValue(), new MaxInfluenceMoves(), 6), // (best so far)
                new NegaMaxPlayer("NM_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10)
        ));
    }

    public static void runTournament(List<Player> players) {
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

        System.out.println("Average score:");

        System.out.printf("%" + AVG_CELL_WIDTH + "s", "");
        for (int i = 0; i < players.size(); i++) {
            System.out.printf("%" + AVG_CELL_WIDTH + "d", i);
        }
        System.out.println();

        for (int i = 0; i < players.size(); i++) {
            System.out.printf("%" + AVG_CELL_WIDTH + "d", i);
            for (int j = 0; j < players.size(); j++) {
                System.out.printf("%" + AVG_CELL_WIDTH + "." + (AVG_CELL_WIDTH - 3) + "g", avgScore[i][j]);
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
        double tt = t / Math.sqrt(2);
        double erftt;

        if (t < 1) {
            erftt = (2 / Math.sqrt(Math.PI)) * (tt - Math.pow(tt, 3) / 3 + Math.pow(tt, 5) / 10 - Math.pow(tt, 7) / 42);
        } else {
            erftt = 1 - (Math.exp(-tt * tt) / Math.sqrt(Math.PI)) * (1 / tt - 0.5 / Math.pow(tt, 3) + 0.75 / Math.pow(tt, 5) - 1.875 / Math.pow(tt, 7));
        }

        return 1 - 0.5 * (erftt + 1);
    }
}
