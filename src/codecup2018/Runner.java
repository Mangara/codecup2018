package codecup2018;

import codecup2018.evaluator.MedianFree;
import codecup2018.movegenerator.MostFreeMax;
import codecup2018.player.AspirationPlayer;
import codecup2018.player.Player;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Runner {

    public static void main(String[] args) throws IOException {
        //Player p = new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()); // Played Nov 11 Test Competition
        Player p = new AspirationPlayer("As_MF_MFM_11", new MedianFree(), new MostFreeMax(), 11);
        
        p.play(new BufferedReader(new InputStreamReader(System.in)), System.out);
    }
}
