package codecup2018.player;

import codecup2018.movegenerator.MoveGenerator;
import java.util.List;
import java.util.Random;

public class RandomPlayer extends Player {

    private static final Random RAND = new Random();
    private final MoveGenerator generator;

    public RandomPlayer(String name, MoveGenerator generator) {
        super(name);
        this.generator = generator;
    }

    @Override
    protected byte[] selectMove() {
        List<byte[]> moves = generator.generateMoves(board, true);
        return moves.get(RAND.nextInt(moves.size()));
    }
}
