package codecup2018.player;

import codecup2018.ArrayBoard;
import codecup2018.Board;
import codecup2018.Util;
import codecup2018.evaluator.ExpectedValue;
import codecup2018.evaluator.MedianFree;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.metal.MetalButtonUI;

public class BlackHoleFrame extends javax.swing.JFrame {

    private static final Color BLOCKED_COLOR = new Color(105, 68, 56);
    private static final Color MY_BG_COLOR = new Color(0.4f, 0.639f, 0.824f);
    private static final Color MY_FG_COLOR = new Color(255, 255, 255);
    private static final Color MY_HOLE_FG_COLOR = new Color(0.043f, 0.38f, 0.643f);
    private static final Color OPP_BG_COLOR = new Color(0.941f, 0.424f, 0.596f);
    private static final Color OPP_FG_COLOR = new Color(0, 0, 0);
    private static final Color OPP_HOLE_FG_COLOR = new Color(0.882f, 0f, 0.298f);
    private static final Color NEUTRAL_HOLE_FG_COLOR = new Color(0, 0, 0);

    private static final ButtonUI MY_UI = new MetalButtonUI() {
        @Override
        protected Color getDisabledTextColor() {
            return MY_FG_COLOR;
        }
    };

    private static final ButtonUI OPP_UI = new MetalButtonUI() {
        @Override
        protected Color getDisabledTextColor() {
            return OPP_FG_COLOR;
        }
    };

    private static final ButtonUI BLOCKED_UI = new MetalButtonUI() {
        @Override
        protected Color getDisabledTextColor() {
            return BLOCKED_COLOR;
        }
    };

    private final JToggleButton[][] boardButtons = new JToggleButton[8][8];
    private final JButton[] numberButtons = new JButton[15];

    private byte[] move = null;
    private boolean[] used = new boolean[15];
    private ArrayBoard board;

    /**
     * Creates new form BlackHoleFrame
     */
    public BlackHoleFrame() {
        board = new ArrayBoard();
        
        initComponents();
        initCustomComponents();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setNumbersEnabled(false);
    }

    public void block(byte a, byte b) {
        board.set(a, b, Board.BLOCKED);
        JToggleButton button = boardButtons[a][b];

        button.setEnabled(false);
        button.setText("X");
        button.setBackground(BLOCKED_COLOR);
        button.setUI(BLOCKED_UI);
    }

    public void processMove(byte[] move, boolean mine) {
        board.set(move[0], move[1], (mine ? move[2] : (byte) -move[2]));
        JToggleButton button = boardButtons[move[0]][move[1]];

        button.setEnabled(false);
        button.setText(Integer.toString(move[2]));
        button.setBackground(OPP_BG_COLOR);
        button.setUI(OPP_UI);
    }

    public void requestMove() {
        // Enable all free board buttons
        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (board.get(a, b) == Board.FREE) {
                    boardButtons[a][b].setEnabled(true);
                    
                    int val = board.getHoleValue(a, b);
                    if (val > 0) {
                        boardButtons[a][b].setText(Integer.toString(val));
                        boardButtons[a][b].setForeground(MY_HOLE_FG_COLOR);
                    } else if (val < 0) {
                        boardButtons[a][b].setText(Integer.toString(-val));
                        boardButtons[a][b].setForeground(OPP_HOLE_FG_COLOR);
                    } else {
                        boardButtons[a][b].setText("0");
                        boardButtons[a][b].setForeground(NEUTRAL_HOLE_FG_COLOR);
                    }
                }
            }
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public boolean moveAvailable() {
        return move != null;
    }

    public byte[] getMove() {
        if (move == null) {
            throw new NullPointerException();
        }

        byte[] result = move;

        move = null;

        return result;
    }

    private void boardSpacePressed(byte a, byte b) {
        System.out.println("Pressed board space: (" + a + ", " + b + ") = " + Util.coordinatesToString(a, b));
        
        // Evaluate possible moves here
        ExpectedValue ev = new ExpectedValue();
        MedianFree mf = new MedianFree();
        
        int currentExpected = ev.evaluate(board);
        int currentMedianFree = mf.evaluate(board);
        
        for (byte v = 1; v <= 15; v++) {
            if (!board.haveIUsed(v)) {
                byte[] move = new byte[] {a, b, v};
                board.applyMove(move);
                int expected = ev.evaluate(board);
                int medianFree = mf.evaluate(board);
                board.undoMove(move);
                
                System.out.printf(" %d - EV: %.4f (%+.3f)  MF: %d (%+d)%n", v, expected / (double) 10000, (expected - currentExpected) / (double) 10000, medianFree, medianFree - currentMedianFree);
            }
        }
        
        if (boardButtons[a][b].isSelected()) {
            setNumbersEnabled(true);
        } else {
            setNumbersEnabled(false);
        }
    }

    private void numberPressed(byte val) {
        System.out.println("Pressed number " + val);
        // Set up move
        move = new byte[3];

        for (byte a = 0; a < 8; a++) {
            for (byte b = 0; b < 8 - a; b++) {
                if (boardButtons[a][b].isSelected()) {
                    move[0] = a;
                    move[1] = b;
                    move[2] = val;

                    used[val - 1] = true;
                    board.set(a, b, val);

                    boardButtons[a][b].setSelected(false);
                    boardButtons[a][b].setText(Integer.toString(val));
                    boardButtons[a][b].setBackground(MY_BG_COLOR);
                    boardButtons[a][b].setUI(MY_UI);
                }
            }
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setNumbersEnabled(false);
        setBoardEnabled(false);
    }

    private void setBoardEnabled(boolean enabled) {
        for (int a = 0; a < 8; a++) {
            for (int b = 0; b < 8 - a; b++) {
                boardButtons[a][b].setEnabled(enabled);
            }
        }
    }

    private void setNumbersEnabled(boolean enabled) {
        for (int i = 0; i < 15; i++) {
            if (!used[i] || !enabled) {
                numberButtons[i].setEnabled(enabled);
            }
        }
    }

    private void initCustomComponents() {
        // Create row panels
        JPanel[] rowPanels = new JPanel[8];
        for (int i = 0; i < 8; i++) {
            rowPanels[i] = new JPanel();
        }

        // Create buttons
        ButtonGroup boardButtonGroup = new ButtonGroup();
        for (int a = 0; a < 8; a++) {
            for (int b = 0; b < 8 - a; b++) {
                JToggleButton boardButton = new JToggleButton();
                boardButtons[a][b] = boardButton;

                boardButton.setPreferredSize(new Dimension(60, 45));
                boardButtonGroup.add(boardButton);

                final byte vA = (byte) a, vB = (byte) b;

                boardButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        boardSpacePressed(vA, vB);
                    }
                });
            }
        }

        // Add buttons to rows
        for (int row = 0; row < 8; row++) {
            for (int i = 0; i <= row; i++) {
                rowPanels[row].add(boardButtons[row - i][i]);
            }
        }

        // Add row panels
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        for (int i = 0; i < 8; i++) {
            contentPane.add(rowPanels[i]);
        }

        // Create number panel
        JPanel numberPanel = new JPanel();
        numberPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        // Create and add number buttons
        for (int i = 1; i <= 15; i++) {
            JButton numberButton = new JButton(Integer.toString(i));
            numberButtons[i - 1] = numberButton;

            numberPanel.add(numberButton);

            final byte val = (byte) i;
            numberButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    numberPressed(val);
                }
            });
        }

        contentPane.add(numberPanel);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Black Hole");
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(BlackHoleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BlackHoleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BlackHoleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BlackHoleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BlackHoleFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
