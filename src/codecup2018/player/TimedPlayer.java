package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import codecup2018.timecontrol.TimeController;

public abstract class TimedPlayer extends StandardPlayer {

    protected final TimeController controller;

    public TimedPlayer(String name, Evaluator evaluator, MoveGenerator generator, TimeController controller) {
        super(name, evaluator, generator);
        this.controller = controller;
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        controller.reset();
    }

    @Override
    protected int selectMove() {
        controller.startMove();
        int time = controller.getMillisecondsForMove(turn);
        //if (TIMING) {
            System.err.printf("%d ms for this move.%n", time);
        //}
        int move = selectMove(time);
        controller.endMove();
        return move;
    }

    protected abstract int selectMove(int millisecondsToMove);

}
