package codecup2018.player;

import codecup2018.data.BitBoard;
import codecup2018.data.Board;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public abstract class Player {

    public static boolean TIMING = false;
    public static boolean DEBUG = false;
    protected Board board;
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
        long start = 0;

        if (TIMING) {
            start = System.currentTimeMillis();
        }

        initialize();

        // Read in 5 blocked fields
        for (int i = 0; i < 5; i++) {
            byte pos = Board.parsePos(in.readLine());
            block(pos);
        }

        if (TIMING) {
            System.err.println("Initialization took " + (System.currentTimeMillis() - start) + " ms.");
        }

        for (String input = in.readLine(); !(input == null || "Quit".equals(input)); input = in.readLine()) {
            if (TIMING) {
                start = System.currentTimeMillis();
            }

            if (!"Start".equals(input)) {
                processMove(Board.parseMove(input), false);
            }

            int move = move();

            if (TIMING) {
                System.err.println("Move took " + (System.currentTimeMillis() - start) + " ms.");
            }

            out.println(Board.moveToString(move));
        }
    }

    public void initialize() {
        initialize(new BitBoard());
    }

    public void initialize(Board currentBoard) {
        board = currentBoard;
    }

    public void block(byte pos) {
        board.block(pos);
    }

    public void processMove(int move, boolean mine) {
        board.applyMove(mine ? move : Board.setMoveVal(move, (byte) -Board.getMoveVal(move)));
    }

    public int move() {
        int move = selectMove();
        processMove(move, true);
        return move;
    }

    protected abstract int selectMove();
}
