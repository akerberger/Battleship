package server;

import gamecomponents.ShipPlacementOrientation;

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

    public void handleClientClicked(String msg){

            String[] tokens = msg.split(" ");

            int clientId = Integer.parseInt(tokens[0]);
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
