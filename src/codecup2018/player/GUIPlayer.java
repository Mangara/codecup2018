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
            for (byte b = 0; b < 8 - a; b++) {
                byte val = board.get(a, b);
                
                if (val == Board.BLOCKED) {
                    frame.block(a, b);
                } else if (val != Board.FREE) {
                    frame.processMove(new byte[] {a, b, (val > 0 ? val : (byte) -val)}, val > 0);
                }
            }
        }
    }
    
    @Override
    public void block(byte a, byte b) {
        super.block(a, b);
        frame.block(a, b);
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
