package client;

import gamecomponents.ShipPlacementOrientation;
import gui.gamewindow.GameWindow;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;



public class BattleshipClient {

//    private final String DEFAULT_HOST = "127.0.0.1";
    //	private final String DEFAULT_HOST = "212.247.27.18";
//    private final int DEFAULT_PORT = 2000;

    private String HOST;

    private int PORT;

    private ClientSender out;

    private GameWindow gameWindow;

    private boolean shipPlacementHorizontal = true;

    private int id = -1;

    public BattleshipClient(String hostName, int port) throws IOException {

//        HOST = (args.length > 0 ? args[0] : DEFAULT_HOST);
//        HOST = DEFAULT_HOST;

        HOST = hostName;
        PORT = port;

//            PORT =  (args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT);

            Socket socket = setUpSocket();

            out = new ClientSender(socket);

            ClientReceiver receiver = new ClientReceiver(this, socket);
            receiver.start();



    }

    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    private Socket setUpSocket() throws IOException {

        Socket socket = new Socket(HOST, PORT);
        //5 min
        socket.setSoTimeout(300 * 1000);

        return socket;
    }

    void handleReceivedMessage(String msg) {
        String[] tokens = msg.split(" ");

        if (tokens[1].equals("setID")) {
            if (id != -1) {
                throw new IllegalArgumentException(" ID already set in BattleshipClient");
            }
            id = Integer.parseInt(tokens[2]);
        }else if (tokens[1].equals("opponentDisconnect")){

            gameWindow.onOpponentDisconnect();

        } else {
            handleGameMessage(tokens);
        }

    }

    public void socketTimedOut(){
        out.reportSocketTimedOut(id);
        gameWindow.socketTimedOut();

    }

    private void handleGameMessage(String[] msgTokens) throws IllegalArgumentException {

        System.out.println("BATTLESHIPCLIENT MED ID: " + id + " FÅR MEDDELANDE: " + Arrays.toString(msgTokens));

        String gameState = msgTokens[0];

        String messageType = msgTokens[1];

        switch (messageType) {
            case "newShipPlacementTurn":
                gameWindow.addMouseListeners(true);
                System.out.println("TAR NY PLACERINGVÄNDA");

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
//                    GameController.setGameState(GameState.SETUP_PHASE);
                    gameWindow.setupPhase();

                } else if (newPhase.equals("gamePhase")) {
                    int starterPlayerId = Integer.parseInt(msgTokens[3]);
//                    GameController.setGameState(GameState.GAME_PHASE);
                    gameWindow.gamePhase(id == starterPlayerId);
                }
                break;
            case "setGameOver":

                int winningPlayerId = Integer.parseInt(msgTokens[2]);
                gameOver(winningPlayerId);
                break;

        }
    }

    private void gameOver(int winningPlayerId){
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
