package codecup2018.player;

import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.List;
import java.util.Random;

public class RoulettePlayer extends StandardPlayer {

    private static final Random RAND = new Random();
    private final double power;

    public RoulettePlayer(String name, Evaluator evaluator, MoveGenerator generator, double power) {
        super(name, evaluator, generator);
        this.power = power;
    }

    @Override
    protected int selectMove() {
        // Pick a move with probability proportional to a power of its expected value
        int[] moves = generator.generateMoves(board, true);

        // Evaluate all moves
        double[] values = new double[moves.length];

        for (int i = 0; i < moves.length; i++) {
            board.applyMove(moves[i]);
            values[i] = evaluator.evaluate(board);
            board.undoMove(moves[i]);
        }

        // Normalize move values
        double minValue = Double.POSITIVE_INFINITY;

        for (int i = 0; i < moves.length; i++) {
            minValue = Math.min(minValue, values[i]);
        }

        for (int i = 0; i < moves.length; i++) {
            values[i] = Math.pow(values[i] - minValue, power);
        }

        // Roulette selection
        double totalValue = 0;

        for (int i = 0; i < moves.length; i++) {
            totalValue += values[i];
        }

        if (totalValue > 0) {
            double t = RAND.nextDouble() * totalValue;
            int index = 0;

            while (t > 0) {
                t -= values[index];
                index++;
            }

            return moves[index - 1];
        } else {
            // All moves have value 0 - pick a random move
            return moves[RAND.nextInt(moves.length)];
        }
    }

}
