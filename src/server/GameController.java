package server;

import gamecomponents.ShipPlacementOrientation;
import gamecomponents.Square;

import java.util.*;

public class GameController {

    private GameState gameState = GameState.CONNECTION_PHASE;

    public static final int BOARD_DIMENSION = 10;

    private final int SHIPS_PER_PLAYER = 3;

    private final int SHIP_SIZE = 3;

    //id, List of ships represented as Square arrays
    private Map<Integer, List<Square[]>> playerShips = new HashMap<>();

    private List<int[]> score = new ArrayList<>();

    //Id of player that set up his/her ships first. -1 when no player have placed a ship
    private int firstReadyPlayer = -1;

    private final BattleshipServer SERVER;

    public GameController(BattleshipServer server) {
        this.SERVER = server;
    }


    public void twoConnectedPlayers() {

        if (gameState != GameState.CONNECTION_PHASE) {
            throw new IllegalStateException("Game should be in GameState.CONNECTION_PHASE");
        }

        gameState = GameState.SETUP_PHASE;
        SERVER.broadcastMessage(gameState + " " + "changePhase" + " " + "setupPhase");

    }

    public void connectedPlayer(int clientId) {
        score.add(new int[]{clientId, 0});
        playerShips.put(clientId, new ArrayList<Square[]>());
    }

    public GameState getCurrentGameState() {
        return gameState;
    }

    //BoardController
    private boolean collides(Square square, int clickedRow, int clickedColumn, int shipSize, ShipPlacementOrientation orientation) {

        int checkingRow = clickedRow;
        int checkingColumn = clickedColumn;

        for (int i = 0; i < shipSize; i++) {
            if (square.getRow() == checkingRow && square.getColumn() == checkingColumn) {
                return true;
            }
            if (orientation == ShipPlacementOrientation.HORIZONTAL) {
                checkingColumn++;
            } else {
                checkingRow++;
            }
        }

        return false;

    }

    private boolean shipOutsideOfBoard(int clickedRow, int clickedColumn, int shipSize, ShipPlacementOrientation orientation) {
        int checkingRow = clickedRow;
        int checkingColumn = clickedColumn;

        //kontrollera mot spelbrädets storlek
        for (int i = 0; i < shipSize; i++) {
            if (checkingRow > BOARD_DIMENSION || checkingColumn > BOARD_DIMENSION) {
                return true;
            }
            if (orientation == ShipPlacementOrientation.HORIZONTAL) {
                checkingColumn++;
            } else {
                checkingRow++;
            }
        }
        return false;
    }

    private boolean shipCollision(List<Square[]> shipsOfPlayer, int clickedRow, int clickedColumn, int shipSize, ShipPlacementOrientation orientation) {

        for (Square[] ship : shipsOfPlayer) {
            for (Square square : ship) {
                if (collides(square, clickedRow, clickedColumn, shipSize, orientation)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean validShipPlacement(List<Square[]> shipsOfPlayer, int clickedRow, int clickedColumn, int shipSize, ShipPlacementOrientation orientation) {

        if (shipOutsideOfBoard(clickedRow, clickedColumn, shipSize, orientation) ||
                shipCollision(shipsOfPlayer, clickedRow, clickedColumn, shipSize, orientation)) {
            return false;
        }

        return true;
    }

    private void addShipToPlayerShips(List<Square[]> shipsOfPlayer, int clickedRow, int clickedColumn, int shipSize, ShipPlacementOrientation orientation) {
        Square[] ship = new Square[shipSize];
        shipsOfPlayer.add(ship);

        for (int i = 0; i < shipSize; i++) {
            if (orientation == ShipPlacementOrientation.HORIZONTAL) {
                ship[i] = new Square(clickedRow, clickedColumn + i, null);
            } else {
                ship[i] = new Square(clickedRow + i, clickedColumn, null);
            }
        }
    }


    //MessageHandler
    public void handleClickInSetupPhase(int clientId, int clickedRow, int clickedColumn, ShipPlacementOrientation orientation) {

        if (!playerShips.containsKey(clientId)) {
            throw new IllegalStateException("Client with id " + clientId + " should have mapping");
        }

        List<Square[]> shipsOfPlayer = playerShips.get(clientId);

        if (!validShipPlacement(shipsOfPlayer, clickedRow, clickedColumn, SHIP_SIZE, orientation)) {
            SERVER.sendMessageToClient(clientId, gameState + " " + "notOkMove" + " " + clickedRow + " " + clickedColumn);
        } else {

            addShipToPlayerShips(shipsOfPlayer, clickedRow, clickedColumn, SHIP_SIZE, orientation);

            SERVER.sendMessageToClient(clientId, gameState + " " + "placeShip" +
                    " " + clickedRow + " " + clickedColumn + " " + SHIP_SIZE + " " +
                    (orientation == ShipPlacementOrientation.HORIZONTAL ? "h" : "v"));

            if (firstReadyPlayer == -1) {
                firstReadyPlayer = clientId;
            }

            //om spelaren inte har placerat alla sina skepp - ta ny placeringsvända

            if (shipsOfPlayer.size() < SHIPS_PER_PLAYER) {
                //notOkMove bara för att spelaren ska få ny placeringsvända i setupphase. Ändra detta!!!
                SERVER.sendMessageToClient(clientId, gameState + " " + "newShipPlacementTurn");
            } else if (allPlayersReady()) {
                //måste tråden pausas här ett tag? innan kommando för byte av fas skickas

                SERVER.broadcastMessage(gameState + " " + "changePhase" + " " + "gamePhase" + " " + firstReadyPlayer);
                gameState = GameState.GAME_PHASE;
            }


        }

    }

    private boolean allPlayersReady() {
        for (Map.Entry<Integer, List<Square[]>> entry : playerShips.entrySet()) {
            //If a player hasn't placed all his/hers ships yet
            if (entry.getValue().size() < SHIPS_PER_PLAYER) {
                return false;
            }
        }
        return true;
    }

    //MessageHandler
    private void broadcastSinkShip(Square[] sunkenShip, int clientId, int clickedRow, int clickedColumn) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sunkenShip.length; i++) {
            Square s = sunkenShip[i];
            sb.append(s.getRow());
            sb.append(" ");
            sb.append(s.getColumn());
            if (i + 1 < sunkenShip.length) {
                sb.append(" ");
            }
        }
        SERVER.broadcastMessage(gameState + " " + "sinkShip" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + sunkenShip.length + " " + sb.toString());
    }

    private void setGameOver(int winnerClientId) {
        gameState = GameState.GAME_OVER;
        SERVER.broadcastMessage(gameState + " " + "setGameOver" + " " + winnerClientId);
    }


    //0 - miss, 1 - hitNotSunken, 2 - hitSunken, kanske returnera ett objekt, typ ShotResultInfo med info om sjunket skepp
    private int handleShot(int clientId, int clickedRow, int clickedColumn) {

        boolean hit = false;
        boolean sunken = false;

        for (Map.Entry<Integer, List<Square[]>> entry : playerShips.entrySet()) {
            //if opponents MapEntry
            if (entry.getKey() != clientId) {
                System.out.println("GÅR IN HÄR");
                List<Square[]> opponentShips = entry.getValue();
                //loops through all the opponents ships that are not sunken
                for (int i = 0; i < opponentShips.size() && !hit; i++) {
                    Square[] ship = opponentShips.get(i);
                    int hitCount = 0;
                    for (Square square : ship) {
                        if (square.isShot()) {
                            hitCount++;
                        }
                        if (square.getRow() == clickedRow && square.getColumn() == clickedColumn) {
                            square.setIsShot();
                            hit = true;
                            hitCount++;
                        }
                    }
                    if (hitCount == ship.length) {
                        sunken = true;
                        opponentShips.remove(ship);
                        broadcastSinkShip(ship, clientId, clickedRow, clickedColumn);
                    }
                }
                //break efter man gått in i första Map.Entry som inte tillhör clienten som har turen nu
                break;
            }
        }

        if (sunken) {
            return 2;
        } else if (hit) {
            return 1;
        }
        return 0;
    }


    //MessageHandler
    public void handleClickInGamePhase(int clientId, int clickedRow, int clickedColumn) {

        int resultOfShot = handleShot(clientId, clickedRow, clickedColumn);

        if (resultOfShot == 1) {
            SERVER.broadcastMessage(gameState + " " + "okMove" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + "hit");

        } else if (resultOfShot == 0) {
            SERVER.broadcastMessage(gameState + " " + "okMove" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + "miss");
        }

        if (isGameOver(clientId)) {
            setGameOver(clientId);
        } else {
            initiateNextTurn(clientId);
        }


    }


    private boolean isGameOver(int currentPlayer) {

        for (Map.Entry<Integer, List<Square[]>> entry : playerShips.entrySet()) {
            if (entry.getKey() != currentPlayer) {
                if (entry.getValue().size() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initiateNextTurn(int playerOfCurrentTurn) {
        SERVER.initiateNewTurn(playerOfCurrentTurn, gameState + " " + "newTurn");
    }

//    private enum GameState {
//        CONNECTION_PHASE,
//        SETUP_PHASE,
//        GAME_PHASE,
//        GAME_OVER
//    }

}
