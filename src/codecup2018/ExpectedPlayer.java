package codecup2018;

public class ExpectedPlayer extends Player {

    private static final Evaluator EXPECTED_VALUE = new ExpectedValue();

    public ExpectedPlayer(String name) {
        super(name);
    }

    @Override
    protected byte[] selectMove() {
        byte[] bestMove = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        // Pick the move with the highest expected value
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (board.get(a, b) != Board.FREE) {
                    continue;
                }

                for (byte v = 1; v <= 15; v++) {
                    if (board.haveIUsed(v)) {
                        continue;
                    }

                    byte[] move = new byte[]{a, b, v};
                    board.applyMove(move);
                    double value = EXPECTED_VALUE.evaluate(board);
                    board.undoMove(move);

                    if (value > bestValue) {
                        bestValue = value;
                        bestMove = move;
                    }
                }
            }
        }

        return bestMove;
    }

}
