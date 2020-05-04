
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Helper class to a BattleshipClient instance. This class handles messages received from the BattleshipServer
 * addressed to the BattleshipClient object related to this class.
 *
 * In the run-method, an object of this class listens for, and handles, incoming messages sent over
 * the Socket that is passed to this class on initiation.
 */
public class ClientReceiver extends Thread {

    private BufferedReader in;

    private BattleshipClient client;

    private Socket socket;

    ClientReceiver(BattleshipClient client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    /**
     * Listens for, and handles, incoming messages addressed to the BattleshipClient related to this class.
     *
     * The listening-loop ends if the incoming message == null, which would indicate
     * that the opponent has disconnected from the game.
     */
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
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the incoming message is of any of the types "setID" or "opponentDisconnect" and takes
     * necessary actions if so. If the message is of any other type it will be passed along to
     * the BattleshipClient object for further handling.
     * @param msg The received message in the form of a String separated with blank space between the message parts
     */
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
