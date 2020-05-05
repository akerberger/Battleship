package gameinitiation;

import client.BattleshipClient;
import gui.gamewindow.GameWindow;
import server.BattleshipServer;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Handles the initiation of a game session.
 */
public class GameInitiationHandler {

    /**
     * Creates a BattleShipClient and a GameWindow.
     * Also creates a BattleShipServer object if the user has chosen to host a game.
     * @param isHosting True if a user has selected to host a game. False if a user has selected to connect to an existing game.
     * @throws IOException If an exception is thrown by either the BattleshipClient constructor or the BattleshipServer constructor
     */
    public void handleGameInitiation(boolean isHosting) throws IOException {

        InetAddress hostAddress = InetAddress.getByName("localhost");
        int defaultPort = 2000;

        if(isHosting) {
            new BattleshipServer(defaultPort).start();
        }

        BattleshipClient client = new BattleshipClient(hostAddress, defaultPort);
        GameWindow window = new GameWindow(client, hostAddress, defaultPort, isHosting);
        client.setGameWindow(window);

    }
}
