package gameinitiation;

import client.BattleshipClient;
import gui.gamewindow.GameWindow;
import server.BattleshipServer;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Handles the initiation of, or the connection to, a game session depending on whether the user
 * has selected to host or to connect to a game.
 */
public class GameInitiationHandler {

    private int port;

    /**
     *
     * @param port The port number that will be used for connecting to a game/initiating a game
     */
    public GameInitiationHandler(int port){
        this.port = port;
    }

    /**
     * Creates a BattleShipClient and a GameWindow.
     * Also creates a BattleShipServer object if the user has chosen to host a game.
     * @param isHosting True if a user has selected to host a game. False if a user has selected to connect to an existing game.
     * @throws IOException If an exception is thrown by either the BattleshipClient constructor or the BattleshipServer constructor
     */
    public void handleGameInitiation(boolean isHosting) throws IOException {

        InetAddress hostAddress = InetAddress.getByName("localhost");

        if(isHosting) {
            new BattleshipServer(port).start();
        }

        BattleshipClient client = new BattleshipClient(hostAddress, port);
        GameWindow window = new GameWindow(client, hostAddress, port, isHosting);

        client.setGameWindow(window);

    }

    public int getPort(){
        return port;
    }
}
