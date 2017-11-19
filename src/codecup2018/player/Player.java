package codecup2018.player;

import codecup2018.Board;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public abstract class Player {

    protected final static boolean DEBUG = false;
    protected Board board = new Board();
    protected final String name;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Board getBoard() {
        return board;
    }

    public void play(BufferedReader in, PrintStream out) throws IOException {
        initialize();

        // Read in 5 blocked fields
        for (int i = 0; i < 5; i++) {
            String loc = in.readLine();
            block(loc);
        }

        for (String input = in.readLine(); !(input == null || "Quit".equals(input)); input = in.readLine()) {
            if (!"Start".equals(input)) {
                processMove(Board.parseMove(input), false);
            }

            byte[] move = move();
            out.println(Board.coordinatesToString(move[0], move[1]) + "=" + move[2]);
        }
    }

    public void initialize() {
        initialize(new Board());
    }

    public void initialize(Board currentBoard) {
        board = currentBoard;
    }

    public void block(String loc) {
        board.set(loc, Board.BLOCKED);
    }

    public void block(byte a, byte b) {
        board.set(a, b, Board.BLOCKED);
    }

    public void processMove(byte[] move, boolean mine) {
        board.set(move[0], move[1], (mine ? move[2] : (byte) -move[2]));
    }

    public byte[] move() {
        byte[] move = selectMove();
        processMove(move, true);
        return move;
    }

    protected abstract byte[] selectMove();
}
