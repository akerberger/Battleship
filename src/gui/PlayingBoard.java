package gui;

import game.GameController;
import game.GameState;
import game.Square;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PlayingBoard extends JPanel {

    private GameWindow gameWindow;

    private String whos;

    private Square[][] squares = new Square[GameController.BOARD_DIMENSION][GameController.BOARD_DIMENSION];


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


    public void placeShipOnMyBoard(int startRow, int startColumn, int shipSize, boolean horizontal) {
        //just nu ska, i SETUP_PHASE, en båt som är tre rutor lång markeras horisontellt
        System.out.println("SKA MARKERA SKEPP PÅ RUTA: " + startRow + " " + startColumn + " " + shipSize + " rutor stort");

        if(horizontal){
            for(int i = startColumn-1; i < startColumn-1+shipSize; i++){
                squares[startRow-1][i].mark();
            }
        }



    }

    public void addMouseListener() {
        for (int row = 1; row <= GameController.BOARD_DIMENSION; row++) {
            for (int column = 1; column <= GameController.BOARD_DIMENSION; column++) {
                squares[row-1][column-1].addMouseListener();
            }

        }
    }

    public void removeMouseListener() {
        for (int row = 1; row <= GameController.BOARD_DIMENSION; row++) {
            for (int column = 1; column <= GameController.BOARD_DIMENSION; column++) {
                squares[row-1][column-1].removeMouseListener();

            }

        }
    }
}
