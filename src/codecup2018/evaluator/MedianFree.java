package codecup2018.evaluator;

import codecup2018.data.Board;
import java.util.Arrays;

public class MedianFree implements Evaluator {

    @Override
    public int evaluate(Board board) {
        // Find out how many moves I have left (this varies depending on if I'm the true player1 or player2)
        int movesLeft = 0;
        
        for (byte v = 1; v <= 15; v++) {
            if (!board.haveIUsed(v)) {
                movesLeft++;
            }
        }
        
        // Find the value of all free spaces
        byte[] free = board.getFreeSpots();

        for (int i = 0; i < free.length; i++) {
            free[i] = (byte) board.getHoleValue(free[i]); // In the range [-75, 75], so fits in a byte
        }

        // Return the one that wouldnt be filled in if the players just took
        // turns optimally filling in free spaces with 0
        Arrays.sort(free);
        return 10000 * free[movesLeft];
    }

    @Override
    public void initialize(Board board) {
    }

    @Override
    public void block(byte pos) {
    }

    @Override
    public void applyMove(int move) {
    }

    @Override
    public void undoMove(int move) {
    }
    
}
