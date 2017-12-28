package codecup2018.timecontrol;

public abstract class TimeController {
    
    protected final int totalTimeMilliseconds;
    protected int timeRemainingMilliseconds;
    protected long moveStartTime;

    public TimeController(int totalTimeMilliseconds) {
        this.totalTimeMilliseconds = totalTimeMilliseconds;
        timeRemainingMilliseconds = totalTimeMilliseconds;
    }
    
    public void reset() {
        timeRemainingMilliseconds = totalTimeMilliseconds;
    }
    
    public void startMove() {
        moveStartTime = System.nanoTime();
    }
    
    public void endMove() {
        int duration = (int) Math.ceil((System.nanoTime() - moveStartTime) / 1000000.0);
        timeRemainingMilliseconds -= duration;
        System.err.printf("That move took %d ms.%n", duration);
    }
    
    public abstract int getMillisecondsForMove(int turn);
    
    protected int getTurnsRemaining(int turn) {
        return 15 - turn / 2;
    }
}
