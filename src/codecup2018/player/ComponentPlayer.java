package codecup2018.player;

import codecup2018.Board;
import java.util.List;

public abstract class ComponentPlayer extends Player {

    protected List<Board> components;

    public ComponentPlayer(String name) {
        super(name);
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        // TODO: set up components
    }

    @Override
    public void block(String loc) {
        super.block(loc);
        // TODO: update components
    }

    @Override
    public void processMove(byte[] move, boolean mine) {
        super.processMove(move, mine);
        // TODO: update components
    }
}
