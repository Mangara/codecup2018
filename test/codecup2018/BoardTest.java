package codecup2018;

import codecup2018.data.ArrayBoard;
import codecup2018.data.Board;
import codecup2018.data.CachingBoard;
import codecup2018.tools.RandomPositionGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BoardTest {

    private final byte[][] grid;
    private final Board board;

    public BoardTest(byte[][] grid, Board board) {
        this.grid = grid;
        this.board = board;
    }

    private String printBoards() {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append(board.getClass());
        sb.append('\n');

        for (byte h = 0; h < 8; h++) {
            // Grid
            sb.append(String.format("%" + (2 * (7 - h) + 1) + "s", ""));
            for (byte i = 0; i <= h; i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                byte value = grid[h - i][i];
                sb.append(value == Board.BLOCKED ? "  X" : String.format("%3d", value));
            }
            sb.append(String.format("%" + (2 * (7 - h) + 1) + "s", ""));

            // Board
            sb.append(String.format("%" + (2 * (7 - h) + 1) + "s", ""));
            for (byte i = 0; i <= h; i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                byte value = board.get(Board.getPos((byte) (h - i), i));
                sb.append(value == Board.BLOCKED ? "  X" : String.format("%3d", value));
            }
            sb.append(String.format("%" + (2 * (7 - h) + 1) + "s%n", ""));
        }

        return sb.toString();
    }

    @Test
    public final void testGet() {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                assertEquals(printBoards() + Board.posToString(Board.getPos(a, b)), grid[a][b], board.get(Board.getPos(a, b)));
            }
        }
    }

    @Test
    public final void testHaveIUsed() {
        boolean[] used = new boolean[16];

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                byte v = grid[a][b];

                if (v != Board.FREE && v != Board.BLOCKED && v > 0) {
                    used[v] = true;
                }
            }
        }

        for (byte v = 1; v <= 15; v++) {
            assertEquals(printBoards() + v, used[v], board.haveIUsed(v));
        }
    }

    @Test
    public final void testHasOppUsed() {
        boolean[] used = new boolean[16];

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                byte v = grid[a][b];

                if (v != Board.FREE && v != Board.BLOCKED && v < 0) {
                    used[-v] = true;
                }
            }
        }

        for (byte v = 1; v <= 15; v++) {
            assertEquals(printBoards() + v, used[v], board.hasOppUsed(v));
        }
    }

    @Test
    public final void testIsFree() {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                assertEquals(printBoards() + Board.posToString(Board.getPos(a, b)), grid[a][b] == Board.FREE, board.isFree(Board.getPos(a, b)));
            }
        }
    }

    @Test
    public final void testGetNFreeSpots() {
        int nFree = 0;
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (grid[a][b] == Board.FREE) {
                    nFree++;
                }
            }
        }

        assertEquals(printBoards(), nFree, board.getNFreeSpots());
    }

    @Test
    public final void testGetHoleValue() {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                int holeValue = 0;

                if (a > 0 && grid[a - 1][b] != Board.BLOCKED) {
                    holeValue += grid[a - 1][b];
                }

                if (a + 1 + b < 8 && grid[a + 1][b] != Board.BLOCKED) {
                    holeValue += grid[a + 1][b];
                }

                if (b > 0 && grid[a][b - 1] != Board.BLOCKED) {
                    holeValue += grid[a][b - 1];
                }

                if (a + b + 1 < 8 && grid[a][b + 1] != Board.BLOCKED) {
                    holeValue += grid[a][b + 1];
                }

                if (a > 0 && grid[a - 1][b + 1] != Board.BLOCKED) {
                    holeValue += grid[a - 1][b + 1];
                }

                if (b > 0 && grid[a + 1][b - 1] != Board.BLOCKED) {
                    holeValue += grid[a + 1][b - 1];
                }

                assertEquals(printBoards() + Board.posToString(Board.getPos(a, b)), holeValue, board.getHoleValue(Board.getPos(a, b)));
            }
        }
    }

    @Test
    public final void testGetFreeSpotsAround() {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                int freeNeighbours = 0;

                if (a > 0 && grid[a - 1][b] == Board.FREE) {
                    freeNeighbours++;
                }

                if (a + 1 + b < 8 && grid[a + 1][b] == Board.FREE) {
                    freeNeighbours++;
                }

                if (b > 0 && grid[a][b - 1] == Board.FREE) {
                    freeNeighbours++;
                }

                if (a + b + 1 < 8 && grid[a][b + 1] == Board.FREE) {
                    freeNeighbours++;
                }

                if (a > 0 && grid[a - 1][b + 1] == Board.FREE) {
                    freeNeighbours++;
                }

                if (b > 0 && grid[a + 1][b - 1] == Board.FREE) {
                    freeNeighbours++;
                }

                assertEquals(printBoards() + Board.posToString(Board.getPos(a, b)), freeNeighbours, board.getFreeSpotsAround(Board.getPos(a, b)));
            }
        }
    }

    @Test
    public final void testGetFreeSpots() {
        Set<Byte> free = new HashSet<>();

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (grid[a][b] == Board.FREE) {
                    free.add(Board.getPos(a, b));
                }
            }
        }

        Set<Byte> boardFree = new HashSet<>();
        for (Byte pos : board.getFreeSpots()) {
            boardFree.add(pos);
        }

        assertEquals(printBoards(), free, boardFree);
    }

    @Test
    public final void testIsGameOver() {
        int free = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (grid[a][b] == Board.FREE) {
                    free++;
                }
            }
        }

        assertEquals(printBoards(), free == 1, board.isGameOver());
    }

    @Test
    public final void testIsGameInEndGame() {
        boolean endGame = true;

        for (byte a = 0; a < 8 && endGame; a++) {
            for (byte b = 0; b < 8 - a && endGame; b++) {
                if (grid[a][b] == Board.FREE) {
                    if (a > 0 && grid[a - 1][b] == Board.FREE) {
                        endGame = false;
                    }

                    if (a + 1 + b < 8 && grid[a + 1][b] == Board.FREE) {
                        endGame = false;
                    }

                    if (b > 0 && grid[a][b - 1] == Board.FREE) {
                        endGame = false;
                    }

                    if (a + b + 1 < 8 && grid[a][b + 1] == Board.FREE) {
                        endGame = false;
                    }

                    if (a > 0 && grid[a - 1][b + 1] == Board.FREE) {
                        endGame = false;
                    }

                    if (b > 0 && grid[a + 1][b - 1] == Board.FREE) {
                        endGame = false;
                    }
                }
            }
        }

        assertEquals(printBoards(), endGame, board.isGameInEndGame());
    }

    @Test
    public final void testGetTranspositionTableKey() {
        int key = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                key ^= Board.KEY_POSITION_NUMBERS[32 * (8 * a + b) + grid[a][b] + 15];
            }
        }

        assertEquals(printBoards(), key, board.getTranspositionTableKey());
    }

    @Test
    public final void testGetHash() {
        long hash = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                hash ^= Board.HASH_POSITION_NUMBERS[32 * (8 * a + b) + grid[a][b] + 15];
            }
        }

        assertEquals(printBoards(), hash, board.getHash());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> instancesToTest() {
        Random rand = new Random(6748093);
        List<Object[]> data = new ArrayList<>();
        int N_TESTS = 100;

        List<byte[][]> situations = RandomPositionGenerator.generateRandomTestBoards(N_TESTS);

        for (int i = 0; i < N_TESTS; i++) {
            // Copy
            //data.add(new Object[]{situations.get(i), new ArrayBoard(new GridBoard(situations.get(i)))});
            //data.add(new Object[]{situations.get(i), new BitBoard(new GridBoard(situations.get(i)))});
            data.add(new Object[]{situations.get(i), new CachingBoard(new GridBoard(situations.get(i)))});
        }
        for (int i = 0; i < N_TESTS; i++) {
            // Set up from scratch
            //data.add(new Object[]{situations.get(i), setUpBoard(situations.get(i), new ArrayBoard(), rand)});
            //data.add(new Object[]{situations.get(i), setUpBoard(situations.get(i), new BitBoard(), rand)});
            data.add(new Object[]{situations.get(i), setUpBoard(situations.get(i), new CachingBoard(), rand)});
        }
        for (int i = 0; i < N_TESTS; i++) {
            // Set up with undo
            //data.add(setUpUndoBoard(situations.get(i), new ArrayBoard(), rand));
            //data.add(setUpUndoBoard(situations.get(i), new BitBoard(), rand));
            data.add(setUpUndoBoard(situations.get(i), new CachingBoard(), rand));
        }

        return data;
    }

    private static Board setUpBoard(byte[][] grid, Board board, Random rand) {
        // Find all blocked and played positions
        List<Integer> moves1 = new ArrayList<>();
        List<Integer> moves2 = new ArrayList<>();

        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                byte val = grid[a][pos % 8];

                if (val == Board.BLOCKED) {
                    board.block(pos);
                } else if (val != Board.FREE) {
                    if (val > 0) {
                        moves1.add(Board.buildMove(pos, val, 0));
                    } else {
                        moves2.add(Board.buildMove(pos, val, 0));
                    }
                }
            }
        }

        // Randomize the order
        Collections.shuffle(moves1, rand);
        Collections.shuffle(moves2, rand);

        while (!moves1.isEmpty()) {
            // Play p1
            board.applyMove(moves1.remove(moves1.size() - 1));

            if (moves2.isEmpty()) {
                break;
            }

            // Play p2
            board.applyMove(moves2.remove(moves2.size() - 1));
        }

        if (!moves1.isEmpty()) {
            System.err.println("Something went wrong!");
        }

        return board;
    }

    private static Object[] setUpUndoBoard(byte[][] grid, Board board, Random rand) {
        /*System.err.println("Setting up undo board. Grid:");
        for (byte h = 0; h < 8; h++) {
            System.err.print(String.format("%" + (2 * (7 - h) + 1) + "s", ""));
            for (byte i = 0; i <= h; i++) {
                if (i > 0) {
                    System.err.print(' ');
                }
                byte value = grid[h - i][i];
                System.err.print(value == Board.BLOCKED ? "  X" : String.format("%3d", value));
            }
            System.err.print(String.format("%" + (2 * (7 - h) + 1) + "s%n", ""));
        }*/

        Board result = setUpBoard(grid, board, rand);

        byte[][] gridCopy = new byte[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            System.arraycopy(grid[i], 0, gridCopy[i], 0, grid[0].length);
        }

        // Back this game up a few steps
        int tilesPlayed = 0;
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (gridCopy[a][b] != Board.FREE && gridCopy[a][b] != Board.BLOCKED) {
                    tilesPlayed++;
                }
            }
        }

        if (tilesPlayed > 0) {
            int steps = 1 + rand.nextInt(tilesPlayed); // Uniform in [1, ..., tilesPlayed]

            //Board.print(result);

            for (int i = 0; i < steps; i++) {
                // Undo a random move
                int moveIndex = rand.nextInt(tilesPlayed);

                for (byte a = 0; a < 8; a++) {
                    for (byte b = 0; b < 8 - a; b++) {
                        if (gridCopy[a][b] != Board.FREE && gridCopy[a][b] != Board.BLOCKED) {
                            moveIndex--;

                            if (moveIndex == 0) {
                                //System.err.println("Undoing move " + Board.moveToString(Board.buildMove(Board.getPos(a, b), gridCopy[a][b], 0)));

                                result.undoMove(Board.buildMove(Board.getPos(a, b), gridCopy[a][b], 0));
                                gridCopy[a][b] = Board.FREE;
                                tilesPlayed--;

                                //Board.print(result);
                            }
                        }
                    }
                }
            }
        }

        return new Object[]{gridCopy, result};
    }

    private static class GridBoard extends Board {

        private final byte[][] grid;

        public GridBoard(byte[][] grid) {
            this.grid = grid;
        }

        @Override
        public byte get(byte pos) {
            byte[] coords = getCoordinates(pos);
            return grid[coords[0]][coords[1]];
        }

        @Override
        public void block(byte pos) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void applyMove(int move) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void undoMove(int move) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean haveIUsed(byte value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean hasOppUsed(byte value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isFree(byte pos) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getNFreeSpots() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getHoleValue(byte pos) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getFreeSpotsAround(byte pos) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public byte[] getFreeSpots() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isGameOver() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isGameInEndGame() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getTranspositionTableKey() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public long getHash() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
