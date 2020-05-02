
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;

//
public class ClientReceiver extends Thread {

    private BufferedReader in;

    private BattleshipClient client;

    private Socket socket;

    ClientReceiver(BattleshipClient client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                String msg = in.readLine();
                if (msg != null) {
                    handleReceivedMessage(msg);
                    Thread.sleep(20);
                } else {
                    client.onOpponentDisconnect();
                    break;
                }
            }

        } catch (SocketTimeoutException e) {
            client.socketTimedOut();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleReceivedMessage(String msg) {
        String[] tokens = msg.split(" ");

        String command = tokens[1];

        if (command.equals("setID")) {
            if (client.getId() != -1) {
                throw new IllegalArgumentException(" ID already set in BattleshipClient");
            }
            client.setId(Integer.parseInt(tokens[2]));
        }else if(command.equals("opponentDisconnect")){
            client.onOpponentDisconnect();
        } else {
            client.handleGameMessage(tokens);
        }

    }
}
