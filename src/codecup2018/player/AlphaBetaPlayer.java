package codecup2018.player;

import codecup2018.evaluator.Evaluator;

public class AlphaBetaPlayer extends Player {

    private final Evaluator evaluator;
    
    public AlphaBetaPlayer(String name, Evaluator evaluator) {
        super(name);
        this.evaluator = evaluator;
    }

    @Override
    protected byte[] selectMove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
