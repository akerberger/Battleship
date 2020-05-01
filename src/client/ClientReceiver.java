
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
                    client.handleReceivedMessage(msg);
                    Thread.sleep(20);
                } else {
                    client.handleReceivedMessage(""+" "+"opponentDisconnect");
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
}
