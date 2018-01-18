package codecup2018.timecontrol;

import codecup2018.player.Player;

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
        
        //// DEBUG
        if (Player.TIMING) {
            System.err.println("Move took " + duration + " ms.");
        }
    }
    
    public abstract int getMillisecondsForMove(int turn);
    
    protected int getTurnsRemaining(int turn) {
        return 15 - (turn - 1) / 2;
    }
}
