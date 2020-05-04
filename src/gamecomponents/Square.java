package gamecomponents;


import gui.gamewindow.PlayingBoard;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Square extends JPanel {

    private final int row;
    private final int column;
    private final PlayingBoard board;
    private boolean isShot = false;
    //    private boolean isShotShip = false;
    private boolean hasSunkenShip = false;

    private ImageIcon icon = null;

    private SquareListener listener = new SquareListener();

    public Square(int row, int column, PlayingBoard board) {
        this.row = row;
        this.column = column;
        this.board = board;

        Border raisedBevele = BorderFactory.createRaisedBevelBorder();
        setBorder(raisedBevele);
        setPreferredSize(new Dimension(30, 30));

    }

    private class SquareListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!isShot) {
                board.removeSquareListeners();
                board.sendClick(row, column);
            }
        }
    }

    public void markShot(ImageIcon icon) {
        isShot = true;

        this.icon = icon;

        repaint();
    }


    //Sätt döskalle på sänkt ruta
    public void markSunkenShip(ImageIcon icon) {
        isShot = true;

        hasSunkenShip = true;

        this.icon = icon;

        repaint();


    }


    public void addMouseListener() {
        addMouseListener(listener);
    }

    public void removeMouseListener() {
        removeMouseListener(listener);
    }


    public void setIsShot() {
        isShot = true;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean isShot() {
        return isShot;
    }

    public void markPartOfShip() {
        setBackground(Color.BLACK);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (hasSunkenShip) {
            setBackground(Color.RED);
        }

        if (icon != null) {
            g.drawImage(icon.getImage(), 5, 5, 20, 20, this);
        }
    }


    //utöka denna definition. Samt överskugga hashcode!
    @Override
    public boolean equals(Object other) {

        if (other instanceof Square) {
            Square otherSquare = (Square) other;
            return (row == otherSquare.getRow() && column == otherSquare.column);
        }

        return false;
    }


    @Override
    public String toString() {
        return row + " " + column + " " + isShot;
    }


}
