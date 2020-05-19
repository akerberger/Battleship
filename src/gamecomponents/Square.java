package gamecomponents;


import gui.gamewindow.PlayingBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Represents a square on a PlayingBoard.
 */
public class Square extends JPanel {

    private final int ROW;
    private final int COLUMN;
    private final PlayingBoard BOARD;
    private boolean isShot = false;
    private boolean hasSunkenShip = false;

    private ImageIcon icon = null;

    private final SquareListener LISTENER = new SquareListener();

    /**
     *
     * @param row The row of the PlayingBoard that this object is located on
     * @param column The column of the PlayingBoard that this object is located on
     * @param board The PlayingBoard where this object is located on
     */
    public Square(int row, int column, PlayingBoard board) {
        this.ROW = row;
        this.COLUMN = column;
        this.BOARD = board;

        setBorder(BorderFactory.createRaisedBevelBorder());
        setPreferredSize(new Dimension(30, 30));

    }

    /**
     * Listens for click events made on this Square object
     */
    private class SquareListener extends MouseAdapter {

        /**
         * If a click occurs on this Square and it's not already marked as shot, the click will
         * be reported to the PlayingBoard.
         * @param e not used
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!isShot) {
                BOARD.clickOccured(ROW,COLUMN);
            }
        }
    }

    /**
     * Actions to take for marking this Square object as being shot. Triggers repaint() of this
     * Square object to make the icon visible.
     * @param icon The ImageIcon that will be displayed on this Square object.
     */
    public void markShot(ImageIcon icon) {
        isShot = true;
        this.icon = icon;

        repaint();
    }


    /**
     * Actions to take for marking this Square object as holding part of a sunken ship. Triggers repaint()
     * of this Square object to make the icon visible.
     * @param icon The ImageIcon that will be displayed on this Square object.
     */
    public void markSunkenShip(ImageIcon icon) {
        isShot = true;

        hasSunkenShip = true;

        this.icon = icon;

        repaint();


    }

    /**
     * Makes this Square clickable
     */
    public void addMouseListener() {
        addMouseListener(LISTENER);
    }

    /**
     * Makes this Square un-clickable
     */
    public void removeMouseListener() {
        removeMouseListener(LISTENER);
    }


    public void setIsShot() {
        isShot = true;
    }

    public int getRow() {
        return ROW;
    }

    public int getColumn() {
        return COLUMN;
    }

    public boolean isShot() {
        return isShot;
    }

    /**
     * Marks this Square object as holding part of a non-sunken, non-hit ship.
     */
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



    @Override
    public boolean equals(Object other) {

        if (other instanceof Square) {
            Square otherSquare = (Square) other;
            return (ROW == otherSquare.getRow() && COLUMN == otherSquare.COLUMN);
        }

        return false;
    }

    @Override
    public int hashCode(){
        return ROW*31*COLUMN*31;
    }


    @Override
    public String toString() {
        return ROW + " " + COLUMN + " " + isShot;
    }


}
