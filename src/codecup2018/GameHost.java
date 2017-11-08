package codecup2018;

import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.MedianFree;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.movegenerator.MostFreeMax;
import codecup2018.player.AlphaBetaPlayer;
import codecup2018.player.GUIPlayer;
import codecup2018.player.Player;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameHost {

    private static final Random rand = new Random();

    public static void main(String[] args) throws IOException {
        GameHost.runGame(new AlphaBetaPlayer("AB_MF_10", new MedianFree(), new MostFreeMax(), 10), new GUIPlayer("GUI"), true);
        //new GameHost().runGame(new ExpectedPlayer("EXP"), new RandomPlayer("RAND"));
        //new GameHost().runGameThreaded(new RandomPlayer("RAND1"), new RandomPlayer("RAND2"));
    }

    public static int runGame(Player p1, Player p2, boolean print) {
        Board board = setUpBoard();

        if (print) {
            board.print();
        }

        p1.initialize();
        p2.initialize();

        passBlockedCells(board, p1);
        passBlockedCells(board, p2);

        for (int i = 0; i < 15; i++) {
            if (print) {
                System.err.println("GAME: Asking player 1 for a move");
            }

            String p1Move = p1.move();

            if (print) {
                System.err.println("GAME: Player 1 returned move: " + p1Move + ". Checking ...");
            }

            verifyMove(board, p1Move, true);

            if (print) {
                board.print();
                System.err.println("GAME: Sending move to player 2");
            }

            p2.processMove(p1Move);

            if (print) {
                System.err.println("GAME: Asking player 2 for a move");
            }

            String p2Move = p2.move();

            if (print) {
                System.err.println("GAME: Player 2 returned move: " + p2Move + ". Checking ...");
            }

            verifyMove(board, p2Move, false);

            if (print) {
                board.print();
            }

            if (i < 14) {
                if (print) {
                    System.err.println("GAME: Sending move to player 1");
                }

                p1.processMove(p2Move);
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

    public void runGameThreaded(final Player p1, final Player p2) throws IOException {
        Board board = setUpBoard();
        board.print();

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
            verifyMove(board, p1Move, true);
            System.err.println("GAME: Sending move to player 2");
            p2InputWriter.println(p1Move);
            p2InputWriter.flush();
            System.err.println("GAME: Asking player 2 for a move");
            String p2Move = p2OutputReader.readLine();
            System.err.println("GAME: Player 2 returned move: " + p2Move + ". Checking ...");
            verifyMove(board, p2Move, false);

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

    private static Board setUpBoard() {
        Board board = new Board();

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

    private void passBlockedCells(Board board, PrintWriter writer) {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (board.get(a, b) == Board.BLOCKED) {
                    writer.println(Board.coordinatesToString(a, b));
                }
            }
        }

        writer.flush();
    }

    private static void passBlockedCells(Board board, Player player) {
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (board.get(a, b) == Board.BLOCKED) {
                    player.block(Board.coordinatesToString(a, b));
                }
            }
        }
    }

    private static void verifyMove(Board board, String move, boolean player1) {
        String location = move.substring(0, 2);
        byte[] loc = Board.getCoordinates(location);
        byte val = (byte) Integer.parseInt(move.substring(3));

        // Position is empty
        if (loc[0] < 0 || loc[0] > 7 || loc[1] < 0 || loc[1] > 7 - loc[0]) {
            throw new IllegalArgumentException("Position does not exist");
        }

        if (board.get(loc[0], loc[1]) != Board.FREE) {
            throw new IllegalArgumentException("Position not free");
        }

        // Number is unused
        if (val < 1 || (player1 && board.haveIUsed(val)) || (!player1 && board.hasOppUsed(val))) {
            throw new IllegalArgumentException("Value already used");
        }

        // Apply move
        board.set(loc[0], loc[1], (player1 ? val : (byte) -val));
    }

    private static int getBlackHoleScore(Board board) {
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
