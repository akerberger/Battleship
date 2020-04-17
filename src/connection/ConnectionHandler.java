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

            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            hostName = server.getHostAddress();
            port = server.getPort();

            System.out.println("hostar på "+hostName+" port "+port);
        }else{
            //gör try catch och försök igen om kopplingen misslyckades (= om inget lokalt spel är uppsatt redan)
            System.out.println("kopplar till annan server");
            hostName = "192.168.1.97";
            port = 2000;

        }

        BattleshipClient client = new BattleshipClient(hostName, port);
        GameWindow window = new GameWindow(client, hostName, port, isHosting);
        client.setGameWindow(window);

    }
}
