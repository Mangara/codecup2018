package codecup2018;

import codecup2018.evaluator.ExpectedValue;
import codecup2018.movegenerator.NoHoles;
import codecup2018.player.SimpleMaxPlayer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Runner {

    public static void main(String[] args) throws IOException {
        new SimpleMaxPlayer("Expy_NH", new ExpectedValue(), new NoHoles()).play(new BufferedReader(new InputStreamReader(System.in)), System.out);
    }
}
