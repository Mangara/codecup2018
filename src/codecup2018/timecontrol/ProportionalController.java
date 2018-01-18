package codecup2018.timecontrol;

public class ProportionalController extends TimeController {

    public static final double[] LINEAR_DECAY = new double[] {
        0.164444444444444,
        0.1796875, 
        0.198176291793313, 
        0.221127116502401, 
        0.250486696950032, 
        0.28961038961039,  
        0.344911639244363, 
        0.430697674418605, 
        0.588235294117647, 
        0.428571428571429, 
        0.5, 
        0.4, 
        0.5, 
        0.666666666666667, 
        1
    };
    
    private final double[] fractionOfRemaining;

    public ProportionalController(int totalTimeMilliseconds) {
        super(totalTimeMilliseconds);

        // Computed by averaging turn times of KMAsTC_IEV_BSM1_7 vs Expy_NH
        fractionOfRemaining = new double[]{
            0.275, 0.275, 0.325, 0.350, 0.420, 0.480, 0.560, 0.440, 0.170, 0.165, 0.2, 0.25, 0.333, 0.5, 1
        };
    }

    public ProportionalController(int totalTimeMilliseconds, double[] fractionOfRemaining) {
        super(totalTimeMilliseconds);
        this.fractionOfRemaining = fractionOfRemaining;
    }

    @Override
    public int getMillisecondsForMove(int turn) {
        return (int) Math.round(timeRemainingMilliseconds * fractionOfRemaining[(turn - 1) / 2]);
    }

}
