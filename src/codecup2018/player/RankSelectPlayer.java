package codecup2018.player;

import codecup2018.movegenerator.MoveGenerator;
import java.util.List;
import java.util.Random;

public class RankSelectPlayer extends Player {

    private static final Random RAND = new Random();
    private final MoveGenerator generator;
    private final double power;

    public RankSelectPlayer(String name, MoveGenerator generator, double power) {
        super(name);
        this.generator = generator;
        this.power = power;
    }

    @Override
    protected int selectMove() {
        int[] moves = generator.generateMoves(board, true);

        double totalValue = (1 - Math.pow(power, moves.length + 1)) / (1 - power) - 1;
        double t = RAND.nextDouble() * totalValue;
        double value = power;
        int index = 0;

        while (t > 0) {
            t -= value;
            value *= power;
            index++;
        }

        return moves[index - 1];
    }

}
