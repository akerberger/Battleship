package gui.gamewindow;

import gamecomponents.ShipPlacementOrientation;
import server.GameController;

import gamecomponents.Square;

import javax.swing.*;
import java.awt.*;

public class PlayingBoard extends JPanel {

    private GameWindow gameWindow;

    private final boolean isMyBoard;

    private Square[][] squares = new Square[GameController.BOARD_DIMENSION][GameController.BOARD_DIMENSION];


    public PlayingBoard(GameWindow gameWindow, boolean isMyBoard) {
        this.gameWindow = gameWindow;
        this.isMyBoard=isMyBoard;

        setLayout(new BorderLayout());
        JLabel label = new JLabel( (isMyBoard ? "My ships" : "Opponents ships"));
        add(label, BorderLayout.NORTH);

        JPanel board = new JPanel();
        add(board, BorderLayout.SOUTH);
        board.setLayout(new GridLayout(10, 10));
        Square square;

        for (int row = 0; row < GameController.BOARD_DIMENSION; row++) {
            for (int column = 0; column < GameController.BOARD_DIMENSION; column++) {
                //Numrera 1-10
                square = new Square(row+1, column+1, this);
                board.add(square);
                squares[row][column] = square;
            }
        }

    }


    public void sendClick(int row, int column) {
        gameWindow.sendClick(row, column);
    }

    public void markSunkenShipSquare(int row, int column, ImageIcon icon){
        squares[row-1][column-1].markSunkenShip(icon);
    }

    public void markShot(int row, int column, boolean isHit, ImageIcon icon){
        squares[row-1][column-1].markShot(isHit, icon);
    }

    public void placeShipOnMyBoard(int startRow, int startColumn, int shipSize, ShipPlacementOrientation orientation) {
        //just nu ska, i SETUP_PHASE, en båt som är tre rutor lång markeras horisontellt
        System.out.println("SKA MARKERA SKEPP PÅ RUTA: " + startRow + " " + startColumn + " " + shipSize + " rutor stort");

        if(orientation == ShipPlacementOrientation.HORIZONTAL){
            for(int i = startColumn-1; i < startColumn-1+shipSize; i++){
                squares[startRow-1][i].markPartOfShip();
            }
        }else{
            for(int i = startRow-1; i < startRow-1+shipSize; i++){
                squares[i][startColumn-1].markPartOfShip();
            }
        }
    }

    public boolean isMyBoard(){
        return isMyBoard;
    }



    public void addSquareListeners() {
        for (int row = 1; row <= GameController.BOARD_DIMENSION; row++) {
            for (int column = 1; column <= GameController.BOARD_DIMENSION; column++) {
                squares[row-1][column-1].addMouseListener();
            }
        }
    }

    public void removeSquareListeners() {

        for (int row = 1; row <= GameController.BOARD_DIMENSION; row++) {
            for (int column = 1; column <= GameController.BOARD_DIMENSION; column++) {
                squares[row-1][column-1].removeMouseListener();
            }
        }
    }
}
