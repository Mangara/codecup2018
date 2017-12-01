package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;

public abstract class StandardPlayer extends Player {

    protected final Evaluator evaluator;
    protected final MoveGenerator generator;

    public StandardPlayer(String name, Evaluator evaluator, MoveGenerator generator) {
        super(name);
        this.evaluator = evaluator;
        this.generator = generator;
    }

    @Override
    public void initialize(Board currentBoard) {
        board = currentBoard;
        evaluator.initialize(currentBoard);
    }

    @Override
    public void block(byte pos) {
        board.block(pos);
        evaluator.block(pos);
    }

    @Override
    public void processMove(int move, boolean mine) {
        int m = (mine ? move : Board.setMoveVal(move, (byte) -Board.getMoveVal(move)));
        board.applyMove(m);
        evaluator.applyMove(m);
    }
}
