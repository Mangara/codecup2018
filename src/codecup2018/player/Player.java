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
                processMove(input);
            }
            
            out.println(move());
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

    public void processMove(String move) {
        String loc = move.substring(0, 2);
        byte val = (byte) Integer.parseInt(move.substring(3));
        board.set(loc, (byte) -val);
    }

    public String move() {
        byte[] move = selectMove();
        board.set(move[0], move[1], move[2]);
        return Board.coordinatesToString(move[0], move[1]) + "=" + move[2];
    }

    protected abstract byte[] selectMove();
}
