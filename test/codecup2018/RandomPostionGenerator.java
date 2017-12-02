package codecup2018;

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

public class RandomPostionGenerator {

    static Collection<Object[]> generateTestData(int n) {
        List<Board> testBoards = generateAllTestBoards(n);
        Collection<Object[]> data = new ArrayList<>();

        for (Board testBoard : testBoards) {
            data.add(new Object[]{testBoard});
        }

        return data;
    }

    static List<Board> generateAllTestBoards(int n) {
        Random rand = new Random(598959643);
        List<Board> boards = new ArrayList<>();

        // Generate some pseudo-random test boards
        while (boards.size() < n) {
            boards.addAll(generateTestBoards(rand));
        }

        return boards.subList(0, n);
    }

    private static List<Board> generateTestBoards(Random rand) {
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

}
