package server;

import gamecomponents.Square;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.net.InetAddress.getLocalHost;


//Only used if the player chooses "Create server"
public class BattleshipServer extends Thread {

    private final int DEFAULT_PORT = 2000;

    private int serverPort;

    private String hostAddress;

    private MessageHandler messageHandler = new MessageHandler(this);

    //Gör detta till Map med id som nyckel och tråd som värde istället
    private final List<ClientHandlerThread> CLIENT_THREADS = new LinkedList<>();

    private boolean isAlive = false;
    private boolean isAvalible = false;

    private ServerSocket serverSocket;

    public BattleshipServer() throws IOException {
        serverPort = DEFAULT_PORT;
        serverSocket = new ServerSocket(serverPort);
        hostAddress = serverSocket.getInetAddress().getLocalHost().getHostAddress();

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
                outputMessage("" + " " + "setID " + threadID);

                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String msg;

                //null om egna spelaren stänger fönstret, "socketTimeOut" om egna spelaren inaktiv för länge
                while ((msg = in.readLine()) != null && !msg.equals("socketTimedOut")) {

                    receiveMessageFromClientThread(msg);

                    Thread.sleep(20);
                }

//				out.close();
                in.close();
                connection.close();

            } catch (IOException ioe) {
                ioe.printStackTrace();

            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            actionsForKillThread(threadID);

        }

        public int getThreadID(){
            return threadID;
        }

        private void outputMessage(String msg) {

            out.println(msg);
        }


    }

    void actionsForKillThread(int threadID){
        System.out.println("KOPPLAR IFRÅN VA");
        synchronized (CLIENT_THREADS) {
            removeKilledThreadFromList(threadID);
            notifyRemainingPlayer();
        }
    }

    private synchronized  void notifyRemainingPlayer(){
        for(ClientHandlerThread client : CLIENT_THREADS){
            client.outputMessage(""+" "+"opponentDisconnect"+" ");
        }
    }

    void socketTimedOut(int idOfTimedOutClient){
        actionsForKillThread(idOfTimedOutClient);
    }

    private synchronized void removeKilledThreadFromList(int idOfThreadToRemove) {
        System.out.println("tar bord med id: "+idOfThreadToRemove+" Trådar i listan innan: " + CLIENT_THREADS.size());


        for(int i = 0; i<CLIENT_THREADS.size(); i++){
            if (CLIENT_THREADS.get(i).getThreadID() == idOfThreadToRemove) {
                CLIENT_THREADS.remove(i);
                break;
            }
        }

        System.out.println("trådar i listan efter: " + CLIENT_THREADS.size());
        
    }

    @Override
    public void run() {

        isAlive = true;

        //sen går den över till klienttrådarna och väntar

        int clientThreadId = 1;

        while (CLIENT_THREADS.size() < 2) {
            try {

                isAvalible = true;

                Socket clientConnection = serverSocket.accept();

                ClientHandlerThread clientThread = new ClientHandlerThread(clientConnection, clientThreadId);

                CLIENT_THREADS.add(clientThread);
                clientThread.start();

                messageHandler.connectedPlayer(clientThreadId);


                System.out.println("KLIENT TILLKOPPLAD, ID: " + clientThread.threadID);
                isAvalible = false;
                clientThreadId++;

            } catch (IOException ioe) {
                System.err.println("Couldn't initialize new ClientHandlerThread: " + ioe);
            }

        }
        try {
            //kanske visa laddningsskärm eller ngt så att ingen kan trycka. Eller sköta det med att ge muslyssnare
            //innifrån two connected players....
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        messageHandler.twoConnectedPlayers();

    }


    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    public boolean isAvalible() {
        return isAvalible;
    }

    private synchronized void receiveMessageFromClientThread(String msg) {

        for(String s : msg.split(" ")){
            if(s.equals("socketTimedOut")){
                System.out.println("FICK MESS OM BORTKOPPLAD");
            }
        }

//        gameController.handleClientClicked(msg);
        messageHandler.handleClientMsg(msg);


    }

    //from GameController. ha kontroller här så att det är en enum MessageType och att för varje sådan
    // typ, att det aktuella meddelandet har rätt parametrar. (eller kontrollera det nån annanstans?
    public synchronized void sendMessageToClient(int clientId, String msg) {

//        System.out.println("SKICKAR TILL SPECIFIK KLIENT MED ID: "+clientId+" "+msg+" "+row+" "+column);

        for (ClientHandlerThread clientThread : CLIENT_THREADS) {
            if (clientThread.threadID == clientId) {

                clientThread.outputMessage(msg);
            }
        }
    }

    public synchronized void initiateNewTurn(int clientIdOfPreviousTurn, String msg) {
        for (ClientHandlerThread clientThread : CLIENT_THREADS) {
            if (clientThread.threadID != clientIdOfPreviousTurn) {
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




