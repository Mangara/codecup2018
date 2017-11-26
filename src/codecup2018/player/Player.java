package codecup2018.player;

import codecup2018.data.BitBoard;
import codecup2018.data.Board;
import codecup2018.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public abstract class Player {

    public static boolean TIMING = false;
    protected final static boolean DEBUG = false;
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
            byte[] loc = Util.getCoordinates(in.readLine());
            block(loc[0], loc[1]);
        }

        if (TIMING) {
            System.err.println("Initialization took " + (System.currentTimeMillis() - start) + " ms.");
        }

        for (String input = in.readLine(); !(input == null || "Quit".equals(input)); input = in.readLine()) {
            if (TIMING) {
                start = System.currentTimeMillis();
            }

            if (!"Start".equals(input)) {
                processMove(Util.parseMove(input), false);
            }

            byte[] move = move();

            if (TIMING) {
                System.err.println("Move took " + (System.currentTimeMillis() - start) + " ms.");
            }

            out.println(Util.coordinatesToString(move[0], move[1]) + "=" + move[2]);
        }
    }

    public void initialize() {
        initialize(new BitBoard());
    }

    public void initialize(Board currentBoard) {
        board = currentBoard;
    }

    public void block(byte a, byte b) {
        board.block(a, b);
    }

    public void processMove(byte[] move, boolean mine) {
        board.applyMove(new byte[]{move[0], move[1], (mine ? move[2] : (byte) -move[2])});
    }

    public byte[] move() {
        byte[] move = selectMove();
        processMove(move, true);
        return move;
    }

    protected abstract byte[] selectMove();
}
