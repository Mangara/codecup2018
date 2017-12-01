package codecup2018.movegenerator;

import codecup2018.data.Board;

public interface MoveGenerator {

    public abstract int[] generateMoves(Board board, boolean player1);
}
