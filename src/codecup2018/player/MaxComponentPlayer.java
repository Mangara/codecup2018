package codecup2018.player;

import codecup2018.data.ArrayBoard;

public class MaxComponentPlayer extends ComponentPlayer {

    private final Player player;

    public MaxComponentPlayer(Player player) {
        super("Co_" + player.getName());
        this.player = player;
    }

    @Override
    protected int selectMove() {
        ArrayBoard largestComponent = components.get(0);

        if (largestComponent.getNFreeSpots()> 1) {
            player.initialize(largestComponent);
        } else {
            player.initialize(board);
        }
        
        return player.selectMove();
    }

}
