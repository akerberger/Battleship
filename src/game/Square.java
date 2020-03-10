package game;

import gui.PlayingBoard;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Square extends JPanel {

    private final int row;
    private final int column;
    private final PlayingBoard board;

    private BoardListener listener = new BoardListener();

    public Square(int row, int column, PlayingBoard board) {
        this.row = row;
        this.column = column;
        this.board = board;
        Border raisedBevele = BorderFactory.createRaisedBevelBorder();
        setBorder(raisedBevele);
        setPreferredSize(new Dimension(30, 30));
    }

    private class BoardListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            board.removeMouseListeners();
            board.sendClick(row, column, "whos");
            System.out.println("testar klick, row: " + row + " column: " + column);
//            removeMouseListener();
        }
    }

    public void markShot(boolean isHit) {
        if (isHit) {
            setBackground(Color.BLUE);
        }else{
            setBackground(Color.RED);
        }
    }

    public void addMouseListener() {
        addMouseListener(listener);
    }

    public void removeMouseListener() {
        removeMouseListener(listener);
    }

    public void mark() {
        setBackground(Color.BLACK);
        repaint();
    }


}
