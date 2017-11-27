package codecup2018.tools;

import codecup2018.Util;
import codecup2018.data.BitBoard;
import codecup2018.data.ArrayBoard;
import codecup2018.data.Board;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.movegenerator.NoHoles;
import codecup2018.movegenerator.NoHolesMax;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.Player;
import codecup2018.player.SimpleMaxPlayer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameHost {

    private static Random rand = new Random();

    public static void main(String[] args) throws IOException {
        setRandom(new Random(614944651));
        
        //GameHost.runGame(new AlphaBetaPlayer("AB_MF_10", new MedianFree(), new MostFreeMax(), 10), new GUIPlayer("GUI"), true);
        //GameHost.runGame(new RandomPlayer("RAND_BestExp", new BestMoves(new ExpectedValue(), 5)), new GUIPlayer("GUI"), true);
        //GameHost.runGame(new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()), new AspirationPlayer("As_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4), false);
        GameHost.runGame(new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()), new AspirationPlayer("As_EV_MI_4", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 4), false);
        //GameHost.runGameThreaded(new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()), new AspirationPlayer("As_EV_NHM_4", new ExpectedValue(), new NoHolesMax(), 4));
    }

    public static void setRandom(Random rand) {
        GameHost.rand = rand;
    }

    public static int runGame(Player p1, Player p2, boolean print) {
        ArrayBoard board = setUpBoard();

        if (print) {
            Util.print(board);
        }

        p1.initialize(new BitBoard(board));
        p2.initialize(new BitBoard(board));

        for (int i = 0; i < 15; i++) {
            if (print) {
                System.err.println("GAME: Asking player 1 for a move");
            }

            byte[] p1Move = p1.move();

            if (print) {
                System.err.println("GAME: Player 1 returned move: " + Arrays.toString(p1Move) + ". Checking ...");
            }

            verifyMove(board, p1Move, true);

            if (print) {
                Util.print(board);
                System.err.println("GAME: Sending move to player 2");
            }

            p2.processMove(p1Move, false);

            if (print) {
                System.err.println("GAME: Asking player 2 for a move");
            }

            byte[] p2Move = p2.move();

            if (print) {
                System.err.println("GAME: Player 2 returned move: " + Arrays.toString(p2Move) + ". Checking ...");
            }

            verifyMove(board, p2Move, false);

            if (print) {
                Util.print(board);
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

    public static void runGameThreaded(final Player p1, final Player p2) throws IOException {
        ArrayBoard board = setUpBoard();
        Util.print(board);

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
            System.err.println("GAME: Asking player 1 for a move");
            String p1Move = p1OutputReader.readLine();
            System.err.println("GAME: Player 1 returned move: " + p1Move + ". Checking ...");
            verifyMove(board, Util.parseMove(p1Move), true);
            System.err.println("GAME: Sending move to player 2");
            p2InputWriter.println(p1Move);
            p2InputWriter.flush();
            System.err.println("GAME: Asking player 2 for a move");
            String p2Move = p2OutputReader.readLine();
            System.err.println("GAME: Player 2 returned move: " + p2Move + ". Checking ...");
            verifyMove(board, Util.parseMove(p2Move), false);

            if (i < 14) {
                System.err.println("GAME: Sending move to player 1");
                p1InputWriter.println(p2Move);
                p1InputWriter.flush();
            }
        }

        p1InputWriter.println("Quit");
        p1InputWriter.flush();
        p2InputWriter.println("Quit");
        p2InputWriter.flush();

        int score = getBlackHoleScore(board);

        System.err.println("GAME: The game ended with a score of " + score);

        if (score > 0) {
            System.err.println("GAME: PLAYER 1 WINS!");
        } else if (score < 0) {
            System.err.println("GAME: PLAYER 2 WINS!");
        } else {
            System.err.println("GAME: THE GAME IS A TIE!");
        }
    }

    private static ArrayBoard setUpBoard() {
        ArrayBoard board = new ArrayBoard();

        // Block 5 locations
        Set<Integer> blocked = new HashSet<>();

        while (blocked.size() < 5) {
            blocked.add(rand.nextInt(36));
        }

        int location = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (blocked.contains(location)) {
                    board.set(a, b, Board.BLOCKED);
                }
                location++;
            }
        }

        return board;
    }

    private static void passBlockedCells(ArrayBoard board, PrintWriter writer) {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (board.get(a, b) == Board.BLOCKED) {
                    writer.println(Util.coordinatesToString(a, b));
                }
            }
        }

        writer.flush();
    }
    
    private static void verifyMove(ArrayBoard board, byte[] move, boolean player1) {
        // Position is empty
        if (move[0] < 0 || move[0] > 7 || move[1] < 0 || move[1] > 7 - move[0]) {
            throw new IllegalArgumentException("Position does not exist");
        }

        if (board.get(move[0], move[1]) != Board.FREE) {
            throw new IllegalArgumentException("Position not free");
        }

        // Number is unused
        if (move[2] < 1 || (player1 && board.haveIUsed(move[2])) || (!player1 && board.hasOppUsed(move[2]))) {
            throw new IllegalArgumentException("Value already used");
        }

        // Apply move
        board.set(move[0], move[1], (player1 ? move[2] : (byte) -move[2]));
    }

    private static int getBlackHoleScore(ArrayBoard board) {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (board.get(a, b) == Board.FREE) {
                    return board.getHoleValue(a, b);
                }
            }
        }

        throw new IllegalArgumentException("Board must have a free space left.");
    }
}