package codecup2018.movegenerator;

import codecup2018.Board;
import java.util.ArrayList;
import java.util.List;

public class AllMoves implements MoveGenerator {

    @Override
    public List<byte[]> generateMoves(Board board, boolean player1) {
        List<byte[]> moves = new ArrayList<>();

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (board.get(a, b) != Board.FREE) {
                    continue;
                }

                for (byte v = 1; v <= 15; v++) {
                    if (board.haveIUsed(v)) {
                        continue;
                    }

                    moves.add(new byte[]{a, b, v});
                }
            }
        }
        
        return moves;
    }
    
}
