package gui.gamewindow;

import gamecomponents.ShipPlacementOrientation;
import server.GameController;

import gamecomponents.Square;

import javax.swing.*;
import java.awt.*;


/**
 * Represents a playing board on the game window. Sends and receives game events to and from it's gameWindow object
 * in the form of player clicks on the squares matrix and validated moves from the server.
 */
public class PlayingBoard extends JPanel {

    private GameWindow gameWindow;

    //Enumerated from 1-10
    private Square[][] squares = new Square[GameController.BOARD_DIMENSION][GameController.BOARD_DIMENSION];

    /**
     * Adds a playing board label indicating which player this PlayingBoard belongs to and creates
     * the board panel with the square matrix.
     *
     * @param gameWindow
     * @param isMyBoard
     */
    public PlayingBoard(GameWindow gameWindow, boolean isMyBoard) {
        this.gameWindow = gameWindow;
        setLayout(new BorderLayout());

        //set the playing board label
        JLabel label = new JLabel( (isMyBoard ? "My ships" : "Opponents ships"));
        add(label, BorderLayout.NORTH);

        //add the board panel
        add(setupBoardPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates the board panel with the Square matrix. N.B that the Squares are numbered from 1.
     * @return The board panel
     */
    private JPanel setupBoardPanel(){
        JPanel board = new JPanel();
        board.setLayout(new GridLayout(10, 10));
        Square square;

        for (int row = 0; row < GameController.BOARD_DIMENSION; row++) {
            for (int column = 0; column < GameController.BOARD_DIMENSION; column++) {

                square = new Square(row+1, column+1, this);
                board.add(square);
                squares[row][column] = square;
            }
        }
        return board;

    }

    /**
     * Passes a player click event through to the gameWindow.
     * @param row The row where the click occurred
     * @param column The Column where the click occurred
     */
    public void sendClick(int row, int column) {
        gameWindow.sendClick(row, column);
    }

    /**
     * Passes on information to a Square that it should be marked as holding a sunken ship.
     * (N.B. compensates the row and column values by -1 due to the way a click event is validated
     * at the server)
     * @param row The row of the Square that should be marked
     * @param column The column of the Square that should be marked
     * @param icon The icon that the Square should present
     */
    public void markSunkenShipSquare(int row, int column, ImageIcon icon){
        squares[row-1][column-1].markSunkenShip(icon);
    }

    /**
     * Passes on information to a Square that it should be marked as holding a sunken ship.
     * (N.B. compensates the row and column values by -1 due to the way a click event is validated
     * at the server)
     * @param row The row of the Square that should be marked
     * @param column The column of the Square that should be marked
     * @param icon The icon that the Square should present
     */
    public void markShot(int row, int column, ImageIcon icon){
        squares[row-1][column-1].markShot(icon);
    }

    /**
     * Actions to take for a ship to be marked on this playing board.
     * (N.B. compensates the row and column values by -1 due to the way a click event is validated
     *  at the server)
     * @param startRow The row of the first Square on the Playing board that will hold the ship
     * @param startColumn The column of the first Square on the Playing board that will hold the ship
     * @param shipSize The number of Squares that will hold the ship
     * @param orientation The orientation of the ship on the board
     */
    public void placeShipOnBoard(int startRow, int startColumn, int shipSize, ShipPlacementOrientation orientation) {

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

    /**
     * Makes the squares of this playing board clickable
     */
    public void addSquareListeners() {
        for (int row = 1; row <= GameController.BOARD_DIMENSION; row++) {
            for (int column = 1; column <= GameController.BOARD_DIMENSION; column++) {
                squares[row-1][column-1].addMouseListener();
            }
        }
    }

    /**
     * Actions to take when a click event has occurred on a Square of this playing board
     * @param row
     * @param column
     */
    public void clickOccured(int row, int column){
        removeSquareListeners();
        sendClick(row, column);
    }

    /**
     * Makes the squares of this playing board un-clickable
     */
    public void removeSquareListeners() {

        for (int row = 1; row <= GameController.BOARD_DIMENSION; row++) {
            for (int column = 1; column <= GameController.BOARD_DIMENSION; column++) {
                squares[row-1][column-1].removeMouseListener();
            }
        }
    }
}
