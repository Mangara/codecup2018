package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;

public class SimpleMaxPlayer extends StandardPlayer {

    public SimpleMaxPlayer(String name, Evaluator evaluator, MoveGenerator generator) {
        super(name, evaluator, generator);
    }

    @Override
    protected int selectMove() {
        int bestMove = 0;
        double bestValue = Double.NEGATIVE_INFINITY;

        int[] moves = generator.generateMoves(board, true);

        for (int move : moves) {
            board.applyMove(move);
            double value = evaluator.evaluate(board);
            board.undoMove(move);

            if (DEBUG) {
                System.err.println(getName() + ": Move " + Board.moveToString(move) + " has value " + value);
            }
            
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }

}
