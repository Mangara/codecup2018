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
    public void block(byte a, byte b) {
        board.block(a, b);
        evaluator.block(a, b);
    }

    @Override
    public void processMove(byte[] move, boolean mine) {
        byte[] m = new byte[]{move[0], move[1], (mine ? move[2] : (byte) -move[2])};
        board.applyMove(m);
        evaluator.applyMove(m);
    }
}
