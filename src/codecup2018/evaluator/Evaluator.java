package codecup2018.evaluator;

import codecup2018.Board;

public interface Evaluator {

    /**
     * Assigns a score to the current board situation. Positive scores are good
     * for player1, negative ones for player2. If the game has finished, the
     * score should be the actual end score.
     *
     * @param board
     * @return
     */
    public abstract int evaluate(Board board);
    
    public void initialize(Board board);
        
    public void block(byte a, byte b);
            
    public void applyMove(byte[] move);

    public void undoMove(byte[] move);
}
