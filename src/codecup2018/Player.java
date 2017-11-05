package codecup2018;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public abstract class Player {

    protected final static boolean DEBUG = false;
    protected Board board = new Board();
    protected final List<Integer> myNumbers = new ArrayList<>();
    protected final List<Integer> hisNumbers = new ArrayList<>();
    protected final String name;

    public Player(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void play(BufferedReader in, PrintStream out) throws IOException {
        initialize();

        // Read in 5 blocked fields
        for (int i = 0; i < 5; i++) {
            String loc = in.readLine();
            if (DEBUG) {
                System.err.println(name + ": Blocking " + loc);
            }
            block(loc);
        }

        for (String input = in.readLine(); !(input == null || "Quit".equals(input)); input = in.readLine()) {
            if (DEBUG) {
                System.err.println(name + ": Input: " + input);
            }

            if (!"Start".equals(input)) {
                processMove(input);
            }

            if (DEBUG) {
                board.print();
            }

            String move = move();
            if (DEBUG) {
                System.err.println(name + ": Output: " + move);
            }
            out.println(move);
        }
    }

    public void initialize() {
        // Reset board
        board = new Board();
        
        // Initialize lists
        for (int i = 1; i <= 15; i++) {
            myNumbers.add(i);
            hisNumbers.add(i);
        }
    }

    public void block(String loc) {
        board.set(loc, Board.BLOCKED);
    }

    public void processMove(String move) {
        String loc = move.substring(0, 2);
        byte val = (byte) Integer.parseInt(move.substring(3));
        hisNumbers.remove((Integer) (int) val);
        board.set(loc, (byte) -val);
    }

    public String move() {
        byte[] move = selectMove();
        board.set(move[0], move[1], move[2]);
        myNumbers.remove((Integer) (int) move[2]);
        return Board.coordinatesToString(move[0], move[1]) + "=" + move[2];
    }

    protected abstract byte[] selectMove();
}
