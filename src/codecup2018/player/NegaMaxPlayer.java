package codecup2018.player;

import codecup2018.data.Board;
import codecup2018.evaluator.Evaluator;
import codecup2018.movegenerator.MoveGenerator;

public class NegaMaxPlayer extends StandardPlayer {

    private static final boolean DEBUG_AB = true;

    private int depth;

    public NegaMaxPlayer(String name, Evaluator evaluator, MoveGenerator generator, int depth) {
        super(name, evaluator, generator);
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    protected int selectMove() {
        return negamax(1, depth + 1, Board.MIN_EVAL_MOVE, Board.MAX_EVAL_MOVE);
    }

    private int negamax(int player, int depth, int alpha, int beta) {
        if (DEBUG_AB) {
            System.err.printf("%s:  Running negamax with %d turns left, interval=[%d, %d] and board state:%n", getName(), depth, Board.getMoveEval(alpha), Board.getMoveEval(beta));
            Board.print(board);
        }

        if (depth == 0 || board.isGameOver()) {
            return Board.buildMove((byte) 0, (byte) 0, player * evaluator.evaluate(board));
        }

        int bestMove = Board.MIN_EVAL_MOVE;
        int[] moves = generator.generateMoves(board, player > 0);

        for (int move : moves) {
            if (DEBUG_AB) {
                System.err.printf("%s:   Evaluating move %s%n", getName(), Board.moveToString(move));
            }

            board.applyMove(move);
            evaluator.applyMove(move);
            int value = -Board.getMoveEval(negamax(-player, depth - 1, Board.negateEval(beta), Board.negateEval(alpha)));
            board.undoMove(move);
            evaluator.undoMove(move);

            // TODO: think more about when we just want an eval and when we want the entire move
            
            if (DEBUG_AB) {
                System.err.printf("%s:   Got back a score of %d%n", getName(), Board.getMoveEval(value));
            }

            if (Board.getMoveEval(value) > Board.getMoveEval(bestMove)) {
                bestMove = Board.setMoveEval(move, Board.getMoveEval(value));

                if (bestMove > alpha) {
                    alpha = bestMove;

                    if (beta <= alpha) {
                        break;
                    }
                }
            }
        }

        return bestMove;
    }

}
