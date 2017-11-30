package codecup2018.player;

import codecup2018.data.Board;

public class GUIPlayer extends Player {

    private final BlackHoleFrame frame = new BlackHoleFrame();
    
    public GUIPlayer(String name) {
        super(name);
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
            }
        });
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        
        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                byte val = board.get(pos);
                
                if (val == Board.BLOCKED) {
                    frame.block(a, (byte) (pos % 8));
                } else if (val != Board.FREE) {
                    frame.processMove(new byte[] {a, (byte) (pos % 8), (val > 0 ? val : (byte) -val)}, val > 0);
                }
            }
        }
    }
    
    @Override
    public void block(byte pos) {
        super.block(pos);
        frame.block((byte) (pos / 8), (byte) (pos % 8));
    }

    @Override
    public void processMove(byte[] move, boolean mine) {
        super.processMove(move, mine);
        frame.processMove(move, mine);
    }

    @Override
    protected byte[] selectMove() {
        frame.requestMove();
        
        while (!frame.moveAvailable()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
        
        return frame.getMove();
    }
    
}
