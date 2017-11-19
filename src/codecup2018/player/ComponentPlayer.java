package codecup2018.player;

import codecup2018.Board;
import codecup2018.Pair;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class ComponentPlayer extends Player {

    private static final byte VISITED = 119;
    protected List<Pair<Board, Integer>> components;

    public ComponentPlayer(String name) {
        super(name);
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        
        Board tempBoard = new Board(currentBoard);

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (tempBoard.get(a, b) == Board.FREE) {
                    // Mark each cell of this connected component as visited
                    markComponent(tempBoard, a, b);
                    
                    Board component = new Board(tempBoard);
                    int size = 0;
                    
                    for (byte a2 = 0; a2 < 8; a2++) {
                        for (byte b2 = 0; b2 < 8 - a2; b2++) {
                            if (tempBoard.get(a2, b2) == Board.FREE) {
                                component.set(a2, b2, Board.BLOCKED);
                            } else if (tempBoard.get(a2, b2) == VISITED) {
                                component.set(a2, b2, Board.FREE);
                                tempBoard.set(a2, b2, Board.BLOCKED);
                                size++;
                            }
                        }
                    }
                    
                    components.add(new Pair<>(component, size));
                }
            }
        }

        Collections.sort(components, new Comparator<Pair<Board, Integer>>() {
            @Override
            public int compare(Pair<Board, Integer> o1, Pair<Board, Integer> o2) {
                return -Integer.compare(o1.getSecond(), o2.getSecond());
            }
        });
    }

    @Override
    public void block(String loc) {
        super.block(loc);
        // TODO: update components
    }

    @Override
    public void processMove(byte[] move, boolean mine) {
        super.processMove(move, mine);
        // TODO: update components
    }

    private void markComponent(Board board, byte a, byte b) {
        // DFS
    }
}
