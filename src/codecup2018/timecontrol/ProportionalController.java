package codecup2018.timecontrol;

public class ProportionalController extends TimeController {

    // Computed by averaging turn times of KMAsTC_IEV_BSM1_7 vs Expy_NH
    private static final double[] FRACTION_OF_REMAINING = new double[]{
        0.275, 0.275, 0.325, 0.350, 0.420, 0.480, 0.560, 0.440, 0.170, 0.165, 0.2, 0.25, 0.333, 0.5, 1
    };

    public ProportionalController(int totalTimeMilliseconds) {
        super(totalTimeMilliseconds);
    }

    @Override
    public int getMillisecondsForMove(int turn) {
        return (int) Math.round(timeRemainingMilliseconds * FRACTION_OF_REMAINING[(turn - 1) / 2]);
    }

}
