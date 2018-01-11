package codecup2018.tools;

import codecup2018.data.Board;
import codecup2018.data.BitBoard;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.evaluator.MixedEvaluator;
import codecup2018.movegenerator.BucketSortMaxMovesOneHole;
import codecup2018.movegenerator.NoHoles;
import codecup2018.player.IterativeDFSPlayer;
import codecup2018.player.KillerMultiAspirationTableCutoffPlayer;
import codecup2018.player.Player;
import codecup2018.player.SimpleMaxPlayer;
import codecup2018.player.TimedUCBPlayer;
import codecup2018.timecontrol.EqualTimeController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameHost {

    private static Random rand = new Random();

    public static void main(String[] args) throws IOException {
        //setRandom(new Random());
        //setRandom(new Random(614944651));
        setRandom(new Random(35945216316303L));

        //Player p1 = new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles());
        //Player p1 = new UpperConfidenceBoundsPlayer("UCB_ME_BSM1_5000", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), 50000);
        //Player p1 = new KillerMultiAspirationTableCutoffPlayer("KMAsTC_IEV_BSM1_4", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 4);
        //Player p2 = new RandomPlayer("Rando", new AllMoves());
        //Player p2 = new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles());
        //GameHost.runGame(new SimpleMaxPlayer("Expy_MI", new ExpectedValue(), new MaxInfluenceMoves()), new RandomPlayer("Rando", new AllMoves()), true);
        //GameHost.runGame(new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()), new MultiAspirationTableCutoffPlayer("MAsTC_IEV_BSM1_6", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 6), true);
        //GameHost.runGame(new RandomPlayer("Rando", new AllMoves()), new AlphaBetaPlayer("AB_IEV_AM_2", new IncrementalExpectedValue(), new AllMoves(), 2), true);
        //GameHost.runGame(new RandomPlayer("RAND_BestExp", new BestMoves(new ExpectedValue(), 5)), new GUIPlayer("GUI"), true);
        //GameHost.runGame(new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()), new AspirationPlayer("As_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4), false);
        //GameHost.runGame(new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()), new AspirationTablePlayer("AsT_IEV_MI_3", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 3), false);
        //GameHost.runGameThreaded(new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()), new AspirationPlayer("As_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4));
        //GameHost.runGame(p1, p2, false);
        //while (true) {
        long seed = -4501800762309174636L;//rand.nextLong();
        setRandom(new Random(seed));
        System.err.println("Seed: " + seed);
        //Player p1 = new TimedUCBPlayer("TUCB_ME_BSM1_1000", new MixedEvaluator(), new BucketSortMaxMovesOneHole(), new EqualTimeController(1000));
        Player p1 = new IterativeDFSPlayer("ID_IEV_BSM1_400", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), new EqualTimeController(4000));
        Player p2 = new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles());
        GameHost.runGame(p1, p2, true);
        //}
    }

    public static void setRandom(Random rand) {
        GameHost.rand = rand;
    }

    public static int runGame(Player p1, Player p2, boolean print) {
        Board board = setUpBoard();

        if (print) {
            Board.print(board);
        }

        p1.initialize(new BitBoard(board));
        p2.initialize(new BitBoard(board));

        for (int i = 0; i < 15; i++) {
            if (print) {
                System.err.println("GAME: Asking player 1 for a move");
            }

            int p1Move = p1.move();

            if (print) {
                System.err.println("GAME: Player 1 returned move: " + Board.moveToString(p1Move) + ". Checking ...");
            }

            verifyMove(board, p1Move, true);

            if (print) {
                Board.print(board);
                System.err.println("GAME: Sending move to player 2");
            }

            p2.processMove(p1Move, false);

            if (print) {
                System.err.println("GAME: Asking player 2 for a move");
            }

            int p2Move = p2.move();

            if (print) {
                System.err.println("GAME: Player 2 returned move: " + Board.moveToString(p2Move) + ". Checking ...");
            }

            verifyMove(board, p2Move, false);

            if (print) {
                Board.print(board);
            }

            if (i < 14) {
                if (print) {
                    System.err.println("GAME: Sending move to player 1");
                }

                p1.processMove(p2Move, false);
            }
        }

        int score = getBlackHoleScore(board);

        if (print) {
            System.err.println("GAME: The game ended with a score of " + score);

            if (score > 0) {
                System.err.println("GAME: PLAYER 1 WINS!");
            } else if (score < 0) {
                System.err.println("GAME: PLAYER 2 WINS!");
            } else {
                System.err.println("GAME: THE GAME IS A TIE!");
            }
        }

        return score;
    }

    public static int runGameThreaded(final Player p1, final Player p2, boolean print) throws IOException {
        Board board = setUpBoard();

        if (print) {
            Board.print(board);
        }

        final CircularByteBuffer p1InputBuffer = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE, true);
        final CircularByteBuffer p1OutputBuffer = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE, true);
        final CircularByteBuffer p2InputBuffer = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE, true);
        final CircularByteBuffer p2OutputBuffer = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE, true);

        PrintWriter p1InputWriter = new PrintWriter(p1InputBuffer.getOutputStream());
        PrintWriter p2InputWriter = new PrintWriter(p2InputBuffer.getOutputStream());
        BufferedReader p1OutputReader = new BufferedReader(new InputStreamReader(p1OutputBuffer.getInputStream()));
        BufferedReader p2OutputReader = new BufferedReader(new InputStreamReader(p2OutputBuffer.getInputStream()));

        new Thread(
                new Runnable() {
            public void run() {
                try {
                    p1.play(new BufferedReader(new InputStreamReader(p1InputBuffer.getInputStream())), new PrintStream(p1OutputBuffer.getOutputStream()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        ).start();

        new Thread(
                new Runnable() {
            public void run() {
                try {
                    p2.play(new BufferedReader(new InputStreamReader(p2InputBuffer.getInputStream())), new PrintStream(p2OutputBuffer.getOutputStream()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        ).start();

        passBlockedCells(board, p1InputWriter);
        passBlockedCells(board, p2InputWriter);

        p1InputWriter.println("Start");
        p1InputWriter.flush();

        for (int i = 0; i < 15; i++) {
            if (print) {
                System.err.println("GAME: Asking player 1 for a move");
            }
            String p1Move = p1OutputReader.readLine();
            if (print) {
                System.err.println("GAME: Player 1 returned move: " + p1Move + ". Checking ...");
            }
            verifyMove(board, Board.parseMove(p1Move), true);
            if (print) {
                System.err.println("GAME: Sending move to player 2");
            }
            p2InputWriter.println(p1Move);
            p2InputWriter.flush();
            if (print) {
                System.err.println("GAME: Asking player 2 for a move");
            }
            String p2Move = p2OutputReader.readLine();
            if (print) {
                System.err.println("GAME: Player 2 returned move: " + p2Move + ". Checking ...");
            }
            verifyMove(board, Board.parseMove(p2Move), false);

            if (i < 14) {
                if (print) {
                    System.err.println("GAME: Sending move to player 1");
                }
                p1InputWriter.println(p2Move);
                p1InputWriter.flush();
            }
        }

        p1InputWriter.println("Quit");
        p1InputWriter.flush();
        p2InputWriter.println("Quit");
        p2InputWriter.flush();

        int score = getBlackHoleScore(board);

        if (print) {
            System.err.println("GAME: The game ended with a score of " + score);

            if (score > 0) {
                System.err.println("GAME: PLAYER 1 WINS!");
            } else if (score < 0) {
                System.err.println("GAME: PLAYER 2 WINS!");
            } else {
                System.err.println("GAME: THE GAME IS A TIE!");
            }
        }

        return score;
    }

    public static Board setUpBoard() {
        Board board = new BitBoard();

        // Block 5 locations
        Set<Integer> blocked = new HashSet<>();

        while (blocked.size() < 5) {
            blocked.add(rand.nextInt(36));
        }

        int location = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (blocked.contains(location)) {
                    board.block(Board.getPos(a, b));
                }
                location++;
            }
        }

        return board;
    }

    private static void passBlockedCells(Board board, PrintWriter writer) {
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                if (board.get(pos) == Board.BLOCKED) {
                    writer.println(Board.posToString(pos));
                }
            }
        }

        writer.flush();
    }

    private static void verifyMove(Board board, int move, boolean player1) {
        // Position is empty
        byte pos = Board.getMovePos(move);
        byte a = (byte) (pos / 8), b = (byte) (pos % 8);

        if (a < 0 || a > 7 || b < 0 || b > 7 - a) {
            throw new IllegalArgumentException("Position does not exist");
        }

        if (board.get(pos) != Board.FREE) {
            throw new IllegalArgumentException("Position not free");
        }

        // Number is unused
        byte val = Board.getMoveVal(move);

        if (val < 1) {
            throw new IllegalArgumentException("Illegal value");
        }

        if ((player1 && board.haveIUsed(val)) || (!player1 && board.hasOppUsed(val))) {
            throw new IllegalArgumentException("Value already used");
        }

        // Apply move
        board.applyMove(Board.setMoveVal(move, (player1 ? val : (byte) -val)));
    }

    private static int getBlackHoleScore(Board board) {
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                if (board.get(pos) == Board.FREE) {
                    return board.getHoleValue(pos);
                }
            }
        }

        throw new IllegalArgumentException("Board must have a free space left.");
    }
}
