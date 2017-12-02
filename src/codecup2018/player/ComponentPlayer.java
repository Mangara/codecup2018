package codecup2018.player;

import codecup2018.data.ArrayBoard;
import codecup2018.data.Board;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class ComponentPlayer extends Player {

    private static final byte VISITED = 119;
    private static final Comparator<ArrayBoard> COMPONENT_SIZE = new Comparator<ArrayBoard>() {
        @Override
        public int compare(ArrayBoard o1, ArrayBoard o2) {
            return -Integer.compare(o1.getNFreeSpots(), o2.getNFreeSpots());
        }
    };

    protected final List<ArrayBoard> components = new ArrayList<>();

    public ComponentPlayer(String name) {
        super(name);
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);

        components.clear();
        ArrayBoard tempBoard = new ArrayBoard(currentBoard);

        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                if (tempBoard.get(pos) == Board.FREE) {
                    markComponent(tempBoard, a, (byte) (pos % 8));

                    /*/// DEBUG
                    System.err.println("After marking component");
                    System.err.println("Main board:");
                    tempBoard.print();
                    System.err.println();
                    ///*/
                    ArrayBoard component = extractComponentBoard(tempBoard);

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
    public void block(byte pos) {
        super.block(pos);
        byte a = (byte) (pos / 8), b = (byte) (pos % 8);
        updateComponentsForMove(a, b, Board.BLOCKED);
    }

    @Override
    public void processMove(int move, boolean mine) {
        /*/// DEBUG
        System.err.printf("Before processing move (%d, %d) = %d:%n", move[0], move[1], move[2]);
        Util.print(board);
        System.err.println("Components:");
        for (Pair<Board, Integer> c : components) {
            System.err.println("Component of size " + c.getSecond());
            c.getFirst().print();
        }
        System.err.println();
        ///*/

        super.processMove(move, mine);
        byte pos = Board.getMovePos(move);
        byte a = (byte) (pos / 8), b = (byte) (pos % 8);
        updateComponentsForMove(a, b, (mine ? Board.getMoveVal(move) : (byte) -Board.getMoveVal(move)));

        /*/// DEBUG
        System.err.printf("After processing move (%d, %d) = %d:%n", move[0], move[1], move[2]);
        Util.print(board);
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
            ArrayBoard bb = components.get(i);

            if (bb.get(Board.getPos(a, b)) == Board.FREE) {
                playedComponent = i;
            }

            bb.set(a, b, value);
        }

        // Find the new components after this move
        ArrayBoard component = components.remove(playedComponent);
        List<ArrayBoard> newComponents = split(component, a, b);

        for (ArrayBoard c : newComponents) {
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

    private void markComponent(ArrayBoard board, byte a, byte b) {
        board.set(a, b, VISITED);

        if (a > 0 && board.get(Board.getPos((byte) (a - 1), b)) == Board.FREE) {
            markComponent(board, (byte) (a - 1), b);
        }

        if (a < 7 - b && board.get(Board.getPos((byte) (a + 1), b)) == Board.FREE) {
            markComponent(board, (byte) (a + 1), b);
        }

        if (b > 0 && board.get(Board.getPos(a, (byte) (b - 1))) == Board.FREE) {
            markComponent(board, a, (byte) (b - 1));
        }

        if (b < 7 - a && board.get(Board.getPos(a, (byte) (b + 1))) == Board.FREE) {
            markComponent(board, a, (byte) (b + 1));
        }

        if (a > 0 && b < 8 - a && board.get(Board.getPos((byte) (a - 1), (byte) (b + 1))) == Board.FREE) {
            markComponent(board, (byte) (a - 1), (byte) (b + 1));
        }

        if (a < 8 - b && b > 0 && board.get(Board.getPos((byte) (a + 1), (byte) (b - 1))) == Board.FREE) {
            markComponent(board, (byte) (a + 1), (byte) (b - 1));
        }
    }

    private ArrayBoard extractComponentBoard(ArrayBoard fullBoard) {
        ArrayBoard componentBoard = new ArrayBoard(fullBoard);

        for (byte a = 0; a < 8; a++) {
            for (byte pos = (byte) (8 * a); pos < 7 * a + 8; pos++) {
                byte b = (byte) (pos % 8);
                if (fullBoard.get(pos) == Board.FREE) {
                    componentBoard.set(a, b, Board.BLOCKED);
                } else if (fullBoard.get(pos) == VISITED) {
                    componentBoard.set(a, b, Board.FREE);
                    fullBoard.set(a, b, Board.BLOCKED);
                }
            }
        }

        return componentBoard;
    }

    private List<ArrayBoard> split(ArrayBoard cBoard, byte a, byte b) {
        List<ArrayBoard> newComponents = new ArrayList<>();

        if (a > 0 && cBoard.get(Board.getPos((byte) (a - 1), b)) == Board.FREE) {
            markComponent(cBoard, (byte) (a - 1), b);
            newComponents.add(extractComponentBoard(cBoard));
        }

        if (a < 7 - b && cBoard.get(Board.getPos((byte) (a + 1), b)) == Board.FREE) {
            markComponent(cBoard, (byte) (a + 1), b);
            newComponents.add(extractComponentBoard(cBoard));
        }

        if (b > 0 && cBoard.get(Board.getPos(a, (byte) (b - 1))) == Board.FREE) {
            markComponent(cBoard, a, (byte) (b - 1));
            newComponents.add(extractComponentBoard(cBoard));
        }

        if (b < 7 - a && cBoard.get(Board.getPos(a, (byte) (b + 1))) == Board.FREE) {
            markComponent(cBoard, a, (byte) (b + 1));
            newComponents.add(extractComponentBoard(cBoard));
        }

        if (a > 0 && b < 8 - a && cBoard.get(Board.getPos((byte) (a - 1), (byte) (b + 1))) == Board.FREE) {
            markComponent(cBoard, (byte) (a - 1), (byte) (b + 1));
            newComponents.add(extractComponentBoard(cBoard));
        }

        if (a < 8 - b && b > 0 && cBoard.get(Board.getPos((byte) (a + 1), (byte) (b - 1))) == Board.FREE) {
            markComponent(cBoard, (byte) (a + 1), (byte) (b - 1));
            newComponents.add(extractComponentBoard(cBoard));
        }

        return newComponents;
    }
}
