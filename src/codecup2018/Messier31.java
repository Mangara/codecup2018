package codecup2018;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Messier31 extends Player {
    
    public static void main(String[] args) throws IOException {
        Player p = new Messier31("Messier31");
        p.play(new BufferedReader(new InputStreamReader(System.in)), System.out);
    }

    public Messier31(String name) throws IOException {
        super(name);
    }

    @Override
    protected byte[] selectMove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
