package gui.gamewindow;

import gamecomponents.ShipPlacementOrientation;
import server.GameController;

import gamecomponents.Square;

import javax.swing.*;
import java.awt.*;

public class PlayingBoard extends JPanel {

    private GameWindow gameWindow;

    private String whos;

    private Square[][] squares = new Square[GameController.BOARD_DIMENSION][GameController.BOARD_DIMENSION];

//    private Square [] squares = new Square [GameController.BOARD_DIMENSION^2];



    public PlayingBoard(GameWindow gameWindow, String whos) {
        this.gameWindow = gameWindow;
        this.whos = whos;

        setLayout(new GridLayout(10, 10));

//        Border raisedBevele = BorderFactory.createRaisedBevelBorder();
//        JPanel square;
//        for(int i = 0; i<100; i++) {
//            square = new JPanel();
//            square.setPreferredSize(new Dimension(30,30));
//            add(square);
//            square.setBorder(raisedBevele);
//        }


        Square square;
        for (int row = 0; row < GameController.BOARD_DIMENSION; row++) {
            for (int column = 0; column < GameController.BOARD_DIMENSION; column++) {

                //Numrera 1-10
                square = new Square(row+1, column+1, this);
                add(square);
                squares[row][column] = square;
            }
        }


    }



    public String whos() {
        return whos;
    }

    public void sendClick(int row, int column, String whos) {
        gameWindow.sendClick(row, column, whos);
    }

    public void markSunkenShipSquare(int row, int column){
        squares[row-1][column-1].markSunkenShip();
    }

    public void markShot(int row, int column, boolean isHit){
        squares[row-1][column-1].markShot(isHit);
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
