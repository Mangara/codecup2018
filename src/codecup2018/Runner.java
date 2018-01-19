package codecup2018;

import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.BucketSortMaxMovesOneHole;
import codecup2018.player.IterativeDFSPlayer;
import codecup2018.player.Player;
import codecup2018.timecontrol.ProportionalController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Runner {

    public static void main(String[] args) throws IOException {
        Player.TIMING = true;
        //KillerMultiAspirationTableCutoffPlayer.DEBUG_FINAL_VALUE = true;
        IterativeDFSPlayer.DEBUG_FINAL_VALUE = true;
        
        Player p = getPlayer();
        p.play(new BufferedReader(new InputStreamReader(System.in)), System.out);
    }
    
    public static Player getPlayer() {
        //return new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves()); // Played Nov 11 Test Competition
        //return new AspirationPlayer("As_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10); // Played Nov 25 Test Competition (did worse and timed out 8 times)
        //return new MultiAspirationTableCutoffPlayer("MAsTC_IEV_MI_6", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 6); // Played Dec 9 Test Competition -> 16th
        //return new KillerMultiAspirationTableCutoffPlayer("KMAsTC_IEV_BSM1_7", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), 7);
        return new IterativeDFSPlayer("ID_IEV_BSM1_LD4700", new IncrementalExpectedValue(), new BucketSortMaxMovesOneHole(), new ProportionalController(4700, ProportionalController.LINEAR_DECAY));
    }
}
