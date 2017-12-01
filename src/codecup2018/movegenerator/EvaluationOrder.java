package codecup2018.movegenerator;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import java.util.Arrays;

public class EvaluationOrder implements MoveGenerator {

    private final Evaluator evaluator;
    private final MoveGenerator generator;

    public EvaluationOrder(Evaluator evaluator, MoveGenerator generator) {
        this.evaluator = evaluator;
        this.generator = generator;
    }

    @Override
    public int[] generateMoves(final Board board, boolean player1) {
        int[] moves = generator.generateMoves(board, player1);

        for (int i = 0; i < moves.length; i++) {
            int move = moves[i];

            board.applyMove(move);
            int eval = evaluator.evaluate(board);
            board.undoMove(move);

            moves[i] = Board.setMoveEval(move, -eval);
        }

        // Sort by negated score
        Arrays.sort(moves);

        return moves;
    }

}
