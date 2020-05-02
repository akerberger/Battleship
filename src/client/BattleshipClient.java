package client;

import gamecomponents.ShipPlacementOrientation;
import gui.gamewindow.GameWindow;
import server.BattleshipServer;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Represents a client playing the game. Even the player hosting the game is represented as a BattleshipClient object.
 *
 * Serves as the link between the GameWindow and the BattleshipServer as it passes along events
 * from the GameWindow to the BattleshipServer and receives messages from the BattleshipServer.
 * This communication happens through the ClientSender/ClientReceiver objects
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
     *
     * @param host The address to connect the BattleShipClient to
     * @param port The port to connect the BattleShipClient to
     * @throws IOException If an exception is thrown fron the setUpSocket-method
     */
    public BattleshipClient(InetAddress host, int port) throws IOException {

        Socket socket = setUpSocket(host, port);
        out = new ClientSender(socket);

        new ClientReceiver(this, socket).start();

    }

    // connects the BattleShipClient to it's Gamewindow object
    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    /**
     * Sets up the Socket connection to the specified host.
     * @param host The InetAddress to bind the socket to.
     * @param port The port number to bind the socket to.
     * @return The successfully setup socket.
     * @throws IOException If the socket setup fails.
     */
    private Socket setUpSocket(InetAddress host, int port) throws IOException {

        Socket socket = new Socket(host, port);
        //5 min
        socket.setSoTimeout(300 * 1000);

        return socket;
    }

    /**
     *
     * @param msg
     */
    void handleReceivedMessage(String msg) {
        String[] tokens = msg.split(" ");

        if (tokens[1].equals("setID")) {
            if (id != -1) {
                throw new IllegalArgumentException(" ID already set in BattleshipClient");
            }
            id = Integer.parseInt(tokens[2]);
        } else if (tokens[1].equals("opponentDisconnect")) {

            gameWindow.onOpponentDisconnect();

        } else {
            handleGameMessage(tokens);
        }

    }

    public void socketTimedOut() {
        out.reportSocketTimedOut(id);
        gameWindow.socketTimedOut();

    }

    private void handleGameMessage(String[] msgTokens) throws IllegalArgumentException {

        String gameState = msgTokens[0];

        String messageType = msgTokens[1];

        switch (messageType) {
            case "newShipPlacementTurn":
                gameWindow.addMouseListeners(true);
                
                break;
            case "placeShip":
                ShipPlacementOrientation orientation =
                        (msgTokens[5].equals("h") ? ShipPlacementOrientation.HORIZONTAL : ShipPlacementOrientation.VERTICAL);
                placeShipOnMyBoard(Integer.parseInt(msgTokens[2]),
                        Integer.parseInt(msgTokens[3]), Integer.parseInt(msgTokens[4]), orientation);
                break;
            case "okMove":
                int senderId = Integer.parseInt(msgTokens[2]);
                int row = Integer.parseInt(msgTokens[3]);
                int column = Integer.parseInt(msgTokens[4]);
                gameWindow.markShot(row, column, senderId == id, msgTokens[5].equals("hit"));
                gameWindow.setNewTurnInfo(senderId == id);
                break;
            case "sinkShip":
                int idOfClicker = Integer.parseInt(msgTokens[2]);
                int shipSize = Integer.parseInt(msgTokens[5]);
                int tokenIndexOffset = 6;
                for (int i = 0; i < shipSize; i++) {
                    gameWindow.markSunkenShipSquare(Integer.parseInt(msgTokens[tokenIndexOffset]), Integer.parseInt(msgTokens[tokenIndexOffset + 1]), idOfClicker == id);
                    tokenIndexOffset += 2;
                }
                gameWindow.setNewTurnInfo(idOfClicker == id);
                break;
            case "notOkMove":
                //  beroende på vilken spelfas det är, addera lyssnare till egna brädet eller motståndarens

                gameWindow.addMouseListeners(gameState.equals("SETUP_PHASE"));
                break;

            case "newTurn":
                gameWindow.addMouseListeners(false);
                break;
            case "changePhase":
                String newPhase = msgTokens[2];
                if (newPhase.equals("setupPhase")) {
                    gameWindow.setupPhase();

                } else if (newPhase.equals("gamePhase")) {
                    int starterPlayerId = Integer.parseInt(msgTokens[3]);
                    gameWindow.gamePhase(id == starterPlayerId);
                }
                break;
            case "setGameOver":

                int winningPlayerId = Integer.parseInt(msgTokens[2]);
                gameOver(winningPlayerId);
                break;

        }
    }

    private void gameOver(int winningPlayerId) {
        gameWindow.gameOver(winningPlayerId == id);
    }

    public void sendClick(int row, int column) {
        out.sendClick(id, row, column, shipPlacementHorizontal);
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
