package codecup2018.player;

import codecup2018.movegenerator.MoveGenerator;
import java.util.Random;

public class RankSelectPlayer extends Player {

    private final Random rand;
    private final MoveGenerator generator;
    private final double power;

    public RankSelectPlayer(String name, MoveGenerator generator, double power) {
        this(name, generator, power, new Random());
    }
    
    public RankSelectPlayer(String name, MoveGenerator generator, double power, Random rand) {
        super(name);
        this.generator = generator;
        this.power = power;
        this.rand = rand;
    }

    @Override
    protected int selectMove() {
        int[] moves = generator.generateMoves(board, true);

        double totalValue = (1 - Math.pow(power, moves.length + 1)) / (1 - power) - 1;
        double t = rand.nextDouble() * totalValue;
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
