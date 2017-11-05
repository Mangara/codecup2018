package codecup2018.player;

import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.List;

public class SimpleMaxPlayer extends Player {

    private final Evaluator evaluator;
    private final MoveGenerator generator;

    public SimpleMaxPlayer(String name, Evaluator evaluator, MoveGenerator generator) {
        super(name);
        this.evaluator = evaluator;
        this.generator = generator;
    }

    @Override
    protected byte[] selectMove() {
        byte[] bestMove = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        List<byte[]> moves = generator.generateMoves(board, true);

        for (byte[] move : moves) {
            board.applyMove(move);
            double value = evaluator.evaluate(board);
            board.undoMove(move);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }

}
