package server;

import client.BattleshipClient;
import game.Square;
import gui.GameWindow;

import java.util.ArrayList;
import java.util.List;

public class GameController {

    //borde vara privat och att avläsning sker genom statisk metod.
    private GameState gameState = GameState.CONNECTION_PHASE;

    public static final int BOARD_DIMENSION = 10;

    private final Square[][] localClientBoard = new Square[BOARD_DIMENSION][BOARD_DIMENSION];
    private final Square[][] remoteClientBoard = new Square[BOARD_DIMENSION][BOARD_DIMENSION];

    List<BattleshipClient> connectedPlayers = new ArrayList<>();

    //Id of player that has set up his/her ships. -1 if no player are ready
    private int readyPlayerId = -1;

    private final BattleshipServer SERVER;

    public GameController(BattleshipServer server) {
        this.SERVER = server;
    }


    public GameState getGameState() {
        return gameState;
    }

    public void twoConnectedPlayers() {

        //onödig kontroll??
        if (gameState == GameState.CONNECTION_PHASE) {

            SERVER.broadcastMessage(gameState + " " + "changePhase" + " " + "setupPhase");
            gameState = GameState.SETUP_PHASE;

        }
    }

    private void handleClickInSetupPhase(int clientId, int clickedRow, int clickedColumn) {

        if (clickedColumn == -1 || clickedRow == -1) {
            throw new IllegalArgumentException();
        }

        //giltigt
        if (clickedColumn <= BOARD_DIMENSION - 2) {
            //skicka här med typ av okMove, typ markShip, samt skeppstorlek (och i framtiden om vertikalt/horisontellt
            int shipSize = 3;
            SERVER.sendMessageToClient(clientId, gameState + " " + "placeShip" + " " + clickedRow + " " + clickedColumn + " " + shipSize);

            if (readyPlayerId == -1) {
                readyPlayerId = clientId;
            } else {
                //måste tråden pausas här ett tag? innan kommando för byte av fas skickas

                SERVER.broadcastMessage(gameState + " " + "changePhase" + " " + "gamePhase" + " " + readyPlayerId);
                gameState = GameState.GAME_PHASE;
            }

        } else {
            //om inte giltigt drag -> lägg till muslyssnare
            SERVER.sendMessageToClient(clientId, gameState + " " + "notOkMove" + " " + clickedRow + " " + clickedColumn);
        }
    }

    public void handleClientClicked(String msg) {

        String[] tokens = msg.split(" ");

        int clientId = Integer.parseInt(tokens[0]);
        int clickedRow = Integer.parseInt(tokens[1]);
        int clickedColumn = Integer.parseInt(tokens[2]);

        if (gameState == GameState.SETUP_PHASE) {
            handleClickInSetupPhase(clientId, clickedRow, clickedColumn);
        } else if (gameState == GameState.GAME_PHASE) {
            validateMove(clientId, clickedRow, clickedColumn);
        }


    }

    public void validateMove(int clientId, int clickedRow, int clickedColumn) {


        SERVER.broadcastMessage(gameState + " " + "okMove" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + "miss");
        SERVER.initiateNewTurn(clientId, gameState + " " + "newTurn");

        //kontrollera spelets status. om inte game over - byt tur



    }

    private int getSquareNumberFromCoordinate(int coordinate) {

        if (coordinate > 0 && coordinate <= GameWindow.PLAYING_BOARD_SIZE / 10) {
            return 1;
        } else if (coordinate > GameWindow.PLAYING_BOARD_SIZE / 10 && coordinate <= GameWindow.PLAYING_BOARD_SIZE / 10 * 2) {
            return 2;
        } else if (coordinate > GameWindow.PLAYING_BOARD_SIZE / 10 * 2 && coordinate <= GameWindow.PLAYING_BOARD_SIZE / 10 * 3) {
            return 3;
        } else if (coordinate > GameWindow.PLAYING_BOARD_SIZE / 10 * 3 && coordinate <= GameWindow.PLAYING_BOARD_SIZE / 10 * 4) {
            return 4;
        } else if (coordinate > GameWindow.PLAYING_BOARD_SIZE / 10 * 4 && coordinate <= GameWindow.PLAYING_BOARD_SIZE / 10 * 5) {
            return 5;
        } else if (coordinate > GameWindow.PLAYING_BOARD_SIZE / 10 * 5 && coordinate <= GameWindow.PLAYING_BOARD_SIZE / 10 * 6) {
            return 6;
        } else if (coordinate > GameWindow.PLAYING_BOARD_SIZE / 10 * 6 && coordinate <= GameWindow.PLAYING_BOARD_SIZE / 10 * 7) {
            return 7;
        } else if (coordinate > GameWindow.PLAYING_BOARD_SIZE / 10 * 7 && coordinate <= GameWindow.PLAYING_BOARD_SIZE / 10 * 8) {
            return 8;
        } else if (coordinate > GameWindow.PLAYING_BOARD_SIZE / 10 * 8 && coordinate <= GameWindow.PLAYING_BOARD_SIZE / 10 * 9) {
            return 9;
        } else if (coordinate > GameWindow.PLAYING_BOARD_SIZE / 10 * 9 && coordinate <= GameWindow.PLAYING_BOARD_SIZE / 10 * 10) {
            return 10;
        }

        return -1;
    }

    private enum GameState {
        CONNECTION_PHASE,
        SETUP_PHASE,
        GAME_PHASE;


    }

}
