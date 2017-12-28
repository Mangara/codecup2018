package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;
import codecup2018.timecontrol.TimeController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimedUCBPlayer extends TimedPlayer {

    private static final int ITERATIONS_PER_MEASUREMENT = 50;
    public static double UCB_PARAMETER = 0.1;
    public static int INITIAL_HEURISTIC_WEIGHT = 200;
    private static final boolean DEBUG_UCB = false;
    private final GameTreeNode[] lineOfPlay = new GameTreeNode[31];
    private int lopIndex = 0;
    private GameTreeNode root;

    public TimedUCBPlayer(String name, Evaluator evaluator, MoveGenerator generator, TimeController controller) {
        super(name, evaluator, generator, controller);
    }

    @Override
    public void initialize(Board currentBoard) {
        super.initialize(currentBoard);
        root = null;
    }

    @Override
    protected int selectMove(int millisecondsToMove) {
        long start = System.nanoTime();
        
        if (root == null || root.moves.length == 0) {
            root = new GameTreeNode(0, true, generator.generateMoves(board, true));
        }

        if (root.moves.length == 1) {
            return root.moves[0];
        }

        lineOfPlay[0] = root;
        long nsToMove = 1000000 * (long) millisecondsToMove;
        int iterationsUntilMeasurement = ITERATIONS_PER_MEASUREMENT;
        int totalIterations = 0;

        while (iterationsUntilMeasurement > 0 || System.nanoTime()- start < nsToMove) {
            totalIterations++;
            navigateTree();
            
            if (DEBUG_UCB) {
                System.err.println("Navigated the tree down to:");
                Board.print(board);
                System.err.println("Evaluation: " + evaluator.evaluate(board));
            }
            
            backupEvaluation(evaluator.evaluate(board));
            
            if (DEBUG_UCB) {
                System.err.println();
            }
            
            if (iterationsUntilMeasurement == 0) {
                iterationsUntilMeasurement = ITERATIONS_PER_MEASUREMENT;
            } else {
                iterationsUntilMeasurement--;
            }
        }
        
        Arrays.fill(lineOfPlay, null); // Clear so the GC can do its thing

        if (DEBUG_UCB) {
            System.err.println("End of turn " + turn);
            root.printChildren();
            System.err.println();
        }
        
        if (root.getMostVisitedChild() == null) {
            System.err.println("WELP! No most visited child. Board:");
            Board.print(board);
            System.err.println("Root moves: " + Arrays.toString(root.moves));
        }
        
        System.err.printf("%d iterations in %d ms. Average iterations per ms: %d.%n", totalIterations, (System.nanoTime() - start) / 1000000, (int) Math.round(totalIterations / (double) millisecondsToMove));

        return root.getMostVisitedChild().getMove();
    }

    @Override
    public void processMove(int move, boolean mine) {
        super.processMove(move, mine);

        if (root != null) {
            int m = (mine ? move : Board.setMoveVal(move, (byte) -Board.getMoveVal(move)));
            root = root.getChildForMove(m);
        }

        if (DEBUG_UCB) {
            System.err.println("Processed move " + Board.moveToString(move) + ". New root: " + root);
        }
    }

    private void navigateTree() {
        GameTreeNode n = lineOfPlay[0];

        while (!board.isGameInEndGame() && n.isFullyExpanded()) {
            n = n.getPriorityChild();
            lineOfPlay[++lopIndex] = n;
            board.applyMove(n.getMove());
            evaluator.applyMove(n.getMove());

            if (DEBUG_UCB) {
                System.err.print(Board.moveToString(n.getMove()) + " -> ");
            }
        }

        if (!board.isGameInEndGame()) {
            n = n.expand(board, generator); // Already applies move to board
            lineOfPlay[++lopIndex] = n;
            evaluator.applyMove(n.getMove());

            if (DEBUG_UCB) {
                System.err.println(Board.moveToString(n.getMove()));
            }
        } else if (DEBUG_UCB) {
            System.err.println("Endgame");
        }
    }

    private void backupEvaluation(int eval) {
        while (lopIndex > 0) {
            GameTreeNode n = lineOfPlay[lopIndex];
            n.update(eval);
            board.undoMove(n.getMove());
            evaluator.undoMove(n.getMove());
            lopIndex--;
        }

        lineOfPlay[0].update(eval);
    }

    private static class GameTreeNode {

        private static final double SCORE_SCALING_FACTOR = 1 / 750000.0;

        // Properties of this node
        private final int move; // Move from parent
        private final boolean player1; // Whether player1 is to move from this state
        private final int[] moves;
        private final GameTreeNode[] children;
        private int nextExpansion = 0;

        // For keeping score
        private int nVisits = 0;
        private double totalScore = 0;

        public GameTreeNode(int move, boolean player1, int[] moves) {
            this.move = move;
            this.player1 = player1;
            this.moves = moves;
            children = new GameTreeNode[moves.length];

            if (moves.length == 0) {
                nextExpansion = -1;
            }
        }

        public int getMove() {
            return move;
        }

        public int getnVisits() {
            return nVisits;
        }

        public double getAverageScore() {
            return totalScore / nVisits;
        }

        /**
         * Returns the priority this node, as given by the UCB1 formula.
         *
         * @param sqrtLognVisitsParent - the square root of the natural
         * logarithm of the number of times the parent has been visited
         * @return
         */
        public double getPriority(double sqrtLognVisitsParent) {
            return (nVisits == 0 ? Double.POSITIVE_INFINITY : totalScore / nVisits + UCB_PARAMETER * sqrtLognVisitsParent / Math.sqrt(nVisits));
        }

        /**
         * Updates the fields of this node with the score obtained from a game
         * playing this move.
         *
         * @param score01
         */
        public void update(int score) {
            if (nVisits == 0) {
                // Initialize with heuristic value
                nVisits = INITIAL_HEURISTIC_WEIGHT;
                totalScore = INITIAL_HEURISTIC_WEIGHT * SCORE_SCALING_FACTOR * (player1 ? -score : score);
            } else {
                nVisits++;
                totalScore += SCORE_SCALING_FACTOR * (player1 ? -score : score);
            }

            if (DEBUG_UCB) {
                System.err.println("After update: " + toString());
            }
        }

        public boolean isFullyExpanded() {
            return nextExpansion < 0;
        }

        private GameTreeNode expand(Board board, MoveGenerator generator) {
            int nextMove = moves[nextExpansion];
            board.applyMove(nextMove);

            GameTreeNode child = new GameTreeNode(nextMove, !player1, (board.isGameInEndGame() ? new int[0] : generator.generateMoves(board, !player1)));
            children[nextExpansion] = child;

            //System.err.println("Created " + nextExpansion + "th child with move " + Board.moveToString(moves[nextExpansion]) + " c: " + children[nextExpansion]);
            nextExpansion++;
            if (nextExpansion == moves.length) {
                nextExpansion = -1;
            }

            return child;
        }

        private GameTreeNode getPriorityChild() {
            //System.err.println("Selecting the next child to visit after move " + Board.moveToString(move));

            double sqrtlogn = Math.sqrt(Math.log(nVisits));
            double maxPriority = Double.NEGATIVE_INFINITY;
            GameTreeNode bestChild = null;

            for (GameTreeNode child : children) {
                double p = child.getPriority(sqrtlogn);

                //System.err.println("Priority of child with move " + Board.moveToString(child.move) + ": " + p);
                if (p > maxPriority) {
                    maxPriority = p;
                    bestChild = child;
                }
            }
            
            if (bestChild == null) {
                System.err.println("Moves: " + Arrays.toString(moves));
                printChildren();
            }

            return bestChild;
        }

        public GameTreeNode getMostVisitedChild() {
            int maxVisited = -1;
            double bestAverage = Double.NEGATIVE_INFINITY;
            GameTreeNode bestChild = null;

            for (GameTreeNode child : children) {
                if (child != null && (child.getnVisits() > maxVisited || (child.getnVisits() == maxVisited && child.getAverageScore() > bestAverage))) {
                    maxVisited = child.getnVisits();
                    bestAverage = child.getAverageScore();
                    bestChild = child;
                }
            }

            return bestChild;
        }

        private GameTreeNode getChildForMove(int m) {
            for (int i = 0; i < moves.length; i++) {
                if (Board.equalMoves(moves[i], m)) {
                    return children[i];
                }
            }

            return null;
        }

        void printChildren() {
            List<Integer> childIndices = new ArrayList<>();

            for (int i = 0; i < moves.length; i++) {
                childIndices.add(i);
            }

            Collections.sort(childIndices, new Comparator<Integer>() {

                @Override
                public int compare(Integer o1, Integer o2) {
                    return Double.compare((children[o1] == null ? -1 : children[o1].getAverageScore()), (children[o2] == null ? -1 : children[o2].getAverageScore()));
                }
            });

            for (Integer i : childIndices) {
                if (children[i] == null) {
                    System.err.println(Board.moveToString(moves[i]) + " has not been played.");
                } else {
                    System.err.println(Board.moveToString(moves[i]) + " has been played " + children[i].getnVisits() + " times. It has an average score of " + children[i].getAverageScore());
                }
            }
        }

        @Override
        public String toString() {
            return "Node " + Board.moveToString(move) + " with " + nVisits + " visits and average score " + getAverageScore();
        }
    }

}
