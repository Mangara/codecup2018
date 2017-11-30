package codecup2018;

import codecup2018.data.ArrayBoard;
import codecup2018.data.Board;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.evaluator.MedianFree;
import codecup2018.movegenerator.AllMoves;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.movegenerator.MostFreeMax;
import codecup2018.movegenerator.NoHoles;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.Player;
import codecup2018.player.RandomPlayer;
import codecup2018.player.SimpleMaxPlayer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EndgameTest {

    private final Board board;
    //private final Player player = new AlphaBetaPlayer("AB_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10);
    //private final Player player = new SimpleMaxPlayer("Mifi", new MedianFree(), new AllMoves());
    private final Player player = new AspirationPlayer("As_IEV_MI_5", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 5);

    public EndgameTest(Board board) throws IOException {
        this.board = board;
    }

    @Parameters
    public static Collection<Object[]> data() {
        int N_TESTS = 100;
        Random rand = new Random(829278166);
        Collection<Object[]> boards = new ArrayList<>();

        // Generate some pseudo-random test boards
        for (int i = 0; i < N_TESTS; i++) {
            Board testBoard = generateTestBoard(rand);
            boards.add(new Object[]{testBoard});
        }

        return boards;
    }

    private static Board generateTestBoard(Random rand) {
        // Generate a pseudo-random endgame by playing two no-holes random players against each other
        Player p1 = new RandomPlayer("R1", new NoHoles(), rand);
        Player p2 = new RandomPlayer("R2", new NoHoles(), rand);

        ArrayBoard board = new ArrayBoard();

        p1.initialize();
        p2.initialize();

        // Block 5 locations
        Set<Integer> blocked = new HashSet<>();

        while (blocked.size() < 5) {
            blocked.add(rand.nextInt(36));
        }

        int location = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                if (blocked.contains(location)) {
                    board.set(a, (byte) (pos % 8), Board.BLOCKED);
                    p1.block(pos);
                    p2.block(pos);
                }
                location++;
            }
        }

        for (int i = 0; i < 15; i++) {
            byte[] p1Move = p1.move();

            if (doMove(board, p1Move, true)) {
                return board;
            }

            p2.processMove(p1Move, false);

            byte[] p2Move = p2.move();

            if (doMove(board, p2Move, false)) {
                return p2.getBoard();
            }

            if (i < 14) {
                p1.processMove(p2Move, false);
            }
        }

        return board;
    }

    private static boolean doMove(ArrayBoard board, byte[] move, boolean player1) {
        if (board.getFreeSpotsAround(Board.getPos(move[0], move[1])) == 0) {
            return true;
        }

        // Apply move
        board.set(move[0], move[1], (player1 ? move[2] : (byte) -move[2]));

        return false;
    }

    @Test
    public void runTest() throws IOException {
        // Copy info for debug purposes
        ArrayBoard originalBoard = new ArrayBoard(board);

        // Figure out the right moves
        List<int[]> holes = new ArrayList<>();

        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                if (board.get(pos) == Board.FREE) {
                    holes.add(new int[]{a, (byte) (pos % 8), board.getHoleValue(pos)});
                }
            }
        }

        Collections.sort(holes, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return Integer.compare(o1[2], o2[2]);
            }
        });

        int movesLeft = 15;

        for (byte i = 1; i <= 15; i++) {
            if (board.haveIUsed(i)) {
                movesLeft--;
            }
        }

        int targetScore = holes.get(movesLeft)[2];

        // Test them
        int holesLeft = holes.size();
        boolean[] closed = new boolean[holesLeft];
        boolean[] playerClosed = new boolean[holesLeft];

        player.initialize(board);

        while (holesLeft > 1) {
            byte[] move = player.move();

            for (int i = 0; i < holes.size(); i++) {
                int[] hole = holes.get(i);
                if (move[0] == hole[0] && move[1] == hole[1]) {
                    closed[i] = true;
                    playerClosed[i] = true;
                    break;
                }
            }

            holesLeft--;
            if (holesLeft == 1) {
                break;
            }

            int[] hole = null;

            for (int i = holes.size() - 1; i >= 0; i--) {
                if (!closed[i]) {
                    hole = holes.get(i);
                    closed[i] = true;
                    holesLeft--;
                    break;
                }
            }

            for (byte j = 1; j <= 15; j++) {
                if (!board.hasOppUsed(j)) {
                    player.processMove(new byte[] {(byte) hole[0], (byte) hole[1], j}, false);
                    break;
                }
            }
        }

        // Check that the player achieved the optimal score
        int finalScore = 0;
        
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                if (board.get(pos) == Board.FREE) {
                    finalScore = board.getHoleValue(pos);
                }
            }
        }

        if (targetScore != finalScore) {
            System.err.println("Starting board:");
            Board.print(originalBoard);
            System.err.print("Holes: ");
            for (int[] hole : holes) {
                System.err.print(Arrays.toString(hole) + ", ");
            }
            System.err.println();
            System.err.println("Player had " + movesLeft + " moves left");
            System.err.println("Expected final score: " + targetScore);
            System.err.println("Real final score:     " + finalScore);
            System.err.print("Player closed holes: ");
            for (int i = 0; i < holes.size(); i++) {
                if (playerClosed[i]) {
                    System.err.print(Arrays.toString(holes.get(i)) + ", ");
                }
            }
            System.err.println();
            System.err.println("Final board:");
            Board.print(board);
            System.err.println();
            fail("Player did not close optimal holes");
        }
    }
}
