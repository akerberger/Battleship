package main;

import gameinitiation.GameInitiationHandler;
import gui.InitialConnectionWindow;

/**
 * Entry point for the Battleships program. The user may provide a port number when starting the program
 * from the commandline. If provided, the game will be hosted on that port. If not provided, the program will try to
 * connect to an existing game on that port. This depends on if the user chooses to host/connect, a choice that
 * is made at the InitialConnectionWindow.
 */
public class Main {

    /**
     * Creates the initial window that is displayed when a user starts the program.
     * @param args Optional argument is port number
     */
    public static void main(String [] args){

        int defaultPort = 2000;

        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : defaultPort;
            new InitialConnectionWindow(new GameInitiationHandler(port));

        }catch (NumberFormatException e){
            System.err.println("Port number expected as first and only argument");
        }



    }
}
