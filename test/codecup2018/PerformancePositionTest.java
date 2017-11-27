package codecup2018;

import codecup2018.evaluator.CountingEvaluator;
import codecup2018.data.BitBoard;
import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.evaluator.MedianFree;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.movegenerator.MostFreeMax;
import codecup2018.player.AlphaBetaPlayer;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.NegaMaxPlayer;
import codecup2018.player.Player;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PerformancePositionTest {

    private final Board board;

    public PerformancePositionTest(Board board) throws IOException {
        this.board = board;
    }
    
    @Test
    public void runTest() throws IOException {
        //CountingEvaluator eval = new CountingEvaluator(new MedianFree());
        CountingEvaluator eval = new CountingEvaluator(new IncrementalExpectedValue());
        
        //Player unoptimized = new AlphaBetaPlayer("NM_MF_MFM_10", eval, new MostFreeMax(), 10);
        Player unoptimized = new NegaMaxPlayer("NM_IEV_MI_6", eval, new MaxInfluenceMoves(), 6);
        unoptimized.initialize(new BitBoard(board));
        byte[] move1 = unoptimized.move();
        int unoptimizedEvaluations = eval.getnEvaluations();
        
        //Player optimized = new AspirationPlayer("As_MF_MFM_10", eval, new MostFreeMax(), 10);
        //AspirationPlayer.WINDOW_SIZE = 2;
        Player optimized = new AspirationPlayer("As_IEV_MI_6", eval, new MaxInfluenceMoves(), 6);
        AspirationPlayer.WINDOW_SIZE = 5400000;
        optimized.initialize(new BitBoard(board));
        byte[] move2 = optimized.move();
        int optimizedEvaluations = eval.getnEvaluations();
        
        System.out.printf("Evals: %10d vs %10d (%d%%) moves: %10s vs %10s%n", optimizedEvaluations, unoptimizedEvaluations, (100 * optimizedEvaluations) / unoptimizedEvaluations, Arrays.toString(move2), Arrays.toString(move1));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> boards = new ArrayList<>();

        boards.add(new Object[]{movesToBoard("A1,A4,A5,C3,C4,B2=15,B6=15,E2=14,E3=14", true)});
        boards.add(new Object[]{movesToBoard("A1,A4,A5,C3,C4,B2=15,B6=15,E2=14,E3=14,C5=12", false)});
        boards.add(new Object[]{movesToBoard("A4,B5,D2,D4,E3,B2=15,C3=8,B6=14,C6=11,F2=13,F1=10,A7=12,G2=14,B3=11,C4=15,C1=10,A3=12", true)});
        boards.add(new Object[]{movesToBoard("B3,B7,C2,D2,D3,B5=15,F2=14,C5=14,A2=15", true)});
        boards.add(new Object[]{movesToBoard("A5,B2,B6,C2,E4,C4=15,E2=15,F2=14,C3=14,C5=13", false)});
        boards.add(new Object[]{movesToBoard("B4,D4,E2,E4,F2,B2=15,B6=15", true)});
        boards.add(new Object[]{movesToBoard("B4,D4,E2,E4,F2,B2=11,B6=15,C5=13", false)});
        boards.add(new Object[]{movesToBoard("B2,C1,C2,C4,D1,B6=15,E2=15,F2=14,D4=14,B4=13", false)});

        return boards;
    }
    
    private static Object movesToBoard(String moves, boolean player1) {
        Board board = new BitBoard();
        String[] ms = moves.split(",");
        
        for (int i = 0; i < 5; i++) {
            byte[] pos = Util.getCoordinates(ms[i]);
            board.block(pos[0], pos[1]);
        }
        
        for (int i = 5; i < ms.length; i++) {
            String m = ms[i];
            byte[] move = Util.parseMove(m);
            boolean myMove = (i % 2 == 1) == player1; // Odd because actual moves start after 5 blocks
            board.applyMove(new byte[] {move[0], move[1], myMove ? move[2] : (byte) -move[2]});
        }
        
        return board;
    }
}