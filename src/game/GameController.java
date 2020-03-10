package game;

import connection.BattleshipServer;
import gui.GameWindow;

import java.util.ArrayList;
import java.util.List;

public class GameController {

    public static GameState gameState = GameState.CONNECTION_PHASE;

    public static final int BOARD_DIMENSION = 10;

    private final Square[][] localClientBoard = new Square[BOARD_DIMENSION][BOARD_DIMENSION];
    private final Square[][] remoteClientBoard = new Square[BOARD_DIMENSION][BOARD_DIMENSION];

    //Id of player that has set up his/her ships. -1 if no player are ready
    private int readyPlayerId = -1;

    private final BattleshipServer SERVER;

    public GameController(BattleshipServer server) {
        this.SERVER = server;
    }

    public static void setGameState(GameState newState) {
        gameState = newState;
    }

    public GameState getGameState() {
        return gameState;
    }

//    public void twoConnectedPlayers() {
////        System.out.println("TVÅ TILLKOPPLADE!");
//        gameState = GameState.SETUP_PHASE;
//        SERVER.broadcastMessage("setupPhase");
//
//    }

    public void twoConnectedPlayers(){

        //onödig kontroll??
        if(gameState == GameState.CONNECTION_PHASE){
            SERVER.broadcastMessage("changePhase"+" "+"setupPhase");
        }

    }

    public boolean validateMove(String msg) {

        //Denna låg tidigare direkt i klienttråden, som anropade broadcast direkt den fick in ngt meddelande

//        System.out.println("nu är det denna fas: "+ gameState);
//        System.out.println(msg);
        String [] tokens =  msg.split(" ");

        int clientId = Integer.parseInt(tokens[0]);
//        int clickedColumn= getSquareNumberFromCoordinate(Integer.parseInt(tokens[0]));
//        int clickedRow = getSquareNumberFromCoordinate(Integer.parseInt(tokens[1]));

        int clickedRow= Integer.parseInt(tokens[1]);
        int clickedColumn = Integer.parseInt(tokens[2]);
//        System.out.println("Row: "+clickedRow+" Column: "+clickedColumn);
        if (gameState == GameState.SETUP_PHASE) {
//            SERVER.broadcastMessage(msg);

            //kontrollera att klienten tryckt på en giltig ruta att placera ut första skeppet
            // (3 rutor horisontellt). Hårdkodata nu, men gör generellt! Typ att skicka med storleken på skeppet
            //som sedan kan användas i kontrollen här

            //om det är giltigt, skicka besked till servern att godkänna draget och att klienten ska markera på board
           if(clickedColumn == -1 || clickedRow == -1){
               //kasta undantag (kan typ inte hända?)
           }

           //giltigt
            if(clickedColumn <= BOARD_DIMENSION - 2){
                //skicka här med typ av okMove, typ markShip, samt skeppstorlek (och i framtiden om vertikalt/horisontellt
                int shipSize = 3;
                SERVER.sendMessageToClient(clientId,"placeShip"+" "+clickedRow+" "+clickedColumn+" "+shipSize);

                if(readyPlayerId == -1){
                    readyPlayerId = clientId;
                }else{
                    //måste tråden pausas här ett tag? innan kommando för byte av fas skickas

                    //Vill man hålla reda på vems tur det är? Isf typ gamestate_remoteplayer, annars bara gamestate_play
                    SERVER.broadcastMessage("changePhase"+" "+"gamePhase"+" "+ readyPlayerId);
                }

            }else{
                //om inte giltigt drag -> lägg till muslyssnare
                SERVER.sendMessageToClient(clientId, "notOkMove"+" " + clickedRow +" "+clickedColumn);
            }


            // markera det i rätt matris i this.
                // Sätt boolean till true och
                // kontrollera om den andra boolean är true också
                    //om ja - initiera nästa fas av spelet
            // Om inte, skicka meddelande bara till den klienten om felaktigt klick.
            // Gör så att muslyssnare adderas igen.
        } else if (gameState == GameState.GAME_PHASE){
            //kontrollera att draget är ok, alltså att rutan som klickas är ledig

            //om inte ok, skicka besked till klienten om notOkMove

            //om ok, broadcasta att markera som hit eller miss med clientId som "avsändare"

            SERVER.broadcastMessage("okMove" +" "+clientId+" "+clickedRow +" "+clickedColumn+" "+"miss");
            SERVER.initiateNewTurn(clientId);

            //kontrollera spelets status. om inte game over - byt tur
        }
        else {
            SERVER.broadcastMessage(msg);
        }


        return false;
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



}
