package codecup2018.movegenerator;

import codecup2018.Board;
import codecup2018.evaluator.Evaluator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BestMoves implements MoveGenerator {

    private final MoveGenerator generator;
    private final Evaluator evaluator;
    private final int nMoves;

    public BestMoves(MoveGenerator generator, Evaluator evaluator, int nMoves) {
        this.generator = generator;
        this.evaluator = evaluator;
        this.nMoves = nMoves;
    }
    
    public BestMoves(Evaluator evaluator, int nMoves) {
        this(new AllMoves(), evaluator, nMoves);
    }

    @Override
    public List<byte[]> generateMoves(Board board, boolean player1) {
        List<byte[]> allMoves = generator.generateMoves(board, player1);

        // Sorted worst-to-best
        List<byte[]> result = new ArrayList<>(nMoves);
        List<Integer> values = new ArrayList<>();

        for (int i = 0; i < allMoves.size(); i++) {
            byte[] move = allMoves.get(i);

            // Find the value
            board.applyMove(move);
            int value = evaluator.evaluate(board);
            board.undoMove(move);

            // Find the insert position
            if (values.size() < nMoves || value > values.get(0)) {
                int insert = Collections.binarySearch(values, value);

                if (insert < 0) {
                    insert = -(insert + 1);
                }

                // Insert
                values.add(insert, value);
                result.add(insert, move);

                if (values.size() > nMoves) {
                    values.remove(0);
                    result.remove(0);
                }
            }
        }
        
        System.out.print("Moves: [");
        for (byte[] m : result) {
            System.out.print(Arrays.toString(m) + ", ");
        }
        System.out.println("]");
        System.out.println("Values: " + values);

        // Return best first
        Collections.reverse(result);
        return result;
    }

}
