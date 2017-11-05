package codecup2018.player;

import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import java.util.List;
import java.util.Random;

public class RoulettePlayer extends Player {

    private static final Random RAND = new Random();
    private final Evaluator evaluator;
    private final MoveGenerator generator;
    private final double power;

    public RoulettePlayer(String name, Evaluator evaluator, MoveGenerator generator, double power) {
        super(name);
        this.evaluator = evaluator;
        this.generator = generator;
        this.power = power;
    }

    @Override
    protected byte[] selectMove() {
        // Pick a move with probability proportional to a power of its expected value
        List<byte[]> moves = generator.generateMoves(board, true);

        // Evaluate all moves
        double[] values = new double[moves.size()];

        for (int i = 0; i < moves.size(); i++) {
            board.applyMove(moves.get(i));
            values[i] = evaluator.evaluate(board);
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
            // All moves have value 0 - pick a random move
            return moves.get(RAND.nextInt(moves.size()));
        }
    }

}
