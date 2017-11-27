package codecup2018.evaluator;

import codecup2018.data.Board;

public class CountingEvaluator implements Evaluator {

    private final Evaluator evaluator;
    private int nEvaluations = 0;
    
    public CountingEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public int getnEvaluations() {
        return nEvaluations;
    }

    @Override
    public int evaluate(Board board) {
        nEvaluations++;
        return evaluator.evaluate(board);
    }

    @Override
    public void initialize(Board board) {
        evaluator.initialize(board);
        nEvaluations = 0;
    }

    @Override
    public void block(byte a, byte b) {
        evaluator.block(a, b);
    }

    @Override
    public void applyMove(byte[] move) {
        evaluator.applyMove(move);
    }

    @Override
    public void undoMove(byte[] move) {
        evaluator.undoMove(move);
    }
    
}
