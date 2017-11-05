package codecup2018.movegenerator;

import codecup2018.Board;
import java.util.List;

public interface MoveGenerator {
    public abstract List<byte[]> generateMoves(Board board, boolean player1);
}
