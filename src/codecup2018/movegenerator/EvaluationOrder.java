package codecup2018.movegenerator;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EvaluationOrder implements MoveGenerator {

    private final Evaluator evaluator;
    private final MoveGenerator generator;

    public EvaluationOrder(Evaluator evaluator, MoveGenerator generator) {
        this.evaluator = evaluator;
        this.generator = generator;
    }

    @Override
    public List<byte[]> generateMoves(final Board board, boolean player1) {
        List<byte[]> moves = generator.generateMoves(board, player1);
        
        // TODO: do this efficiently
        Collections.sort(moves, new Comparator<byte[]>() {
            @Override
            public int compare(byte[] m1, byte[] m2) {
                board.applyMove(m1);
                int val1 = evaluator.evaluate(board);
                board.undoMove(m1);
                
                board.applyMove(m2);
                int val2 = evaluator.evaluate(board);
                board.undoMove(m2);
                
                return -Integer.compare(val1, val2);
            }
        });
        
        return moves;
    }
    
}
