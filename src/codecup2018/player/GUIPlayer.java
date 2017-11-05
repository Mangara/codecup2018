package codecup2018.player;

import codecup2018.player.Player;

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
    public void block(String loc) {
        super.block(loc);
        frame.block(loc);
    }
    
    @Override
    public void processMove(String move) {
        super.processMove(move);
        frame.processMove(move);
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
