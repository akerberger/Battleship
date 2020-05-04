package client;

import gamecomponents.ShipPlacementOrientation;
import gui.gamewindow.GameWindow;
import server.BattleshipServer;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Represents a user playing the game.
 * <p>
 * Serves as the link between the GameWindow and the BattleshipServer as it passes along events
 * from the GameWindow to the BattleshipServer and receives messages from the BattleshipServer.
 * This communication happens through the ClientSender/ClientReceiver objects, over a Socket that is set up
 * in the constructor of this class.
 */
public class BattleshipClient {

    private ClientSender out;

    private GameWindow gameWindow;

    private GameMessageHandler gameMessageHandler = new GameMessageHandler();

    /*
      Indicates which direction (horizontal or vertical) a ship will be placed on the own board
      during the pre-game setup phase
     */
    private boolean shipPlacementHorizontal = true;

    /*
      Will be set to a valid (positive) value through a message from the BattleshipServer
      if the BattleshipClient's connection to the BattleshipServer is successful
     */
    private int id = -1;

    /**
     * Makes a call to the setUpSocket-method and passes along the resulting Socket object
     * to a ClientSender object and a ClientRecevier object.
     *
     * @param host The address to connect the BattleShipClient to
     * @param port The port to connect the BattleShipClient to
     * @throws IOException If an exception is thrown from the setUpSocket-method
     */
    public BattleshipClient(InetAddress host, int port) throws IOException {

        Socket socket = setUpSocket(host, port);
        out = new ClientSender(socket);

        new ClientReceiver(this, socket).start();

    }

    /**
     * Helper class that handles different messages sent from the BattleshipServer during the game phase of the game
     */
    private class GameMessageHandler{

        /**
         * Handles messages regarding the game session from the BattleshipServer. A message is represented
         * in the form of a msgTokens array.
         * <p>
         * The first two tokens always represents the game state (msgTokens[1]) and the message type (msgTokens[2]).
         * The type of information of the remaining tokens varies depending on the message type but usually contains information
         * about a click that a user has made.
         *
         * @param msgTokens The message token array
         */
        void handleGameMessage(String[] msgTokens) {

            String gameState = msgTokens[0];

            String msgType = msgTokens[1];

            switch (msgType) {
                case "newShipPlacementTurn":
                    actionsForNewShipPlacementTurn();
                    break;
                case "placeShip":
                    actionsForPlaceShip(msgTokens);
                    break;
                case "okMove":
                    actionsForOkMove(msgTokens);
                    break;
                case "sinkShip":
                    actionsForSinkShip(msgTokens);
                    break;
                case "notOkMove":
                    actionsForNotOkMove(gameState);
                    break;
                case "newTurn":
                    actionsForNewTurn();
                    break;
                case "changePhase":
                    actionsForChangePhase(msgTokens);
                    break;
                case "setGameOver":
                    actionsForGameOver(msgTokens);
                    break;

            }
        }

        void actionsForGameOver(String [] msgTokens){
            int winningPlayerId = Integer.parseInt(msgTokens[2]);
            gameWindow.gameOver(winningPlayerId == id);
        }

        /**
         * Actions for a reported change of game phase, e.g. from the initial connection phase to the setup phase
         * @param msgTokens The message token array
         */
        void actionsForChangePhase(String [] msgTokens){
            String newPhase = msgTokens[2];

            if (newPhase.equals("setupPhase")) {
                gameWindow.setupPhase();

            } else if (newPhase.equals("gamePhase")) {
                int starterPlayerId = Integer.parseInt(msgTokens[3]); //id of the player who will start the game
                gameWindow.gamePhase(id == starterPlayerId);
            }
        }

        /**
         * Actions to take when a new turn for this BattleshipClient is starting
         */
        void actionsForNewTurn() {
            gameWindow.addMouseListeners(false);
        }

        /**
         * Actions to take when this BattleshipClient is allowed to place another ship on the own board
         */
        void actionsForNewShipPlacementTurn() {
            gameWindow.addMouseListeners(true);
        }

        /**
         * Indicates that a click from this BattleshipClient has been declared as not valid by the BattleshipServer.
         *
         * This BattleshipClient is given a new chance to click on the own board (placing a ship)
         * or the opponents board (making a shot) depending on the current game state.
         * @param gameState The current game state
         */
        void actionsForNotOkMove(String gameState) {
            gameWindow.addMouseListeners(gameState.equals("SETUP_PHASE"));
        }

        /**
         * Indicates that a click for placing a ship on the own board has been validated by the BattleshipServer.
         *
         * The msgToken array contains information about where the ship is to be placed on the board
         * and the ships orientation (horizontal/vertical).
         * @param msgTokens The message token array
         */
        void actionsForPlaceShip(String[] msgTokens) {
            ShipPlacementOrientation orientation =
                    (msgTokens[5].equals("h") ? ShipPlacementOrientation.HORIZONTAL : ShipPlacementOrientation.VERTICAL);

            placeShipOnMyBoard(Integer.parseInt(msgTokens[2]),
                    Integer.parseInt(msgTokens[3]), Integer.parseInt(msgTokens[4]), orientation);
        }

        /**
         * Indicates that a click has occurred which has hit and sunk a ship.
         *
         * The msgTokens array contains all the Square coordinates for the sunken ship.
         * The method will make one call to the gameWindow for each Square to be marked as containing part of a sunken ship.
         *
         * Which board that this method will mark the sunken ship on depends on if this BattleshipClient
         * made the click or not (i.e if the idOfClicker == id of this BattleshipClient).
         * @param msgTokens The message token array
         */
        void actionsForSinkShip(String[] msgTokens) {
            int idOfClicker = Integer.parseInt(msgTokens[2]);
            int shipSize = Integer.parseInt(msgTokens[5]);
            boolean markOnOpponentsBoard = (idOfClicker == id);

            int tokenIndexOffset = 6; //indicates on what index the square coordinates begin in the msgToken array

            for (int i = 0; i < shipSize; i++) {
                int row = Integer.parseInt(msgTokens[tokenIndexOffset]);
                int column = Integer.parseInt(msgTokens[tokenIndexOffset + 1]);

                gameWindow.markSunkenShipSquare(row,column, markOnOpponentsBoard);
                tokenIndexOffset += 2; //change the tokenIndexOffset to point on the next Square coordinate in the msgTokenArray
            }

            gameWindow.setNewTurnInfo(idOfClicker == id); //orders the gameWindow to display information about new turn
        }

        /**
         * Indicates that a click from a BattleshipClient has occurred and has been validated
         * at the BattleshipServer
         * @param msgTokens Contains information about the validated click:
         *                  [2] - The id of the BattleshipClient that performed the click
         *                  [3] - The row of the clicked square
         *                  [4] - The column of the clicked square
         *                  [5] - If the click was a hit or a miss
         */

        void actionsForOkMove(String[] msgTokens) {
            int senderId = Integer.parseInt(msgTokens[2]);
            int row = Integer.parseInt(msgTokens[3]);
            int column = Integer.parseInt(msgTokens[4]);

            gameWindow.markShot(row, column, senderId == id, msgTokens[5].equals("hit"));
            gameWindow.setNewTurnInfo(senderId == id);
        }


    }

    // connects the BattleShipClient to it's Gamewindow object
    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    /**
     * Sets up the Socket connection which is used for communication with the BattleshipServer
     *
     * @param host The InetAddress to bind the socket to.
     * @param port The port number to bind the socket to.
     * @return The successfully set up socket.
     * @throws IOException If the socket setup fails.
     */
    private Socket setUpSocket(InetAddress host, int port) throws IOException {

        Socket socket = new Socket(host, port);
        //5 min
        socket.setSoTimeout(300 * 1000);

        return socket;
    }

    /**
     * If a time out has occurred for the Socket of this BattleshipClient.
     * <p>
     * Reports this to the BattleshipServer through the ClientSender
     * and notifies the gameWindow of this BattleshipClient.
     */
    public void socketTimedOut() {
        out.reportSocketTimedOut(id);
        gameWindow.socketTimedOut();

    }

    /**
     * Handles a message with information about a game event
     * @param msgTokens The message token array
     */
    void handleGameMessage(String[] msgTokens){
        gameMessageHandler.handleGameMessage(msgTokens);
    }

    /**
     * Indicates that an opponent has disconnected.
     */
    void onOpponentDisconnect() {
        gameWindow.onOpponentDisconnect();
    }

    /**
     * Passes on information about a click that has occurred to the ClientSender.
     * @param row The row of the Square where the click occurred
     * @param column The column of the Square where the click occurred
     */
    public void sendClick(int row, int column) {
        out.sendClick(id, row, column, shipPlacementHorizontal);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Indicates that a request for changing the ship placement direction (during the setup phase of the game) has occurred.
     */
    public void switchShipPlacementDirection() {
        shipPlacementHorizontal = !shipPlacementHorizontal;
        gameWindow.updateShipPlacementDirection(shipPlacementHorizontal);
    }

    /**
     * Indicates that a ship placement request has been validated in the BattleshipServer
     * @param startRow The row of the Square where the ship shall be placed
     * @param startColumn The column of the Square where the ship shall be placed
     * @param noOfSquares The ship size
     * @param orientation The orientation of the ship to be placed
     */

    private void placeShipOnMyBoard(int startRow, int startColumn, int noOfSquares, ShipPlacementOrientation orientation) {

        gameWindow.placeShipOnBoard(startRow, startColumn, noOfSquares, orientation);

    }


}
