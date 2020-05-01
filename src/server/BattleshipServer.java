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

    private String hostAddress;

    private MessageHandler messageHandler = new MessageHandler(this);

    //Gör detta till Map med id som nyckel och tråd som värde istället
    private final List<ClientThread> CLIENT_THREADS = new LinkedList<>();


    private ServerSocket serverSocket;

    public BattleshipServer() throws IOException {
        serverPort = DEFAULT_PORT;
        serverSocket = new ServerSocket(serverPort);
        hostAddress = serverSocket.getInetAddress().getLocalHost().getHostAddress();
        

    }

    private class ClientThread extends Thread {
        Socket connection;
        BufferedReader in;
        PrintWriter out;
        int threadID;


        public ClientThread(Socket connection, int threadID) {
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

        public int getThreadID() {
            return threadID;
        }

        private void outputMessage(String msg) {

            out.println(msg);
        }


    }

    void actionsForKillThread(int threadID) {
        System.out.println("KOPPLAR IFRÅN VA");
        synchronized (CLIENT_THREADS) {
            removeKilledThreadFromList(threadID);
            notifyRemainingPlayer();
        }
    }

    private synchronized void notifyRemainingPlayer() {
        for (ClientThread client : CLIENT_THREADS) {
            client.outputMessage("" + " " + "opponentDisconnect" + " ");
        }
    }

    void socketTimedOut(int idOfTimedOutClient) {
        actionsForKillThread(idOfTimedOutClient);
    }

    private synchronized void removeKilledThreadFromList(int idOfThreadToRemove) {
        System.out.println("tar bord med id: " + idOfThreadToRemove + " Trådar i listan innan: " + CLIENT_THREADS.size());


        for (int i = 0; i < CLIENT_THREADS.size(); i++) {
            if (CLIENT_THREADS.get(i).getThreadID() == idOfThreadToRemove) {
                CLIENT_THREADS.remove(i);
                break;
            }
        }

        System.out.println("trådar i listan efter: " + CLIENT_THREADS.size());

    }

    private void actionsForClientConnected(Socket clientConnection, int clientThreadId) {


        ClientThread clientThread = new ClientThread(clientConnection, clientThreadId);
        CLIENT_THREADS.add(clientThread);
        clientThread.start();


    }

    @Override
    public void run() {

        int clientThreadId = 1;

        while (CLIENT_THREADS.size() < 2) {
            try {

                Socket clientConnection = serverSocket.accept();

                actionsForClientConnected(clientConnection, clientThreadId);

                messageHandler.connectedPlayer(clientThreadId);

                clientThreadId++;

            } catch (IOException ioe) {
                System.err.println("Couldn't initialize new ClientThread: " + ioe);
            }

        }
        try {
            //väntar så att inte exeveringen kör ifrån den nya tillkopplade klienten...
            //kanske visa laddningsskärm eller ngt så att ingen kan trycka. Eller sköta det med att ge muslyssnare
            //innifrån two connected players....
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        messageHandler.twoConnectedPlayers();

    }


    private synchronized void receiveMessageFromClientThread(String msg) {

        for (String s : msg.split(" ")) {
            if (s.equals("socketTimedOut")) {
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

        for (ClientThread clientThread : CLIENT_THREADS) {
            if (clientThread.threadID == clientId) {

                clientThread.outputMessage(msg);
            }
        }
    }

    public synchronized void initiateNewTurn(int clientIdOfPreviousTurn, String msg) {
        for (ClientThread clientThread : CLIENT_THREADS) {
            if (clientThread.threadID != clientIdOfPreviousTurn) {
                clientThread.outputMessage(msg);
                break;
            }
        }
    }

    public synchronized void broadcastMessage(String msg) {

        for (ClientThread client : CLIENT_THREADS) {

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




