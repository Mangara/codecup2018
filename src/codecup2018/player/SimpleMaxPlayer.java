package codecup2018.player;

import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.Arrays;
import java.util.List;

public class SimpleMaxPlayer extends StandardPlayer {

    public SimpleMaxPlayer(String name, Evaluator evaluator, MoveGenerator generator) {
        super(name, evaluator, generator);
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

            if (DEBUG) {
                System.err.println(getName() + ": Move " + Arrays.toString(move) + " has value " + value);
            }
            
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }

}
