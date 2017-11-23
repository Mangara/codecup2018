package codecup2018.player;

import codecup2018.ArrayBoard;

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
