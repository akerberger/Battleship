package server;

import client.BattleshipClient;
import gamecomponents.ShipPlacementOrientation;
import gamecomponents.Square;
import gui.gamewindow.GameWindow;

import java.util.*;

public class GameController {

    //borde vara privat och att avläsning sker genom statisk metod.
    private GameState gameState = GameState.CONNECTION_PHASE;

    public static final int BOARD_DIMENSION = 10;

//    private final Square[][] localClientBoard = new Square[BOARD_DIMENSION][BOARD_DIMENSION];
//    private final Square[][] remoteClientBoard = new Square[BOARD_DIMENSION][BOARD_DIMENSION];

    //id, List of ships represented as Square arrays
    private Map<Integer, List<Square[]>> playerShips = new HashMap<>();

    private List<int[]> score = new ArrayList<>();

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

        if (gameState != GameState.CONNECTION_PHASE) {
            throw new IllegalStateException("Game should be in GameState.CONNECTION_PHASE");
        }

        SERVER.broadcastMessage(gameState + " " + "changePhase" + " " + "setupPhase");
        gameState = GameState.SETUP_PHASE;
    }

    public void connectedPlayer(Integer clientId) {
        score.add(new int[]{clientId, 0});
        playerShips.put(clientId, new ArrayList<>());
    }

    private void handleClickInSetupPhase(int clientId, int clickedRow, int clickedColumn, ShipPlacementOrientation orientation) {

        if (clickedColumn == -1 || clickedRow == -1) {
            throw new IllegalArgumentException();
        }

        //giltigt
        if (clickedColumn <= BOARD_DIMENSION - 2) {
            //skicka här med typ av okMove, typ markShip, samt skeppstorlek (och i framtiden om vertikalt/horisontellt
            int shipSize = 3;

            if (!playerShips.containsKey(clientId)) {
                throw new IllegalStateException("Client with id " + clientId + " should have mapping");
            }

            List<Square[]> shipsOfPlayer = playerShips.get(clientId);

            Square[] ship = new Square[shipSize];
            shipsOfPlayer.add(ship);

            for (int i = 0; i < shipSize; i++) {
                if (orientation == ShipPlacementOrientation.HORIZONTAL) {
                    ship[i] = new Square(clickedRow, clickedColumn + i, null);
                } else {
                    ship[i] = new Square(clickedRow + i, clickedColumn, null);
                }
            }

            System.out.println("GAMECONTROLLER ADDERAT SKEPP: " + Arrays.toString(ship));

            SERVER.sendMessageToClient(clientId, gameState + " " + "placeShip" +
                    " " + clickedRow + " " + clickedColumn + " " + shipSize + " " +
                    (orientation == ShipPlacementOrientation.HORIZONTAL ? "h" : "v"));

            if (readyPlayerId == -1) {
                readyPlayerId = clientId;
            } else {
                //måste tråden pausas här ett tag? innan kommando för byte av fas skickas

                SERVER.broadcastMessage(gameState + " " + "changePhase" + " " + "gamePhase" + " " + readyPlayerId);
                gameState = GameState.GAME_PHASE;
            }


            System.out.print("SÅHÄR SER MAPEN UT I SETUP PHASE: ");
            for (Map.Entry<Integer, List<Square[]>> entry : playerShips.entrySet()) {
                for (Square[] s : entry.getValue()) {
                    System.out.print("ID: " + entry.getKey() + " " + Arrays.toString(s) + " ");
                }
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
            String shipPlacementOrientation = tokens[3];

            handleClickInSetupPhase(clientId, clickedRow, clickedColumn,
                    shipPlacementOrientation.equals("h") ?
                            ShipPlacementOrientation.HORIZONTAL : ShipPlacementOrientation.VERTICAL);
        } else if (gameState == GameState.GAME_PHASE) {
//            validateMove(clientId, clickedRow, clickedColumn);
            testMethod(clientId, clickedRow, clickedColumn);
        }
    }

    private void checkShip() {

    }

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

    private void gameOver(int winnerClientId){
        gameState=GameState.GAME_OVER;
        SERVER.broadcastMessage(gameState+ " "+"gameOver"+ " "+winnerClientId);
    }


    private void testMethod(int clientId, int clickedRow, int clickedColumn) {

        boolean hit = false;

        boolean gameOver = false;

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
                    if (hit) {
                        SERVER.broadcastMessage(gameState + " " + "okMove" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + "hit");

                    }
                    if (hitCount == ship.length) {
                        opponentShips.remove(ship);
                        broadcastSinkShip(ship, clientId, clickedRow, clickedColumn);

                        if(opponentShips.size()==0){
                            gameOver = true;
                        }
                    }
                }
                if (!hit) {
                    SERVER.broadcastMessage(gameState + " " + "okMove" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + "miss");
                }

                //break efter man gått in i första Map.Entry som inte tillhör clienten som har turen nu
                break;
            }
        }

        System.out.println("SKEPP-MAPPEN: ");
        for (Map.Entry<Integer, List<Square[]>> entry : playerShips.entrySet()) {
            System.out.print("ID: " + entry.getKey() + " SHIPS: ");
            for (Square[] ship : entry.getValue()) {
                System.out.print(Arrays.toString(ship) + " ");
            }
            System.out.println();
        }

        if(gameOver){
            gameOver(clientId);
        }else{
            initiateNextTurn(clientId);

        }


    }

    private void initiateNextTurn(int playerOfCurrentTurn){
        SERVER.initiateNewTurn(playerOfCurrentTurn, gameState + " " + "newTurn");
    }


    //Gör så att ett sjunket skepp inte kollas... ta bort från lista när sjunket!
    public void validateMove(int clientId, int clickedRow, int clickedColumn) {

        System.out.print("SÅHÄR SER MAPEN UT I VALIDATE MOVE: ");

        for (Map.Entry<Integer, List<Square[]>> entry : playerShips.entrySet()) {
            for (Square[] s : entry.getValue()) {
                System.out.print("ID: " + entry.getKey() + " " + Arrays.toString(s) + " ");
            }
        }

        List<Square[]> opponentShips;

        boolean hit = false;
        int sunkenShipSize = -1;
        Square[] sunkenShip;

        for (Map.Entry<Integer, List<Square[]>> entry : playerShips.entrySet()) {
            //if opponents entry
            if (entry.getKey() != clientId) {

                opponentShips = entry.getValue();


                for (Square[] s : opponentShips) {
                    System.out.print(Arrays.toString(s) + " ");
                }

                //loop through every ship of the opponent
                // or until a ship of the opponent is hit by current click
                for (int i = 0; i < opponentShips.size() && !hit; i++) {
                    Square[] ship = opponentShips.get(i);
                    System.out.println("SKEPP KOLLAS: " + Arrays.toString(ship));
                    int hitCount = 0;
                    for (Square shipSquare : ship) {
                        if (shipSquare.getRow() == clickedRow && shipSquare.getColumn() == clickedColumn) {
                            shipSquare.setIsShot();
                            hit = true;
                        }
                        if (shipSquare.isShot()) {
                            hitCount++;
                        }
                    }
                    if (hitCount == ship.length) {
                        sunkenShip = ship;
                        sunkenShipSize = ship.length;
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < sunkenShipSize; j++) {
                            Square s = sunkenShip[j];
                            sb.append(s.getRow());
                            sb.append(" ");
                            sb.append(s.getColumn());

                            if (j + 1 < sunkenShipSize) {
                                sb.append(" ");
                            }
                        }
                        System.out.print("SKA SÄNKA :");
                        System.out.println(sb.toString());
                        SERVER.broadcastMessage(gameState + " " + "sinkShip" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + sunkenShipSize + " " + sb.toString());
                    }
                }
                break;
            }
        }
        if (hit) {
            if (sunkenShipSize < 0) {
                SERVER.broadcastMessage(gameState + " " + "okMove" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + "hit");
            }

        } else {
            SERVER.broadcastMessage(gameState + " " + "okMove" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + "miss");
        }

        SERVER.initiateNewTurn(clientId, gameState + " " + "newTurn");


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
        GAME_PHASE,
        GAME_OVER;


    }

}
