package client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSender {

    private PrintWriter out;

    public ClientSender(Socket socket){
        try {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        }catch (IOException e) {
            e.printStackTrace();
            // fel i setUpSocket()
        }
    }



    void sendClick(int id, int row, int column, boolean horizontal){
        out.println(id + " " + row + " " + column+" "+(horizontal ? "h" : "v"));
    }


}
