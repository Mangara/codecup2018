package codecup2018;

import codecup2018.evaluator.IncrementalExpectedValue;
import codecup2018.movegenerator.MaxInfluenceMoves;
import codecup2018.player.MultiAspirationTableCutoffPlayer;
import codecup2018.player.Player;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Runner {

    public static void main(String[] args) throws IOException {
        Player.TIMING = true;
        MultiAspirationTableCutoffPlayer.DEBUG_FINAL_VALUE = true;
        
        Player p = getPlayer();
        p.play(new BufferedReader(new InputStreamReader(System.in)), System.out);
    }
    
    public static Player getPlayer() {
        //return new SimpleMaxPlayer("Expy", new ExpectedValue(), new AllMoves()); // Played Nov 11 Test Competition
        //return new AspirationPlayer("As_MF_MFM_10", new MedianFree(), new MostFreeMax(), 10); // Played Nov 25 Test Competition (did worse and timed out 8 times)
        return new MultiAspirationTableCutoffPlayer("MAsTC_IEV_MI_6", new IncrementalExpectedValue(), new MaxInfluenceMoves(), 6);
    }
}
