package main;

import gameinitiation.GameInitiationHandler;
import gui.InitialConnectionWindow;

/**
 * Entry point for the Battleships program
 */
public class Main {

    /**
     * Creates the initial window that is displayed when a user starts the program
     * @param args - unused
     */
    public static void main(String [] args){

        int defaultPort = 2000;

        int port = args.length>0 ? Integer.parseInt(args[0]) : defaultPort;

        new InitialConnectionWindow(new GameInitiationHandler());
    }
}
