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
    private boolean hasSunkenShip = false;

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

        //kanske lägga in en funktion så att om man klickar på en markerad ruta får man nån info, istf att
        // sendClick skickas iväg...
        @Override
        public void mouseClicked(MouseEvent e) {
            if(!isShot){
                board.removeSquareListeners();
                board.sendClick(row, column, "whos");
            }
        }
    }

    public void markShot(boolean hit) {
        isShot=true;
        if (hit) {
            setBackground(Color.RED);
        }else{
            setBackground(Color.BLUE);
        }
    }


    //Sätt döskalle på sänkt ruta
    public void markSunkenShip(){
        isShot=true;
//        setBackground(Color.YELLOW);

        hasSunkenShip=true;

        repaint();


    }


    public void addMouseListener() {
        addMouseListener(listener);
    }

    public void removeMouseListener() {
        removeMouseListener(listener);
    }


    public void setIsShot(){
        isShot = true;
    }
    public int getRow(){
        return row;
    }

    public int getColumn(){
        return column;
    }

    public boolean isShot(){
        return isShot;
    }

    public void markPartOfShip() {
        setBackground(Color.BLACK);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        if(hasSunkenShip){
            ImageIcon skull = new ImageIcon("/Users/Erik/IdeaProjects/Battleships/skull.png");
            g.drawImage(skull.getImage(), 6,6,20,20,this);


        }


    }

    @Override
    public boolean equals(Object other){

        if(other instanceof Square){
            Square otherSquare = (Square) other;
            return(row == otherSquare.getRow() && column==otherSquare.column);
        }

        return false;
    }

    @Override
    public String toString(){
        return row+" "+column+" "+isShot;
    }


}