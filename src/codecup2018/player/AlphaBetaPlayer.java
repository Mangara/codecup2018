package codecup2018.player;

import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;

public class AlphaBetaPlayer extends Player {

    private final Evaluator evaluator;
    private final MoveGenerator generator;
    
    public AlphaBetaPlayer(String name, Evaluator evaluator, MoveGenerator generator) {
        super(name);
        this.evaluator = evaluator;
        this.generator = generator;
    }

    @Override
    protected byte[] selectMove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
