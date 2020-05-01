package connection;

import client.BattleshipClient;
import gui.gamewindow.GameWindow;
import server.BattleshipServer;

import java.io.IOException;
import java.net.InetAddress;

public class ConnectionHandler {

    public void initializeConnection(boolean isHosting) throws IOException {

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
