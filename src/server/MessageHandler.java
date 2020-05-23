package server;

import gamecomponents.ShipPlacementOrientation;


/**
 * Layer in between the BattleshipServer and the GameController to facilitate message sending between them
 */
public class MessageHandler {

    private final BattleshipServer SERVER;

    private final GameController GAME_CONTROLLER;

    public MessageHandler(BattleshipServer battleshipServer){
        this.SERVER = battleshipServer;
       GAME_CONTROLLER = new GameController(SERVER);
    }

    public void connectedPlayer(int clientId){
        GAME_CONTROLLER.connectedPlayer(clientId);
    }

    public void twoConnectedPlayers(){
        GAME_CONTROLLER.twoConnectedPlayers();
    }

    /**
     * Handles a message sent from a BattleshipClient to the server. Checks if the message was reporting
     * a socket time out event. If not it is assumed that the message is a game event and parses bits of the
     * message depending on what game phase it currently is. Then sends the message through to the GameController
     * @param msg The message
     */
    public void handleClientMsg(String msg){

            String[] tokens = msg.split(" ");

            int clientId = Integer.parseInt(tokens[0]);
            //a socket time out event
            if(tokens[1].equals("socketTimedOut")){
              SERVER.socketTimedOut(clientId);
              //a game event
            }else{
                 int clickedRow = Integer.parseInt(tokens[1]);
                 int clickedColumn = Integer.parseInt(tokens[2]);

                 GameState currentGameState = GAME_CONTROLLER.getCurrentGameState();

                 if (currentGameState == GameState.SETUP_PHASE) {
                     String shipPlacementOrientation = tokens[3];
                     GAME_CONTROLLER.handleClickInSetupPhase(clientId, clickedRow, clickedColumn,
                             shipPlacementOrientation.equals("h") ?
                                     ShipPlacementOrientation.HORIZONTAL : ShipPlacementOrientation.VERTICAL);
                 } else if (currentGameState == GameState.GAME_PHASE) {
                     GAME_CONTROLLER.handleClickInGamePhase(clientId, clickedRow, clickedColumn);
                 }
            }
        }



}
