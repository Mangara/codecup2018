package codecup2018.tools;

import codecup2018.data.ArrayBoard;
import codecup2018.data.BitBoard;
import codecup2018.data.Board;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.NoHoles;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.SimpleMaxPlayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomPositionGenerator {

    public static Collection<Object[]> generateRealisticTestData(int n) {
        List<Board> testBoards = generateRealisticTestBoards(n);
        Collection<Object[]> data = new ArrayList<>();

        for (Board testBoard : testBoards) {
            data.add(new Object[]{testBoard});
        }

        return data;
    }

    public static List<Board> generateRealisticTestBoards(int n) {
        Random rand = new Random(598959643);
        List<Board> boards = new ArrayList<>();

        // Generate some pseudo-random test boards
        while (boards.size() < n) {
            boards.addAll(generateRealisticTestBoards(rand));
        }

        return boards.subList(0, n);
    }
    
    public static List<byte[][]> generateRandomTestBoards(int n) {
        Random rand = new Random(598949643);
        List<byte[][]> boards = new ArrayList<>();

        // Generate some pseudo-random test boards
        while (boards.size() < n) {
            boards.add(generateRandomTestBoard(rand));
        }

        return boards;
    }

    private static List<Board> generateRealisticTestBoards(Random rand) {
        // Generate a pseudo-random game
        double SELECTION_CHANCE = 0.1; // Sample 10% of positions
        Player p1 = new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves());
        Player p2 = new RandomPlayer("R2", new NoHoles(), rand);

        // Randomly select player1
        if (rand.nextBoolean()) {
            Player temp = p1;
            p1 = p2;
            p2 = temp;
        }

        Board board = new BitBoard();
        List<Board> testPositions = new ArrayList<>();

        // Block 5 locations
        Set<Integer> blocked = new HashSet<>();

        while (blocked.size() < 5) {
            blocked.add(rand.nextInt(36));
        }

        int location = 0;
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                if (blocked.contains(location)) {
                    board.block(pos);
                }
                location++;
            }
        }

        p1.initialize(new BitBoard(board));
        p2.initialize(new BitBoard(board));

        // Play a game
        for (int i = 0; i < 15; i++) {
            if (rand.nextDouble() < SELECTION_CHANCE) {
                testPositions.add(new BitBoard(p1.getBoard()));
            }

            int p1Move = p1.move();
            p2.processMove(p1Move, false);

            if (rand.nextDouble() < SELECTION_CHANCE) {
                testPositions.add(new BitBoard(p2.getBoard()));
            }

            int p2Move = p2.move();
            if (i < 14) {
                p1.processMove(p2Move, false);
            }
        }

        return testPositions;
    }

    private static byte[][] generateRandomTestBoard(Random rand) {
        byte[][] board = new byte[8][8];
        boolean[] used1 = new boolean[16];
        boolean[] used2 = new boolean[16];
        
        // Block 5 spaces
        Set<Integer> blocked = new HashSet<>();

        while (blocked.size() < 5) {
            blocked.add(rand.nextInt(36));
        }

        int location = 0;
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (blocked.contains(location)) {
                    board[a][b] = Board.BLOCKED;
                }
                location++;
            }
        }
        
        // Pick random
        boolean player1 = true;
        for (int turns = rand.nextInt(31); turns > 0; turns--) {
            byte a, b;
            do {
                a = (byte) rand.nextInt(8);
                b = (byte) rand.nextInt(8);
            } while (!(Board.isValidPos(Board.getPos(a, b)) && board[a][b] == Board.FREE));
            
            byte v;
            do {
                v = (byte) (1 + rand.nextInt(15));
            } while ((player1 && used1[v]) || (!player1 && used2[v]));
            
            //System.err.printf("Turn: %d. Move: %s%n", turns, Board.moveToString(Board.buildMove(Board.getPos(a, b), (turns % 2 == 0 ? v : (byte) -v), 0)));
            
            board[a][b] = (player1 ? v : (byte) -v);
            
            if (player1) {
                used1[v] = true;
            } else {
                used2[v] = true;
            }
            
            player1 = !player1;
        }
        
        return board;
    }

}
