package codecup2018.player;

import codecup2018.movegenerator.MoveGenerator;
import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {

    private final Random rand;
    private final MoveGenerator generator;

    public RandomPlayer(String name, MoveGenerator generator) {
        this(name, generator, new Random());
    }
    
    public RandomPlayer(String name, MoveGenerator generator, Random rand) {
        super(name);
        this.generator = generator;
        this.rand = rand;
    }

    @Override
    protected byte[] selectMove() {
        List<byte[]> moves = generator.generateMoves(board, true);
        return moves.get(rand.nextInt(moves.size()));
    }
}
