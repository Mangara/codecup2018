package codecup2018.player;

import codecup2018.Board;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class RandomPlayer extends Player {

    private static final Random rand = new Random();

    public RandomPlayer(String name) {
        super(name);
    }

    @Override
    protected byte[] selectMove() {
        // Pick a random free square
        int nOpen = 0;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (board.get(a, b) == Board.FREE) {
                    nOpen++;
                }
            }
        }

        int pick = rand.nextInt(nOpen);
        byte moveA = -1, moveB = -1;

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (board.get(a, b) == Board.FREE) {
                    if (pick == 0) {
                        moveA = a;
                        moveB = b;
                    }
                    pick--;
                }
            }
        }

        // Pick a random free number
        byte moveVal = (byte) (int) myNumbers.get(rand.nextInt(myNumbers.size()));

        return new byte[]{moveA, moveB, moveVal};
    }
    
    public static void main(String[] args) throws IOException {
        RandomPlayer r = new RandomPlayer("Random");
        r.play(new BufferedReader(new InputStreamReader(System.in)), System.out);
    }
}
