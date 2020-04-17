package connection;

import client.BattleshipClient;
import gui.gamewindow.GameWindow;
import server.BattleshipServer;

import java.io.IOException;

public class ConnectionHandler {

    public void initializeConnection(boolean isHosting) throws IOException {

        String hostName;
        int port;

        if(isHosting) {
            BattleshipServer server = new BattleshipServer();
            server.start();

            hostName = server.getHostAddress();
            port = server.getPort();

        }else{
            hostName = "192.168.1.97";
            port = 2000;
        }

        BattleshipClient client = new BattleshipClient(hostName, port);
        GameWindow window = new GameWindow(client, hostName, port, isHosting);
        client.setGameWindow(window);

    }
}
