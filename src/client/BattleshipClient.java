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

    private class GameMessageHandler{

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

    void onOpponentDisconnect() {
        gameWindow.onOpponentDisconnect();
    }

    /**
     * Handles messages regarding the game session from the BattleshipServer in the form of msgTokens.
     * <p>
     * The first two tokens always represents the game state (msgTokens[1]) and the message type (msgTokens[2]).
     * The tokens following the first two varies depending on the message type but usually contains information
     * about a click that a user has made.
     *
     * @param msgTokens . The current game state
     * @throws IllegalArgumentException
     */
    void handleGameMessage(String[] msgTokens) throws IllegalArgumentException {

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

    private void actionsForGameOver(String [] msgTokens){
        int winningPlayerId = Integer.parseInt(msgTokens[2]);
        gameOver(winningPlayerId);
    }

    private void actionsForChangePhase(String [] msgTokens){
        String newPhase = msgTokens[2];
        if (newPhase.equals("setupPhase")) {
            gameWindow.setupPhase();

        } else if (newPhase.equals("gamePhase")) {
            int starterPlayerId = Integer.parseInt(msgTokens[3]);
            gameWindow.gamePhase(id == starterPlayerId);
        }
    }

    private void actionsForNewTurn() {
        gameWindow.addMouseListeners(false);
    }

    private void actionsForNewShipPlacementTurn() {
        gameWindow.addMouseListeners(true);
    }

    private void actionsForNotOkMove(String gameState) {
        gameWindow.addMouseListeners(gameState.equals("SETUP_PHASE"));
    }

    private void actionsForPlaceShip(String[] msgTokens) {
        ShipPlacementOrientation orientation =
                (msgTokens[5].equals("h") ? ShipPlacementOrientation.HORIZONTAL : ShipPlacementOrientation.VERTICAL);

        placeShipOnMyBoard(Integer.parseInt(msgTokens[2]),
                Integer.parseInt(msgTokens[3]), Integer.parseInt(msgTokens[4]), orientation);
    }

    private void actionsForSinkShip(String[] msgTokens) {
        int idOfClicker = Integer.parseInt(msgTokens[2]);
        int shipSize = Integer.parseInt(msgTokens[5]);
        int tokenIndexOffset = 6;

        for (int i = 0; i < shipSize; i++) {
            gameWindow.markSunkenShipSquare(Integer.parseInt(msgTokens[tokenIndexOffset]),
                    Integer.parseInt(msgTokens[tokenIndexOffset + 1]),
                    idOfClicker == id);
            tokenIndexOffset += 2;
        }

        gameWindow.setNewTurnInfo(idOfClicker == id);
    }

    private void actionsForOkMove(String[] msgTokens) {
        int senderId = Integer.parseInt(msgTokens[2]);
        int row = Integer.parseInt(msgTokens[3]);
        int column = Integer.parseInt(msgTokens[4]);

        gameWindow.markShot(row, column, senderId == id, msgTokens[5].equals("hit"));
        gameWindow.setNewTurnInfo(senderId == id);
    }

    private void gameOver(int winningPlayerId) {
        gameWindow.gameOver(winningPlayerId == id);
    }

    public void sendClick(int row, int column) {
        out.sendClick(id, row, column, shipPlacementHorizontal);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void switchShipPlacementDirection() {
        shipPlacementHorizontal = !shipPlacementHorizontal;
        gameWindow.updateShipPlacementDirection(shipPlacementHorizontal);
    }

    private void placeShipOnMyBoard(int startRow, int startColumn, int noOfSquares, ShipPlacementOrientation orientation) {


        System.out.println("MARKERA SKEPP I SETUPPHASE!");
        //just nu är det första och enda skeppet som ska placeras 3 rutor stort.
        // Om detta ska funka generellt måste kontroller ske på andra ställen

        gameWindow.placeShipOnBoard(startRow, startColumn, noOfSquares, orientation);


    }


}
