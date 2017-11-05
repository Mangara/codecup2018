package codecup2018;

import codecup2018.evaluator.ExpectedValue;
import codecup2018.movegenerator.AllMoves;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.SimpleMaxPlayer;
import codecup2018.player.RoulettePlayer;
import java.util.Arrays;
import java.util.List;

public class Tournament {

    private static final int GAMES = 1000;

    public static void main(String[] args) {
        runTournament(Arrays.asList(
                new RandomPlayer("Rando", new AllMoves()), 
                new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves()), 
                new RoulettePlayer("ExpD", new ExpectedValue(), new AllMoves(), 4)));
    }

    public static void runTournament(List<Player> players) {
        int n = players.size();
        int[][] wins = new int[n][n];
        double[][] avgScore = new double[n][n];
        
        long totalGames = (n * (n - 1) * GAMES) / 2;
        long currentGame = 0;
        System.out.println("Playing all the games:");

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double totalScore = 0;

                for (int k = 0; k < GAMES; k++) {
                    int score = GameHost.runGame(players.get(i), players.get(j), false);
                    totalScore += score;

                    if (score > 0) {
                        wins[i][j]++;
                    } else if (score < 0) {
                        wins[j][i]++;
                    }
                    
                    currentGame++;
                    if (currentGame % 100 == 0) {
                        System.out.printf("%10d/%d%n", currentGame, totalGames);
                    }
                }

                avgScore[i][j] = totalScore / GAMES;
                avgScore[j][i] = -avgScore[i][j];
            }
        }

        report(players, wins, avgScore);
    }

    private static void report(List<Player> players, int[][] wins, double[][] avgScore) {
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
    }
}
