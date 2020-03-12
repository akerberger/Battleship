package client;

import gui.GameWindow;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;


//Byt namn till Player, mer rimligt...
public class BattleshipClient {

//    private final String DEFAULT_HOST = "127.0.0.1";
    //	private final String DEFAULT_HOST = "212.247.27.18";
//    private final int DEFAULT_PORT = 2000;

    private String HOST;

    private int PORT;

    private PrintWriter out;



    private GameWindow gameWindow;

    private int id = -1;

    public BattleshipClient(String hostName, int port) {

//        HOST = (args.length > 0 ? args[0] : DEFAULT_HOST);
//        HOST = DEFAULT_HOST;


        HOST = hostName;
        PORT = port;
        try {
//            PORT =  (args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT);

            Socket socket = setUpSocket();

            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            ClientReceiver receiver = new ClientReceiver(this,socket);
            receiver.start();


        } catch (NumberFormatException e) {
            //Fel i parsning av args
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
            // fel i setUpSocket()
        }

    }

    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    //Behöver vara trådad, annars låser programmet på listen-metoden


    private Socket setUpSocket() throws IOException {

        Socket socket = new Socket(HOST, PORT);
        //5 min
        socket.setSoTimeout(300 * 1000);

        return socket;
    }

    void handleReceivedMessage(String msg){
        String [] tokens = msg.split(" ");

        if(tokens[1].equals("setID")){
            if (id != -1) {
                throw new IllegalArgumentException(" ID already set in BattleshipClient");
            }
            id = Integer.parseInt(tokens[2]);
        }else{
            handleGameMessage(tokens);
        }

    }

    void handleGameMessage(String [] msgTokens) throws IllegalArgumentException {

        System.out.println("BATTLESHIPCLIENT MED ID: " + id + " FÅR MEDDELANDE: " + Arrays.toString(msgTokens));

        String gameState = msgTokens[0];

        String messageType = msgTokens[1];

        switch (messageType) {
            case "placeShip":
                markSquaresOnMyBoard(gameState,Integer.parseInt(msgTokens[2]), Integer.parseInt(msgTokens[3]), Integer.parseInt(msgTokens[4]));
                break;
            case "okMove":
                int senderId = Integer.parseInt(msgTokens[2]);
                int row = Integer.parseInt(msgTokens[3]);
                int column = Integer.parseInt(msgTokens[4]);
                gameWindow.markShot(row, column, senderId == id, msgTokens[5].equals("hit"));
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

        }

//        if(tokens.length > 1){
//        if (messageType.equals("setID")) {
//            if (id != -1) {
//                throw new IllegalArgumentException(" ID already set in BattleshipClient");
//            } else {
//                id = Integer.parseInt(tokens[1]);
//            }
//        } else if (messageType.equals("placeShip")) {
//            markSquaresOnMyBoard(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
//        } else if (messageType.equals("okMove")) {
//
//            int senderId = Integer.parseInt(tokens[1]);
//            int row = Integer.parseInt(tokens[2]);
//            int column = Integer.parseInt(tokens[3]);
//            gameWindow.markShot(row, column, senderId == id, tokens[4].equals("hit"));
//
//
//        }else if(msg.equals("newTurn")){
//            gameWindow.addMouseListeners(false);
//        }
//        else if (messageType.equals("notOkMove")) {
//            //beroende på vilken spelfas det är, addera lyssnare till egna brädet eller motståndarens
//            boolean toOwnBoard;
//            if (GameController.gameState == GameState.SETUP_PHASE) {
//                toOwnBoard = true;
//            } else {
//                toOwnBoard = false;
//            }
//            gameWindow.addMouseListeners(toOwnBoard);
//
//        } else if (messageType.equals("changePhase")) {
//            String newPhase = tokens[1];
//            if (newPhase.equals("setupPhase")) {
//                GameController.setGameState(GameState.SETUP_PHASE);
//                gameWindow.setupPhase();
//            } else if (newPhase.equals("gamePhase")) {
//
//                int starterPlayerId = Integer.parseInt(tokens[2]);
//                GameController.setGameState(GameState.GAME_PHASE);
//
//                gameWindow.gamePhase(id == starterPlayerId);
//
//            }
//
//        }

    }

    public void sendClick(int row, int column, String whichBoard) {

        out.println(id + " " + row + " " + column);


    }

    //borde heta markSquaresOnBoard och använda boolean om vilken board det blir...
    private void markSquaresOnMyBoard(String gamePhase, int startRow, int startColumn, int noOfSquares) {
        if (gamePhase.equals("SETUP_PHASE")) {

            System.out.println("MARKERA SKEPP I SETUPPHASE!");
            //just nu är det första och enda skeppet som ska placeras 3 rutor stort.
            // Om detta ska funka generellt måste kontroller ske på andra ställen

            gameWindow.placeShipOnMyBoard(startRow, startColumn, noOfSquares, false);
        } else if (gamePhase.equals("GAME_PHASE")) {

            gameWindow.placeShipOnMyBoard(startRow, startColumn, 1, true);
        }

    }


}
