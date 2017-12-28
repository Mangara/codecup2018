package codecup2018.timecontrol;

public class EqualTimeController extends TimeController {

    public EqualTimeController(int totalTimeMilliseconds) {
        super(totalTimeMilliseconds);
    }

    @Override
    public int getMillisecondsForMove(int turn) {
        return Math.max(0, (int) Math.round(timeRemainingMilliseconds / (double) getTurnsRemaining(turn)));
    }
    
}
