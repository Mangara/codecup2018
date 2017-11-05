package codecup2018.player;

import codecup2018.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.evaluator.ExpectedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExpectedRandomPlayer extends Player {

    private static final Evaluator EXPECTED_VALUE = new ExpectedValue();
    private static final Random RAND = new Random();
    private final double power;

    public ExpectedRandomPlayer(String name, double power) {
        super(name);
        this.power = power;
    }

    @Override
    protected byte[] selectMove() {
        // Pick a move with probability proportional to a power of its expected value
        List<byte[]> moves = new ArrayList<>();

        // Find all valid moves
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

        // Evaluate all moves
        double[] values = new double[moves.size()];

        for (int i = 0; i < moves.size(); i++) {
            board.applyMove(moves.get(i));
            values[i] = EXPECTED_VALUE.evaluate(board);
            board.undoMove(moves.get(i));
        }

        // Normalize move values
        double minValue = Double.POSITIVE_INFINITY;

        for (int i = 0; i < moves.size(); i++) {
            minValue = Math.min(minValue, values[i]);
        }

        for (int i = 0; i < moves.size(); i++) {
            values[i] = Math.pow(values[i] - minValue, power);
        }

        // Roulette selection
        double totalValue = 0;

        for (int i = 0; i < moves.size(); i++) {
            totalValue += values[i];
        }

        if (totalValue > 0) {
            double t = RAND.nextDouble() * totalValue;
            int index = 0;

            while (t > 0) {
                t -= values[index];
                index++;
            }

            return moves.get(index - 1);
        } else {
            // Pick a random move
            return moves.get(RAND.nextInt(moves.size()));
        }
    }

}
