package client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;



/**
 * Helper class to a BattleshipClient instance. This class receives messages from the BattleshipClient
 * and sends them, over the Socket that is passed on during instantiation of this class, to the BattleshipServer
 */
public class ClientSender {

    private PrintWriter out;

    /**
     * Sets up the PrintWriter object used for sending messages from the BattleshipClient over the Socket
     * @param socket The Socket that are to be used for sending the messages
     */
    public ClientSender(Socket socket){
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    void reportSocketTimedOut(){
        out.println("socketTimedOut");
    }

    /**
     * Receives information about a click event that has occurred on a Square. Passes the information on over the Socket.
     *
     * As the click event might represent a shot during the game phase and a ship placement request, information
     * about the ship placement is also passed along to this method.
     * @param id The id of the BattleshipClient sending the information
     * @param row The row of the Square that has been clicked
     * @param column The column of the Square that has been clicked
     * @param horizontal Information about the current Ship placement orientation (horizontal/vertical)
     *                   in the sending BattleshipClient.
     */
    void sendClick(int id, int row, int column, boolean horizontal){
        out.println(id + " " + row + " " + column+" "+(horizontal ? "h" : "v"));
    }


}
