package codecup2018.player;

import codecup2018.Board;
import codecup2018.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public abstract class ComponentPlayer extends Player {

    private static final byte VISITED = 119;
    private static final Comparator<Board> COMPONENT_SIZE = new Comparator<Board>() {
        @Override
        public int compare(Board o1, Board o2) {
            return -Integer.compare(o1.getFreeSpots(), o2.getFreeSpots());
        }
    };

    protected final List<Board> components = new ArrayList<>();

    public ComponentPlayer(String name) {
        super(name);
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        
        components.clear();
        Board tempBoard = new Board(currentBoard);

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (tempBoard.get(a, b) == Board.FREE) {
                    markComponent(tempBoard, a, b);

                    /*/// DEBUG
                    System.err.println("After marking component");
                    System.err.println("Main board:");
                    tempBoard.print();
                    System.err.println();
                    ///*/
                    Board component = extractComponentBoard(tempBoard);

                    /*/// DEBUG
                    System.err.println("After extracting component");
                    System.err.println("Main board:");
                    tempBoard.print();
                    System.err.println("Component:");
                    component.print();
                    System.err.println();
                    ///*/
                    components.add(component);
                }
            }
        }

        Collections.sort(components, COMPONENT_SIZE);

        /*/// DEBUG
        System.err.println("Components:");
        for (Pair<Board, Integer> component : components) {
            System.err.println("Component of size " + component.getSecond());
            component.getFirst().print();
            System.err.println();
        }
        ///*/
    }

    @Override
    public void block(byte a, byte b) {
        super.block(a, b);
        updateComponentsForMove(a, b, Board.BLOCKED);
    }

    @Override
    public void processMove(byte[] move, boolean mine) {
        /*/// DEBUG
        System.err.printf("Before processing move (%d, %d) = %d:%n", move[0], move[1], move[2]);
        board.print();
        System.err.println("Components:");
        for (Pair<Board, Integer> c : components) {
            System.err.println("Component of size " + c.getSecond());
            c.getFirst().print();
        }
        System.err.println();
        ///*/
        
        super.processMove(move, mine);
        updateComponentsForMove(move[0], move[1], (mine ? move[2] : (byte) -move[2]));
        
        /*/// DEBUG
        System.err.printf("After processing move (%d, %d) = %d:%n", move[0], move[1], move[2]);
        board.print();
        System.err.println("Components:");
        int totalFree = 0;
        for (Pair<Board, Integer> c : components) {
            System.err.println("Component of size " + c.getSecond());
            c.getFirst().print();
            totalFree += c.getFirst().getFreeSpots();
        }
        System.err.println();
        
        if (board.getFreeSpots() != totalFree) {
            throw new InternalError();
        }
        ///*/
    }

    private void updateComponentsForMove(byte a, byte b, byte value) {
        // Set this value everywhere and find the component that contains this move
        int playedComponent = -1;

        for (int i = 0; i < components.size(); i++) {
            Board bb = components.get(i);

            if (bb.get(a, b) == Board.FREE) {
                playedComponent = i;
            }

            bb.set(a, b, value);
        }

        // Find the new components after this move
        Board component = components.remove(playedComponent);
        List<Board> newComponents = split(component, a, b);

        for (Board c : newComponents) {
            int i = Collections.binarySearch(components, c, COMPONENT_SIZE);

            if (i < 0) {
                i = -(i + 1);
            }

            components.add(i, c);
        }

        /*/// DEBUG
        System.err.printf("Components after setting (%d, %d):%n", a, b);
        for (Pair<Board, Integer> c : components) {
            System.err.println("Component of size " + c.getSecond());
            c.getFirst().print();
            System.err.println();
        }
        ///*/
    }

    private void markComponent(Board board, byte a, byte b) {
        board.set(a, b, VISITED);

        if (a > 0 && board.get((byte) (a - 1), b) == Board.FREE) {
            markComponent(board, (byte) (a - 1), b);
        }

        if (a < 7 - b && board.get((byte) (a + 1), b) == Board.FREE) {
            markComponent(board, (byte) (a + 1), b);
        }

        if (b > 0 && board.get(a, (byte) (b - 1)) == Board.FREE) {
            markComponent(board, a, (byte) (b - 1));
        }

        if (b < 7 - a && board.get(a, (byte) (b + 1)) == Board.FREE) {
            markComponent(board, a, (byte) (b + 1));
        }

        if (a > 0 && b < 8 - a && board.get((byte) (a - 1), (byte) (b + 1)) == Board.FREE) {
            markComponent(board, (byte) (a - 1), (byte) (b + 1));
        }

        if (a < 8 - b && b > 0 && board.get((byte) (a + 1), (byte) (b - 1)) == Board.FREE) {
            markComponent(board, (byte) (a + 1), (byte) (b - 1));
        }
    }

    private Board extractComponentBoard(Board fullBoard) {
        Board componentBoard = new Board(fullBoard);

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (fullBoard.get(a, b) == Board.FREE) {
                    componentBoard.set(a, b, Board.BLOCKED);
                } else if (fullBoard.get(a, b) == VISITED) {
                    componentBoard.set(a, b, Board.FREE);
                    fullBoard.set(a, b, Board.BLOCKED);
                }
            }
        }

        return componentBoard;
    }

    private List<Board> split(Board cBoard, byte a, byte b) {
        List<Board> newComponents = new ArrayList<>();

        if (a > 0 && cBoard.get((byte) (a - 1), b) == Board.FREE) {
            markComponent(cBoard, (byte) (a - 1), b);
            newComponents.add(extractComponentBoard(cBoard));
        }

        if (a < 7 - b && cBoard.get((byte) (a + 1), b) == Board.FREE) {
            markComponent(cBoard, (byte) (a + 1), b);
            newComponents.add(extractComponentBoard(cBoard));
        }

        if (b > 0 && cBoard.get(a, (byte) (b - 1)) == Board.FREE) {
            markComponent(cBoard, a, (byte) (b - 1));
            newComponents.add(extractComponentBoard(cBoard));
        }

        if (b < 7 - a && cBoard.get(a, (byte) (b + 1)) == Board.FREE) {
            markComponent(cBoard, a, (byte) (b + 1));
            newComponents.add(extractComponentBoard(cBoard));
        }

        if (a > 0 && b < 8 - a && cBoard.get((byte) (a - 1), (byte) (b + 1)) == Board.FREE) {
            markComponent(cBoard, (byte) (a - 1), (byte) (b + 1));
            newComponents.add(extractComponentBoard(cBoard));
        }

        if (a < 8 - b && b > 0 && cBoard.get((byte) (a + 1), (byte) (b - 1)) == Board.FREE) {
            markComponent(cBoard, (byte) (a + 1), (byte) (b - 1));
            newComponents.add(extractComponentBoard(cBoard));
        }

        return newComponents;
    }
}
