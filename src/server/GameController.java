package server;

import gamecomponents.ShipPlacementOrientation;
import gamecomponents.Square;

import java.util.*;

/**
 * Receives events from the BattleshipClients, such as a shot during game play, via the BattleshipServer. Validates the
 * events and sends result of the validation back to the clients via the BattleshipServer,
 * informing the clients of any new game state caused by the event.
 * <p>
 * Holds the playerShips map that represents the players ships and checks this for each shot a player makes
 * in order to see if the shot results in a hit (or sunken) ship.
 * <p>
 * Holds information about the current game state and is responsible for changing the game state
 * when necessary. Informs the clients of when such a change occurs.
 */

public class GameController {

    private GameState currentGameState = GameState.CONNECTION_PHASE;

    public static final int BOARD_DIMENSION = 10; //dimension that the playing boards should have

    private final int SHIPS_PER_PLAYER = 3;

    private int shipSize = 3; //the size of the ships to be placed by the BattleshipClients during setup phase.
    //(defaults to 3 and does not change as of this version of the game)

    /**
     * The ships of each connected player. Key - the id of the player. Value - the ships of that player
     * <p>
     * Gets updated if e.g. a players ship is hit or sunk during a game.
     */
    private Map<Integer, List<Square[]>> playerShips = new HashMap<>();

    /**
     * Id of the BattleshipClient that finished placing his/her ships first.
     * -1 when no player have finished placing their ships
     */
    private int firstReadyPlayer = -1;

    private final BattleshipServer THE_SERVER;

    public GameController(BattleshipServer server) {
        this.THE_SERVER = server;
    }

    /**
     * Actions to take for initiating a game after the second BattleshipClient has
     * connected to the BattleshipServer.
     */
    public void twoConnectedPlayers() {

        if (currentGameState != GameState.CONNECTION_PHASE) {
            throw new IllegalStateException("Game should be in GameState.CONNECTION_PHASE");
        }

        currentGameState = GameState.SETUP_PHASE;
        THE_SERVER.broadcastMessage(currentGameState + " " + "changePhase" + " " + "setupPhase");
    }

    /**
     * Actions to take when a BattleshipClient has connected to the BattleshipServer
     *
     * @param clientId The id of the client that has connected
     */
    public void connectedPlayer(int clientId) {
        playerShips.put(clientId, new ArrayList<Square[]>());
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }


    /**
     * Checks if a click for placing a ship on the board would result in that ship
     * being placed outside of the board (i.e an illegal placement)
     *
     * @param clickedRow    The row of the Square where the click occurred
     * @param clickedColumn The column of the Square where the click occurred
     * @param shipSize      The number of Squares of the ship to be placed
     * @param orientation   The orientation of the ship to be placed (horizontal/vertical)
     * @return False if the click would not result in the ship being outside of the board (i.e. valid placement)
     * True if the click would result in the ship being placed outside of the board (i.e. invalid placement)
     */
    private boolean shipOutsideOfBoard(int clickedRow, int clickedColumn, int shipSize, ShipPlacementOrientation orientation) {
        int checkingRow = clickedRow;
        int checkingColumn = clickedColumn;

        for (int i = 0; i < shipSize; i++) {
            if (checkingRow > BOARD_DIMENSION || checkingColumn > BOARD_DIMENSION) {
                return true;
            }
            //
            if (orientation == ShipPlacementOrientation.HORIZONTAL) {
                checkingColumn++;
            } else {
                checkingRow++;
            }
        }
        return false;
    }

    /**
     * Checks if a ship placement that a player is trying to do would result in that ship overlapping with one
     * of that players existing ships on his/hers playing board.
     *
     * @param shipsOfPlayer The existing ships on the playing board of the player trying to make the ship placement
     * @param clickedRow    The row of the Square where the click occurred and the new ship would start
     * @param clickedColumn The column of the Square where the click occurred and the new ship would start
     * @param shipSize      The number of Squares of the ship to be placed
     * @param orientation   The orientation of the ship to be placed (horizontal/vertical)
     * @return True if the new ship placement would result in an overlap with an existing ship
     */
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

    /**
     * Checks if a ship would collide with a specific Square
     *
     * @param square      The Square which is being checked
     * @param startRow    The row of the Square where ship starts
     * @param startColumn The column of the Square where ship starts
     * @param shipSize    The number of Squares of the ship
     * @param orientation The orientation of the ship (horizontal/vertical)
     * @return True if the ship collides with the square
     */
    private boolean collides(Square square, int startRow, int startColumn, int shipSize, ShipPlacementOrientation orientation) {

        int checkingRow = startRow;
        int checkingColumn = startColumn;

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

    /**
     * Checks if a ship placement that a player is trying to do would result in that ship exceeding the board edges (illegal move)
     * or it it would overlap with one of that players existing ships on his/hers playing board (illegal move).
     *
     * @param shipsOfPlayer The existing ships on the playing board of the player trying to make the ship placement
     * @param clickedRow    The row of the Square where the click occurred and the new ship would start
     * @param clickedColumn The column of the Square where the click occurred and the new ship would start
     * @param shipSize      The number of Squares of the ship to be placed
     * @param orientation   The orientation of the ship to be placed (horizontal/vertical)
     * @return True if the new ship placement would not result in that ship exceeding the board edges and would not overlap
     * with one of that players existing ships.
     */
    private boolean validShipPlacement(List<Square[]> shipsOfPlayer, int clickedRow, int clickedColumn, int shipSize, ShipPlacementOrientation orientation) {

        if (shipOutsideOfBoard(clickedRow, clickedColumn, shipSize, orientation) ||
                shipCollision(shipsOfPlayer, clickedRow, clickedColumn, shipSize, orientation)) {
            return false;
        }

        return true;
    }

    /**
     * Adds a new ship that a player has placed on his/her's playing board to that player's ship collection
     *
     * @param shipsOfPlayer The ship collection of a specific player
     * @param clickedRow    The row of the Square where the player has clicked to place the ship
     * @param clickedColumn The column of the Square where the player has clicked to place the ship
     * @param shipSize      The number of Squares the new ship takes up
     * @param orientation   The orientation of the new ship (horizontal/vertical)
     */
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

    /**
     * Actions to take when a BattleshipClient (a player) has made a click on his/her's playing board during setup phase, i.e.
     * when a player is trying to place a ship on his/her's playing board.
     * <p>
     * Checks, with helper methods, if the new ship placement is legal or not and reports this back to the client.
     * <p>
     * Sets the specific player to be the firstReadyPlayer if he/she finished placing all the ships first
     * <p>
     * Initiates the game phase if both players have placed all their ships
     *
     * @param clientId      The id of the BattleshipClient who performed the click
     * @param clickedRow    The row of the Square on which the click occurred.
     * @param clickedColumn The column of the Square on which the click occurred.
     * @param orientation   The ship orientation setting at the client who performed the click
     * @throws IllegalArgumentException If the client who performed the click does not have a map with ship, as should have been
     *                                  setup during the setup phase
     */
    public void handleClickInSetupPhase(int clientId, int clickedRow, int clickedColumn, ShipPlacementOrientation orientation) throws IllegalArgumentException {

        if (!playerShips.containsKey(clientId)) {
            throw new IllegalStateException("Client with id " + clientId + " should have mapping");
        }

        List<Square[]> shipsOfPlayer = playerShips.get(clientId);

        //Check if the new ship placement is legal. If not, report that back to the client
        if (!validShipPlacement(shipsOfPlayer, clickedRow, clickedColumn, shipSize, orientation)) {
            THE_SERVER.sendMessageToClient(clientId, currentGameState + " " + "notOkMove" + " " + clickedRow + " " + clickedColumn);
        } else {
            //add the new ship to the player's ship collection
            addShipToPlayerShips(shipsOfPlayer, clickedRow, clickedColumn, shipSize, orientation);

            //report legal ship placement back to the client
            THE_SERVER.sendMessageToClient(clientId, currentGameState + " " + "placeShip" +
                    " " + clickedRow + " " + clickedColumn + " " + shipSize + " " +
                    (orientation == ShipPlacementOrientation.HORIZONTAL ? "h" : "v"));

            //if the player has not yet placed all the ships, report back to the player to place another ship
            if (shipsOfPlayer.size() < SHIPS_PER_PLAYER) {
                THE_SERVER.sendMessageToClient(clientId, currentGameState + " " + "newShipPlacementTurn");
                //if the player has placed all the ships and the opponent hasn't
            } else if (firstReadyPlayer == -1) {
                firstReadyPlayer = clientId;
            }
            //if all players has placed their ships, initiate game phase
            else if (allPlayersReady()) {
                THE_SERVER.broadcastMessage(currentGameState + " " + "changePhase" + " " + "gamePhase" + " " + firstReadyPlayer);
                currentGameState = GameState.GAME_PHASE;
            }
        }
    }

    /**
     * Checks if every connected player have placed all the ships, as specified by SHIPS_PER_PLAYER
     * @return False if any one of the connected players hasn't placed enough ships yet
     */
    private boolean allPlayersReady() {
        for (Map.Entry<Integer, List<Square[]>> entry : playerShips.entrySet()) {
            if (entry.getValue().size() < SHIPS_PER_PLAYER) {
                return false;
            }
        }
        return true;
    }

    /**
     * Send a message to each player that a click has resulted in a sunken ship
     * @param sunkenShip The Squares of the sunken ship. Used to get the row and column information.
     * @param clientId The id of the BattleshipClient who performed the click resulting in the sunken ship
     * @param clickedRow The row of the Square on which the click occurred
     * @param clickedColumn The row of the Square on which the click occurred
     */
    private void broadcastSinkShip(Square[] sunkenShip, int clientId, int clickedRow, int clickedColumn) {

        StringBuilder rowsAndColumnOfSunkenShip = new StringBuilder();
        //append all the rows and columns of each Square of the sunken ship, separated by a blank space
        for (int i = 0; i < sunkenShip.length; i++) {
            Square s = sunkenShip[i];
            rowsAndColumnOfSunkenShip.append(s.getRow());
            rowsAndColumnOfSunkenShip.append(" ");
            rowsAndColumnOfSunkenShip.append(s.getColumn());
            //to avoid getting a blank space after the last row/column information
            if (i + 1 < sunkenShip.length) {
                rowsAndColumnOfSunkenShip.append(" ");
            }
        }
        THE_SERVER.broadcastMessage(currentGameState + " " + "sinkShip" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + sunkenShip.length + " " + rowsAndColumnOfSunkenShip.toString());
    }

    /**
     * Actions to take 
     * @param winnerClientId
     */
    private void setGameOver(int winnerClientId) {
        currentGameState = GameState.GAME_OVER;
        THE_SERVER.broadcastMessage(currentGameState + " " + "setGameOver" + " " + winnerClientId);
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
            THE_SERVER.broadcastMessage(currentGameState + " " + "okMove" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + "hit");

        } else if (resultOfShot == 0) {
            THE_SERVER.broadcastMessage(currentGameState + " " + "okMove" + " " + clientId + " " + clickedRow + " " + clickedColumn + " " + "miss");
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
        THE_SERVER.initiateNewTurn(playerOfCurrentTurn, currentGameState + " " + "newTurn");
    }


}
