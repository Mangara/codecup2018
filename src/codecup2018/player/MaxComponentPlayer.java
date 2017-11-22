package codecup2018.player;

import codecup2018.Board;

public class MaxComponentPlayer extends ComponentPlayer {

    private final Player player;

    public MaxComponentPlayer(Player player) {
        super("Co_" + player.getName());
        this.player = player;
    }

    @Override
    protected byte[] selectMove() {
        Board largestComponent = components.get(0);

        if (largestComponent.getFreeSpots()> 1) {
            player.initialize(largestComponent);
        } else {
            player.initialize(board);
        }
        
        return player.selectMove();
    }

}
