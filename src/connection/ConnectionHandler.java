package connection;

import gui.GameWindow;

import java.util.Scanner;

public class ConnectionHandler {

    public void initializeConnection(boolean isHosting){

        String hostName;
        int port;

        BattleshipClient client;

        if(isHosting) {
            BattleshipServer server = new BattleshipServer();
            server.start();

            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
            }

            hostName = server.getHostAddress();
            port = server.getPort();

            System.out.println("hostar på "+hostName+" port "+port);
        }else{
//            System.out.println("kopplar till annan server, ange ip-adress:");
            System.out.println("kopplar till annan server");
//            Scanner scan = new Scanner(System.in);

//            hostName = scan.nextLine();
//
//            port = scan.nextInt();
//            scan.nextLine();

            hostName = "192.168.1.97";
            port = 2000;

        }

        client = new BattleshipClient(hostName, port, isHosting);
        GameWindow window = new GameWindow(client, hostName, port);
        client.setGameWindow(window);

    }
}
