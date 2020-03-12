//package client;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.Socket;
//import java.net.SocketTimeoutException;
//
public class ClientReceiver extends Thread {
//    Socket socket;
//
//    ClientReceiver(Socket socket) {
//        this.socket = socket;
//
//    }
//
//    @Override
//    public void run() {
//        try {
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//            //while inputen inte är "quit" eller så...
//            while (true) {
//
//                handleReceivedMessage(in.readLine());
//                Thread.sleep(20);
//            }
//
//        } catch (SocketTimeoutException e) {
//            System.err.println("No response from opponent ");
//            e.printStackTrace();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//
//        }
//    }
}
