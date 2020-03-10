package connection;

import game.GameController;
import game.GameState;
import gui.GameWindow;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class BattleshipClient {

//    private final String DEFAULT_HOST = "127.0.0.1";
    //	private final String DEFAULT_HOST = "212.247.27.18";
//    private final int DEFAULT_PORT = 2000;

    private String HOST;

    private int PORT;


    //gör klient-id här som skickas med i klicket
    private boolean isHosting;

//    private ClientWriter out;

    private PrintWriter out;

    private BufferedReader in;

    private GameWindow gameWindow;

    private int id = -1;

    public BattleshipClient(String hostName, int port, boolean isHosting) {

//        HOST = (args.length > 0 ? args[0] : DEFAULT_HOST);

//        HOST = DEFAULT_HOST;

        this.isHosting = isHosting;
        HOST = hostName;
        PORT = port;
        try {
//            PORT =  (args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT);

            Socket socket = setUpSocket();

            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            ClientReceiver receiver = new ClientReceiver(socket);
            receiver.start();


//            listen(socket);


        } catch (NumberFormatException e) {
            //Fel i parsning av args
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
            // fel i setUpSocket()
        }

    }

    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }


    //Behöver vara trådad, annars låser programmet på listen-metoden
    private class ClientReceiver extends Thread {
        Socket socket;

        ClientReceiver(Socket socket) {
            this.socket = socket;

        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //5 min
                socket.setSoTimeout(300 * 1000);

                //while inputen inte är "quit" eller så...
                while (true) {

//                    System.out.println("mottagit motståndares klick i BattleshipClient: "+in.readLine());
                    handleReceivedMessage(in.readLine());
                    Thread.sleep(20);
                }

            } catch (SocketTimeoutException e) {
                System.err.println("No response from opponent ");
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }
    }


    private Socket setUpSocket() throws IOException {

        Socket socket = new Socket(HOST, PORT);

        socket.setSoTimeout(15000);

        return socket;
    }

    private void listen(Socket socket) {


    }





    void handleReceivedMessage(String msg) throws IllegalArgumentException {

        System.out.println("BATTLESHIPCLIENT MED ID: " + id + " FÅR MEDDELANDE: " + msg);
//        String[] arr = msg.split(" ");
//        System.out.println(Arrays.toString(arr));
//        if (arr[3].equals("mine")) {
//            String[] coordinates = {arr[1], arr[2]};
////            gameWindow.receiveClick(coordinates);
//        }
        String[] tokens = msg.split(" ");

        String messageType = tokens[0];

        System.out.println("ARRAYEN: "+ Arrays.toString(tokens)+" MESSAGE TYPE: "+messageType);

//        if(tokens.length > 1){
            if(messageType.equals("setID")){
                if (id != -1) {
                    throw new IllegalArgumentException(" ID already set in BattleshipClient");
                } else {
                    id = Integer.parseInt(tokens[1]);
                }
            }else if(messageType.equals("okMove")){
                markSquaresOnMyBoard(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
            }else if(messageType.equals("changePhase")){
                String newPhase = tokens[1];
                if(newPhase.equals("setupPhase")){
                    GameController.setGameState(GameState.SETUP_PHASE);
                    gameWindow.setupPhase();
                }

            }
//        }


//        if (tokens.length > 1 && tokens[0].equals("SetID")) {
//
//        } else if (tokens[0].equals("approved")) {
//            markSquaresOnMyBoard(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
//        } else if (msg.equals("setup")) {
//            gameWindow.setupPhase();
//        }
    }

    public void sendClick(int row, int column, String whichBoard) {


        out.println(row + " " + column + " " + id);
//        System.out.println("sendklick mottaget i BattleshipClient");


    }

    private void markSquaresOnMyBoard(int startRow, int startColumn){
        if(GameController.gameState == GameState.SETUP_PHASE){

            //just nu är det första och enda skeppet som ska placeras 3 rutor stort.
            // Om detta ska funka generellt måste kontroller ske på andra ställen
            int shipSize = 3;
            gameWindow.placeShipOnMyBoard(startRow, startColumn, shipSize);
        }

    }

    public boolean isHosting() {
        return isHosting;
    }


}
