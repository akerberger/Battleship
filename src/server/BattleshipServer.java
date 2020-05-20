package server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;


/**
 * Handles the hosting of a battleship game session by setting up the serverSocket object and listening for
 * two clients to connect. After a client theSocket is detected the client will be represented by an instance
 * of the nested class ClientThread.
 *
 * During game play the BattleshipServer listens for game events from the CLIENT_THREADS. The events are passed on
 * to a GameController, through the messageHandler, for handling/validation. When the GameController has handled the
 * event, it passes any new game information on to the messageHandler back to this BattleshipServer
 * which passes along the information to the relevant clients.
 */
public class BattleshipServer extends Thread {

    private final int PORT; //the port where the game is hosted

    private InetAddress hostAddress; //the host address where the game is hosted

    private MessageHandler messageHandler = new MessageHandler(this); //Handles messages to/from the clients

    private final List<ClientThread> CLIENT_THREADS = new LinkedList<>(); //The connected clients represented as ClientThread objects

    private ServerSocket serverSocket; //the theSocket where the game is hosted by this BattleshipServer

    public BattleshipServer(int port) throws IOException {
        PORT = port;
        serverSocket = new ServerSocket(PORT);
        hostAddress=serverSocket.getInetAddress();
    }

    /**
     * Represents the connection of a BattleshipClient on the BattleshipServer side. The threadID will correspond to the
     * id of the BattleshipClient that is connected through theSocket of this ClientThread object.
     *
     * Handles communication to/from the client via in and out.
     */
    private class ClientThread extends Thread {
        Socket theSocket; //the socket representing the connection to a client
        BufferedReader in;//handle incoming messages from the client over the connection
        PrintWriter out; //sends messages to the client over the connection
        int threadID;

        /**
         *
         * @param socket The socket to represent the connection to the client
         * @param threadID The id of this ClientThread object. Corresponds to the id of the BattleshipClient
         *                 which this ClientThread object represents the connection to.
         */
        public ClientThread(Socket socket, int threadID) {
            this.theSocket = socket;
            this.threadID = threadID;

        }

        /**
         * Sets up the out Writer and the in Reader objects. Sends an initial message to the BattleshipClient to set
         * it's id so that it corresponds with the threadID of this ClientThread object, then listens to incoming
         * messages from the BattleshipClient.
         */
        @Override
        public void run() {

            try {
                out = new PrintWriter(new OutputStreamWriter(theSocket.getOutputStream()), true);
                outputMessage("" + " " + "setID " + threadID);

                in = new BufferedReader(new InputStreamReader(theSocket.getInputStream()));
                String msg;

                /**
                 * Listen for incoming messages from the BattleshipClient.
                 *
                 * Listen as long as the incoming message not indicates that the client has disconnected.
                 * Such indication can happen if the message == null (indicating e.g. that the client has closed it's window)
                 * or if the message equals "socketTimedOut" (indicating that the client has been inactive too long)
                 */
                while ((msg = in.readLine()) != null && !msg.equals("socketTimedOut")) {

                    receiveMessageFromClientThread(msg);

                    Thread.sleep(20); //pause the thread to let potential other queued events happen
                }
                //cleanup after client disconnection.
                //the out object isn't closed here as the actionsOnClientDisconnect method needs it.
                in.close();
                theSocket.close();

            } catch (IOException ioe) {
                ioe.printStackTrace();

            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            actionsOnClientDisconnect(threadID);
            out.close();
        }

        public int getThreadID() {
            return threadID;
        }

        /**
         * send a message to the BattleshipClient
         * @param msg the message
         */
        private void outputMessage(String msg) {

            out.println(msg);
        }


    }

    /**
     * Actions to take when a BattleshipClient has disconnected.
     * @param threadID The threadID of the ClientThread object that has disconnected
     */
    void actionsOnClientDisconnect(int threadID) {

        synchronized (CLIENT_THREADS) {
            removeDisconnectedThreadFromList(threadID);
            notifyRemainingPlayer();
        }
    }

    /**
     * Notify the remaining player that their opponent has disconnected
     */
    private synchronized void notifyRemainingPlayer() {
        for (ClientThread client : CLIENT_THREADS) {
            client.outputMessage("" + " " + "opponentDisconnect" + " ");
        }
    }

    /**
     * If a socket time out has occurred at some of the connected clients
     */
    void socketTimedOut(int idOfTimedOutClient) {
        actionsOnClientDisconnect(idOfTimedOutClient);
    }

    private synchronized void removeDisconnectedThreadFromList(int idOfThreadToRemove) {

        for (int i = 0; i < CLIENT_THREADS.size(); i++) {
            if (CLIENT_THREADS.get(i).getThreadID() == idOfThreadToRemove) {
                CLIENT_THREADS.remove(i);
                break;
            }
        }
    }

    /**
     * Actions to perform when a new client has connected to this BattleshipServer.
     *
     * Creates a ClientThread object that will represent the connection to the newly connected client
     * and this BattleshipServer.
     * @param clientConnection The socket of the new client connection,
     * @param clientThreadId The id to be given to the ClientThread object
     */
    private void actionsForClientConnected(Socket clientConnection, int clientThreadId) {
        ClientThread clientThread = new ClientThread(clientConnection, clientThreadId);
        CLIENT_THREADS.add(clientThread);
        clientThread.start();
    }

    /**
     * Listens for, and takes necessary actions for, new connections to this BattleshipServer. The listening
     * stops when two clients are connected and reports to the messageHandler that two clients are connected.
     */
    @Override
    public void run() {

        int clientThreadId = 1;

        while (CLIENT_THREADS.size() < 2) {
            try {

                Socket clientConnection = serverSocket.accept(); //listen for new connections

                actionsForClientConnected(clientConnection, clientThreadId);

                messageHandler.connectedPlayer(clientThreadId); //report that a client has connected

                clientThreadId++;

            } catch (IOException ioe) {
                System.err.println("Couldn't initialize new ClientThread: " + ioe);
            }

        }
        try {
            Thread.sleep(1000); //after two players are connected, pause the thread to let necessary processes of the connection phase finnish before reporting that two players are connected
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        messageHandler.twoConnectedPlayers();
    }


    /**
     * Passes along a received message from a client thread to the message handler
     * @param msg The message
     */
    private synchronized void receiveMessageFromClientThread(String msg) {

        messageHandler.handleClientMsg(msg);

    }

    /**
     * Transmits a message to a specific BattleshipClient via it's corresponding ClientThread object.
     * @param threadID The threadID of the ClientThread to receive the message
     * @param msg The message to be transmitted
     */
    public synchronized void sendMessageToClient(int threadID, String msg) {

        for (ClientThread clientThread : CLIENT_THREADS) {
            if (clientThread.threadID == threadID) {

                clientThread.outputMessage(msg);
            }
        }
    }

    /**
     * Transmits a message to every connected BattleshipClient via their corresponding ClientThread object
     * @param msg The message to be transmitted
     */
    public synchronized void broadcastMessage(String msg) {

        for (ClientThread client : CLIENT_THREADS) {

            client.outputMessage(msg);
        }

    }

    /**
     * Transmits a message to a specific BattleshipClient via it's corresponding ClientThread object to initiate
     * a new turn.
     * @param clientIdOfPreviousTurn The id of the BattleshipClient who just finished it's turn
     * @param msg The message containing information to initiate a new turn
     */
    public synchronized void initiateNewTurn(int clientIdOfPreviousTurn, String msg) {
        for (ClientThread clientThread : CLIENT_THREADS) {
            if (clientThread.threadID != clientIdOfPreviousTurn) {
                clientThread.outputMessage(msg);
                break;
            }
        }
    }

    public int getPort() {
        return PORT;
    }

    public InetAddress getHostAddress() {
        return hostAddress;
    }

}




