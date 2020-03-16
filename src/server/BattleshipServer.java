package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;


//Only used if the player chooses "Create server"
public class BattleshipServer extends Thread {

    private final int DEFAULT_PORT = 2000;

    private int serverPort;

    private String hostAddress = null;

    private GameController gameController = new GameController(this);


    //Gör detta till Map med id som nyckel och tråd som värde istället
    private final List<ClientHandlerThread> CLIENT_THREADS = new LinkedList<>();

    private boolean isAlive = false;
    private boolean isAvalible = false;

//    public BattleshipServer(Socket BattleshipClientSocket) {
////        this.connection = BattleshipClientSocket;
//
//    }

    public BattleshipServer() {
        serverPort = DEFAULT_PORT;

    }

    private class ClientHandlerThread extends Thread {
        Socket connection;
        BufferedReader in;
        PrintWriter out;
        int threadID;


        public ClientHandlerThread(Socket connection, int threadID) {
            this.connection = connection;
            this.threadID = threadID;

        }

        @Override
        public void run() {

            try {
                out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()), true);
                outputMessage("hej"+" "+"setID "+threadID);

                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String msg;

                while ((msg = in.readLine()) != null) {
//                    System.out.println("klienttråd läser : "+msg+" trådID: "+threadID);

                    receiveMessageFromClientThread(msg);
                    //skickar
//                    GUI.broadcastedMessage(connection.getInetAddress(), msg);
                    Thread.sleep(20);
                }

//				out.close();
                in.close();
                connection.close();

            } catch (IOException ioe) {

            } catch (InterruptedException ie) {

            }

            synchronized (CLIENT_THREADS) {
                removeKilledThreadFromList(this);
//                broadcastMessage(connection.getInetAddress(), "*CLIENT DISCONNECTED*");
//                GUI.onClientDisconnect(CLIENT_THREADS.size(), connection.getInetAddress().getHostName());
            }

        }

        //        private void outputMessage(InetAddress clientAddress, String msg) {
        private void outputMessage(String msg) {

//			StringBuilder sb = new StringBuilder();

//            String output = "";
//
//            if (clientAddress == null) {
////				sb.append("Anonymous");
//                output += "Anonymous";
//            } else {
////				sb.append(clientAddress);
//                output += clientAddress;
//            }

//			sb.append(": ");
//			sb.append(msg);



//            try {
//                //kan inte denna göras i run och sen gör man bara out.println(msg) här?
////                out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()), true);
//                out.println(msg);
//            }
//            catch (IOException e) {
//                System.err.println("Fel i outputMessage: " + e);
//            }
            out.println(msg);
        }



    }

    private synchronized void removeKilledThreadFromList(Thread thread) {
        System.out.println("trådar i listan innan: " + CLIENT_THREADS.size());

        Thread toRemove = null;

        for (ClientHandlerThread client : CLIENT_THREADS) {
            if (client.getId() == thread.getId()) {
                toRemove = client;
            }
        }

        if (toRemove != null) {
            CLIENT_THREADS.remove(toRemove);
        }

        System.out.println("trådar i listan efter: " + CLIENT_THREADS.size());

    }

    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            isAlive = true;
            hostAddress = serverSocket.getInetAddress().getLocalHost().getHostAddress();

//            GUI = new ServerGUI(InetAddress.getLocalHost().getHostName(), port);

            //sen går den över till klienttrådarna och väntar

            int clientThreadId = 1;

            while (CLIENT_THREADS.size() < 2) {
                try {

                    isAvalible = true;

                    Socket clientConnection = serverSocket.accept();

                    ClientHandlerThread clientThread = new ClientHandlerThread(clientConnection, clientThreadId);

                    CLIENT_THREADS.add(clientThread);
                    clientThread.start();
//                    GUI.onNewClientConnected(CLIENT_THREADS.size(), clientConnection.getInetAddress().getHostName());
                    gameController.connectedPlayer(clientThreadId);
                    System.out.println("KLIENT TILLKOPPLAD, ID: "+clientThread.threadID);
                    isAvalible = false;
                    clientThreadId++;

                } catch (IOException ioe) {
                    System.err.println("Couldn't initialize new ClientHandlerThread: " + ioe);
                }

            }
            try{
                //kanske visa laddningsskärm eller ngt så att ingen kan trycka. Eller sköta det med att ge muslyssnare
                //innifrån two connected players
            Thread.sleep(1000);}catch(InterruptedException e){e.printStackTrace();}


            gameController.twoConnectedPlayers();


        } catch (IOException ioe) {
            System.err.println("Couldn't start server: " + ioe);

        }
    }

    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    public boolean isAvalible() {
        return isAvalible;
    }

    private synchronized void receiveMessageFromClientThread(String msg){
        //validerar drag. Kanske snarare ska vara handle message...
        gameController.handleClientClicked(msg);

    }

    //from GameController. ha kontroller här så att det är en enum MessageType och att för varje sådan
    // typ, att det aktuella meddelandet har rätt parametrar. (eller kontrollera det nån annanstans?
    public synchronized void sendMessageToClient(int clientId, String msg){

//        System.out.println("SKICKAR TILL SPECIFIK KLIENT MED ID: "+clientId+" "+msg+" "+row+" "+column);

        for(ClientHandlerThread clientThread : CLIENT_THREADS){
            if (clientThread.threadID == clientId){

                clientThread.outputMessage(msg);
            }
        }
    }

    public synchronized void initiateNewTurn(int clientIdOfPreviousTurn, String msg){
        for(ClientHandlerThread clientThread : CLIENT_THREADS){
            if (clientThread.threadID != clientIdOfPreviousTurn){
                clientThread.outputMessage(msg);
                break;
            }
        }
    }

    public synchronized void broadcastMessage(String msg) {
//        String[] tokens = msg.split(" ");

//        System.out.println(Arrays.toString(tokens));

        for (ClientHandlerThread client : CLIENT_THREADS) {

            client.outputMessage(msg);
        }

    }

    public int getPort() {
        return serverPort;
    }


    public String getHostAddress() {

        return hostAddress;
    }


}




